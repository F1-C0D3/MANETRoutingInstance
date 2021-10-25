package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.genetic.app.GeneticRun;
import de.genetic.optimization.GeneticOptimization;
import de.genetic.optimization.TerminationConditionMANET;
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
import de.manetmodel.units.Time;
import de.manetmodel.units.Unit.TimeSteps;
import de.parallelism.RunEcecutionCallable;
import ilog.concert.IloException;

public class GeneticApp extends App {

	public GeneticApp(Scenario scenario, RandomNumbers random,boolean visual) {
		super(scenario, random,visual);
	}

	public static void main(String[] args)
			throws InterruptedException, ExecutionException, IloException, InvocationTargetException, IOException {
		 boolean visual=false; 
		int numRuns = 1;
		int numFlows = 10;
		int overUtilizationPercentage = 1;
		Scenario scenario = new Scenario("genetic", numFlows, 100, numRuns, overUtilizationPercentage);
		GeneticApp greedyApp = new GeneticApp(scenario, RandomNumbers.getInstance(0),visual);

		greedyApp.execute();
	}

	@Override
	public RunEcecutionCallable configureRun(
			ScalarRadioMANET manet, MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> resultRecorder) {
		GeneticOptimization go = new GeneticOptimization(manet, /* Initial Population */1000, /* Termination condition */new TerminationConditionMANET(/*Successive fitness levels*/10, /*Max generations*/100, /*Min fitness level*/ 0.03, /*Max runtime*/new Time(TimeSteps.minute, 2L)),
				/* mutation probability */0.25, /* Instruction factor */0.20d, RandomNumbers.getInstance(0));
		return new GeneticRun(go, resultRecorder);
	}
}
