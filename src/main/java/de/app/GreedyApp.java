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
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.scenarios.Scenario;
import de.parallelism.RunEcecutionCallable;
import ilog.concert.IloException;

public class GreedyApp extends App {

	public GreedyApp(Scenario scenario, RandomNumbers random,boolean visual) {
		super(scenario, random,visual);
	}

	public static void main(String[] args)
			throws InterruptedException, ExecutionException, IloException, InvocationTargetException, IOException {
		boolean visual = false;
		int numRuns=50;
		int numFlows=10;
		int overUtilizationPercentage = 5;
		Scenario scenario = new Scenario("greedy", numFlows, 100, numRuns,overUtilizationPercentage);
		GreedyApp greedyApp = new GreedyApp(scenario, RandomNumbers.getInstance(0),visual);

		greedyApp.execute();
	}

	@Override
	public RunEcecutionCallable configureRun(
			ScalarRadioMANET manet, MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> resultRecorder) {
		GreedyCombinationOptimization go = new GreedyCombinationOptimization(manet);
		return new DeterministicRun(go, resultRecorder);
	}

}
