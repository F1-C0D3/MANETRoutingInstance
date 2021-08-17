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
import de.jgraphlib.graph.generator.GridGraphGenerator;
import de.jgraphlib.graph.generator.GridGraphProperties;
import de.jgraphlib.graph.generator.NetworkGraphProperties;
import de.jgraphlib.gui.VisualGraphApp;
import de.jgraphlib.util.RandomNumbers;
import de.jgraphlib.util.Triple;
import de.manetmodel.gui.LinkQualityPrinter;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.mobility.MobilityModel;
import de.manetmodel.network.radio.IRadioModel;
import de.manetmodel.network.radio.IdealRadioModel;
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
//			IRadioModel radioModel = program.setRadioModel();
			IRadioModel radioModel = new IdealRadioModel(100, new DataRate(30, DataUnit.Type.megabit));
			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet = program
					.createMANET(mobilityModel, radioModel);

			NetworkGraphProperties networkProperties = program.generateNetwork(manet, runs, scenario.getNumNodes());

//			GridGraphProperties gridPropoerties = new GridGraphProperties(800,800, 100, 100);
//			GridGraphGenerator<Node, Link<LinkQuality>, LinkQuality> generator = new GridGraphGenerator<Node, Link<LinkQuality>, LinkQuality>(
//					manet, RandomNumbers.getInstance(runs));

//			generator.generate(gridPropoerties);
//			manet.initialize();
//			scenario.generateFlows(manet, runs);
			Flow<Node, Link<LinkQuality>, LinkQuality> flow1 = new Flow<Node, Link<LinkQuality>, LinkQuality>(
					manet.getVertex(31), manet.getVertex(40), new DataRate(5, DataUnit.Type.megabit));

			Flow<Node, Link<LinkQuality>, LinkQuality> flow2 = new Flow<Node, Link<LinkQuality>, LinkQuality>(
					manet.getVertex(28), manet.getVertex(34), new DataRate(1, DataUnit.Type.megabit));

			Flow<Node, Link<LinkQuality>, LinkQuality> flow3 = new Flow<Node, Link<LinkQuality>, LinkQuality>(
					manet.getVertex(42), manet.getVertex(52), new DataRate(2.5, DataUnit.Type.megabit));
//
			Flow<Node, Link<LinkQuality>, LinkQuality> flow4 = new Flow<Node, Link<LinkQuality>, LinkQuality>(
					manet.getVertex(29), manet.getVertex(58), new DataRate(0.05, DataUnit.Type.megabit));
//
			Flow<Node, Link<LinkQuality>, LinkQuality> flow5 = new Flow<Node, Link<LinkQuality>, LinkQuality>(
					manet.getVertex(56), manet.getVertex(68), new DataRate(1.0, DataUnit.Type.megabit));
//
			Flow<Node, Link<LinkQuality>, LinkQuality> flow6 = new Flow<Node, Link<LinkQuality>, LinkQuality>(
					manet.getVertex(36), manet.getVertex(20), new DataRate(0.01, DataUnit.Type.megabit));
//			Flow<Node, Link<LinkQuality>, LinkQuality> flow7 = new Flow<Node, Link<LinkQuality>, LinkQuality>(
//					manet.getVertex(49), manet.getVertex(7), new DataRate(1.0, DataUnit.Type.megabit));
//
//			Flow<Node, Link<LinkQuality>, LinkQuality> flow8 = new Flow<Node, Link<LinkQuality>, LinkQuality>(
//					manet.getVertex(10), manet.getVertex(6), new DataRate(1.0, DataUnit.Type.megabit));
//
//			Flow<Node, Link<LinkQuality>, LinkQuality> flow9 = new Flow<Node, Link<LinkQuality>, LinkQuality>(
//					manet.getVertex(66), manet.getVertex(3), new DataRate(1.0, DataUnit.Type.megabit));
			manet.addFlow(flow1);
			manet.addFlow(flow2);
			manet.addFlow(flow3);
			manet.addFlow(flow4);
			manet.addFlow(flow5);
			manet.addFlow(flow6);
//			manet.addFlow(flow7);
			RunResultMapper<RunResultParameter> runResultMapper = program.setIndividualRunResultMapper(
					new RunResultParameterSupplier(), networkProperties, mobilityModel, radioModel, scenario);
			runResultMapper.getMappingStrategy().setType(RunResultParameter.class);

			ExecutionCallable<Flow<Node, Link<LinkQuality>, LinkQuality>, Node, Link<LinkQuality>, LinkQuality> run = this
					.configureRun(manet, resultRecorder, runResultMapper);
			Future<List<Flow<Node, Link<LinkQuality>, LinkQuality>>> futureFlows = executor.submit(run);
			futureFlows.get();
			boolean taskFinished = false;
			while (!taskFinished) {
				taskFinished = futureFlows.isDone();

			}
			manet.undeployFlows();
			
			for (Flow<Node,Link<LinkQuality>,LinkQuality> f: futureFlows.get()) {
				manet.deployFlow(f);
				System.out.println(f.toString());
				
			}
			System.out.println(manet.getOverUtilization());
			runs--;

			VisualGraphApp<Node, Link<LinkQuality>, LinkQuality> visualGraphApp = new VisualGraphApp<Node, Link<LinkQuality>, LinkQuality>(
					manet, new LinkQualityPrinter());
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
