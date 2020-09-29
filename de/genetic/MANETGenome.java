package genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.MANETGraph;
import de.terministic.serein.core.genome.ValueGenome;

public class MANETGenome extends ValueGenome<Integer>
{

	private double[] geneArray;
	MANETGraph G;
	Pair<Integer, Integer> SourceTarget;
	private int PathSeperator = -2;
	private List<Integer> geneInteger;

	public MANETGenome(List<Integer> genes, MANETGraph g, Pair<Integer, Integer> sourceTarget)
	{
		this(genes, g);
		this.G = g;
		this.SourceTarget = sourceTarget;
	}

	public MANETGenome(List<Integer> genes, MANETGraph g)
	{
		super(genes);
		this.G = g;
		this.geneInteger = genes;
	}

//	@Override
//	public List<Integer> getGenes()
//	{
//
//		List<Integer> result = new ArrayList<Integer>();
//
//		List<Integer> genome = super.getGenes();
//		result = genome.subList(1, (genome.size() - 1));
//		return result;
//	}

	@Override
	public String getGenomeId()
	{

		return this.getClass().getName();
	}

	@Override
	public MANETGenome createInstance(List<Integer> genes)
	{
//		List<Integer> manetGeneSourceDest = appendSourceTarget(genes);
		return new MANETGenome(genes, G, SourceTarget);
	}

//	public List<Integer> appendSourceTarget(List<Integer> genes)
//	{
//		genes.add(0, SourceTarget.getFirst());
//		genes.add(SourceTarget.getSecond());
//		return genes;
//	}

	@Override
	public MANETGenome createRandomInstance(Random random)
	{
		List<Integer> result = new ArrayList<Integer>();

		int genomeSize = this.size();
//		result.add(SourceTarget.getFirst());

		for (int numGenes = 0; numGenes < (genomeSize); numGenes++)
		{
			result.add(this.G.getRandomNodeId(random));
		}

//		result.add(SourceTarget.getSecond());
		return new MANETGenome(result, G, SourceTarget);
	}

	public int getPathSeperator()
	{
		return PathSeperator;
	}

	@Override
	public Integer getRandomValue(Random random)
	{
		List<Integer> nodeIds = G.getNodeIds();
		int l = (nodeIds.size());
		return nodeIds.get(random.nextInt(l));
	}

	public int getPathSize()
	{
		return Collections.frequency(getGenes(), PathSeperator);
	}

	public MANETGenome genomeConstruction(List<List<Integer>> lists)
	{
		List<Integer> result = new ArrayList<Integer>();
		for (List path : lists)
		{
			result = (List<Integer>) Stream.concat(result.stream(), path.stream()).collect(Collectors.toList());
		}
		return new MANETGenome(result, G);
	}

//	public boolean IsValidGene(Set<Pair<Integer, Integer>> sourceTarget, int geneIndex)
//	{
//		int gene = getGenes().get(geneIndex);
//		int predecessorGene = getGenes().get(geneIndex - 1);
//		int successorGene = getGenes().get(geneIndex + 1);
//		sourceTarget.stream().anyMatch(
//				x -> (x.getFirst() == gene && getGenes().get(geneIndex + 1) != gene) || x.getSecond() == gene);
//	}

}
