package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.app.commander.CommandArgument;
import de.heuristic.optimization.ACOOptimization;
import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;
import de.parallelism.Run;
import de.parallelism.RunEcecutionCallable;

public class ACOApp extends App {
	protected CommandArgument<String> scenarioName;
	protected CommandArgument<Integer> runtimeConstraint;
	protected CommandArgument<Double> instructionFactor;
	protected CommandArgument<Integer> population;

	public ACOApp(String[] args) {
		super(args);
		// prompt parameter initialization
		// For instance, number of ants and iterations can be set via command line and initialized hiere
		this.scenarioName = new CommandArgument<String>("--name", "-n", "Genetic");
		
		parseCommandLine(args);
		this.scenario.setScenarioName(scenarioName.value);
	}

	/*
	 * Also, arguments in constructor must be parsed
	 */
	private void parseCommandLine(String[] args) {
		this.scenarioName.setValue(commandLineReader.parse(this.scenarioName));
	}

	@Override
	public RunEcecutionCallable configureRun(ScalarRadioMANET manet,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> resultRecorder) {
		ACOOptimization acoo = new ACOOptimization(manet,
				RandomNumbers.getInstance(0));
		return new Run(acoo, resultRecorder);
	}

	public static void main(String[] args) throws InvocationTargetException, InterruptedException, ExecutionException, IOException {
		ACOApp geneticApp = new ACOApp(args);
		geneticApp.execute();
	}
}
