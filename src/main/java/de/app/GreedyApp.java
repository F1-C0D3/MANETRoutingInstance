package de.app;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.deterministic.app.DeterministicRun;
import de.deterministic.optimization.GreedyCombinationOptimization;
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

	public GreedyApp(int runs, Scenario scenario) {
		super(runs, scenario);
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException, IloException, InvocationTargetException {
		HighUtilizedMANETSecenario scenario = new HighUtilizedMANETSecenario("greedy", 3, 100, 1);
		GreedyApp greedyApp = new GreedyApp(1, scenario);

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
