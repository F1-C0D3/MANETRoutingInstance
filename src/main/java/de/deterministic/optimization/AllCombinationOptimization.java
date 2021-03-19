package de.deterministic.optimization;

import java.util.List;
import java.util.function.Function;

import de.algorithmhelper.Selection;
import de.deterministic.algorithm.DijkstraShortestDataRateConstrainedPath;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.DataRate;
import de.parallelism.Optimization;

public class AllCombinationOptimization<M extends MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>>
		extends Optimization<Void, M> {

	protected DijkstraShortestDataRateConstrainedPath sp;

	public AllCombinationOptimization(M manet) {
		super(manet);
		sp = new DijkstraShortestDataRateConstrainedPath(manet);
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

	@Override
	public Void execute() {
		List<Integer> flowIds = manet.getFlowIds();
		int[] flowIdArray = new int[flowIds.size()];
		for (int i = 0; i < flowIdArray.length; i++) {
			flowIdArray[i] = flowIds.get(i);
		}

		int bestCombination = -1;
		double utilization = Double.POSITIVE_INFINITY;
		double overUtilization = Double.POSITIVE_INFINITY;
		List<List<Integer>> flowCombinations = Selection.allCombination(flowIdArray);
		int index = 0;
		for (List<Integer> flowIdSequence : flowCombinations) {

			deployFlow(flowIdSequence);
			double currentOverUtilization = manet.getOverUtilizedLinks().get();
			double currentUtilization = manet.getUtilization().get();

			if (overUtilization >= currentOverUtilization) {

				if (currentOverUtilization == 0d && overUtilization == 0d && currentUtilization < utilization) {
					bestCombination = index;
					overUtilization = currentOverUtilization;
					utilization = currentUtilization;
				} else if (currentOverUtilization < overUtilization) {
					bestCombination = index;
					overUtilization = currentOverUtilization;
					utilization = currentUtilization;
				}
			}
			index++;
		}

		deployFlow(flowCombinations.get(bestCombination));
		return null;
	}

	private void deployFlow(List<Integer> flowIds) {
		manet.eraseFlows();
		for (int fId : flowIds) {
			Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> flow = manet.getFlow(fId);
			flow.clear();
			flow = sp.compute(flow, metric);
			manet.deployFlow(flow);
		}

	}

}
