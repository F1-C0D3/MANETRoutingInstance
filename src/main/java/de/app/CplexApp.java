package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.app.commander.CommandArgument;
import de.app.commander.CommandLineReader;
import de.app.commander.PathComputationTechnique;
import de.approximation.app.ApproximationRun;
import de.approximation.optimization.CplexOptimization;
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

public class CplexApp extends App {
	protected CommandArgument<String> scenarioName;
	protected CommandArgument<Double> runtimeConstraint;

	public CplexApp(String[] args) {
		super(args);
		this.scenarioName = new CommandArgument<String>("--name", "-n", "Cplex");
		this.runtimeConstraint = new CommandArgument<Double>("--time", "-t", 7d);
		parseCommandLine(args);
		scenario.setScenarioName(scenarioName.value);
	}

	private void parseCommandLine(String[] args) {
		this.scenarioName.setValue(commandLineReader.parse(this.scenarioName));
		runtimeConstraint.setValue(Double.parseDouble(commandLineReader.parse(this.runtimeConstraint)));

	}

	public static void main(String[] args)
			throws InvocationTargetException, InterruptedException, ExecutionException, IOException {
		CplexApp cplexApp = new CplexApp(args);
		cplexApp.execute();
	}

	@Override
	public RunEcecutionCallable configureRun(ScalarRadioMANET manet,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> resultRecorder) {

		CplexOptimization co = new CplexOptimization(manet, runtimeConstraint.value);
		return new ApproximationRun(co, resultRecorder);
	}
}
