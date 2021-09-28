package de.genetic.optimization;

import java.util.List;
import java.util.Random;

import de.jgraphlib.util.RandomNumbers;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.scalar.ScalarRadioMANET;
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

public class GeneticOptimization extends Optimization<ScalarRadioMANET> {

	private GenesManetGraphTranslator translator;

	private final int populationSize;
	private final int generations;
	private final double mutationProbability;

	private RandomNumbers random;

	public GeneticOptimization(ScalarRadioMANET manet,int populationSize, int generations, double mutationProbability, RandomNumbers random) {
		super(manet);
		this.generations = generations;
		this.populationSize = populationSize;
		this.random = random;
		this.mutationProbability = mutationProbability;
		 this.translator = new GenesManetGraphTranslator(manet);
	}

	@Override
	public ScalarRadioMANET execute() {

		Tuple<List<List<Integer>>, List<List<Integer>>> graphGenoRepresentation = translator.manetGraphPhenotoGeno();
		List<Tuple<Integer, Integer>> flowsPhenoToGeno = translator.flowsPhenoToGeno();
		List<List<Integer>> manetVerticesPhenoToGeno = translator.manetVerticesPhenoToGeno();
		Mutation<GraphGenome> mutation = new CompletePathsMutation<GraphGenome>();

		TwoPointMultiplePathCrossover recombination = new TwoPointMultiplePathCrossover();
		FlowDistributionFitness<PathComposition> fitness = new FlowDistributionFitness<PathComposition>();
		TerminationCondition<PathComposition> termination = new TerminationConditionGenerations<PathComposition>(
				generations);

		// Initial individual
		GraphGenome genome = new GraphGenome(manetVerticesPhenoToGeno, graphGenoRepresentation.getFirst(),graphGenoRepresentation.getSecond(), flowsPhenoToGeno,1.0d);
		BasicIndividual<PathComposition, GraphGenome> initialIndividual = new BasicIndividual<PathComposition, GraphGenome>(
				genome, translator);
		initialIndividual.<Double>setProperty("ProbabilityOfMutation", mutationProbability, true);
		initialIndividual.setRecombination(recombination);
		initialIndividual.setMutation(mutation);
		initialIndividual.setMateSelection(new TournamentSelection<>(fitness, 3));
		Population<PathComposition> initialPopulation = Populations.generatePopulation(initialIndividual,
				populationSize, random.getDoubleRandom());
		// assembling the metaheuristic
		AlgorithmFactory<PathComposition> factory = new AlgorithmFactory<>();
		factory.termination = termination;
		EvolutionEnvironment<PathComposition> algo = factory.createReferenceEvolutionaryAlgorithm(fitness,
				initialPopulation, random.getDoubleRandom());

		StatsListener<PathComposition> listener = new StatsListener<PathComposition>(fitness, 1);
		algo.addListener(listener);

		// run optimization
		algo.evolve();

		// result
		manet.undeployFlows();
		manet.clearFlows();
		
		manet.addFlows(algo.getFittest().getPhenotype().getFlows());
		manet.deployFlows(algo.getFittest().getPhenotype().getFlows());
		return manet;

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
