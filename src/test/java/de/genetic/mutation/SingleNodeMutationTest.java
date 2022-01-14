package de.genetic.mutation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import org.junit.Test;

import de.heuristic.optimization.geneticprogramming.GenesManetGraphTranslator;
import de.heuristic.optimization.geneticprogramming.GraphGenome;
import de.heuristic.optimization.geneticprogramming.SingleNodeMutation;
import de.jgraphlib.generator.GraphProperties.DoubleRange;
import de.jgraphlib.generator.GraphProperties.EdgeStyle;
import de.jgraphlib.generator.GraphProperties.IntRange;
import de.jgraphlib.generator.GridGraphGenerator;
import de.jgraphlib.generator.GridGraphProperties;
import de.jgraphlib.generator.NetworkGraphGenerator;
import de.jgraphlib.generator.NetworkGraphProperties;
import de.jgraphlib.graph.algorithms.DijkstraShortestPath;
import de.jgraphlib.graph.elements.Path;
import de.jgraphlib.gui.VisualGraphApp;
import de.jgraphlib.util.RandomNumbers;
import de.jgraphlib.util.Tuple;
import de.manetmodel.evaluator.DoubleScope;
import de.manetmodel.evaluator.ScalarLinkQualityEvaluator;
import de.manetmodel.gui.printer.LinkUtilizationPrinter;
import de.manetmodel.mobilitymodel.PedestrianMobilityModel;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioMANETSupplier;
import de.manetmodel.network.scalar.ScalarRadioModel;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.units.Speed;
import de.manetmodel.units.Speed.SpeedRange;
import de.manetmodel.units.Unit;
import de.manetmodel.units.Watt;

public class SingleNodeMutationTest {

	@Test
	public void directConnectedSourceDestPairTest() throws InvocationTargetException, InterruptedException {

		RandomNumbers randomInstance = RandomNumbers.getInstance(0);
		ScalarRadioModel radioModel = new ScalarRadioModel(new Watt(0.001d), new Watt(1e-11), 2000000d, 2412000000d,
				35d,100);
		PedestrianMobilityModel mobilityModel = new PedestrianMobilityModel(randomInstance,
				new SpeedRange(0, 100, Unit.TimeSteps.second, Unit.Distance.meter),
				new Speed(50, Unit.Distance.meter, Unit.TimeSteps.second));
		ScalarLinkQualityEvaluator evaluator = new ScalarLinkQualityEvaluator(new DoubleScope(0d, 1d), radioModel,
				mobilityModel);

		Supplier<ScalarLinkQuality> linkPropertySupplier = new ScalarRadioMANETSupplier().getLinkPropertySupplier();
		ScalarRadioMANET manet = new ScalarRadioMANET(new ScalarRadioMANETSupplier().getNodeSupplier(),
				new ScalarRadioMANETSupplier().getLinkSupplier(),
				linkPropertySupplier,
				new ScalarRadioMANETSupplier().getFlowSupplier(), radioModel, mobilityModel, evaluator);

		GridGraphProperties properties = new GridGraphProperties(400, 500, 100, 100,EdgeStyle.BIDIRECTIONAL);

		GridGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> generator = new GridGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet,linkPropertySupplier, RandomNumbers.getInstance(-1));

		generator.generate(properties);
		GenesManetGraphTranslator translator = new GenesManetGraphTranslator(manet);
		Tuple<List<List<Integer>>, List<List<Integer>>> graphGenoRepresentation = translator.manetGraphPhenotoGeno();
		List<Tuple<Integer, Integer>> flowsPhenoToGeno = translator.flowsPhenoToGeno();
		List<List<Integer>> manetVerticesPhenoToGeno = translator.manetVerticesPhenoToGeno();

		// Initial individual
		GraphGenome genome = new GraphGenome(manetVerticesPhenoToGeno, graphGenoRepresentation.getFirst(),
				graphGenoRepresentation.getSecond(), flowsPhenoToGeno,0d);

		List<List<Integer>> singlePathList = new ArrayList<List<Integer>>();
		List<Integer> singlePath = new ArrayList<Integer>();
		singlePath.add(14);
		singlePath.add(15);
		singlePathList.add(singlePath);
		GraphGenome artificialGenome = genome.createInstance(singlePathList);
		GraphGenome newPath = new SingleNodeMutation<GraphGenome>().mutate(artificialGenome,
				randomInstance.getDoubleRandom());

		assertEquals(singlePath, newPath.getGenes().get(0));

	}

	@Test
	public void singleNodePathdeviationTest() throws InvocationTargetException, InterruptedException {

		RandomNumbers randomInstance = RandomNumbers.getInstance(0);
		ScalarRadioModel radioModel = new ScalarRadioModel(new Watt(0.001d), new Watt(1e-11), 2000000d, 2412000000d,
				35d,100);
		PedestrianMobilityModel mobilityModel = new PedestrianMobilityModel(randomInstance,
				new SpeedRange(0, 100, Unit.TimeSteps.second, Unit.Distance.meter),
				new Speed(50, Unit.Distance.meter, Unit.TimeSteps.second));
		ScalarLinkQualityEvaluator evaluator = new ScalarLinkQualityEvaluator(new DoubleScope(0d, 1d), radioModel,
				mobilityModel);

		ScalarRadioMANET manet = new ScalarRadioMANET(new ScalarRadioMANETSupplier().getNodeSupplier(),
				new ScalarRadioMANETSupplier().getLinkSupplier(),
				new ScalarRadioMANETSupplier().getLinkPropertySupplier(),
				new ScalarRadioMANETSupplier().getFlowSupplier(), radioModel, mobilityModel, evaluator);

		NetworkGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> generator = new NetworkGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet, new ScalarRadioMANETSupplier().getLinkPropertySupplier(), new RandomNumbers());

		NetworkGraphProperties graphProperties = new NetworkGraphProperties(/* playground width */ 1024,
				/* playground height */ 768, /* number of vertices */ new IntRange(100, 100),
				/* distance between vertices */ new DoubleRange(50d, 100d),
				/* edge distance */ new DoubleRange(100d, 100d));

		generator.generate(graphProperties);
		manet.initialize();

		Function<ScalarLinkQuality, Double> metric = (ScalarLinkQuality w) -> {
			return w.getDistance();
		};

		DijkstraShortestPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> sp = new DijkstraShortestPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet);

		int numTestRuns = 100;
		while (numTestRuns != 0) {
			int sourceId = randomInstance.getRandom(0, manet.getVertices().size());
			int targetId = randomInstance.getRandom(0, manet.getVertices().size());

			if (sourceId != targetId && !manet.getVerticesInRadius(manet.getVertex(sourceId), 100d)
					.contains(manet.getVertex(targetId))) {

				Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> path = sp.compute(manet.getVertex(sourceId),
						manet.getVertex(targetId), metric);
				// Initial individual

				// Path to genotype
				List<List<Integer>> singlePathList = new ArrayList<List<Integer>>();
				List<Integer> singelPath = new ArrayList<Integer>();

				for (ScalarRadioNode node : path.getVertices()) {
					singelPath.add(node.getID());
				}
				singlePathList.add(singelPath);


				singlePathList.clear();

			}
			numTestRuns--;
		}

	}

	@Test
	public void multiplePathdeviationTest() throws InvocationTargetException, InterruptedException {

		RandomNumbers randomInstance = RandomNumbers.getInstance(0);
		ScalarRadioModel radioModel = new ScalarRadioModel(new Watt(0.001d), new Watt(1e-11), 2000000d, 2412000000d,
				35d,100);
		PedestrianMobilityModel mobilityModel = new PedestrianMobilityModel(randomInstance,
				new SpeedRange(0, 100, Unit.TimeSteps.second, Unit.Distance.meter),
				new Speed(50, Unit.Distance.meter, Unit.TimeSteps.second));
		ScalarLinkQualityEvaluator evaluator = new ScalarLinkQualityEvaluator(new DoubleScope(0d, 1d), radioModel,
				mobilityModel);

		ScalarRadioMANET manet = new ScalarRadioMANET(new ScalarRadioMANETSupplier().getNodeSupplier(),
				new ScalarRadioMANETSupplier().getLinkSupplier(),
				new ScalarRadioMANETSupplier().getLinkPropertySupplier(),
				new ScalarRadioMANETSupplier().getFlowSupplier(), radioModel, mobilityModel, evaluator);

		NetworkGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> generator = new NetworkGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet, new ScalarRadioMANETSupplier().getLinkPropertySupplier(), new RandomNumbers());

		NetworkGraphProperties graphProperties = new NetworkGraphProperties(/* playground width */ 1024,
				/* playground height */ 768, /* number of vertices */ new IntRange(100, 100),
				/* distance between vertices */ new DoubleRange(50d, 100d),
				/* edge distance */ new DoubleRange(100d, 100d));

		generator.generate(graphProperties);
		manet.initialize();

		Function<ScalarLinkQuality, Double> metric = (ScalarLinkQuality w) -> {
			return w.getDistance();
		};

		DijkstraShortestPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> sp = new DijkstraShortestPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet);

		SwingUtilities.invokeAndWait(new VisualGraphApp<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(manet,
				new LinkUtilizationPrinter<ScalarRadioLink, ScalarLinkQuality>()));
		GenesManetGraphTranslator translator = new GenesManetGraphTranslator(manet);
		Tuple<List<List<Integer>>, List<List<Integer>>> graphGenoRepresentation = translator.manetGraphPhenotoGeno();
		List<Tuple<Integer, Integer>> flowsPhenoToGeno = translator.flowsPhenoToGeno();
		List<List<Integer>> manetVerticesPhenoToGeno = translator.manetVerticesPhenoToGeno();

		int numTestRuns = 100;
		while (numTestRuns != 0) {
			int numSourceTargetPairs = 4;
			List<Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>> paths = new ArrayList<Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>>();
			List<Tuple<Integer, Integer>> sourceTargetPairs = new ArrayList<Tuple<Integer, Integer>>();
			for (int i = 0; i < numSourceTargetPairs;) {
				int sourceId = randomInstance.getRandom(0, manet.getVertices().size());
				int targetId = randomInstance.getRandom(0, manet.getVertices().size());

				if (sourceId != targetId && !manet.getVerticesInRadius(manet.getVertex(sourceId), 100d)
						.contains(manet.getVertex(targetId))) {
					sourceTargetPairs.add(new Tuple<Integer, Integer>(sourceId, targetId));
					paths.add(sp.compute(manet.getVertex(sourceId), manet.getVertex(targetId), metric));

					i++;
				}
			}

			// Initial individual
			GraphGenome genome = new GraphGenome(manetVerticesPhenoToGeno, graphGenoRepresentation.getFirst(),
					graphGenoRepresentation.getSecond(), flowsPhenoToGeno,0d);

			// Path to genotype
			List<List<Integer>> singlePathList = new ArrayList<List<Integer>>();

			for (Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> path : paths) {
				List<Integer> singelPath = new ArrayList<Integer>();
				for (ScalarRadioNode node : path.getVertices()) {

					singelPath.add(node.getID());
				}

				singlePathList.add(singelPath);
			}

			GraphGenome artificialGenome = genome.createInstance(singlePathList);
			GraphGenome newPaths = new SingleNodeMutation<GraphGenome>().mutate(artificialGenome,
					randomInstance.getDoubleRandom());
			for (int j = 0; j < newPaths.getGenes().size(); j++) {
				int numOfDifferentNodes = 0;

				if (newPaths.getGenes().get(j) != null) {

					for (int k = 0; k < newPaths.getGenes().get(j).size(); k++) {
						if (!newPaths.getGenes().get(j).get(k).equals(singlePathList.get(j).get(k))) {
							numOfDifferentNodes++;
						}
					}
					System.out.println(String.format("List entries that differ: %d", numOfDifferentNodes));
					assertTrue(numOfDifferentNodes <= 1);
				}
			}

			singlePathList.clear();
			numTestRuns--;
		}

	}

	@Test
	public void isPathConnectedTest() throws InvocationTargetException, InterruptedException {

		RandomNumbers randomInstance = RandomNumbers.getInstance(0);
		ScalarRadioModel radioModel = new ScalarRadioModel(new Watt(0.001d), new Watt(1e-11), 2000000d, 2412000000d,
				35d,100);		PedestrianMobilityModel mobilityModel = new PedestrianMobilityModel(randomInstance,
				new SpeedRange(0, 100, Unit.TimeSteps.second, Unit.Distance.meter),
				new Speed(50, Unit.Distance.meter, Unit.TimeSteps.second));
		ScalarLinkQualityEvaluator evaluator = new ScalarLinkQualityEvaluator(new DoubleScope(0d, 1d), radioModel,
				mobilityModel);

		ScalarRadioMANET manet = new ScalarRadioMANET(new ScalarRadioMANETSupplier().getNodeSupplier(),
				new ScalarRadioMANETSupplier().getLinkSupplier(),
				new ScalarRadioMANETSupplier().getLinkPropertySupplier(),
				new ScalarRadioMANETSupplier().getFlowSupplier(), radioModel, mobilityModel, evaluator);

		NetworkGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> generator = new NetworkGraphGenerator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet, new ScalarRadioMANETSupplier().getLinkPropertySupplier(), new RandomNumbers());

		NetworkGraphProperties graphProperties = new NetworkGraphProperties(/* playground width */ 1024,
				/* playground height */ 768, /* number of vertices */ new IntRange(100, 100),
				/* distance between vertices */ new DoubleRange(50d, 100d),
				/* edge distance */ new DoubleRange(100d, 100d));

		generator.generate(graphProperties);
		manet.initialize();

		Function<ScalarLinkQuality, Double> metric = (ScalarLinkQuality w) -> {
			return w.getDistance();
		};

		DijkstraShortestPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> sp = new DijkstraShortestPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(
				manet);

		SwingUtilities.invokeAndWait(new VisualGraphApp<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>(manet,
				new LinkUtilizationPrinter<ScalarRadioLink, ScalarLinkQuality>()));
		GenesManetGraphTranslator translator = new GenesManetGraphTranslator(manet);
		Tuple<List<List<Integer>>, List<List<Integer>>> graphGenoRepresentation = translator.manetGraphPhenotoGeno();
		List<Tuple<Integer, Integer>> flowsPhenoToGeno = translator.flowsPhenoToGeno();
		List<List<Integer>> manetVerticesPhenoToGeno = translator.manetVerticesPhenoToGeno();

		int numTestRuns = 1000;
		while (numTestRuns != 0) {
			int numSourceTargetPairs = 4;
			List<Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>> paths = new ArrayList<Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>>();
			List<Tuple<Integer, Integer>> sourceTargetPairs = new ArrayList<Tuple<Integer, Integer>>();
			for (int i = 0; i < numSourceTargetPairs;) {
				int sourceId = randomInstance.getRandom(0, manet.getVertices().size());
				int targetId = randomInstance.getRandom(0, manet.getVertices().size());

				if (sourceId != targetId && !manet.getVerticesInRadius(manet.getVertex(sourceId), 100d)
						.contains(manet.getVertex(targetId))) {
					sourceTargetPairs.add(new Tuple<Integer, Integer>(sourceId, targetId));
					paths.add(sp.compute(manet.getVertex(sourceId), manet.getVertex(targetId), metric));

					i++;
				}
			}

			// Initial individual
			GraphGenome genome = new GraphGenome(manetVerticesPhenoToGeno, graphGenoRepresentation.getFirst(),
					graphGenoRepresentation.getSecond(), flowsPhenoToGeno,0d);

			// Path to genotype
			List<List<Integer>> singlePathList = new ArrayList<List<Integer>>();

			for (Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> path : paths) {
				List<Integer> singelPath = new ArrayList<Integer>();
				for (ScalarRadioNode node : path.getVertices()) {

					singelPath.add(node.getID());
				}

				singlePathList.add(singelPath);
			}

			GraphGenome artificialGenome = genome.createInstance(singlePathList);
			GraphGenome newPaths = new SingleNodeMutation<GraphGenome>().mutate(artificialGenome,
					randomInstance.getDoubleRandom());
			for (int j = 0; j < newPaths.getGenes().size(); j++) {

				for (int i = 0; i < newPaths.getGenes().get(j).size();i++) {
					if (i + 1 < newPaths.getGenes().get(j).size()) {
						assertTrue(
								genome.sourceTargetAdjacvencyGenes.get(newPaths.getGenes().get(j).get(i)).contains(newPaths.getGenes().get(j).get(i + 1)));
					}
				}
			}

			singlePathList.clear();
			numTestRuns--;
		}

	}

}