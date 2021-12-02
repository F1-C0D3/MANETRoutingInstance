package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.scenarios.Scenario;

public class Start {

	public static void main(String[] args)
			throws InvocationTargetException, InterruptedException, ExecutionException, IOException {
		int optMethod = Integer.parseInt(args[0]);
		int numRuns = Integer.parseInt(args[1]);
		int numFlows = Integer.parseInt(args[2]);
		int overUtilizationPercentage = Integer.parseInt(args[3]);
		double methodSpecifiyConstraint = Double.parseDouble(args[4]);
		boolean visual = Integer.parseInt(args[4]) == 0 ? false : true;

		Scenario scenario = new Scenario(numFlows, 100, numRuns, overUtilizationPercentage);

		switch (optMethod) {
		case 0:
			scenario.setScenarioName("cplex_LimitedTime");
			CplexApp app = new CplexApp(methodSpecifiyConstraint, scenario, RandomNumbers.getInstance((3)), visual);
			app.execute();
			break;
		case 1:
			scenario.setScenarioName("genetic_LimitedTime");
			GeneticApp geneticApp = new GeneticApp(methodSpecifiyConstraint,scenario, RandomNumbers.getInstance(3), visual);
			geneticApp.execute();
			break;
		case 2:
			scenario.setScenarioName("kMostDisjountPath");
			KMostDiscjointPathApp kMostDisjointPathsApp = new KMostDiscjointPathApp(scenario, RandomNumbers.getInstance(3),visual);
			kMostDisjointPathsApp.execute();
			break;
		case 3:
			scenario.setScenarioName("greedy");
			GreedyApp greedyApp = new GreedyApp(scenario, RandomNumbers.getInstance(3),visual);
			greedyApp.execute();

			break;
		case 4:

			scenario.setScenarioName("RBDRP");
			RateBasedDistributedRobustPathApp rbdrpApp = new RateBasedDistributedRobustPathApp(50, scenario, RandomNumbers.getInstance(3),visual);
			rbdrpApp.execute();
			break;
		default:
			break;
		}

	}

}
