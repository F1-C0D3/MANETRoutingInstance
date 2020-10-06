package genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.MANETGraph;
import de.terministic.serein.core.genome.ValueGenome;

public class GraphGenome extends ValueGenome<List<Integer>>
{

	private double[] geneArray;
	MANETGraph G;
	List<Pair<Integer, Integer>> SourceTargetPairs;
	private int PathSeperator = -2;

	public GraphGenome(List<List<Integer>> genes, MANETGraph g, List<Pair<Integer, Integer>> sourceTargetPairs)
	{
		this(genes, g);
		this.G = g;
		this.SourceTargetPairs = sourceTargetPairs;
	}

	public GraphGenome(List<List<Integer>> genes, MANETGraph g)
	{
		super(genes);
		this.G = g;
	}

	@Override
	public String getGenomeId()
	{

		return this.getClass().getName();
	}

	@Override
	public GraphGenome createInstance(List<List<Integer>> genes)
	{
		return new GraphGenome(genes, G, SourceTargetPairs);
	}

	@Override
	public GraphGenome createRandomInstance(Random random)
	{
		List<List<Integer>> result = new ArrayList<List<Integer>>();

		for (Pair<Integer, Integer> sd : SourceTargetPairs)
		{
			List<Integer> path = G.generateRandomPath(sd.getFirst(), sd.getSecond(), random);
			result.add(path);
		}
		return new GraphGenome(result, G, SourceTargetPairs);
	}

	public int getPathSeperator()
	{
		return PathSeperator;
	}

	public int getPathSize()
	{
		return Collections.frequency(getGenes(), PathSeperator);
	}
//
//	public List<List<Integer>> extractGenome()
//	{
//		List<List<Integer>> result = new ArrayList<List<Integer>>();
//		int startIndex = 0;
//		int indexOfPathSeperator = getGenes().indexOf(PathSeperator);
//
//		while (indexOfPathSeperator != -1)
//		{
//			indexOfPathSeperator += startIndex;
//			result.add(getGenes().subList(startIndex, indexOfPathSeperator + 1));
//			startIndex = indexOfPathSeperator + 1;
//			indexOfPathSeperator = getGenes().subList(startIndex, getGenes().size()).indexOf(PathSeperator);
//		}
//
//		return result;
//
//	}

	@Override
	public List<Integer> getRandomValue(Random random)
	{
		// TODO Auto-generated method stub
		return null;
	}

//	public GraphGenome genomeConstruction(List<List<Integer>> lists)
//	{
//		List<Integer> result = new ArrayList<Integer>();
//		for (List path : lists)
//		{
//			result = (List<Integer>) Stream.concat(result.stream(), path.stream()).collect(Collectors.toList());
//		}
//		return new GraphGenome(result, G, SourceTargetPairs);
//	}

//	public boolean IsValidGene(Set<Pair<Integer, Integer>> sourceTarget, int geneIndex)
//	{
//		int gene = getGenes().get(geneIndex);
//		int predecessorGene = getGenes().get(geneIndex - 1);
//		int successorGene = getGenes().get(geneIndex + 1);
//		sourceTarget.stream().anyMatch(
//				x -> (x.getFirst() == gene && getGenes().get(geneIndex + 1) != gene) || x.getSecond() == gene);
//	}

}