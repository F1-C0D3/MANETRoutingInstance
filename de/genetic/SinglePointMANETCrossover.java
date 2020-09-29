package genetic;

import java.util.List;
import java.util.Random;

import de.terministic.serein.api.Recombination;
import de.terministic.serein.api.RecombinationException;

public class SinglePointMANETCrossover implements Recombination<MANETGenome>
{

	@Override
	public MANETGenome recombine(List<MANETGenome> genomes, Random random) throws RecombinationException
	{

		MANETGenome g1 = (MANETGenome) genomes.get(0);
		MANETGenome g2 = (MANETGenome) genomes.get(1);

		List g1Modified = g1.getGenes();

		int xPoint = random.nextInt(g1Modified.size());
		List result = g1Modified.subList(0, xPoint);
		List g2Modified = g2.getGenes();
		result.addAll(g2Modified.subList(xPoint, g2Modified.size()));
		return (MANETGenome) g1.createInstance(result);
	}

	@Override
	public int getMaximumSupportedNoGenomes()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
