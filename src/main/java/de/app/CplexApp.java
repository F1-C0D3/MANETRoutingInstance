package de.app;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.approximation.app.ApproximationRun;
import de.approximation.optimization.CplexOptimization;
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

	public static void main(String[] args) throws InterruptedException, ExecutionException, IloException, InvocationTargetException {
		HighUtilizedMANETSecenario scenario = new HighUtilizedMANETSecenario("cplex",7 , 100,1);
		CplexApp app = new CplexApp(1, scenario);
		app.execute();
	}

	public CplexApp(int runs, HighUtilizedMANETSecenario scenario) {
		super(runs, scenario);
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
