package de.app;

import java.util.concurrent.ExecutionException;

import de.genetic.app.GeneticRun;
import de.genetic.optimization.GeneticOptimization;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.scenarios.Scenario;
import de.results.MANETResultRecorder;
import de.results.RunResultMapper;
import de.results.RunResultParameter;
import de.runprovider.ExecutionCallable;
import ilog.concert.IloException;

public class GeneticApp extends App {

	public GeneticApp(int runs, Scenario scenario) {
		super(runs, scenario);
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException, IloException {
		HighUtilizedMANETSecenario scenario = new HighUtilizedMANETSecenario("test", 10, 100, 1);
		GeneticApp greedyApp = new GeneticApp(1, scenario);

		greedyApp.execute();
	}

	@Override
	public ExecutionCallable<Flow<Node, Link<LinkQuality>, LinkQuality>, Node, Link<LinkQuality>, LinkQuality> configureRun(
			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet,
			MANETResultRecorder<RunResultParameter> geneticEvalRecorder,
			RunResultMapper<RunResultParameter> runResultMapper) {
		GeneticOptimization go = new GeneticOptimization(manet, 200, 12);
		return new GeneticRun(go, geneticEvalRecorder, runResultMapper);
	}
}
