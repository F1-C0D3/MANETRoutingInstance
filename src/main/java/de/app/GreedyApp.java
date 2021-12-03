package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.app.commander.CommandArgument;
import de.deterministic.app.DeterministicRun;
import de.deterministic.optimization.GreedyCombinationOptimization;
import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;
import de.manetmodel.scenarios.Scenario;
import de.parallelism.RunEcecutionCallable;

public class GreedyApp extends App {
	protected CommandArgument<String> scenarioName;

	public GreedyApp(String[] args) {
		super(args);

		this.scenarioName = new CommandArgument<String>("--name", "n", "Greedy");
		parseCommandLine(args);
		this.scenario.setScenarioName(scenarioName.value);
	}

	private void parseCommandLine(String[] args) {
		this.scenarioName.setValue(commandLineReader.parse(this.scenarioName));
	}

	@Override
	public RunEcecutionCallable configureRun(ScalarRadioMANET manet,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> resultRecorder) {
		GreedyCombinationOptimization go = new GreedyCombinationOptimization(manet);
		return new DeterministicRun(go, resultRecorder);
	}

	public static void main(String[] args)
			throws InvocationTargetException, InterruptedException, ExecutionException, IOException {
		GreedyApp greedyApp = new GreedyApp(args);
		greedyApp.execute();
	}

}
