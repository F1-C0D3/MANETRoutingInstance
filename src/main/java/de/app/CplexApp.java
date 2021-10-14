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
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.scenarios.Scenario;
import de.parallelism.ExecutionCallable;
import ilog.concert.IloException;

public class CplexApp extends App {

	public static void main(String[] args) throws InterruptedException, ExecutionException, IloException, InvocationTargetException, IOException {
		boolean visual = false;
		int numRuns=10;
		int numFlows=3;
		int overUtilizationPercentage = 1;
		Scenario scenario = new Scenario("cplex_1.00_0.00_0.00", numFlows, 100, numRuns,overUtilizationPercentage);
		CplexApp app = new CplexApp(numRuns, scenario,RandomNumbers.getInstance((1)),visual);
		app.execute();
	}

	public CplexApp(int runs, Scenario scenario,RandomNumbers random,boolean visual) {
		super(scenario,random,visual);
	}


	@Override
	public ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> configureRun(
			ScalarRadioMANET manet,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> resultRecorder) {
		
		CplexOptimization co = new CplexOptimization(
				manet);
		return new ApproximationRun(co, resultRecorder);
	}
}
