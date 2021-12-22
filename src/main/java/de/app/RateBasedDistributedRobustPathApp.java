package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.app.commander.CommandArgument;
import de.deterministic.optimization.RateBasedDistributedRobustPathsOptimization;
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

public class RateBasedDistributedRobustPathApp extends App {
	protected CommandArgument<String> scenarioName;

	public RateBasedDistributedRobustPathApp(String[] args) {
		super(args);
		this.scenarioName = new CommandArgument<String>("--name", "-n", "RBDRP");
		parseCommandLine(args);
		this.scenario.setScenarioName(scenarioName.value);
	}

	private void parseCommandLine(String[] args) {
		this.scenarioName.setValue(commandLineReader.parse(this.scenarioName));
	}

	@Override
	public RunEcecutionCallable configureRun(ScalarRadioMANET manet,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> resultRecorder) {

		RateBasedDistributedRobustPathsOptimization rbdrpo = new RateBasedDistributedRobustPathsOptimization(manet);
		return new Run(rbdrpo, resultRecorder);
	}

	public static void main(String[] args)
			throws InvocationTargetException, InterruptedException, ExecutionException, IOException {
		RateBasedDistributedRobustPathApp rbdrp = new RateBasedDistributedRobustPathApp(args);
		rbdrp.execute();
	}

}
