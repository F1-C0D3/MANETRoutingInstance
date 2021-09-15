package de.genetic.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.inference.GTest;

import de.terministic.serein.api.Recombination;
import de.terministic.serein.api.RecombinationException;

public class OnePointMultiplePathCrossover implements Recombination<GraphGenome> {

	@Override
	public GraphGenome recombine(List<GraphGenome> genomes, Random random) throws RecombinationException {

		// Random define which genome will be chosen to iterate over
		int genomeInjectorIndex = random.nextInt(2);
		List<List<Integer>> cutterGenome = genomes.get(genomeInjectorIndex).getGenes();
		List<List<Integer>> cuttingGenome = genomes.get(Math.abs(genomeInjectorIndex - 1)).getGenes();

		// New GraphGenome
		List<List<Integer>> pathList = new ArrayList<List<Integer>>();

		// Index at position to start searching for equal genes in the chromosome
		int initialCutIndex = random.nextInt(cutterGenome.size());
		int currentCutIndex = initialCutIndex;

		for (int i = 0; i < cutterGenome.size(); i++) {
			
			List<Integer> cutterGenesPart = cutterGenome.get(i);
			List<Integer> cuttingGenesPart = cuttingGenome.get(i);

			int attemps = 100;
			List<Integer> newChromosomePart = null;
			while (attemps >= 0) {

				// get next potential cut index
				int nextCutIndex = random.nextInt(cutterGenesPart.size());

				// Search after equal gene in cuttingGenesPart
				int cuttingIndex = cutIndex(cutterGenesPart, cuttingGenesPart, random, nextCutIndex);
				if (cuttingIndex != -1) {

					newChromosomePart = new ArrayList<Integer>(cutterGenesPart.subList(0, nextCutIndex));
					newChromosomePart.addAll(cuttingGenesPart.subList(cuttingIndex, cuttingGenesPart.size()));
					
					attemps = 0;
				}

				attemps--;
			}
			
			if(newChromosomePart==null) {
				newChromosomePart = new ArrayList<Integer>(cutterGenesPart);
			}
			
			pathList.add(newChromosomePart);
		}
		return (GraphGenome) genomes.get(0).createInstance(pathList);

	}

	/**
	 * @param chromosomePartOne and chromosomePartTwo contain genes crossover
	 *                          operation takes place.
	 * @param random            object
	 * @param index             that points to individual chromosome of
	 *                          chromosomePartOne where desired crossover operation
	 *                          takes place
	 * @return Returns the index of {@code chromosomePartTwo} for crossover
	 *         operation. Return -1 if crossover is not possible
	 */
	private int cutIndex(List<Integer> chromosomePartOne, List<Integer> chromosomePartTwo, Random random,
			int initialCutIndex) {

		Integer gene = chromosomePartOne.get(initialCutIndex);

		if (!chromosomePartTwo.contains(gene))
			return -1;

		if (initialCutIndex == 0)
			return 0;

		if (initialCutIndex == chromosomePartOne.size() - 1)
			return chromosomePartTwo.size() - 1;

		int cutIndexChromosomePartTwo = chromosomePartTwo.indexOf(gene);

		List<Integer> partOneLeft = new ArrayList<Integer>(chromosomePartOne.subList(0, initialCutIndex));
		partOneLeft.retainAll(chromosomePartTwo.subList(cutIndexChromosomePartTwo, chromosomePartTwo.size()));

		List<Integer> partTwoLeft = new ArrayList<Integer>(chromosomePartTwo.subList(0, cutIndexChromosomePartTwo));
		partTwoLeft.retainAll(chromosomePartOne.subList(initialCutIndex, chromosomePartOne.size()));

		if (partOneLeft.size() <= 2 && partTwoLeft.size() <= 2) {
			return cutIndexChromosomePartTwo;
		}

		return -1;
	}

	@Override
	public int getMaximumSupportedNoGenomes() {
		// TODO Auto-generated method stub
		return 0;
	}

}