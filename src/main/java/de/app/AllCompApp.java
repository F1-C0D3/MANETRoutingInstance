package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.app.commander.CommandArgument;
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

	protected CommandArgument<String> scenarioName;

	public AllCompApp(String[] args) {
		super(args);
		this.scenarioName = new CommandArgument<String>("--scenarioName", "-sN", "AllCombinations");
		parseCommandLine(args);
		this.scenario.setScenarioName(scenarioName.value);
	}
	
	private void parseCommandLine(String[] args) {
		scenarioName.setValue(commandLineReader.parse(this.scenarioName));
	}
	@Override
	public RunEcecutionCallable configureRun(ScalarRadioMANET manet,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> resultRecorder) {

		AllCombinationOptimization aco = new AllCombinationOptimization(manet);
		return new DeterministicRun(aco, resultRecorder);
	}

	public static void main(String[] args)
			throws InterruptedException, ExecutionException, IloException, InvocationTargetException, IOException {

		AllCompApp allComp = new AllCompApp(args);
		allComp.execute();

	}
}
