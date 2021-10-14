package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.deterministic.app.DeterministicRun;
import de.deterministic.optimization.AllCombinationOptimization;
import de.deterministic.optimization.RateBasedDistributedRobustPathsOptimization;
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

public class RateBasedDistributedRobustPathApp extends App {

	public static void main(String[] args)
			throws InterruptedException, ExecutionException, IloException, InvocationTargetException {
		boolean visual = false;
		int numRuns=10;
		int numFlows=4;
		int overUtilizationPercentage = 1;
		Scenario scenario = new Scenario("RBDRP", numFlows, 100, numRuns,overUtilizationPercentage);

		RateBasedDistributedRobustPathApp allComp = new RateBasedDistributedRobustPathApp(2, scenario, RandomNumbers.getInstance(10),visual);

		try {
			allComp.execute();
		} catch (InvocationTargetException | InterruptedException | ExecutionException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//	System.exit(0);
	}

	public RateBasedDistributedRobustPathApp(int runs, Scenario scenario, RandomNumbers random,boolean visual) {
		super(scenario, random,visual);
	}

	@Override
	public ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> configureRun(
			ScalarRadioMANET manet, MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> resultRecorder) {

		RateBasedDistributedRobustPathsOptimization rbdrpo = new RateBasedDistributedRobustPathsOptimization(manet);
		return new DeterministicRun(rbdrpo, resultRecorder);
	}

}
