package de.app;

import java.util.ArrayList;
import java.util.List;

import de.deterministic.algorithm.DijkstraShortesFlow;
import de.jgraphlib.graph.algorithms.DijkstraShortestPath;
import de.jgraphlib.graph.elements.Path;
import de.jgraphlib.util.RandomNumbers;
import de.jgraphlib.util.Triple;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.DataRate;
import de.manetmodel.network.unit.DataUnit;
import de.manetmodel.scenarios.Scenario;

public class HighUtilizedMANETSecenario extends Scenario {

	public HighUtilizedMANETSecenario(String individualName, int numFlows, int numNodes) {
		super(individualName, numFlows, numNodes);
	}

	public List<Flow<Node, Link<LinkQuality>, LinkQuality>> generateFlows(
			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet, int runs) {
		DijkstraShortesFlow sp = new DijkstraShortesFlow(manet);
		List<Integer> exclusionList = new ArrayList<Integer>();
		List<Triple<Integer, Integer, Integer>> flowSourceTargetPairs = new ArrayList<Triple<Integer, Integer, Integer>>();

		for (int i = 0; i < (numFlows * 2); i++) {
			int randomNodeId = RandomNumbers.getInstance(runs).getRandomNotInE(0, numNodes, exclusionList);
			exclusionList.add(randomNodeId);
		}

		Triple<Integer, Integer, Integer> stTriple = null;
		for (int i = 0; i < exclusionList.size(); i++) {

			if (i % 2 == 0) {
				stTriple = new Triple<Integer, Integer, Integer>();
				stTriple.setFirst(exclusionList.get(i));
			} else {
				stTriple.setSecond(exclusionList.get(i));
				flowSourceTargetPairs.add(stTriple);
			}
			stTriple.setThird(-1);
		}

		for (Triple<Integer, Integer, Integer> triple : flowSourceTargetPairs) {
			double random = RandomNumbers.getInstance(runs).getRandom(0d, 1d);
			if (random >= 0 && random < 0.2) {
				triple.setThird(1);
			} else if (random >= 0.2 && random < 0.55) {
				triple.setThird(2);
			} else if (random >= 0.55 && random <= 1d) {
				triple.setThird(3);
			}
			manet.addFlow(manet.getVertex(triple.getFirst()), manet.getVertex(triple.getSecond()), new DataRate());
		}

		for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {
			f = sp.compute(f, (Tuple<LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> q) -> {
				return 1d;
			});

		}

		DataRate step = new DataRate(1d, DataUnit.Type.megabit);

		boolean thresholdReached = false;
		while (!thresholdReached) {

			if (thresholdReached)
				break;

			this.adaptDataRate(flowSourceTargetPairs, manet.getFlows(), step);
			for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {

				manet.deployFlow(f);
			}
			if (manet.getOverUtilization().get() != 0L)
				thresholdReached = true;

			manet.undeployFlows();
		}
		for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {
			f.clear();
		}
		return manet.getFlows();
	}

	private void adaptDataRate(List<Triple<Integer, Integer, Integer>> flowSourceTargetPairs,
			List<Flow<Node, Link<LinkQuality>, LinkQuality>> flows, DataRate step) {

		int numFlowTypeOne = 0;
		int numFlowTypeTwo = 0;
		int numFlowTypeThree = 0;

		for (Triple<Integer, Integer, Integer> triple : flowSourceTargetPairs) {
			int flowType = triple.getThird();

			switch (flowType) {

			case 1:
				numFlowTypeOne++;
				break;
			case 2:
				numFlowTypeTwo++;
				break;
			case 3:
				numFlowTypeThree++;
				break;
			default:
				break;
			}
		}
		int index = 0;
		for (Triple<Integer, Integer, Integer> triple : flowSourceTargetPairs) {
			int flowType = triple.getThird();
			long amount = 0;
			switch (flowType) {

			case 1:
				amount = (long) (step.get() * 0.6);
				amount = amount / numFlowTypeThree;
				break;
			case 2:
				amount = (long) (step.get() * 0.3);
				amount = amount / numFlowTypeThree;

				break;
			case 3:
				amount = (long) (step.get() * 0.1);
				amount = amount / numFlowTypeThree;
				break;
			default:
				break;
			}
			DataRate currentDataRate = flows.get(index).getDataRate();
			flows.get(index).setDataRate(new DataRate(amount + currentDataRate.get()));
			index++;
		}

	}

}
