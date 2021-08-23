package de.app;

import java.util.concurrent.ExecutionException;

import de.deterministic.app.DeterministicRun;
import de.deterministic.optimization.GreedyCombinationOptimization;
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

public class GreedyApp extends App {

	public GreedyApp(int runs, Scenario scenario) {
		super(runs, scenario);
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException, IloException {
		HighUtilizedMANETSecenario scenario = new HighUtilizedMANETSecenario("greedy", 4, 100,1);
		GreedyApp greedyApp = new GreedyApp(1, scenario);

		greedyApp.execute();
	}

	@Override
	public ExecutionCallable<Flow<Node, Link<LinkQuality>, LinkQuality>, Node, Link<LinkQuality>, LinkQuality> configureRun(
			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet,
			MANETResultRecorder<RunResultParameter> geneticEvalRecorder,
			RunResultMapper<RunResultParameter> runResultMapper) {
		/* Evaluation of each run starts here */
		GreedyCombinationOptimization<MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>> go = new GreedyCombinationOptimization<MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>>(
				manet);
		return new DeterministicRun(go, geneticEvalRecorder, runResultMapper);
	}

}
