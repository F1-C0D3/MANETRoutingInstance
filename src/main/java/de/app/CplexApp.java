package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.approximation.app.ApproximationRun;
import de.approximation.optimization.CplexOptimization;
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

public class CplexApp extends App {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IloException, InvocationTargetException, IOException {
		int numRuns=7;
		int numFlows=5;
		Scenario scenario = new Scenario("cplex_s_d", numFlows, 100, numRuns);
		CplexApp app = new CplexApp(numRuns, scenario,RandomNumbers.getInstance(0));
		app.execute();
	}

	public CplexApp(int runs, Scenario scenario,RandomNumbers random) {
		super(scenario,random);
	}


	@Override
	public ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> configureRun(
			ScalarRadioMANET manet,
			MANETResultRecorder<RunResultParameter, AverageResultParameter> resultRecorder,
			RunResultMapper<RunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> runResultMapper) {
		
		CplexOptimization co = new CplexOptimization(
				manet);
		return new ApproximationRun(co, resultRecorder, runResultMapper);
	}
}
