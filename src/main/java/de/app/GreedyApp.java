package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.deterministic.app.DeterministicRun;
import de.deterministic.optimization.GreedyCombinationOptimization;
import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageResultParameter;
import de.manetmodel.results.MANETResultRecorder;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.RunResultParameter;
import de.manetmodel.scenarios.Scenario;
import de.parallelism.ExecutionCallable;
import ilog.concert.IloException;

public class GreedyApp extends App {

	public GreedyApp(Scenario scenario, RandomNumbers random) {
		super(scenario, random);
	}

	public static void main(String[] args)
			throws InterruptedException, ExecutionException, IloException, InvocationTargetException, IOException {
		int numRuns=1;
		int numFlows=15;
		int overUtilizationPercentage = 5;
		Scenario scenario = new Scenario("greedy", numFlows, 100, numRuns,overUtilizationPercentage);
		GreedyApp greedyApp = new GreedyApp(scenario, RandomNumbers.getInstance(0));

		greedyApp.execute();
	}

	@Override
	public ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> configureRun(
			ScalarRadioMANET manet, MANETResultRecorder<RunResultParameter, AverageResultParameter> resultRecorder,
			RunResultMapper<RunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> runResultMapper) {
		GreedyCombinationOptimization go = new GreedyCombinationOptimization(manet);
		return new DeterministicRun(go, resultRecorder, runResultMapper);
	}

}
