package de.heuristic.optimization.geneticprogramming;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.terministic.serein.api.Mutation;
import de.terministic.serein.core.genome.ValueGenome;

public class SingleNodeMutation<G extends ValueGenome<?>> implements Mutation<G> {
	int test = 0;

	public SingleNodeMutation() {
	}

	@Override
	public G mutate(G genome, Random random) {

		GraphGenome genes = (GraphGenome) genome;
		List<List<Integer>> chromosome = genes.getGenes();
		List<List<Integer>> newInstance = new ArrayList<List<Integer>>();

		for (List<Integer> part : chromosome) {

//
			if (part.size() <= 2) {

				newInstance.add(new ArrayList<Integer>(part));
			} else {

				int attemps = 100;

				while (attemps != 0) {

					attemps--;

					if (attemps == 0) {
						List<Integer> newRandomPath = new ArrayList<Integer>(part);
						newInstance.add(newRandomPath);
						break;
					}
					int randomNumber = random.nextInt(part.size());

					if (randomNumber == 0) {
						randomNumber++;
					} else if (randomNumber == part.size() - 1) {
						randomNumber--;
					}
					List<Integer> tmp = new ArrayList<Integer>();
					tmp.addAll(genes.getOutgoingGenes(part.get(randomNumber - 1)));
					tmp.retainAll(genes.getIncomingGenes(part.get(randomNumber + 1)));
					tmp.remove(part.get(randomNumber));

					for (Integer gene : part.subList(0, randomNumber)) {

						tmp.remove(gene);
					}

					if (tmp.size() > 0) {

						for (Integer gene : tmp) {
							if (!part.subList(randomNumber, part.size()).contains(gene)) {
								List<Integer> copy = new ArrayList<Integer>(part);
								copy.set(randomNumber, gene);
								newInstance.add(copy);
								attemps = 0;
								break;
							}
						}

					}
				}
			}
		}
		GraphGenome createInstance = genes.createInstance(newInstance);
//		System.out.println(createInstance.toString());
		return (G) createInstance;
	}
}