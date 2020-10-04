package genetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.terministic.serein.api.Recombination;
import de.terministic.serein.api.RecombinationException;

public class SinglePointCrossoverPathSeperator implements Recombination<GraphGenome>
{

	@Override
	public GraphGenome recombine(List<GraphGenome> genomes, Random random) throws RecombinationException
	{

		GraphGenome graphGenes1 = (GraphGenome) genomes.get(0);
		GraphGenome graphGenes2 = (GraphGenome) genomes.get(1);
		int elements = graphGenes1.getPathSize();

		List<List<Integer>> pathGeneList1 = graphGenes1.extractGenome();
		List<List<Integer>> pathGenesList2 = graphGenes2.extractGenome();

		List<Integer> resulteGenes = new ArrayList<Integer>();

		for (int i = 0; i < elements; i++)
		{
			List<Integer> genes1 = pathGeneList1.get(i);
			List<Integer> genes2 = pathGenesList2.get(i);
			int maxRangeSize = genes1.size() > genes2.size() ? genes2.size() : genes1.size();
			int crossoverIndex = random.nextInt(maxRangeSize);

			if (graphGenes1.getPathSeperator() == graphGenes1.getGenes().get(crossoverIndex))
			{
				crossoverIndex--;
			}

			List result = genes1.subList(0, crossoverIndex);
			result.addAll(genes2.subList(crossoverIndex, genes2.size()));
			resulteGenes.addAll(resulteGenes.size(), result);
			result.clear();
		}

		return graphGenes1.createInstance(resulteGenes);
	}

	@Override
	public int getMaximumSupportedNoGenomes()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
