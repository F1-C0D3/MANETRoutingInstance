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
		Scenario scenario = new Scenario("cplex_both", 2, 100, 2);
		CplexApp app = new CplexApp(2, scenario,RandomNumbers.getInstance(0));
		app.execute();
	}

	public CplexApp(int runs, Scenario scenario,RandomNumbers random) {
		super(runs, scenario,random);
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
