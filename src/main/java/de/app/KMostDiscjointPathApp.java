package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.app.commander.CommandArgument;
import de.deterministic.optimization.KMostDisjointPathsOptimization;
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

public class KMostDiscjointPathApp extends App {
	protected CommandArgument<String> scenarioName;
	protected CommandArgument<Integer> kFlows;

	public KMostDiscjointPathApp(String[] args) {
		super(args);
		this.scenarioName = new CommandArgument<String>("--name", "-n", "KMDP");
		this.kFlows = new CommandArgument<Integer>("--kFlows", "-k", 3);
		parseCommandLine(args);
		this.scenario.setScenarioName(scenarioName.value);
	}
	private void parseCommandLine(String[] args) {
		this.scenarioName.setValue(commandLineReader.parse(this.scenarioName));
		this.kFlows.setValue(Integer.parseInt(commandLineReader.parse(this.kFlows)));
	}
	@Override
	public RunEcecutionCallable configureRun(
			ScalarRadioMANET manet, MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> resultRecorder) {
		KMostDisjointPathsOptimization go = new KMostDisjointPathsOptimization(manet,kFlows.value,RandomNumbers.getInstance(3));
		return new Run(go, resultRecorder);
	}
	
	public static void main (String[] args) throws InvocationTargetException, InterruptedException, ExecutionException, IOException {
		KMostDiscjointPathApp kmdp = new KMostDiscjointPathApp(args);
		kmdp.execute();
	}

}
