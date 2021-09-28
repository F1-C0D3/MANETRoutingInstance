package de.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import de.jgraphlib.graph.generator.GraphProperties.DoubleRange;
import de.jgraphlib.graph.generator.GraphProperties.IntRange;
import de.genetic.app.GeneticRun;
import de.genetic.optimization.GeneticOptimization;
import de.jgraphlib.graph.generator.NetworkGraphGenerator;
import de.jgraphlib.graph.generator.NetworkGraphProperties;
import de.jgraphlib.gui.VisualGraphApp;
import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.evaluator.DoubleScope;
import de.manetmodel.evaluator.ScalarLinkQualityEvaluator;
import de.manetmodel.generator.OverUtilizedProblemProperties;
import de.manetmodel.generator.OverUtilzedProblemGenerator;
import de.manetmodel.gui.LinkUtilizationPrinter;
import de.manetmodel.mobilitymodel.PedestrianMobilityModel;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioMANETSupplier;
import de.manetmodel.network.scalar.ScalarRadioModel;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageResultParameter;
import de.manetmodel.results.MANETAverageResultMapper;
import de.manetmodel.results.MANETResultRecorder;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.RunResultParameter;
import de.manetmodel.scenarios.Scenario;
import de.manetmodel.units.DataRate;
import de.manetmodel.units.Speed;
import de.manetmodel.units.Speed.SpeedRange;
import de.manetmodel.units.Time;
import de.manetmodel.units.Unit;
import de.manetmodel.units.Watt;
import de.parallelism.ExecutionCallable;
import de.results.ScalarRadioRunResultMapper;

public abstract class App {
	private int runs;
	private Scenario scenario;
	ExecutorService executor;
	private RandomNumbers random;
	private List<ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>> executionList;

	public App(int runs, Scenario scenario, RandomNumbers random) {
		this.runs = runs;
		this.scenario = scenario;
		this.random = random;
		this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.executionList = new ArrayList<ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>>();
	}

	public abstract ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> configureRun(
			ScalarRadioMANET manet, MANETResultRecorder<RunResultParameter, AverageResultParameter> resultRecorder,
			RunResultMapper<RunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> runResultMapper);

	protected void execute() throws InvocationTargetException, InterruptedException, ExecutionException, IOException {

		/* Result recording options for further evaluation */
		MANETResultRecorder<RunResultParameter, AverageResultParameter> resultRecorder = new MANETResultRecorder<RunResultParameter, AverageResultParameter>(
				scenario.getScenarioName());

		/* Result recording options for further evaluation */
		ColumnPositionMappingStrategy<AverageResultParameter> mappingStrategy = new ColumnPositionMappingStrategy<AverageResultParameter>() {
			@Override
			public String[] generateHeader(AverageResultParameter bean) throws CsvRequiredFieldEmptyException {
				return this.getColumnMapping();
			}
		};
		mappingStrategy.setColumnMapping("overUtilization", "utilization", "activePathParticipants",
				"connectionStability", "simulationTime");
		MANETAverageResultMapper totalResultMapper = new MANETAverageResultMapper(mappingStrategy, scenario);
		totalResultMapper.getMappingStrategy().setType(AverageResultParameter.class);

		while (runs > 0) {

			// Define Mobility Model
			PedestrianMobilityModel mobilityModel = new PedestrianMobilityModel(random,
					new SpeedRange(2d, 10d, Unit.TimeSteps.hour, Unit.Distance.kilometer),
					new Time(Unit.TimeSteps.second, 30l), new Speed(1.2d, Unit.Distance.kilometer, Unit.TimeSteps.hour),
					10);

			double maxCommunicationRange = 100d;
			// Set RadioModel
			ScalarRadioModel radioModel = new ScalarRadioModel(new Watt(0.001d), new Watt(1e-11), 2000000d, 2412000000d,
					maxCommunicationRange);

			// ScalLinkQualityEvaluator
			ScalarLinkQualityEvaluator evaluator = new ScalarLinkQualityEvaluator(new DoubleScope(0d, 1d), radioModel,
					mobilityModel);
			// Create MANET with scalar radio properties
			ScalarRadioMANET manet = new ScalarRadioMANET(new ScalarRadioMANETSupplier.ScalarRadioNodeSupplier(),
					new ScalarRadioMANETSupplier.ScalarRadioLinkSupplier(),
					new ScalarRadioMANETSupplier.ScalarLinkQualitySupplier(),
					new ScalarRadioMANETSupplier.ScalarRadioFlowSupplier(), radioModel, mobilityModel, evaluator);

			NetworkGraphProperties properties = new NetworkGraphProperties( /* playground width */ 1024,
					/* playground height */ 768,
					/* number of vertices */ new IntRange(scenario.getNumNodes(), scenario.getNumNodes()),
					/* distance between vertices */ new DoubleRange(45d, maxCommunicationRange),
					/* edge distance */ new DoubleRange(100, 100));
			NetworkGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> networkGraphGenerator = new NetworkGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
					manet, random);
			networkGraphGenerator.generate(properties);
			manet.initialize();

			Function<ScalarLinkQuality, Double> metric = (ScalarLinkQuality w) -> {
				return w.getDistance();
			};

			OverUtilzedProblemGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> overUtilizedProblemGenerator = new OverUtilzedProblemGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow>(
					manet, metric);

			OverUtilizedProblemProperties problemProperties = new OverUtilizedProblemProperties();
			problemProperties.pathCount = 5;
			problemProperties.minLength = 10;
			problemProperties.maxLength = 20;
			problemProperties.minDemand = new DataRate(100);
			problemProperties.maxDemand = new DataRate(200);
			problemProperties.overUtilizationPercentage = 2;
			problemProperties.uniqueSourceDestination = true;

			List<ScalarRadioFlow> flowProblems = overUtilizedProblemGenerator.compute(problemProperties, random);
			manet.addFlows(flowProblems);
//			ScalarRadioFlow f1 = new ScalarRadioFlow(manet.getVertex(0), manet.getVertex(99),
//					new DataRate(1, Type.megabit));
//			ScalarRadioFlow f2 = new ScalarRadioFlow(manet.getVertex(10), manet.getVertex(89),
//					new DataRate(1, Type.megabit));
//
//			ScalarRadioFlow f3 = new ScalarRadioFlow(manet.getVertex(20), manet.getVertex(79),
//					new DataRate(1, Type.megabit));
//			manet.addFlow(f1);
//			manet.addFlow(f2);
//			manet.addFlow(f3);
			// Define individual run result recorder
			ColumnPositionMappingStrategy<RunResultParameter> individualMappingStrategy = new ColumnPositionMappingStrategy<RunResultParameter>() {
				@Override
				public String[] generateHeader(RunResultParameter bean) throws CsvRequiredFieldEmptyException {
					return this.getColumnMapping();
				}
			};
			individualMappingStrategy.setColumnMapping("lId", "n1Id", "n2Id", "overUtilization", "utilization",
					"isPathParticipant", "connectionStability");
			ScalarRadioRunResultMapper runResultMapper = new ScalarRadioRunResultMapper(individualMappingStrategy,
					scenario, mobilityModel);
			runResultMapper.getMappingStrategy().setType(RunResultParameter.class);
//
//			ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> run = this
//					.configureRun(manet, resultRecorder, runResultMapper);
			
			GeneticOptimization go = new GeneticOptimization(manet,2500, 20,0.2, RandomNumbers.getInstance(0));
			 GeneticRun geneticRun = new GeneticRun(go, resultRecorder, runResultMapper);
			 geneticRun.call();
			

//			executionList.add(run);
			runs--;

		}

		List<Future<ScalarRadioMANET>> futureList = executor.invokeAll(executionList);
		
		int i = 0;
		for (Future<ScalarRadioMANET> future : futureList) {
			ScalarRadioMANET scalarRadioMANET = future.get();
				SwingUtilities.invokeAndWait(new VisualGraphApp<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
						scalarRadioMANET, new LinkUtilizationPrinter<ScalarRadioLink, ScalarLinkQuality>()));

			System.out.println(String.format("Finished with Setting %d, OverUtilization=%s", ++i,scalarRadioMANET.getOverUtilization().toString()));
		}
		resultRecorder.finish(totalResultMapper);
		executor.shutdown();

		System.in.read();
		System.exit(0);
	}

}
