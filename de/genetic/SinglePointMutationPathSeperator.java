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

		GraphGenome graphGenome = (GraphGenome) genome;
		int index = random.nextInt(graphGenome.size() - 1);

		List<Integer> graphGenes = graphGenome.getGenes();

		if (graphGenome.getGenes().get(index) == graphGenome.getPathSeperator())
		{
			index--;
		}
		graphGenes.set(index, graphGenome.getRandomValue(random));
		return (G) graphGenome.createInstance(graphGenes);

	}

}
