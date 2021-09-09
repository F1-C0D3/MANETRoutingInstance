package de.app;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.genetic.app.GeneticRun;
import de.genetic.optimization.GeneticOptimization;
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

public class GeneticApp extends App {


	public GeneticApp(int runs, Scenario scenario, RandomNumbers random) {
		super(runs, scenario, random);
	}

	public static void main(String[] args)
			throws InterruptedException, ExecutionException, IloException, InvocationTargetException {
		Scenario scenario = new Scenario("genetic", 2, 100, 1);
		GeneticApp greedyApp = new GeneticApp(1, scenario, RandomNumbers.getInstance(0));

		greedyApp.execute();
	}

	@Override
	public ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> configureRun(
			ScalarRadioMANET manet, MANETResultRecorder<RunResultParameter, AverageResultParameter> resultRecorder,
			RunResultMapper<RunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> runResultMapper) {
		GeneticOptimization go = new GeneticOptimization(manet, 1000, 2, RandomNumbers.getInstance(0));
		return new GeneticRun(go, resultRecorder, runResultMapper);
	}
}
