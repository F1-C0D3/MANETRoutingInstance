package genetic;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.MANETGraph;
import de.terministic.serein.api.Mutation;
import de.terministic.serein.core.genome.ValueGenome;

public class SinglePointMANETPathSeperator<MANETGenome extends ValueGenome<?>> implements Mutation<MANETGenome>
{

	private MANETGraph G;
	private Set<Pair<Integer, Integer>> SourceTarget;

	public SinglePointMANETPathSeperator()
	{
	}

	@Override
	public MANETGenome mutate(MANETGenome genome, Random random)
	{
		List genes = genome.getGenes();
		int index = random.nextInt(genes.size() - 1);

		genes.set(index, genome.getRandomValue(random));
		return (MANETGenome) genome.createInstance(genes);
	}

}
