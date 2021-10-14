package de.deterministic.optimization;

import java.util.List;
import java.util.function.Function;

import de.deterministic.algorithm.DijkstraShortestFlow;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.util.Selection;
import de.parallelism.Optimization;

public class AllCombinationOptimization extends DeterministicOptimization<ScalarRadioMANET> {

	public AllCombinationOptimization(ScalarRadioMANET manet) {
		super(manet);
	}

	@Override
	public ScalarRadioMANET execute() {

		Function<ScalarLinkQuality, Double> metric = (flowAndlinkQality) -> {
			return flowAndlinkQality.getReceptionConfidence() * 0.6d + flowAndlinkQality.getRelativeMobility() * 0.2d
					+ flowAndlinkQality.getSpeedQuality() * 0.2d;
		};
		
		List<Integer> flowIds = manet.getFlowIDs();
		int[] flowIdArray = new int[flowIds.size()];
		for (int i = 0; i < flowIdArray.length; i++) {
			flowIdArray[i] = flowIds.get(i);
		}

		int bestCombination = -1;
		double previousRobustness = Double.POSITIVE_INFINITY;
		double overUtilization = Double.POSITIVE_INFINITY;
		List<List<Integer>> flowCombinations = Selection.allCombination(flowIdArray);
		int index = 0;

		for (List<Integer> flowIdSequence : flowCombinations) {

			deployPathsBasedOnMethric(flowIdSequence,metric);
			double currentOverUtilization = manet.getOverUtilization().get();
			double currentRobustness = this.evaluateSolution();

			if (overUtilization >= currentOverUtilization) {

				if (currentOverUtilization == 0d && overUtilization == 0d && currentRobustness < previousRobustness) {

					bestCombination = index;
					overUtilization = currentOverUtilization;
					previousRobustness = currentRobustness;

				} else if (currentOverUtilization < overUtilization) {

					bestCombination = index;
					overUtilization = currentOverUtilization;
					previousRobustness = currentRobustness;

				}
			}

			index++;
		}

		deployPathsBasedOnMethric(flowCombinations.get(bestCombination), metric);
		return manet;
	}

}
