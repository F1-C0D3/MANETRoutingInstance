package genetic;

import java.util.List;
import java.util.Random;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.MANETGraph;
import de.terministic.serein.api.Mutation;
import de.terministic.serein.core.genome.ValueGenome;

public class MultiplePathSingleMutation<G extends ValueGenome<?>> implements Mutation<G>
{
	private MANETGraph G;
	private List<Pair<Integer, Integer>> SourceTarget;

	public MultiplePathSingleMutation(MANETGraph g, List<Pair<Integer, Integer>> sourceTarget)
	{
		this.SourceTarget = sourceTarget;
		this.G = g;
	}

	@Override
	public G mutate(G genome, Random random)
	{
		GraphGenome genes = (GraphGenome) genome;
//		List<List<Integer>> geneList = genes.extractGenome();
//
//		int randomMutationIndex = random.nextInt(geneList.size() - 1);
//		List<Integer> mutationPath = geneList.get(randomMutationIndex);
//
//		mutationPath = G.generateRandomPath(mutationPath.get(0), mutationPath.get(mutationPath.size() - 2), random);
//		List<List<Integer>> newGene = new ArrayList<List<Integer>>();
//
//		for (int i = 0; i < geneList.size(); i++)
//		{
//			if (i != randomMutationIndex)
//			{
//				newGene.add(geneList.get(i));
//			} else
//			{
//				newGene.add(mutationPath);
//			}
//		}
		return (G) genes.createRandomInstance(random);
	}
}
