package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.app.commander.CommandArgument;
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
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;
import de.manetmodel.scenarios.Scenario;
import de.manetmodel.units.Time;
import de.manetmodel.units.Unit.TimeSteps;
import de.parallelism.RunEcecutionCallable;

public class GeneticApp extends App {
	protected CommandArgument<String> scenarioName;
	protected CommandArgument<Integer> runtimeConstraint;
	protected CommandArgument<Double> instructionFactor;
	protected CommandArgument<Integer> population;

	public GeneticApp(String[] args) {
		super(args);
		this.scenarioName = new CommandArgument<String>("--name", "-n", "Genetic");
		this.runtimeConstraint = new CommandArgument<Integer>("--time", "-t", 7000);
		this.instructionFactor = new CommandArgument<Double>("--instructionFactor", "-i", 0.4);
		this.population = new CommandArgument<Integer>("--population", "-p", 500);
		parseCommandLine(args);

		this.scenario.setScenarioName(scenarioName.value);
	}

	private void parseCommandLine(String[] args) {
		this.scenarioName.setValue(commandLineReader.parse(this.scenarioName));
		runtimeConstraint.setValue(Integer.parseInt(commandLineReader.parse(this.runtimeConstraint)));
		instructionFactor.setValue(Double.parseDouble(commandLineReader.parse(this.instructionFactor)));
		population.setValue(Integer.parseInt(commandLineReader.parse(this.population)));

	}

	@Override
	public RunEcecutionCallable configureRun(ScalarRadioMANET manet,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> resultRecorder) {
		GeneticOptimization go = new GeneticOptimization(manet, /* Initial Population */population.value,
				/* Termination condition */new TerminationConditionMANET(/* Successive fitness levels */15,
						/* Max generations */700, /* Min fitness level */ 0.05,
						/* Max runtime */new Time(TimeSteps.milliseconds, runtimeConstraint.value)),
				/* mutation probability */0.25, /* Instruction factor */instructionFactor.value,
				RandomNumbers.getInstance(0));
		return new GeneticRun(go, resultRecorder);
	}

	public static void main(String[] args) throws InvocationTargetException, InterruptedException, ExecutionException, IOException {
		GeneticApp geneticApp = new GeneticApp(args);
		geneticApp.execute();
	}
}
