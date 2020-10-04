package genetic;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.MANETGraph;
import de.terministic.serein.api.Mutation;
import de.terministic.serein.core.genome.ValueGenome;

public class SinglePointMutationPathSeperator<G extends ValueGenome<?>> implements Mutation<G>
{

	private MANETGraph G;
	private Set<Pair<Integer, Integer>> SourceTarget;

	public SinglePointMutationPathSeperator()
	{
	}

	@Override
	public G mutate(G genome, Random random)
	{

		GraphGenome graphGenes = (GraphGenome) genome;
		int index = random.nextInt(graphGenes.size() - 1);

		List<Integer> geneList = graphGenes.getGenes();

		if (graphGenes.getGenes().get(index) == graphGenes.getPathSeperator())
		{
			index--;
		}
		geneList.set(index, graphGenes.getRandomValue(random));
		return (G) graphGenes.createInstance(geneList);

	}

}
