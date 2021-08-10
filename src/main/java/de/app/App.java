package de.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.genetic.app.GeneticRun;
import de.genetic.network.GeneticMANETSupplier;
import de.genetic.optimization.GeneticOptimization;
import de.jgraphlib.graph.generator.NetworkGraphProperties;
import de.jgraphlib.util.Triple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.mobility.MobilityModel;
import de.manetmodel.network.radio.IRadioModel;
import de.manetmodel.network.unit.DataRate;
import de.manetmodel.network.unit.DataUnit;
import de.manetmodel.scenarios.Scenario;
import de.results.AverageResultParameter;
import de.results.AverageResultParameterSupplier;
import de.results.MANETAverageResultMapper;
import de.results.MANETResultRecorder;
import de.results.RunResultMapper;
import de.results.RunResultParameter;
import de.results.RunResultParameterSupplier;
import de.runprovider.ExecutionCallable;
import de.runprovider.Program;
import ilog.concert.IloException;

public abstract class App {
	private int runs;
	private Scenario scenario;
	ExecutorService executor;

	public App(int runs, Scenario scenario) {
		this.runs = runs;
		this.scenario = scenario;
		this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}

	public abstract ExecutionCallable<Flow<Node, Link<LinkQuality>, LinkQuality>, Node, Link<LinkQuality>, LinkQuality> configureRun(
			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet,
			MANETResultRecorder<RunResultParameter> geneticEvalRecorder,
			RunResultMapper<RunResultParameter> runResultMapper);

	protected void execute() throws InterruptedException, ExecutionException, IloException {
		Program<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> program = new Program<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>(
				new GeneticMANETSupplier.GeneticMANETNodeSupplier(),
				new GeneticMANETSupplier.GeneticMANETLinkSupplier(),
				new GeneticMANETSupplier.GeneticMANETLinkQualitySupplier(),
				new GeneticMANETSupplier.GeneticMANETFlowSupplier());

		MANETResultRecorder<RunResultParameter> resultRecorder = program.setResultRecorder(scenario.getScenarioName());
		MANETAverageResultMapper<AverageResultParameter> totalResultMapper = program
				.setTotalResultMapper(new AverageResultParameterSupplier(), scenario);
		totalResultMapper.getMappingStrategy().setType(AverageResultParameter.class);

		while (runs > 0) {

			MobilityModel mobilityModel = program.setMobilityModel(runs);
			IRadioModel radioModel = program.setRadioModel();
			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet = program
					.createMANET(mobilityModel, radioModel);

			Visualization<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> visualization = null;

			NetworkGraphProperties networkProperties = program.generateNetwork(manet, runs, scenario.getNumNodes());
			scenario.generateFlows(manet, runs);
			RunResultMapper<RunResultParameter> runResultMapper = program.setIndividualRunResultMapper(
					new RunResultParameterSupplier(), networkProperties, mobilityModel, radioModel, scenario);
			runResultMapper.getMappingStrategy().setType(RunResultParameter.class);

			/* Visialization */
//				visualization = new Visualization<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>(
//						manet);
//				visualization.run();

			/* Evaluation of each run starts here */
			ExecutionCallable<Flow<Node, Link<LinkQuality>, LinkQuality>, Node, Link<LinkQuality>, LinkQuality> run = this
					.configureRun(manet, resultRecorder, runResultMapper);
			Future<List<Flow<Node, Link<LinkQuality>, LinkQuality>>> futureFlows = executor.submit(run);

//				for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : futureFlows.get())
//					visualization.printPath(flow);
			runs--;

		}
		executor.shutdown();
		try {
			executor.awaitTermination(1L, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		resultRecorder.finish(totalResultMapper);
	}

}
