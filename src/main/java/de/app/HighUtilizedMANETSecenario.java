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
			DataRate initDataRate = new DataRate();
			if (random >= 0 && random < 0.2) {
				initDataRate = new DataRate(1.5d, DataUnit.Type.megabit);
				triple.setThird(1);
			} else if (random >= 0.2 && random < 0.55) {
				initDataRate = new DataRate(512d, DataUnit.Type.kilobit);
				triple.setThird(2);
			} else if (random >= 0.55 && random <= 1d) {
				initDataRate = new DataRate(90d, DataUnit.Type.kilobit);
				triple.setThird(3);
			}
			manet.addFlow(manet.getVertex(triple.getFirst()), manet.getVertex(triple.getSecond()), initDataRate);
		}

		for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {
			f = sp.compute(f, (Tuple<LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> q) -> {
				return 1d;
			});

		}

		for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {
			manet.deployFlow(f);
		}

		DataRate step = new DataRate(1d, DataUnit.Type.megabit);
		boolean initialOverUtilized = manet.getOverUtilization().get() > 0L;

		boolean thresholdReached = false;
		while (!thresholdReached) {

			if (thresholdReached)
				break;

			manet.eraseFlows();

			for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {
				Triple<Integer, Integer, Integer> triple = flowSourceTargetPairs.get(f.getID());

				f.setDataRate(adaptDataRate(f.getDataRate(), step, triple.getThird(), initialOverUtilized));
			}

			for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {
				f = sp.compute(f, (Tuple<LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> q) -> {
					return 1d;
				});
			}

			for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {
				manet.deployFlow(f);
			}
			long currentUtilization = manet.getOverUtilization().get();

			if ((currentUtilization == 0L && initialOverUtilized)
					|| (currentUtilization > 0L && !initialOverUtilized)) {
				thresholdReached = true;
			}

		}
		return manet.getFlows();
	}

	private DataRate adaptDataRate(DataRate currentDataRate, DataRate step, int type, boolean overUtilized) {
		long bits = currentDataRate.get();
		DataRate newDatarate = new DataRate();
		switch (type) {

		case 1:
			long amount = (long) (step.get() * 0.6);
			if (overUtilized && bits - amount > 0) {
				newDatarate.set(bits - amount);
			} else {
				newDatarate.set(bits + amount);
			}
			break;
		case 2:
			amount = (long) (step.get() * 0.3);
			if (overUtilized && bits - amount > 0) {
				newDatarate.set(bits - amount);
			} else {
				newDatarate.set(bits + amount);
			}
			break;
		case 3:
			amount = (long) (step.get() * 0.1);
			if (overUtilized && bits - amount > 0) {
				newDatarate.set(bits - amount);
			} else {
				newDatarate.set(bits + amount);
			}
			break;
		default:
			break;
		}

		return newDatarate;
	}

}
