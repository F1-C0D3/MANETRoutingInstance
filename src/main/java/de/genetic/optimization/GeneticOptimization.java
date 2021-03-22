package de.genetic.optimization;

import java.util.List;
import java.util.Random;

import de.deterministic.optimization.MultipleDijkstraLinkQuality;
import de.genetic.network.GeneticMANET;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.parallelism.Optimization;
import de.terministic.serein.api.EvolutionEnvironment;
import de.terministic.serein.api.Mutation;
import de.terministic.serein.api.Population;
import de.terministic.serein.api.TerminationCondition;
import de.terministic.serein.core.AlgorithmFactory;
import de.terministic.serein.core.BasicIndividual;
import de.terministic.serein.core.Populations;
import de.terministic.serein.core.StatsListener;
import de.terministic.serein.core.selection.individual.TournamentSelection;
import de.terministic.serein.core.termination.TerminationConditionGenerations;

public class GeneticOptimization extends
		Optimization<PathComposition, MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>> {

	private GenesManetGraphTranslator translator;

	final int populationSize;
	final int generations;

	public GeneticOptimization(
			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet,
			int populationSize, int generations) {
		super(manet);
		this.generations = generations;
		this.populationSize = populationSize;
		translator = new GenesManetGraphTranslator(manet);

	}

	public PathComposition execute() {

		Random random = new Random(1233);
		List<List<Tuple<Integer, Integer>>> manetGraphPhenotoGeno = translator.manetGraphPhenotoGeno();
		List<Tuple<Integer, Integer>> flowsPhenoToGeno = translator.flowsPhenoToGeno();
		List<List<Integer>> manetVerticesPhenoToGeno = translator.manetVerticesPhenoToGeno();
		Mutation<GraphGenome> mutation = new MultiplePathSingleMutation<GraphGenome>();

		UniformCrossoverPathSeperator recombination = new UniformCrossoverPathSeperator();
		FlowDistributionFitness<PathComposition> fitness = new FlowDistributionFitness<PathComposition>();
		TerminationCondition<PathComposition> termination = new TerminationConditionGenerations<PathComposition>(
				generations);

		// Initial individual
		GraphGenome genome = new GraphGenome(manetVerticesPhenoToGeno, manetGraphPhenotoGeno, flowsPhenoToGeno);
		BasicIndividual<PathComposition, GraphGenome> initialIndividual = new BasicIndividual<PathComposition, GraphGenome>(
				genome, translator);
		initialIndividual.<Double>setProperty("ProbabilityOfMutation", 0.2, true);
		initialIndividual.setRecombination(recombination);
		initialIndividual.setMutation(mutation);
		initialIndividual.setMateSelection(new TournamentSelection<>(fitness, 3));
		Population<PathComposition> initialPopulation = Populations.generatePopulation(initialIndividual,
				populationSize, random);
		// assembling the metaheuristic
		AlgorithmFactory<PathComposition> factory = new AlgorithmFactory<>();
		factory.termination = termination;
		EvolutionEnvironment<PathComposition> algo = factory.createReferenceEvolutionaryAlgorithm(fitness,
				initialPopulation, random);

		StatsListener<PathComposition> listener = new StatsListener<PathComposition>(fitness, 1);
		algo.addListener(listener);

		// run optimization
		algo.evolve();

		// result
		return algo.getFittest().getPhenotype();

	}

	public static int getRandomWithExclusion(Random rnd, int start, int end, List<Integer> exclude) {
		int random = start + rnd.nextInt(end - start + 1 - exclude.size());
		for (int ex : exclude) {
			if (random < ex) {
				break;
			}
			random++;
		}
		return random;
	}
}