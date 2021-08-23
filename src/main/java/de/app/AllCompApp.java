package de.app;

import java.util.concurrent.ExecutionException;

import de.deterministic.app.DeterministicRun;
import de.deterministic.optimization.AllCombinationOptimization;
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

public class AllCompApp extends App {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IloException {
		HighUtilizedMANETSecenario scenario = new HighUtilizedMANETSecenario("allCombination", 4, 100,1);
		AllCompApp allComp = new AllCompApp(1, scenario);

		allComp.execute();
		
//	System.exit(0);
	}

	public AllCompApp(int runs, Scenario scenario) {
		super(runs, scenario);
	}

	@Override
	public ExecutionCallable<Flow<Node, Link<LinkQuality>, LinkQuality>, Node, Link<LinkQuality>, LinkQuality> configureRun(
			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet,
			MANETResultRecorder<RunResultParameter> geneticEvalRecorder,
			RunResultMapper<RunResultParameter> runResultMapper) {

		AllCombinationOptimization<MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>> aco = new AllCombinationOptimization<MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>>(
				manet);
		return new DeterministicRun(aco, geneticEvalRecorder, runResultMapper);
	}
}
