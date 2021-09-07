package de.deterministic.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import de.deterministic.algorithm.DijkstraShortesFlow;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.parallelism.Optimization;

public class GreedyCombinationOptimization extends Optimization<Void, ScalarRadioMANET> {

	private Random random;
	protected DijkstraShortesFlow sp;

	public GreedyCombinationOptimization(ScalarRadioMANET manet) {
		super(manet);
		this.sp = new DijkstraShortesFlow(manet);
		this.random = new Random();
	}

	Function<ScalarLinkQuality, Double> metric = (linkQuality) -> {

		return linkQuality.getReceptionConfidence();

	};

	public Void execute() {
		List<Integer> flowIds = manet.getFlowIDs();
		List<Integer> visitedIds = new ArrayList<Integer>(flowIds.size());

		int currentId = -1;
		while (visitedIds.size() < flowIds.size()) {
			double utilization = Double.POSITIVE_INFINITY;
			double overUtilization = Double.POSITIVE_INFINITY;
			for (int flowId : flowIds) {

				if (!visitedIds.contains(flowId)) {

					ScalarRadioFlow suggestedSolution = sp.compute(manet.getFlow(flowId),
							metric);

					manet.deployFlow(suggestedSolution);

					double currentOverUtilization = manet.getOverUtilization().get();
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

			for (ScalarRadioFlow l : manet.getFlows()) {
				if (!visitedIds.contains(l.getID())) {
					l.clear();
				}

			}
		}
		return null;
	}

}
