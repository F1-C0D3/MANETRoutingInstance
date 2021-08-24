package de.app;

import java.util.concurrent.ExecutionException;

import de.approximation.app.ApproximationRun;
import de.approximation.optimization.CplexOptimization;
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

public class CplexApp extends App {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IloException {
		HighUtilizedMANETSecenario scenario = new HighUtilizedMANETSecenario("cplex", 10, 100,1);
		CplexApp app = new CplexApp(1, scenario);
		app.execute();
	}

	public CplexApp(int runs, Scenario scenario) {
		super(runs, scenario);
	}


	@Override
	public ExecutionCallable<Flow<Node, Link<LinkQuality>, LinkQuality>, Node, Link<LinkQuality>, LinkQuality> configureRun(
			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet,
			MANETResultRecorder<RunResultParameter> geneticEvalRecorder,
			RunResultMapper<RunResultParameter> runResultMapper) {
		CplexOptimization<MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>> co = new CplexOptimization<MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>>(
				manet);
		return new ApproximationRun(co, geneticEvalRecorder, runResultMapper);
	}
}
