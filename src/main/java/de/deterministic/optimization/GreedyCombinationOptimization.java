package de.deterministic.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import de.deterministic.algorithm.DijkstraShortesFlow;
import de.deterministic.network.DeterministicMANET;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.DataRate;
import de.parallelism.Optimization;

public class GreedyCombinationOptimization<M extends MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>>
		extends Optimization<Void, M> {

	private Random random;
	protected DijkstraShortesFlow sp;

	public GreedyCombinationOptimization(M manet) {
		super(manet);
		this.sp = new DijkstraShortesFlow(manet);
		this.random = new Random();
	}

	Function<Tuple<LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>, Double> metric = (tuple) -> {
		LinkQuality linkQuality = tuple.getFirst();
		Flow<Node, Link<LinkQuality>, LinkQuality> reversePath = tuple.getSecond();

		Tuple<Link<LinkQuality>, Node> current = reversePath.getLast();
		double cost = manet.getUtilizedLinksOf(current.getFirst()).size() * reversePath.getDataRate().get();
		manet.deployFlow(reversePath);

		if (manet.getOverUtilization().get() != 0) {
			cost = manet.getCapacity().get() + 1L;
		}
		manet.undeployFlow(reversePath);
		return cost;

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

					Flow<Node, Link<LinkQuality>, LinkQuality> suggestedSolution = sp.compute(manet.getFlow(flowId),
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

			for (Flow<Node, Link<LinkQuality>, LinkQuality> l : manet.getFlows()) {
				if (!visitedIds.contains(l.getID())) {
					l.clear();
				}

			}
		}
		return null;
	}

}
