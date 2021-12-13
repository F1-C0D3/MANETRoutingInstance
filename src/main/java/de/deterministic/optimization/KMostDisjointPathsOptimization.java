package de.deterministic.optimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.jgraphlib.graph.algorithms.YenTopKShortestPaths;
import de.jgraphlib.graph.elements.EdgeDistance;
import de.jgraphlib.graph.elements.Path;
import de.jgraphlib.graph.elements.Position2D;
import de.jgraphlib.graph.elements.Vertex;
import de.jgraphlib.graph.elements.WeightedEdge;
import de.jgraphlib.util.RandomNumbers;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;

public class KMostDisjointPathsOptimization extends DeterministicOptimization<ScalarRadioMANET> {

	private YenTopKShortestPaths<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> ksp;
	private RandomNumbers random;

	public KMostDisjointPathsOptimization(ScalarRadioMANET manet, int numTopKPaths, RandomNumbers random) {
		super(manet);
		this.ksp = new YenTopKShortestPaths<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(manet, numTopKPaths);
		this.random = random;

	}

	public ScalarRadioMANET execute() {

		TreeMap<Integer, Tuple<List<Integer>, List<ScalarRadioFlow>>> kspFlows = new TreeMap<Integer, Tuple<List<Integer>, List<ScalarRadioFlow>>>();
		Function<ScalarLinkQuality, Double> edgeMetric = (flowAndlinkQality) -> {
//			return flowAndlinkQality.getReceptionConfidence() * 0.6d + flowAndlinkQality.getRelativeMobility() * 0.1d
//					+ flowAndlinkQality.getSpeedQuality() * 0.3d;
			return 1d;

		};

		Function<Tuple<Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>, Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>>, Double> pathMetric = (
				Tuple<Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>, Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>> pathTuple) -> {

			Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> firstPath = pathTuple.getFirst();
			Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> secondPath = pathTuple.getSecond();
			Set<ScalarRadioLink> union = new HashSet<ScalarRadioLink>(firstPath.getEdges());
			union.addAll(secondPath.getEdges());

			int commonLinks = 0;
			for (ScalarRadioLink fEdge : firstPath.getEdges()) {
				for (ScalarRadioLink sEdge : secondPath.getEdges()) {
					if (fEdge.getID() == sEdge.getID())
						commonLinks++;
				}
			}
			return (commonLinks / (double) union.size());

		};

		for (ScalarRadioFlow flow : manet.getFlows()) {

			kspFlows.put(flow.getID(), new Tuple<List<Integer>, List<ScalarRadioFlow>>(new ArrayList<Integer>(),
					new ArrayList<ScalarRadioFlow>()));

			for (Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> altPath : ksp.compute(flow.getSource(),
					flow.getTarget(), edgeMetric, pathMetric)) {
				ScalarRadioFlow newFlow = new ScalarRadioFlow(flow);
				newFlow.update(altPath);
				kspFlows.get(flow.getID()).getSecond().add(newFlow);
			}
		}

		List<ScalarRadioFlow> compareList = new ArrayList<ScalarRadioFlow>();
		List<List<ScalarRadioFlow>> candidates = new ArrayList<List<ScalarRadioFlow>>();

		int iterations = kspFlows.get(0).getSecond().size();
		while (iterations > 0) {

			List<Integer> chosen = new ArrayList<Integer>();
			for (int i = 0; i < kspFlows.size(); i++) {
					int randomFlowID = random.getRandomNotInE(0, kspFlows.size(), chosen);
					chosen.add(randomFlowID);
					double totalScore = Double.MAX_VALUE;
					int pathIndex = 0;
					int bestIndex = 0;
					if (chosen.size() == 1) {
						int randomPathID = random.getRandomNotInE(0, kspFlows.get(randomFlowID).getSecond().size(),
								kspFlows.get(randomFlowID).getFirst());
						kspFlows.get(randomFlowID).getFirst().add(randomPathID);
						compareList.add(new ScalarRadioFlow(kspFlows.get(randomFlowID).getSecond().get(randomPathID)));
					} else {
						for (ScalarRadioFlow innerFlow : kspFlows.get(randomFlowID).getSecond()) {

							if (!kspFlows.get(randomFlowID).getFirst().contains(pathIndex)) {

								compareList.add(kspFlows.get(innerFlow.getID()).getSecond().get(pathIndex));

								double score = computeSimilarity(compareList);

								if (score < totalScore) {

									totalScore = score;
									bestIndex = pathIndex;

								}

								compareList.remove(innerFlow);

							}

							pathIndex++;

							if (kspFlows.get(randomFlowID).getSecond().size() == pathIndex) {
								compareList.add(new ScalarRadioFlow(kspFlows.get(randomFlowID).getSecond().get(bestIndex)));
							kspFlows.get(randomFlowID).getFirst().add(bestIndex);
							}
						}
					}
			}

			List<ScalarRadioFlow> candidate = new ArrayList<ScalarRadioFlow>(compareList);
			compareList.clear();
			candidates.add(candidate);

			--iterations;
		}

		double bestScore = Double.POSITIVE_INFINITY;
		int candidateIndex = 0;
		for (List<ScalarRadioFlow> candidate : candidates) {

			for (ScalarRadioFlow candidateFlow : candidate) {

				ScalarRadioFlow manetFlow = manet.getFlow(candidateFlow.getID());
				manetFlow.update(candidateFlow);
			}

			manet.undeployFlows();
			manet.deployFlows(manet.getFlows());

			double currentRobustness = this.evaluateSolution();
			boolean foundFeasibleSolution = false;

			if (!manet.isOverutilized() && currentRobustness < bestScore) {
				bestScore = currentRobustness;
				candidateIndex = candidates.indexOf(candidate);
				foundFeasibleSolution = true;
			}

			if (!foundFeasibleSolution && currentRobustness < bestScore) {
				bestScore = currentRobustness;
				candidateIndex = candidates.indexOf(candidate);
			}
		}

		manet.undeployFlows();
		for (ScalarRadioFlow candidateFlow : candidates.get(candidateIndex)) {

			ScalarRadioFlow manetFlow = manet.getFlow(candidateFlow.getID());
			manetFlow.update(candidateFlow);
			manet.deployFlow(manetFlow);

		}
		return manet;
	}

	private double computeSimilarity(List<ScalarRadioFlow> flows) {

		List<ScalarRadioLink> union = new ArrayList<ScalarRadioLink>();
		List<ScalarRadioLink> intersection = new ArrayList<ScalarRadioLink>();

		for (int i = 0; i < (flows.size() - 1); i++)
			for (ScalarRadioLink link : flows.get(i).getEdges())
				intersection.addAll(manet.getUtilizedLinksOf(link));

		List<ScalarRadioLink> edgesOfFlowLastFlow = new ArrayList<ScalarRadioLink>();
		for (ScalarRadioLink link : flows.get(flows.size() - 1).getEdges())
			edgesOfFlowLastFlow.addAll(manet.getUtilizedLinksOf(link));

		union.addAll(intersection);
		union.addAll(edgesOfFlowLastFlow);

		intersection.retainAll(edgesOfFlowLastFlow);

		return intersection.size() / (double) union.size();
	}

	private double computeUtilization(List<ScalarRadioFlow> flows) {

		manet.deployFlows(flows);

		double utilization = manet.getUtilization().get() / (double) manet.maxPossibleUtilization().get();
		manet.undeployFlows();
		return utilization;
	}

}
