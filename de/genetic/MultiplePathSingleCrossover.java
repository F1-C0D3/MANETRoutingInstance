package genetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.terministic.serein.api.Recombination;
import de.terministic.serein.api.RecombinationException;

public class MultiplePathSingleCrossover implements Recombination<GraphGenome>
{

	@Override
	public GraphGenome recombine(List<GraphGenome> genomes, Random random) throws RecombinationException
	{

		int genomeInjectorIndex = random.nextInt(2);
		GraphGenome injector = genomes.get(genomeInjectorIndex);
		GraphGenome injected = genomes.get(Math.abs(genomeInjectorIndex - 1));
		int elements = injector.getPathSize();
		int crossoverIndex = random.nextInt(elements);

		List<List<Integer>> injectorPaths = injector.extractGenome();
		List<List<Integer>> injectedPaths = injected.extractGenome();
		List<List<Integer>> pathList = new ArrayList<List<Integer>>();
		for (int i = 0; i < elements; i++)
		{
			if (i == crossoverIndex)
			{
				pathList.add(injectorPaths.get(i));
			} else
			{
				pathList.add(injectedPaths.get(i));
			}
		}
		return injected.genomeConstruction(pathList);

	}

	@Override
	public int getMaximumSupportedNoGenomes()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}