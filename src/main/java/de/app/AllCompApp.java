package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.deterministic.app.DeterministicRun;
import de.deterministic.optimization.AllCombinationOptimization;
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
import de.parallelism.RunEcecutionCallable;
import ilog.concert.IloException;

public class AllCompApp extends App {

	public static void main(String[] args)
			throws InterruptedException, ExecutionException, IloException, InvocationTargetException {
		 boolean visual=false; 
		int numRuns=1;
		int numFlows=4;
		int overUtilizationPercentage = 10;
		Scenario scenario = new Scenario("allComb", numFlows, 100, numRuns,overUtilizationPercentage);

		AllCompApp allComp = new AllCompApp(2, scenario, RandomNumbers.getInstance(0),visual);

		try {
			allComp.execute();
		} catch (InvocationTargetException | InterruptedException | ExecutionException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//	System.exit(0);
	}

	public AllCompApp(int runs, Scenario scenario, RandomNumbers random,boolean visual) {
		super(scenario, random,visual);
	}

	@Override
	public RunEcecutionCallable configureRun(
			ScalarRadioMANET manet, MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> resultRecorder) {

		AllCombinationOptimization aco = new AllCombinationOptimization(manet);
		return new DeterministicRun(aco, resultRecorder);
	}

//	@Override
//	public ExecutionCallable<ScalarLinkQuality> configureRun(
//			MANET<ScalarLinkQuality> manet,
//			MANETResultRecorder<ScalarLinkQuality,RunResultParameter,AverageResultParameter> geneticEvalRecorder,
//			RunResultMapper<ScalarLinkQuality,RunResultParameter> runResultMapper) {
//
//		AllCombinationOptimization<MANET<ScalarLinkQuality>> aco = new AllCombinationOptimization<MANET<ScalarLinkQuality>>(
//				manet);
//		return new DeterministicRun(aco, geneticEvalRecorder, runResultMapper);
//	}
}
