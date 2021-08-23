package de.deterministic.optimization;

import java.util.List;
import java.util.function.Function;

import de.deterministic.algorithm.DijkstraShortesFlow;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.DataRate;
import de.manetmodel.util.Selection;
import de.parallelism.Optimization;

public class AllCombinationOptimization<M extends MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>>
		extends Optimization<Void, M> {

	protected DijkstraShortesFlow sp;

	public AllCombinationOptimization(M manet) {
		super(manet);
		sp = new DijkstraShortesFlow(manet);
	}

	Function<Tuple<Flow<Node, Link<LinkQuality>, LinkQuality>,LinkQuality>, Double> metric = (tuple) -> {
		LinkQuality linkQuality = tuple.getSecond();
		Flow<Node, Link<LinkQuality>, LinkQuality> reversePath = tuple.getFirst();

		return  linkQuality.getReceptionPower();

	};

	@Override
	public Void execute() {
		List<Integer> flowIds = manet.getFlowIDs();
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

			deploySolution(flowIdSequence);
			double currentOverUtilization = manet.getOverUtilization().get();
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

		deploySolution(flowCombinations.get(bestCombination));
		return null;
	}

	private void deploySolution(List<Integer> flowIds) {
		manet.undeployFlows();
		for (int fId : flowIds) {
			Flow<Node, Link<LinkQuality>, LinkQuality> flow = manet.getFlow(fId);
			flow.clear();
			flow = sp.compute(flow, metric);
			manet.deployFlow(flow);
		}

	}
}
