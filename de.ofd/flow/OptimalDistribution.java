package flow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.MANETGraph;
import de.manet.print.Printer;
import de.terministic.serein.api.EvolutionEnvironment;
import de.terministic.serein.api.Mutation;
import de.terministic.serein.api.Population;
import de.terministic.serein.api.Recombination;
import de.terministic.serein.api.TerminationCondition;
import de.terministic.serein.core.AlgorithmFactory;
import de.terministic.serein.core.BasicIndividual;
import de.terministic.serein.core.Populations;
import de.terministic.serein.core.StatsListener;
import de.terministic.serein.core.selection.individual.RandomSelection;
import de.terministic.serein.core.termination.TerminationConditionGenerations;
import flow.PathCompositionFitness;
import genetic.GraphGenome;
import genetic.PathTranslator;
import genetic.SinglePointCrossoverPathSeperator;
import genetic.SinglePointMutationPathSeperator;

public class OptimalDistribution
{
	static int PlaygroundMaxX = 500;
	static int PlaygroundMaxY = 500;
	static int NumberOfNodes = 30;
	static int NodeReceptionRange = 100;
	static int density = 6;

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		MANETGraph g = new MANETGraph(PlaygroundMaxX, PlaygroundMaxY, NumberOfNodes, NodeReceptionRange);
		g.createMANET(density);

		ArrayList<Pair<Integer, Integer>> SourceTargetSet = new ArrayList<Pair<Integer, Integer>>();

		SourceTargetSet.add(g.generateSourceTarget());

		List<List<Integer>> pc = optimization(g, SourceTargetSet);
		Printer manetPrinter = new Printer("ManetGraph");
		manetPrinter.configureMANETVizualisaiton(SourceTargetSet, pc);
		manetPrinter.print(g);

	}

	public static List<List<Integer>> optimization(MANETGraph g, List<Pair<Integer, Integer>> sourceTargetSet)
	{
		Random random = new Random(1233);
		int populationSize = 4;
		Mutation<GraphGenome> mutation = new SinglePointMutationPathSeperator<GraphGenome>();

		Recombination<GraphGenome> recombination = new SinglePointCrossoverPathSeperator();
		PathCompositionFitness fitness = new PathCompositionFitness();
		TerminationCondition<PathComposition> termination = new TerminationConditionGenerations(100000);

		// Initial individual
		GraphGenome genome = new GraphGenome(g.getNodeIds(), g, sourceTargetSet);
		BasicIndividual<PathComposition, GraphGenome> initialIndividual = new BasicIndividual<PathComposition, GraphGenome>(
				genome, new PathTranslator());
		initialIndividual.setRecombination(recombination);
		initialIndividual.setMutation(mutation);
		initialIndividual.setMateSelection(new RandomSelection());
		Population<PathComposition> initialPopulation = Populations.generatePopulation(initialIndividual,
				populationSize, random);

		// assembling the metaheuristic
		AlgorithmFactory<PathComposition> factory = new AlgorithmFactory<>();
		factory.termination = termination;
		EvolutionEnvironment<PathComposition> algo = factory.createReferenceEvolutionaryAlgorithm(fitness,
				initialPopulation, random);

		StatsListener<PathComposition> listener = new StatsListener<PathComposition>(fitness, 10);
		algo.addListener(listener);

		// run optimization
		algo.evolve();

		// result
		ArrayList<Integer> pc = (ArrayList<Integer>) algo.getFittest().getPhenotype();
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		int startIndex = 0;
		int indexOfPathSeperator = pc.indexOf(-2);

		while (indexOfPathSeperator != -1)
		{
			indexOfPathSeperator += startIndex;
			result.add(pc.subList(startIndex, indexOfPathSeperator + 1));
			startIndex = indexOfPathSeperator + 1;
			indexOfPathSeperator = pc.subList(startIndex, pc.size()).indexOf(-2);
		}

		return result;
	}

	public static int getRandomWithExclusion(Random rnd, int start, int end, List<Integer> exclude)
	{
		int random = start + rnd.nextInt(end - start + 1 - exclude.size());
		for (int ex : exclude)
		{
			if (random < ex)
			{
				break;
			}
			random++;
		}
		return random;
	}
}
