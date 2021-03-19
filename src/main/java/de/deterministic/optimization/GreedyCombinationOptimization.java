package de.deterministic.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import de.deterministic.algorithm.DijkstraShortestDataRateConstrainedPath;
import de.deterministic.network.DeterministicMANET;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.DataRate;
import de.parallelism.Optimization;

public class GreedyCombinationOptimization<M extends MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>>
		extends Optimization<Void, M> {

	private Random random;
	protected DijkstraShortestDataRateConstrainedPath sp;

	public GreedyCombinationOptimization(M manet) {
		super(manet);
		this.sp = new DijkstraShortestDataRateConstrainedPath(manet);
		this.random = new Random();
	}

	Function<Tuple<MultipleDijkstraLinkQuality, DataRate>, Double> metric = (tuple) -> {
		MultipleDijkstraLinkQuality linkQuality = tuple.getFirst();
		DataRate rate = tuple.getSecond();
		Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> reversePath = linkQuality
				.getReversePath();

		Tuple<Link<MultipleDijkstraLinkQuality>, Node> current = reversePath.getLast();
		double cost = current.getFirst().getWeight().getNumUtilizedLinks() * rate.get();
		manet.deployFlow(reversePath);

		if (manet.getOverUtilizedLinks().get() != 0) {
			cost = manet.getCapacity().get() + 1L;
		}
		manet.undeployFlow(reversePath);
		return cost;

	};

	public Void execute() {
		List<Integer> flowIds = manet.getFlowIds();
		List<Integer> visitedIds = new ArrayList<Integer>(flowIds.size());

		int currentId = random.nextInt(flowIds.size());
		visitedIds.add(currentId);
		Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> flow = sp
				.compute(manet.getFlow(currentId), metric);
		manet.deployFlow(flow);

		while (visitedIds.size() < flowIds.size()) {
			double utilization = Double.POSITIVE_INFINITY;
			double overUtilization = Double.POSITIVE_INFINITY;
			for (int flowId : flowIds) {

				if (!visitedIds.contains(flowId)) {

					Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> suggestedSolution = sp
							.compute(manet.getFlow(flowId), metric);

					manet.deployFlow(suggestedSolution);

					double currentOverUtilization = manet.getOverUtilizedLinks().get();
					double currentUtilization = manet.getUtilization().get();

					if (overUtilization >= currentOverUtilization) {
						if (currentOverUtilization == 0d && overUtilization == 0d && currentUtilization < utilization) {
							currentId = flowId;
							overUtilization = currentOverUtilization;
							utilization = currentUtilization;
						} else if (currentOverUtilization < overUtilization) {
							currentId = flowId;
							overUtilization = currentOverUtilization;
							utilization = currentUtilization;
						}
					}

					manet.undeployFlow(manet.getFlow(flowId));
				}
			}

			visitedIds.add(currentId);
			manet.deployFlow(manet.getFlow(currentId));

			for (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> l : manet.getFlows()) {
				if (!visitedIds.contains(l.getId())) {
					l.clear();
				}

			}
		}
		return null;
	}

}
