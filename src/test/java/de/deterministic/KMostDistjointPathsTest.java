package de.deterministic;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import org.junit.Test;

import de.deterministic.optimization.KMostDisjointPathsOptimization;
import de.jgraphlib.generator.GridGraphGenerator;
import de.jgraphlib.generator.GridGraphProperties;
import de.jgraphlib.generator.GraphProperties.EdgeStyle;
import de.jgraphlib.gui.VisualGraphApp;
import de.jgraphlib.gui.printer.WeightedEdgeIDPrinter;
import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.evaluator.DoubleScope;
import de.manetmodel.evaluator.ScalarLinkQualityEvaluator;
import de.manetmodel.generator.OverUtilizedProblemProperties;
import de.manetmodel.generator.OverUtilzedProblemGenerator;
import de.manetmodel.mobilitymodel.PedestrianMobilityModel;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioMANETSupplier;
import de.manetmodel.network.scalar.ScalarRadioModel;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.scenarios.Scenario;
import de.manetmodel.units.DataRate;
import de.manetmodel.units.DataUnit.Type;
import de.manetmodel.units.Speed;
import de.manetmodel.units.Speed.SpeedRange;
import de.manetmodel.units.Unit;
import de.manetmodel.units.Watt;
import de.terministic.serein.api.RecombinationException;

public class KMostDistjointPathsTest {

	@Test
	public void directeConnectedSourceTargetPathSingleFlowTest()
			throws InvocationTargetException, InterruptedException, RecombinationException {

		int numRuns=1;
		int numFlows=2;
		int overUtilizationPercentage = 10;
		Scenario scenario = new Scenario("kMostDisjountPath", numFlows, 100, numRuns,overUtilizationPercentage,0,-1);
		RandomNumbers random = RandomNumbers.getInstance(0);
		ScalarRadioModel radioModel = new ScalarRadioModel(new Watt(0.001d), new Watt(1e-11), 2000000d, 2412000000d,
				35d,100);
		PedestrianMobilityModel mobilityModel = new PedestrianMobilityModel(random,
				new SpeedRange(0, 100, Unit.TimeSteps.second, Unit.Distance.meter),
				new Speed(50, Unit.Distance.meter, Unit.TimeSteps.second));
		ScalarLinkQualityEvaluator evaluator = new ScalarLinkQualityEvaluator(new DoubleScope(0d, 1d), radioModel,
				mobilityModel);

		Supplier<ScalarLinkQuality> linkPropertySupplier = new ScalarRadioMANETSupplier().getLinkPropertySupplier();
		ScalarRadioMANET manet = new ScalarRadioMANET(new ScalarRadioMANETSupplier().getNodeSupplier(),
				new ScalarRadioMANETSupplier().getLinkSupplier(),
				linkPropertySupplier,
				new ScalarRadioMANETSupplier().getFlowSupplier(), radioModel, mobilityModel, evaluator);

		GridGraphProperties properties = new GridGraphProperties(200, 200, 100, 100,EdgeStyle.BIDIRECTIONAL);

		GridGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> generator = new GridGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet,linkPropertySupplier, RandomNumbers.getInstance(-1));

		generator.generate(properties);
		manet.initialize();
		SwingUtilities.invokeAndWait(new VisualGraphApp<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet, new WeightedEdgeIDPrinter<ScalarRadioLink, ScalarLinkQuality>()));
		Function<ScalarLinkQuality, Double> metric = (ScalarLinkQuality w) -> {
			return (w.getReceptionConfidence() * 0.6) + (w.getRelativeMobility() * 0.2)
					+ (w.getSpeedQuality() * 0.2);
		};

		OverUtilzedProblemGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> overUtilizedProblemGenerator = new OverUtilzedProblemGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow>(
				manet, metric,random);

		OverUtilizedProblemProperties problemProperties = new OverUtilizedProblemProperties(
				/* Number of paths */scenario.getNumFlows(), /* Minimum path length */10,
				/* Maximum path length */20, /* Minimum demand of each flow */new DataRate(50, Type.kilobit),
				/* Maximum demand of each flow */new DataRate(100, Type.kilobit),
				/* Unique source destination pairs */true,
				/* Over-utilization percentage */scenario.getOverUtilizePercentage(),
				/* Increase factor of each tick */new DataRate(20, Type.kilobit));

		List<ScalarRadioFlow> flowProblems = overUtilizedProblemGenerator.compute(problemProperties);
		manet.addFlows(flowProblems);
		
		KMostDisjointPathsOptimization optimization = new KMostDisjointPathsOptimization(manet, 10,random);
		manet= optimization.execute();

		
	}
}