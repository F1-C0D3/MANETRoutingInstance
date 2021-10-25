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
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import de.jgraphlib.graph.generator.GraphProperties.DoubleRange;
import de.jgraphlib.graph.generator.GraphProperties.IntRange;
import de.jgraphlib.graph.generator.NetworkGraphGenerator;
import de.jgraphlib.graph.generator.NetworkGraphProperties;
import de.jgraphlib.gui.VisualGraphApp;
import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.evaluator.DoubleScope;
import de.manetmodel.evaluator.SimpleLinkQualityEvaluator;
import de.manetmodel.generator.OverUtilizedProblemProperties;
import de.manetmodel.generator.OverUtilzedProblemGenerator;
import de.manetmodel.gui.printer.LinkUtilizationPrinter;
import de.manetmodel.mobilitymodel.PedestrianMobilityModel;
import de.manetmodel.mobilitymodel.RandomWaypointMobilityModel;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioMANETSupplier;
import de.manetmodel.network.scalar.ScalarRadioModel;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;
import de.manetmodel.results.MANETTotalResultRecorder;
import de.manetmodel.results.RunResultRecorder;
import de.manetmodel.results.TotalResultParameter;
import de.manetmodel.scenarios.Scenario;
import de.manetmodel.units.DataRate;
import de.manetmodel.units.DataUnit.Type;
import de.manetmodel.units.Speed;
import de.manetmodel.units.Speed.SpeedRange;
import de.manetmodel.units.Time;
import de.manetmodel.units.Unit;
import de.manetmodel.units.Watt;
import de.parallelism.RunEcecutionCallable;
import de.result.ScalarRadioRunResultMapper;
import de.result.ScalarRadioTotalResultMapper;

public abstract class App {
	private boolean visual;
	private int runs;
	private Scenario scenario;
	ExecutorService executor;
	private RandomNumbers random;
	private List<RunEcecutionCallable> executionList;
	List<MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow>> runResultrecorders;

	public App(Scenario scenario, RandomNumbers random, boolean visual) {
		this.visual = visual;
		this.runs = scenario.getNumRuns();
		this.scenario = scenario;
		this.random = random;
		this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.executionList = new ArrayList<RunEcecutionCallable>();
		this.runResultrecorders = new ArrayList<MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow>>();
	}

	public abstract RunEcecutionCallable configureRun(
			ScalarRadioMANET manet,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> runResultRecorder);

	protected void execute() throws InvocationTargetException, InterruptedException, ExecutionException, IOException {

		int currentRun = 0;
		while (currentRun < runs) {

			// Define Mobility Model
			PedestrianMobilityModel mobilityModel = new PedestrianMobilityModel(random, /* Speed min and max of nodes */
					new SpeedRange(0d, 10d, Unit.TimeSteps.hour, Unit.Distance.kilometer), /* Tick duration */
					new Time(Unit.TimeSteps.second, 1l),
					/* deviation of assigned speed */new Speed(1.2d, Unit.Distance.kilometer, Unit.TimeSteps.hour),
					/* ticks */10);

//			RandomWaypointMobilityModel mobilityModel = new RandomWaypointMobilityModel(random, /*
//																								 * Speed min and max of
//																								 * nodes
//																								 */
//					new SpeedRange(59d, 60d, Unit.TimeSteps.hour, Unit.Distance.kilometer), /* Tick duration */
//					new Time(Unit.TimeSteps.second, 1l), /* ticks */10);

			double maxCommunicationRange = 100d;
			double minCommunicationRange = 35d;
			// Set RadioModel
			ScalarRadioModel radioModel = new ScalarRadioModel(new Watt(0.001d), new Watt(1e-11), 2000000d, 2412000000d,
					minCommunicationRange, maxCommunicationRange);

			// ScalLinkQualityEvaluator
			SimpleLinkQualityEvaluator evaluator = new SimpleLinkQualityEvaluator(new DoubleScope(0d, 1d), radioModel,
					mobilityModel);
			// Create MANET with scalar radio properties
			ScalarRadioMANET manet = new ScalarRadioMANET(new ScalarRadioMANETSupplier.ScalarRadioNodeSupplier(),
					new ScalarRadioMANETSupplier.ScalarRadioLinkSupplier(),
					new ScalarRadioMANETSupplier.ScalarLinkQualitySupplier(),
					new ScalarRadioMANETSupplier.ScalarRadioFlowSupplier(), radioModel, mobilityModel, evaluator);

			NetworkGraphProperties properties = new NetworkGraphProperties( /* playground width */ 1024,
					/* playground height */ 768,
					/* number of vertices */ new IntRange(scenario.getNumNodes(), scenario.getNumNodes()),
					/* distance between vertices */ new DoubleRange(minCommunicationRange, maxCommunicationRange),
					/* edge distance */ new DoubleRange(100, 100));
			NetworkGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> networkGraphGenerator = new NetworkGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
					manet, random);
			networkGraphGenerator.generate(properties);
			manet.initialize();

			Function<ScalarLinkQuality, Double> metric = (ScalarLinkQuality w) -> {
				return (w.getReceptionConfidence() * 0.6) + (w.getRelativeMobility() * 0.2)
						+ (w.getSpeedQuality() * 0.2);
			};

			OverUtilzedProblemGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> overUtilizedProblemGenerator = new OverUtilzedProblemGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow>(
					manet, metric);

			OverUtilizedProblemProperties problemProperties = new OverUtilizedProblemProperties(
					/* Number of paths */scenario.getNumFlows(), /* Minimum path length */10,
					/* Maximum path length */20, /* Minimum demand of each flow */new DataRate(50, Type.kilobit),
					/* Maximum demand of each flow */new DataRate(100, Type.kilobit),
					/* Unique source destination pairs */true,
					/* Over-utilization percentage */scenario.getOverUtilizePercentage(),
					/* Increase factor of each tick */new DataRate(20, Type.kilobit));
			
			List<ScalarRadioFlow> flowProblems = overUtilizedProblemGenerator.compute(problemProperties, random);
			manet.addFlows(flowProblems);
//			System.out.println(manet.getUtilization());
//			ScalarRadioFlow f1 = new ScalarRadioFlow(manet.getVertex(0), manet.getVertex(99),
//					new DataRate(1, Type.megabit));
//			ScalarRadioFlow f2 = new ScalarRadioFlow(manet.getVertex(10), manet.getVertex(89),
//					new DataRate(0.1, Type.megabit));
//
//			ScalarRadioFlow f3 = new ScalarRadioFlow(manet.getVertex(20), manet.getVertex(79),
//					new DataRate(1, Type.megabit));
//			manet.addFlow(f1);
//			manet.addFlow(f2);
//			manet.addFlow(f3);

			/* Result recording options for further evaluation */
			ColumnPositionMappingStrategy<AverageRunResultParameter> averageMappingStrategy = new ColumnPositionMappingStrategy<AverageRunResultParameter>() {
				@Override
				public String[] generateHeader(AverageRunResultParameter bean) throws CsvRequiredFieldEmptyException {
					return this.getColumnMapping();
				}
			};
			averageMappingStrategy.setColumnMapping("overUtilization", "utilization", "activePathParticipants",
					"meanConnectionStability", "minConnectionStability", "maxConnectionStability","numberOfUndeployedFlows", "simulationTime","runNumber");

			ColumnPositionMappingStrategy<IndividualRunResultParameter> individualMappingStrategy = new ColumnPositionMappingStrategy<IndividualRunResultParameter>() {
				@Override
				public String[] generateHeader(IndividualRunResultParameter bean)
						throws CsvRequiredFieldEmptyException {
					return this.getColumnMapping();
				}
			};
			individualMappingStrategy.setColumnMapping("lId", "n1Id", "n2Id", "overUtilization", "utilization",
					"isPathParticipant", "connectionStability");

			ScalarRadioRunResultMapper runResultMapper = new ScalarRadioRunResultMapper(individualMappingStrategy,
					averageMappingStrategy, scenario, mobilityModel);

			runResultMapper.getAverageMappingStrategy().setType(AverageRunResultParameter.class);
			runResultMapper.getIndividualMappingStrategy().setType(IndividualRunResultParameter.class);

			/* Result recording options for further evaluation */
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> resultRecorder = new MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow>(
					scenario.getScenarioName(), runResultMapper, currentRun);

			// Define individual run result recorder

//
			RunEcecutionCallable run = this
					.configureRun(manet, resultRecorder);
//			CplexOptimization aco = new CplexOptimization(manet);
//			ApproximationRun dr= new ApproximationRun(aco, resultRecorder, runResultMapper);
//			dr.call();
//			System.out.println(String.format("Finished with Setting %d, OverUtilization=%s", 1,manet.getOverUtilization().toString()));
//			ScalarRadioMANET scalarRadioMANET = future.get();
			executionList.add(run);
			runResultrecorders.add(resultRecorder);
			currentRun++;

		}

		List<Future<ScalarRadioMANET>> futureList = executor.invokeAll(executionList);

		int i = 0;
		for (Future<ScalarRadioMANET> future : futureList) {
			ScalarRadioMANET scalarRadioMANET = future.get();
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> resultRecorder = runResultrecorders
					.get(i);
			// Record average of each run
			resultRecorder.recordAverage(scalarRadioMANET);

			// Display result with VisualGraph
			if (visual)
				SwingUtilities.invokeAndWait(new VisualGraphApp<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
						scalarRadioMANET, new LinkUtilizationPrinter<ScalarRadioLink, ScalarLinkQuality>()));

			// Print Utilization
			System.out.println(String.format("Finished with Setting %d, OverUtilization=%s, ActiveUtilizedLinks=%d",
					i++, scalarRadioMANET.getOverUtilization().toString(),
					scalarRadioMANET.getActiveUtilizedLinks().size()));
		}

		ColumnPositionMappingStrategy<TotalResultParameter> totalMappingStrategy = new ColumnPositionMappingStrategy<TotalResultParameter>() {
			@Override
			public String[] generateHeader(TotalResultParameter bean) throws CsvRequiredFieldEmptyException {
				return this.getColumnMapping();
			}
		};
		totalMappingStrategy.setColumnMapping("meanOverUtilization", "meanUtilization", "activePathParticipants",
				"meanAverageConnectionStability", "minAverageConnectionStability","maxAverageConnectionStability", "meanNumberOfUndeployedFlows",
				"meanAveragesimulationTime", "minAveragesimulationTime", "maxAveragesimulationTime","finishedRuns");

		ScalarRadioTotalResultMapper runResultMapper = new ScalarRadioTotalResultMapper(scenario, totalMappingStrategy);

		runResultMapper.getTotalMappingStrategy().setType(TotalResultParameter.class);

		MANETTotalResultRecorder<TotalResultParameter, IndividualRunResultParameter, AverageRunResultParameter> totalResultRecorder = new MANETTotalResultRecorder<TotalResultParameter, IndividualRunResultParameter, AverageRunResultParameter>(
				scenario.getScenarioName(), runResultMapper,runResultrecorders.stream().map(rrr -> rrr.getRunResultContent()).collect(Collectors.toList()));
		totalResultRecorder.finish();
		executor.shutdown();

		if (visual)
			System.in.read();
		System.exit(0);
	}

}
