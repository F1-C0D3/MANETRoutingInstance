package program;

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
import de.terministic.serein.core.genome.mutation.SinglePointMutation;
import de.terministic.serein.core.genome.recombination.SinglePointCrossover;
import de.terministic.serein.core.selection.individual.RandomSelection;
import de.terministic.serein.core.termination.TerminationConditionGenerations;
import genetic.MANETGenome;
import genetic.PathTranslator;

public class OptimalDistribution
{
	static int PlaygroundMaxX = 500;
	static int PlaygroundMaxY = 500;
	static int NumberOfNodes = 30;
	static int NodeReceptionRange = 100;
	static int density = 8;

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		MANETGraph g = new MANETGraph(PlaygroundMaxX, PlaygroundMaxY, NumberOfNodes, NodeReceptionRange);
		g.createMANET(density);

		/*
		 * Creates random source and target
		 */

		Pair<Integer, Integer> st = g.generateSourceTarget();
		System.out.println("source: " + st.getFirst() + ", Target: " + st.getSecond());
		List<Integer> pc = optimization(g, st);
		Printer manetPrinter = new Printer("ManetGraph");
		manetPrinter.configureMANETVizualisaiton(st, pc);
		manetPrinter.print(g);

		System.out.println("source: " + st.getFirst() + ", Target: " + st.getSecond());

	}

	public static List<Integer> optimization(MANETGraph g, Pair<Integer, Integer> sourceTarget)
	{
		Random random = new Random(1233);
		int populationSize = 10;
		Mutation<MANETGenome> mutation = new SinglePointMutation<MANETGenome>();

		Recombination<MANETGenome> recombination = new SinglePointCrossover<MANETGenome>();
		PathCompositionFitness fitness = new PathCompositionFitness();
		TerminationCondition<PathComposition> termination = new TerminationConditionGenerations(2000);

		// Initial individual
		MANETGenome genome = new MANETGenome(g.getNodeIds(), g, sourceTarget);
		BasicIndividual<PathComposition, MANETGenome> initialIndividual = new BasicIndividual<PathComposition, MANETGenome>(
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

//		while (indexOfPathSeperator != -1)
//		{
//			indexOfPathSeperator += startIndex;
//			result.add(pc.subList(startIndex, indexOfPathSeperator + 1));
//			startIndex = indexOfPathSeperator + 1;
//			indexOfPathSeperator = pc.subList(startIndex, pc.size()).indexOf(-2);
//		}

		return pc;
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
