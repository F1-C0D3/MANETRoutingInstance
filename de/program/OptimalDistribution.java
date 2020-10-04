package program;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.MANETGraph;
import de.manet.print.DOTGraphPrinter;
import de.terministic.serein.api.EvolutionEnvironment;
import de.terministic.serein.api.Mutation;
import de.terministic.serein.api.Population;
import de.terministic.serein.api.Recombination;
import de.terministic.serein.api.TerminationCondition;
import de.terministic.serein.core.AlgorithmFactory;
import de.terministic.serein.core.BasicIndividual;
import de.terministic.serein.core.Populations;
import de.terministic.serein.core.StatsListener;
import de.terministic.serein.core.selection.individual.TournamentSelection;
import de.terministic.serein.core.termination.TerminationConditionGenerations;
import genetic.GraphGenome;
import genetic.MultiplePathSingleMutation;
import genetic.PathTranslator;
import genetic.UniformCrossoverPathSeperator;

public class OptimalDistribution
{
	static int PlaygroundMaxX = 500;
	static int PlaygroundMaxY = 500;
	static int NumberOfNodes = 60;
	static int NodeReceUpperBoundNodeCoverage = 100;
	static int UpperBoundVelocity = 20;
	static int density = 8;

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		MANETGraph g = new MANETGraph(PlaygroundMaxX, PlaygroundMaxY, NumberOfNodes);
		g.createMANET(density, NodeReceUpperBoundNodeCoverage, UpperBoundVelocity);

		/*
		 * Creates random source and target
		 */
		List<Pair<Integer, Integer>> sourceTarget = new ArrayList<Pair<Integer, Integer>>();
		Pair<Integer, Integer> st1 = g.generateSourceTarget();
		Pair<Integer, Integer> st2 = g.generateSourceTarget();
		Pair<Integer, Integer> st3 = g.generateSourceTarget();
		Pair<Integer, Integer> st4 = g.generateSourceTarget();
		sourceTarget.add(st1);
		sourceTarget.add(st2);
		sourceTarget.add(st3);
		sourceTarget.add(st4);
		DOTGraphPrinter manetPrinter = new DOTGraphPrinter(sourceTarget);
		manetPrinter.printPlainGraph();
		manetPrinter.print(g, "ManetGraph_plain");
		List<List<Integer>> pc = optimization(g, sourceTarget);
		System.out.println(pc);
		manetPrinter.printPathsInGraph(pc);
		manetPrinter.print(g, "ManetGraph_path");
//		System.out.println("source: " + st.getFirst() + ", Target: " + st.getSecond());

	}

	public static List<List<Integer>> optimization(MANETGraph g, List<Pair<Integer, Integer>> sourceTarget)
	{
		Random random = new Random(1233);

		int populationSize = 10000;

		Mutation<GraphGenome> mutation = new MultiplePathSingleMutation<GraphGenome>(g, sourceTarget);

		Recombination<GraphGenome> recombination = new UniformCrossoverPathSeperator();
		FlowDistributionFitness fitness = new FlowDistributionFitness();
		TerminationCondition<PathComposition> termination = new TerminationConditionGenerations<PathComposition>(10);

		// Initial individual
		GraphGenome genome = new GraphGenome(g.getNodeIds(), g, sourceTarget);
		BasicIndividual<PathComposition, GraphGenome> initialIndividual = new BasicIndividual<PathComposition, GraphGenome>(
				genome, new PathTranslator());
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

		StatsListener<PathComposition> listener = new StatsListener<PathComposition>(fitness, 10);
		algo.addListener(listener);

		// run optimization
		algo.evolve();

		// result
		PathComposition pc = algo.getFittest().getPhenotype();

		List<List<Integer>> paths = pc.extractPaths();

		return paths;
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
