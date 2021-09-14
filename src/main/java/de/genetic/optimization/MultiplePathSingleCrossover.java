package de.genetic.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.inference.GTest;

import de.terministic.serein.api.Recombination;
import de.terministic.serein.api.RecombinationException;

public class MultiplePathSingleCrossover implements Recombination<GraphGenome> {

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

		int elements = cutterGenome.size();
		for (int i = 0; i < cutterGenome.size(); i++) {
			List<Integer> cutterGenesPart = cutterGenome.get(i);
			List<Integer> cuttingGenesPart = cutterGenome.get(i);

			while (elements != 0) {

				// get next potential cut index
				int nextCutIndex = nextCutIndex(cutterGenesPart, random, initialCutIndex, currentCutIndex);

				// Search after equal gene in cuttingGenesPart
				Integer gene = cutterGenesPart.get(nextCutIndex);
				if (cuttingGenesPart.contains(gene)) {

					short counter = 2;
					boolean containsDuplicate = false;
					while (counter != 0) {
						List<Integer> leftCutterSubList = new ArrayList<Integer>(
								cutterGenesPart.subList(0, nextCutIndex));
						List<Integer> rightCuttingSubList = new ArrayList<Integer>(
								cuttingGenesPart.subList(cutterGenesPart.indexOf(gene), cutterGenesPart.size()));

						leftCutterSubList.retainAll(rightCuttingSubList);

						if (leftCutterSubList.size() > 2) {
							containsDuplicate = true;
						}
					}
				}

				elements--;
			}
		}
		return (GraphGenome) genomes.get(0).createInstance(pathList);

	}

	private int nextCutIndex(List<Integer> chromosomePart, Random random, int initialCutIndex, int currentCutIndex) {

		return 0;
	}

	@Override
	public int getMaximumSupportedNoGenomes() {
		// TODO Auto-generated method stub
		return 0;
	}

}