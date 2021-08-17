package de.app;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.genetic.app.GeneticRun;
import de.genetic.network.GeneticMANETSupplier;
import de.genetic.optimization.GeneticOptimization;
import de.jgraphlib.graph.generator.NetworkGraphProperties;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.mobility.MobilityModel;
import de.manetmodel.network.radio.IRadioModel;
import de.manetmodel.scenarios.Scenario;
import de.results.RunResultParameter;
import de.results.RunResultParameterSupplier;
import de.results.AverageResultParameter;
import de.results.AverageResultParameterSupplier;
import de.results.MANETAverageResultMapper;
import de.results.MANETResultRecorder;
import de.results.RunResultMapper;
import de.runprovider.ExecutionCallable;
import de.runprovider.Program;
import ilog.concert.IloException;

public class GeneticApp extends App {

	public GeneticApp(int runs, Scenario scenario) {
		super(runs, scenario);
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException, IloException {
		HighUtilizedMANETSecenario scenario = new HighUtilizedMANETSecenario("test", 10, 100);
		GeneticApp greedyApp = new GeneticApp(1, scenario);

		greedyApp.execute();
	}

	@Override
	public ExecutionCallable<Flow<Node, Link<LinkQuality>, LinkQuality>, Node, Link<LinkQuality>, LinkQuality> configureRun(
			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet,
			MANETResultRecorder<RunResultParameter> geneticEvalRecorder,
			RunResultMapper<RunResultParameter> runResultMapper) {
		GeneticOptimization go = new GeneticOptimization(manet, 20000, 12);
		return new GeneticRun(go, geneticEvalRecorder, runResultMapper);
	}
}
