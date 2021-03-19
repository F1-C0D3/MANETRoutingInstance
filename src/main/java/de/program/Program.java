package de.program;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.WindowConstants;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import de.deterministic.app.DeterministicRun;
import de.deterministic.network.DeterministicMANET;
import de.deterministic.network.DeterministicMANETSupplier;
import de.deterministic.optimization.AllCombinationOptimization;
import de.deterministic.optimization.GreedyCombinationOptimization;
import de.deterministic.optimization.MultipleDijkstraLinkQuality;
import de.genetic.app.GeneticRun;
import de.genetic.network.GeneticMANET;
import de.genetic.network.GeneticMANETSupplier;
import de.genetic.optimization.GeneticOptimization;
import de.genetic.optimization.PathComposition;
import de.jgraphlib.graph.generator.GraphProperties.DoubleRange;
import de.jgraphlib.graph.generator.GraphProperties.IntRange;
import de.jgraphlib.graph.generator.NetworkGraphGenerator;
import de.jgraphlib.graph.generator.NetworkGraphProperties;
import de.jgraphlib.gui.VisualGraph;
import de.jgraphlib.gui.VisualGraphFrame;
import de.jgraphlib.gui.VisualGraphMarkUp;
import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.mobility.PedestrianMobilityModel;
import de.manetmodel.network.radio.ScalarRadioModel;
import de.manetmodel.network.unit.DataRate;
import de.manetmodel.network.unit.DataUnit;
import de.manetmodel.network.unit.Speed;
import de.manetmodel.network.unit.VelocityUnits;
import de.parallelism.Run;
import de.parallelism.Task;
import de.manetmodel.network.unit.Speed.SpeedRange;
import de.manetmodel.network.unit.Speed.Time;
import de.results.MANETParameterRecorder;
import de.results.MANETResultRunSupplier;
import de.results.MANETRunResult;
import de.results.ResultRecorder;
import de.results.Scenario;

public class Program implements Runnable {

	MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet;
	private VisualGraphFrame<Node, Link<LinkQuality>> frame;
	VisualGraph<Node, Link<LinkQuality>> visualGraph;

	public Program(MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet) {
		this.manet = manet;
		visualGraph = new VisualGraph<Node, Link<LinkQuality>>(this.manet, new VisualGraphMarkUp());
		frame = new VisualGraphFrame<Node, Link<LinkQuality>>(visualGraph);
	}

	public static void main(String[] args) {

		/* Defining Thread pool */
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		NetworkGraphProperties properties = new NetworkGraphProperties(/* width */ 1000, /* height */ 1000,
				/* vertices */ new IntRange(100, 100), /* vertex distance */ new DoubleRange(55d, 100d),
				/* edge distance */ 100);

		/* Radio wave propagation model to determine bitrate and receptionpower */
		ScalarRadioModel radioModel = new ScalarRadioModel(0.002d, 1e-11, 2000000d, 2412000000d);

		/* Mobility model to include movement of nodes based on velocity and pattern */
		PedestrianMobilityModel mobilityModel = new PedestrianMobilityModel(new RandomNumbers(2),
				new SpeedRange(4d, 40d, VelocityUnits.TimeUnit.hour, VelocityUnits.DistanceUnit.kilometer),
				new Time(VelocityUnits.TimeUnit.second, 30d),
				new Speed(4d, VelocityUnits.DistanceUnit.kilometer, VelocityUnits.TimeUnit.hour), 10);

		/* Result recording options for further evaluation */
		ColumnPositionMappingStrategy<MANETRunResult> mappingStrategy = new ColumnPositionMappingStrategy<MANETRunResult>() {
			@Override
			public String[] generateHeader(MANETRunResult bean) throws CsvRequiredFieldEmptyException {
				return this.getColumnMapping();
			}
		};
		mappingStrategy.setType(MANETRunResult.class);

		MANETParameterRecorder<LinkQuality, MANETRunResult> geneticRunRecorder = new MANETParameterRecorder<LinkQuality, MANETRunResult>(
				new MANETResultRunSupplier(), new Scenario(), properties, radioModel, mobilityModel);
		ResultRecorder<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>, MANETRunResult> geneticEvalRecorder = new ResultRecorder<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>, MANETRunResult>(
				GeneticOptimization.class.getSimpleName(), geneticRunRecorder, mappingStrategy);

		MANETParameterRecorder<MultipleDijkstraLinkQuality, MANETRunResult> allCombinationsRunRecorder = new MANETParameterRecorder<MultipleDijkstraLinkQuality, MANETRunResult>(
				new MANETResultRunSupplier(), new Scenario(), properties, radioModel, mobilityModel);
		ResultRecorder<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, MANETRunResult> allCombinationsEvalRecorder = new ResultRecorder<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, MANETRunResult>(
				AllCombinationOptimization.class.getSimpleName(), allCombinationsRunRecorder, mappingStrategy);

		MANETParameterRecorder<MultipleDijkstraLinkQuality, MANETRunResult> greedyRunRecorder = new MANETParameterRecorder<MultipleDijkstraLinkQuality, MANETRunResult>(
				new MANETResultRunSupplier(), new Scenario(), properties, radioModel, mobilityModel);
		ResultRecorder<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, MANETRunResult> greedyEvalRecorder = new ResultRecorder<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, MANETRunResult>(
				GreedyCombinationOptimization.class.getSimpleName(), greedyRunRecorder, mappingStrategy);

		for (int i = 0; i < 2; i++) {

			GeneticMANET geneticMANET = new GeneticMANET(new GeneticMANETSupplier.GeneticMANETNodeSupplier(),
					new GeneticMANETSupplier.GeneticMANETLinkSupplier(),
					new GeneticMANETSupplier.GeneticMANETFlowSupplier(), radioModel, mobilityModel);

			DeterministicMANET allCombinationDeterministicMANET = new DeterministicMANET(
					new DeterministicMANETSupplier.DeterministicMANETNodeSupplier(),
					new DeterministicMANETSupplier.DeterministicMANETLinkSupplier(),
					new DeterministicMANETSupplier.DeterministicMANETFlowSupplier(), radioModel, mobilityModel);

			DeterministicMANET greedyDeterministicMANET = new DeterministicMANET(
					new DeterministicMANETSupplier.DeterministicMANETNodeSupplier(),
					new DeterministicMANETSupplier.DeterministicMANETLinkSupplier(),
					new DeterministicMANETSupplier.DeterministicMANETFlowSupplier(), radioModel, mobilityModel);

//			GridGraphGenerator<Node, Link<LinkQuality>, LinkQuality> generator = new GridGraphGenerator<Node, Link<LinkQuality>, LinkQuality>(
//					manet, new RandomNumbers(i));
//			GridGraphProperties properties = new GridGraphProperties(1000, 1000, 100, 100);
//			generator.generate(properties);
			NetworkGraphGenerator<Node, Link<LinkQuality>, LinkQuality> geneticGenerator = new NetworkGraphGenerator<Node, Link<LinkQuality>, LinkQuality>(
					geneticMANET, new RandomNumbers(i));
			geneticGenerator.generate(properties);

			NetworkGraphGenerator<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> deterministicAllCombinationsGenerator = new NetworkGraphGenerator<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>(
					allCombinationDeterministicMANET, new RandomNumbers(i));
			deterministicAllCombinationsGenerator.generate(properties);

			NetworkGraphGenerator<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> deterministicGreedyGenerator = new NetworkGraphGenerator<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>(
					greedyDeterministicMANET, new RandomNumbers(i));

			deterministicGreedyGenerator.generate(properties);
			Program program = new Program(geneticMANET);
			program.run();

			/* Flows to be routed */
			Node source1 = geneticMANET.getVertex(2);
			Node target1 = geneticMANET.getVertex(51);
			DataRate rate1 = new DataRate(1.2d, DataUnit.Type.megabit);

			Node source2 = geneticMANET.getVertex(100);
			Node target2 = geneticMANET.getVertex(83);
			DataRate rate2 = new DataRate(1.8d, DataUnit.Type.megabit);

			Node source3 = geneticMANET.getVertex(68);
			Node target3 = geneticMANET.getVertex(10);
			DataRate rate3 = new DataRate(1.2d, DataUnit.Type.megabit);

			Node source4 = geneticMANET.getVertex(54);
			Node target4 = geneticMANET.getVertex(27);
			DataRate rate4 = new DataRate(1.4d, DataUnit.Type.megabit);

			geneticMANET.addFlow(source1, target1, rate1);
			geneticMANET.addFlow(source2, target2, rate2);
			geneticMANET.addFlow(source3, target3, rate3);
			geneticMANET.addFlow(source4, target4, rate4);

			allCombinationDeterministicMANET.addFlow(source1, target1, rate1);
			allCombinationDeterministicMANET.addFlow(source2, target2, rate2);
			allCombinationDeterministicMANET.addFlow(source3, target3, rate3);
			allCombinationDeterministicMANET.addFlow(source4, target4, rate4);

			greedyDeterministicMANET.addFlow(source1, target1, rate1);
			greedyDeterministicMANET.addFlow(source2, target2, rate2);
			greedyDeterministicMANET.addFlow(source3, target3, rate3);
			greedyDeterministicMANET.addFlow(source4, target4, rate4);

			/* Execution */
			GeneticOptimization go = new GeneticOptimization(geneticMANET, 500000, 4);
			AllCombinationOptimization<MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>> dao = new AllCombinationOptimization<MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>>(
					allCombinationDeterministicMANET);
			GreedyCombinationOptimization<MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>> dgo = new GreedyCombinationOptimization<MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>>(
					greedyDeterministicMANET);

			geneticRunRecorder
					.setScenario(new Scenario(de.genetic.optimization.GeneticOptimization.class.getSimpleName(),
							geneticMANET.getFlows().size(), geneticMANET.getVertices().size()));

			allCombinationsRunRecorder.setScenario(
					new Scenario(de.deterministic.optimization.AllCombinationOptimization.class.getSimpleName(),
							geneticMANET.getFlows().size(), allCombinationDeterministicMANET.getVertices().size()));

			greedyRunRecorder.setScenario(
					new Scenario(de.deterministic.optimization.GreedyCombinationOptimization.class.getSimpleName(),
							geneticMANET.getFlows().size(), greedyDeterministicMANET.getVertices().size()));

			GeneticRun geneticRun = new GeneticRun(go, geneticEvalRecorder);
			DeterministicRun greedyHeuristicRun = new DeterministicRun(dgo, greedyEvalRecorder);
			DeterministicRun allHeuristicRun = new DeterministicRun(dao, allCombinationsEvalRecorder);
//			executor.execute(GeneticRun::executeRun(go));
//			executor.execute(greedyHeuristicRun);
			executor.execute(allHeuristicRun);
		}

		executor.shutdown();
		try {
			boolean awaitTermination = executor.awaitTermination(1L, TimeUnit.DAYS);
			System.err.println(awaitTermination);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		geneticEvalRecorder.finish();
		allCombinationsEvalRecorder.finish();
		greedyEvalRecorder.finish();
	}

	@Override
	public void run() {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setPreferredSize(
				new Dimension((int) screenSize.getWidth() * 3 / 4, (int) screenSize.getHeight() * 3 / 4));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void printPath(Flow<Node, Link<LinkQuality>, LinkQuality> flow) {
		visualGraph.addVisualPath(flow);
		frame.getVisualGraphPanel().repaint();
	}
}
