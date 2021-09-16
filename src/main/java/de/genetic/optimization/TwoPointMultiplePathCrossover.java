package de.genetic.optimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.inference.GTest;

import de.jgraphlib.util.RandomNumbers;
import de.terministic.serein.api.Recombination;
import de.terministic.serein.api.RecombinationException;
import de.terministic.serein.core.util.Lists;

public class TwoPointMultiplePathCrossover implements Recombination<GraphGenome> {

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

				int[] cutIndicesChromosomeOne = new int[2];
				cutIndicesChromosomeOne[0] = this.getRandom(random,0,cutterGenesPart.size());
				cutIndicesChromosomeOne[1] = this.getRandomNotInE(random,0, cutterGenesPart.size(),
						new ArrayList<Integer>(cutIndicesChromosomeOne[0]));
				// Search after equal gene in cuttingGenesPart
				int[] cuttingIndices = cutIndex(cutterGenesPart, cuttingGenesPart,cutIndicesChromosomeOne);
				if (cuttingIndices[0] != -1) {
List<Integer> partToIncludeInCutterGenesPart = new ArrayList<Integer>();
					newChromosomePart = new ArrayList<Integer>(cutterGenesPart.subList(0, nextCutIndex));
					newChromosomePart.addAll(cuttingGenesPart.subList(cuttingIndex, cuttingGenesPart.size()));
					attemps = 0;
				}

				attemps--;
			}

			if (newChromosomePart == null) {
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
	 * @param cutIndexOne       and cutIndexTwo specify the position to cut the
	 *                          desired chromosomes takes place
	 * @return Returns the index of {@code chromosomePartTwo} for crossover
	 *         operation. Return -1 if crossover is not possible
	 */
	private int[] cutIndex(List<Integer> chromosomePartOne, List<Integer> chromosomePartTwo, int[] cutIndicesChromosomeOne) {

		
		int[] cutIndicesChromosomeTwo = new int[] {-1,-1};
		
		Arrays.sort(cutIndicesChromosomeOne);

		Integer geneOne = chromosomePartOne.get(cutIndicesChromosomeOne[0]);
		Integer geneTwo = chromosomePartTwo.get(cutIndicesChromosomeOne[1]);
		if (!chromosomePartTwo.contains(geneOne) || !chromosomePartTwo.contains(geneTwo))
			return cutIndicesChromosomeTwo;

		cutIndicesChromosomeTwo[0] = chromosomePartTwo.indexOf(geneOne);
		cutIndicesChromosomeTwo[1] = chromosomePartTwo.indexOf(geneTwo);
		Arrays.sort(cutIndicesChromosomeTwo);
		
		ArrayList<Integer> cutPartChromosomeOne = new ArrayList<Integer>(chromosomePartOne.subList(cutIndicesChromosomeOne[0], cutIndicesChromosomeOne[1] + 1));
		ArrayList<Integer> cutPartChromosomeTwo = new ArrayList<Integer>(chromosomePartOne.subList(cutIndicesChromosomeTwo[0], cutIndicesChromosomeTwo[1] + 1));
		
		ArrayList<Integer> cutComplementChromosomeOne = new ArrayList<Integer>(cutPartChromosomeOne);
		cutComplementChromosomeOne.removeAll(cutPartChromosomeOne);
		
		ArrayList<Integer> cutComplementChromosomeTwo = new ArrayList<Integer>(cutPartChromosomeTwo);
		cutComplementChromosomeTwo.removeAll(cutPartChromosomeTwo);
		
		cutComplementChromosomeOne.retainAll(cutPartChromosomeTwo);
		cutComplementChromosomeTwo.retainAll(cutComplementChromosomeOne);
		
		if(cutComplementChromosomeOne.size()==0 && cutComplementChromosomeTwo.size()==0)
		return cutIndicesChromosomeTwo;
		

		return cutIndicesChromosomeTwo;
	}
	
	private int getRandomNotInE(Random randomInstance,int min, int max, List<Integer> e) {
		
		int random = getRandom(randomInstance,min, max);

		while (e.contains(random))
			random = getRandom(randomInstance,min, max);

		return random;

	}
	
	private int getRandom(Random randomInstance,int min, int max) {
		
		if (min >= max)
			return min;

		return randomInstance.nextInt(max - min) + min;
	}

	@Override
	public int getMaximumSupportedNoGenomes() {
		// TODO Auto-generated method stub
		return 0;
	}

}