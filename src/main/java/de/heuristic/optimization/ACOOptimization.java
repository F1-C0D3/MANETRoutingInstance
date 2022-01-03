package de.heuristic.optimization;

import java.util.List;
import java.util.function.Function;

import de.aco.alg.ACOProperties;
import de.aco.alg.multipath.IndependentMultiPath;
import de.aco.ant.Ant;
import de.aco.ant.AntConsumer;
import de.aco.ant.AntGroup;
import de.aco.ant.evaluation.AntEvaluator;
import de.aco.ant.evaluation.AntGroupEvaluator;
import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.parallelism.Optimization;

public class ACOOptimization extends Optimization<ScalarRadioMANET> {

	private IndependentMultiPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow, ScalarRadioMANET> aco;

	private RandomNumbers random;

	public ACOOptimization(ScalarRadioMANET manet, RandomNumbers random) {
		super(manet);
		this.random = random;

		ACOProperties properties = new ACOProperties(de.aco.pheromone.ScoreOrder.DESCENDING);
		properties.antQuantity = 10000;
		properties.antReorientationLimit = 50;
		properties.iterationQuantity = 10;

		aco = new IndependentMultiPath<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow, ScalarRadioMANET>(
				properties);

		aco.setMetric((ScalarRadioLink link) -> {
			return (double) link.getWeight().getScore();
		});

		// AntConsumer is used to show ACO how to consume data rate while building paths.
		aco.setAntConsumer(
				new AntConsumer<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow, ScalarRadioMANET>() {
					@Override
					public void consume(ScalarRadioMANET graph,
							Ant<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> ant) {
						for (ScalarRadioLink link : ant.getPath().getEdges())
							graph.increaseUtilizationBy(link, ant.getPath().getDataRate());
					}

					@Override
					public void reset(ScalarRadioMANET graph) {
						graph.undeployFlows();
					}
				});

		// AntEvaluator is used to evaluate/compare paths only based on the link scores.
		// The path to be evaluated is represented through the ant object. 
		// Since "IndepedentMultiPath" constructs paths for each source-target pair
		// independently, this function is used to find the best path (path with the
		// smallest sum over all link scores) for a source-target pair. 

		aco.setAntEvaluator(
				new AntEvaluator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow, ScalarRadioMANET>() {
					@Override
					public double evaluate(ScalarRadioMANET graph,
							Ant<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> ant,
							Function<ScalarRadioLink, Double> metric) {
						double score = 0d;
						for (ScalarRadioLink link : ant.getPath().getEdges())
							score += link.getWeight().getScore();
						return score;
					}
				});

		// AntGroupEveluator is used to evaluate/compare paths with reference to the network's utilization.
		// The solution (one ant for each source-target pair) is represented through the antGroup object.
		// The solution's score is evaluated by the sum of all utilized link scores plus the over-utilization on each link.
		// Each over-utilization on a link adds a penalty of "link.getOverUtilization().get() * 10" to the score.
		// Hence, if a solutions is not over-utilized, the score of the solution only consists of the sum over all link scores.
		
		aco.setAntGroupEvaluator(
				new AntGroupEvaluator<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow, ScalarRadioMANET>() {
					@Override
					public double evaluate(ScalarRadioMANET graph,
							AntGroup<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> antGroup,
							Function<ScalarRadioLink, Double> metric) {

						double score = 0d;

						for (Ant<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> ant : antGroup)
							for (ScalarRadioLink link : ant.getPath().getEdges())
								score += link.getWeight().getScore();

						List<ScalarRadioLink> overutilizedLinks = graph.getOverUtilizedLinks();
						if (overutilizedLinks.size() > 0)
							for (ScalarRadioLink link : overutilizedLinks)
								score += link.getOverUtilization().get() * 10;

						return score;
					}
				});

		aco.initialize(manet);
	}

	@Override
	public ScalarRadioMANET execute() {

		aco.run();

		if (aco.foundSolution()) {
			for (int i = 0; i < aco.getSolution().getAnts().getPaths().size(); i++) {
				manet.getFlow(i).update(aco.getSolution().getAnts().getPaths().get(i));
				manet.deployFlow(manet.getFlow(i));
			}
			return manet;
		}

		return null;
	}
}
