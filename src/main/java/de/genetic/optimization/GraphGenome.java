package de.genetic.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.jgraphlib.util.Tuple;
import de.terministic.serein.core.genome.ValueGenome;

public class GraphGenome extends ValueGenome<List<Integer>> {

	List<List<Tuple<Integer, Integer>>> adjacencyGenesList;
	List<Tuple<Integer, Integer>> genesSourceTargets;

	public GraphGenome(List<List<Integer>> genes, List<List<Tuple<Integer, Integer>>> adjacencyGenesList,
			List<Tuple<Integer, Integer>> genesSourceTargets) {
		super(genes);
		this.adjacencyGenesList = adjacencyGenesList;
		this.genesSourceTargets = genesSourceTargets;
	}

	@Override
	public String getGenomeId() {

		return this.getClass().getName();
	}

	@Override
	public GraphGenome createInstance(List<List<Integer>> genes) {
		return new GraphGenome(genes, adjacencyGenesList, genesSourceTargets);
	}

	@Override
	public GraphGenome createRandomInstance(Random random) {
		List<List<Integer>> chromosome = new ArrayList<List<Integer>>();

		for (Tuple<Integer, Integer> sourceTargetGene : genesSourceTargets) {
			List<Integer> chromosomePart = null;

			while (chromosomePart == null) {
				chromosomePart = generateRandomPath(sourceTargetGene.getFirst(), sourceTargetGene.getSecond(), random);
			}
			chromosome.add(chromosomePart);
		}
		return createInstance(chromosome);
	}

	private List<Integer> generateRandomPath(int sourceGene, int targetGene, Random random) {
		List<Integer> pathChromosome = new ArrayList<Integer>();
		List<Integer> visited = new ArrayList<Integer>();

		while (targetGene != sourceGene) {
			List<Tuple<Integer, Integer>> sinkGenes = new ArrayList<Tuple<Integer, Integer>>();
			sinkGenes.addAll(adjacencyGenesList.get(sourceGene));
			sinkGenes.removeIf(l -> visited.contains(l.getSecond()));

			if (!sinkGenes.isEmpty()) {
				Tuple<Integer, Integer> linkGene = sinkGenes.get(random.nextInt(sinkGenes.size()));

				visited.add(sourceGene);
				pathChromosome.add(sourceGene);
				sourceGene = linkGene.getSecond();
			} else {
				return null;
			}

		}
		pathChromosome.add(sourceGene);
		return pathChromosome;
	}

	@Override
	public List<Integer> getRandomValue(Random random) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPathSize() {
		return this.getGenes().size();
	}

}