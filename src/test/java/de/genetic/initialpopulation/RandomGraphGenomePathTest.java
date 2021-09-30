package de.genetic.initialpopulation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.junit.Test;

import de.genetic.optimization.FlowDistributionFitness;
import de.genetic.optimization.GenesManetGraphTranslator;
import de.genetic.optimization.GraphGenome;
import de.genetic.optimization.PathComposition;
import de.genetic.optimization.SingleNodeMutation;
import de.genetic.optimization.UniformCrossoverPathSeperator;
import de.jgraphlib.graph.algorithms.DijkstraShortestPath;
import de.jgraphlib.graph.generator.GridGraphGenerator;
import de.jgraphlib.graph.generator.GridGraphProperties;
import de.jgraphlib.gui.VisualGraphApp;
import de.jgraphlib.gui.printer.WeightedEdgeIDPrinter;
import de.jgraphlib.util.RandomNumbers;
import de.jgraphlib.util.Tuple;
import de.manetmodel.evaluator.DoubleScope;
import de.manetmodel.evaluator.ScalarLinkQualityEvaluator;
import de.manetmodel.mobilitymodel.PedestrianMobilityModel;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioMANETSupplier;
import de.manetmodel.network.scalar.ScalarRadioModel;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.units.DataRate;
import de.manetmodel.units.Speed;
import de.manetmodel.units.Speed.SpeedRange;
import de.manetmodel.units.Unit;
import de.manetmodel.units.Watt;
import de.terministic.serein.api.Mutation;
import de.terministic.serein.api.TerminationCondition;
import de.terministic.serein.core.termination.TerminationConditionGenerations;

public class RandomGraphGenomePathTest {

	@Test
	public void generateRandomPathsTest() throws InvocationTargetException, InterruptedException {

		RandomNumbers randomInstance = RandomNumbers.getInstance(0);
		ScalarRadioModel radioModel = new ScalarRadioModel(new Watt(0.002d), new Watt(1e-11), 1000d, 2412000000d, /**
																													 * maxCommunicationRange
																													 **/
				100d);
		PedestrianMobilityModel mobilityModel = new PedestrianMobilityModel(randomInstance,
				new SpeedRange(0, 100, Unit.TimeSteps.second, Unit.Distance.meter),
				new Speed(50, Unit.Distance.meter, Unit.TimeSteps.second));
		ScalarLinkQualityEvaluator evaluator = new ScalarLinkQualityEvaluator(new DoubleScope(0d, 1d), radioModel,
				mobilityModel);

		ScalarRadioMANET manet = new ScalarRadioMANET(new ScalarRadioMANETSupplier().getNodeSupplier(),
				new ScalarRadioMANETSupplier().getLinkSupplier(),
				new ScalarRadioMANETSupplier().getLinkPropertySupplier(),
				new ScalarRadioMANETSupplier().getFlowSupplier(), radioModel, mobilityModel, evaluator);

		GridGraphProperties properties = new GridGraphProperties(1000, 1000, 100, 100);

		GridGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> generator = new GridGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet, RandomNumbers.getInstance(-1));

		generator.generate(properties);

		Function<ScalarLinkQuality, Double> metric = (ScalarLinkQuality w) -> {
			return w.getDistance();
		};

		DijkstraShortestPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> sp = new DijkstraShortestPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet);
//		int sourceId = randomInstance.getRandom(0, manet.getVertices().size());
//		int targetId = randomInstance.getRandom(0, manet.getVertices().size());
		
		int sourceId = 0;
		int targetId = 120;
		ScalarRadioFlow scalarRadioFlow = new ScalarRadioFlow(manet.getVertex(sourceId),manet.getVertex(targetId),new DataRate());
		manet.addFlow(scalarRadioFlow);
		GenesManetGraphTranslator translator = new GenesManetGraphTranslator(manet);
		Tuple<List<List<Integer>>, List<List<Integer>>> graphGenoRepresentation = translator.manetGraphPhenotoGeno();
		List<Tuple<Integer, Integer>> flowsPhenoToGeno = translator.flowsPhenoToGeno();
		List<List<Integer>> manetVerticesPhenoToGeno = translator.manetVerticesPhenoToGeno();
		Mutation<GraphGenome> mutation = new SingleNodeMutation<GraphGenome>();

		UniformCrossoverPathSeperator recombination = new UniformCrossoverPathSeperator();
		FlowDistributionFitness<PathComposition> fitness = new FlowDistributionFitness<PathComposition>();
		TerminationCondition<PathComposition> termination = new TerminationConditionGenerations<PathComposition>(100);
		int numTestRuns = 4000;

		
		

		SwingUtilities.invokeAndWait(new VisualGraphApp<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(manet,
				new WeightedEdgeIDPrinter<ScalarRadioLink, ScalarLinkQuality>()));
		int i = 0;
		List<List<Integer>> foundPaths = new ArrayList<List<Integer>>();
		// Initial individual
		GraphGenome genome = new GraphGenome(manetVerticesPhenoToGeno, graphGenoRepresentation.getFirst(),
				graphGenoRepresentation.getSecond(), flowsPhenoToGeno, 0d);
		while (numTestRuns != 0) {
				List<Integer> instructedPath = genome.createRandomInstance(randomInstance.getDoubleRandom()).getGenes().get(0);
				foundPaths.add(instructedPath);
		

			numTestRuns--;
		}
		Set<List<Integer>> uniquePaths = new HashSet<List<Integer>>(foundPaths);
		System.out.println(String.format("InstructedPaths Random: %d ",foundPaths.size()-uniquePaths.size()));
	}

	@Test
	public void generateInstructedPathsTest() throws InvocationTargetException, InterruptedException {

		RandomNumbers randomInstance = RandomNumbers.getInstance(0);
		ScalarRadioModel radioModel = new ScalarRadioModel(new Watt(0.002d), new Watt(1e-11), 1000d, 2412000000d, /**
																													 * maxCommunicationRange
																													 **/
				100d);
		PedestrianMobilityModel mobilityModel = new PedestrianMobilityModel(randomInstance,
				new SpeedRange(0, 100, Unit.TimeSteps.second, Unit.Distance.meter),
				new Speed(50, Unit.Distance.meter, Unit.TimeSteps.second));
		ScalarLinkQualityEvaluator evaluator = new ScalarLinkQualityEvaluator(new DoubleScope(0d, 1d), radioModel,
				mobilityModel);

		ScalarRadioMANET manet = new ScalarRadioMANET(new ScalarRadioMANETSupplier().getNodeSupplier(),
				new ScalarRadioMANETSupplier().getLinkSupplier(),
				new ScalarRadioMANETSupplier().getLinkPropertySupplier(),
				new ScalarRadioMANETSupplier().getFlowSupplier(), radioModel, mobilityModel, evaluator);

		GridGraphProperties properties = new GridGraphProperties(1000, 1000, 100, 100);

		GridGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> generator = new GridGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet, RandomNumbers.getInstance(-1));

		generator.generate(properties);

		Function<ScalarLinkQuality, Double> metric = (ScalarLinkQuality w) -> {
			return w.getDistance();
		};

		DijkstraShortestPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> sp = new DijkstraShortestPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet);
		
		int sourceId = 0;
		int targetId = 120;
		ScalarRadioFlow scalarRadioFlow = new ScalarRadioFlow(manet.getVertex(sourceId),manet.getVertex(targetId),new DataRate());
		manet.addFlow(scalarRadioFlow);
		GenesManetGraphTranslator translator = new GenesManetGraphTranslator(manet);
		Tuple<List<List<Integer>>, List<List<Integer>>> graphGenoRepresentation = translator.manetGraphPhenotoGeno();
		List<Tuple<Integer, Integer>> flowsPhenoToGeno = translator.flowsPhenoToGeno();
		List<List<Integer>> manetVerticesPhenoToGeno = translator.manetVerticesPhenoToGeno();
		Mutation<GraphGenome> mutation = new SingleNodeMutation<GraphGenome>();

		UniformCrossoverPathSeperator recombination = new UniformCrossoverPathSeperator();
		FlowDistributionFitness<PathComposition> fitness = new FlowDistributionFitness<PathComposition>();
		TerminationCondition<PathComposition> termination = new TerminationConditionGenerations<PathComposition>(100);
		int numTestRuns = 4000;

		
		

		SwingUtilities.invokeAndWait(new VisualGraphApp<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(manet,
				new WeightedEdgeIDPrinter<ScalarRadioLink, ScalarLinkQuality>()));
		int i = 0;
		List<List<Integer>> foundPaths = new ArrayList<List<Integer>>();
		// Initial individual
		GraphGenome genome = new GraphGenome(manetVerticesPhenoToGeno, graphGenoRepresentation.getFirst(),
				graphGenoRepresentation.getSecond(), flowsPhenoToGeno, 1d);
		while (numTestRuns != 0) {
				List<Integer> instructedPath = genome.createRandomInstance(randomInstance.getDoubleRandom()).getGenes().get(0);
				foundPaths.add(instructedPath);
		

//			}
			numTestRuns--;
		}
		Set<List<Integer>> uniquePaths = new HashSet<List<Integer>>(foundPaths);
		System.out.println(String.format("InstructedPaths KSP: %d ",foundPaths.size()-uniquePaths.size()));
	}

}