package de.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.deterministic.optimization.AllCombinationOptimization;
import de.jgraphlib.util.Triple;
import de.manetmodel.network.unit.DataRate;
import de.manetmodel.network.unit.DataUnit;

public class App {
	protected int runs;
	protected int numNodes;
	protected List<Triple<Integer, Integer, DataRate>> flowSourceTargetIds;
	protected DataRate meanTransmissionRate;
	protected String appName;
	ExecutorService executor;

	public App(int runs, int numNodes, List<Triple<Integer, Integer, DataRate>> flowSourceTargetIds,
			DataRate meanTransmissionRate, String appName) {
		this.runs = runs;
		this.numNodes = numNodes;
		this.flowSourceTargetIds = flowSourceTargetIds;
		this.meanTransmissionRate = meanTransmissionRate;
		this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.appName = appName;
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		int runs = 1;
		int numNodes = 100;
		OptimizationType oType = OptimizationType.greedy;

		List<Triple<Integer, Integer, DataRate>> flowSourceTargetIds = new ArrayList<Triple<Integer, Integer, DataRate>>();

		flowSourceTargetIds
				.add(new Triple<Integer, Integer, DataRate>(100, 83, new DataRate(1.8d, DataUnit.Type.megabit)));
		flowSourceTargetIds
				.add(new Triple<Integer, Integer, DataRate>(2, 51, new DataRate(1.4d, DataUnit.Type.megabit)));
		flowSourceTargetIds
				.add(new Triple<Integer, Integer, DataRate>(68, 10, new DataRate(1.0d, DataUnit.Type.megabit)));

		flowSourceTargetIds
				.add(new Triple<Integer, Integer, DataRate>(54, 27, new DataRate(1.4d, DataUnit.Type.megabit)));

//		List<Triple<Integer, Integer, DataRate>> flowSourceTargetIds = new ArrayList<Triple<Integer, Integer, DataRate>>();
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
//		flowSourceTargetIds.add(new Triple<Integer, Integer, DataRate>(1, 100, new DataRate(-1)));
		DataRate meanTransmissionRate = new DataRate();

		for (Triple<Integer, Integer, DataRate> triple : flowSourceTargetIds) {
			meanTransmissionRate.set(meanTransmissionRate.get() + triple.getThird().get());
		}
		meanTransmissionRate.set(meanTransmissionRate.get() / flowSourceTargetIds.size());
		switch (oType) {
		case genetic:
			new GeneticApp(runs, numNodes, flowSourceTargetIds, meanTransmissionRate, GeneticApp.class.getSimpleName())
					.start();
			break;
		case greedy:
			new GreedyApp(runs, numNodes, flowSourceTargetIds, meanTransmissionRate, GreedyApp.class.getSimpleName())
					.start();
			break;
		case allComb:
			new AllCompApp(runs, numNodes, flowSourceTargetIds, meanTransmissionRate, AllCompApp.class.getSimpleName())
					.start();
			break;
		default:
			break;
		}
	}

}
