package de.deterministic.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioMANET;

public class GreedyCombinationOptimization extends DeterministicOptimization<ScalarRadioMANET> {


	public GreedyCombinationOptimization(ScalarRadioMANET manet) {
		super(manet);
	}


	public ScalarRadioMANET execute() {

		Function<ScalarLinkQuality, Double> metric = (flowAndlinkQality) -> {
			return flowAndlinkQality.getReceptionConfidence() * 0.6d + flowAndlinkQality.getRelativeMobility() * 0.2d
					+ flowAndlinkQality.getSpeedQuality() * 0.2d;
		};
		
		List<Integer> flowIds = manet.getFlowIDs();
		List<Integer> visitedIds = new ArrayList<Integer>(flowIds.size());

		int currentId = -1;
		while (visitedIds.size() < flowIds.size()) {
			double previousRobustness = Double.POSITIVE_INFINITY;
			double overUtilization = Double.POSITIVE_INFINITY;
			for (int flowId : flowIds) {

				if (!visitedIds.contains(flowId)) {

					ScalarRadioFlow suggestedSolution = sp.compute(manet.getFlow(flowId),
							metric);

					manet.deployFlow(suggestedSolution);

					double currentOverUtilization = manet.getOverUtilization().get();
					double currentRobustness = this.evaluateSolution();

					if (overUtilization >= currentOverUtilization) {
						if (currentOverUtilization == 0d && overUtilization == 0d && currentRobustness < previousRobustness) {
							currentId = flowId;
							overUtilization = currentOverUtilization;
							previousRobustness = currentRobustness;
						} else if (currentOverUtilization < overUtilization) {
							currentId = flowId;
							overUtilization = currentOverUtilization;
							previousRobustness = currentRobustness;
						}
					}

					manet.undeployFlow(manet.getFlow(flowId));
				}
			}

			visitedIds.add(currentId);
			manet.deployFlow(manet.getFlow(currentId));

			for (ScalarRadioFlow l : manet.getFlows()) {
				if (!visitedIds.contains(l.getID())) {
					l.clear();
				}

			}
		}
		return manet;
	}

}
