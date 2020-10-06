package program;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.Flow;
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
	static int density = 10;

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		MANETGraph g = new MANETGraph(PlaygroundMaxX, PlaygroundMaxY, NumberOfNodes, 0.1);
		g.createMANET(density, NodeReceUpperBoundNodeCoverage, UpperBoundVelocity);

		/*
		 * Creates random source and target
		 */
		int numFlow = 6;
		List<Flow> flows = new ArrayList<Flow>();
		List<Pair<Integer, Integer>> sourceTargets = new ArrayList<Pair<Integer, Integer>>();
		Set<Integer> stSet = new HashSet<Integer>();
		for (int i = 0; i < numFlow; i++)
		{
			Pair<Integer, Integer> st = g.generateSourceTarget(stSet);
			stSet.add(st.getFirst());
			stSet.add(st.getSecond());
			Flow flow = new Flow(st.getFirst(), st.getSecond(), 0.8);
			sourceTargets.add(st);
			flows.add(flow);
		}

		DOTGraphPrinter manetPrinter = new DOTGraphPrinter(flows);
		manetPrinter.printPlainGraph();
		manetPrinter.print(g, "ManetGraph_plain");
		List<List<Integer>> pc = optimization(g, sourceTargets);
		System.out.println(pc);
		manetPrinter.printPathsInGraph(pc);
		manetPrinter.print(g, "ManetGraph_path");
//		System.out.println("source: " + st.getFirst() + ", Target: " + st.getSecond());

	}

	public static List<List<Integer>> optimization(MANETGraph g, List<Pair<Integer, Integer>> sourceTargets)
	{
		Random random = new Random(1233);

		int populationSize = 200;

		Mutation<GraphGenome> mutation = new MultiplePathSingleMutation<GraphGenome>();

		Recombination<GraphGenome> recombination = new UniformCrossoverPathSeperator();
		FlowDistributionFitness fitness = new FlowDistributionFitness();
		TerminationCondition<PathComposition> termination = new TerminationConditionGenerations<PathComposition>(100);

		// Initial individual
		List<List<Integer>> elements = new ArrayList<List<Integer>>();
		elements.add(g.getAllNodeIds());
		GraphGenome genome = new GraphGenome(elements, g, sourceTargets);
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
