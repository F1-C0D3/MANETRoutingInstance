package de.heuristic.optimization.geneticprogramming;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.jgraphlib.util.Tuple;
import de.terministic.serein.core.genome.ValueGenome;

public class GraphGenome extends ValueGenome<List<Integer>> {

	public List<List<Integer>> sourceTargetAdjacvencyGenes;
	public List<List<Integer>> targetSourceAdjacencyGenes;
	List<Tuple<Integer, Integer>> genesSourceTargets;

	private List<InstructedIndividualPathGeneration> instructedIndividualGenerations;
	private double instructionFactor;

	public GraphGenome(List<List<Integer>> genes, List<List<Integer>> sourceTargetAdjacvencyGenes,
			List<List<Integer>> targetSourceAdjacencyGenes, List<Tuple<Integer, Integer>> genesSourceTargets,
			double instructionFactor) {
		super(genes);

		this.targetSourceAdjacencyGenes = targetSourceAdjacencyGenes;
		this.genesSourceTargets = genesSourceTargets;
		this.instructionFactor = instructionFactor;
		this.sourceTargetAdjacvencyGenes = sourceTargetAdjacvencyGenes;
		this.instructedIndividualGenerations = new ArrayList<InstructedIndividualPathGeneration>();

		for (Tuple<Integer, Integer> sourceTargetGenes : genesSourceTargets) {

			List<List<Integer>> sourceTargetAdjacvencyGenesCopy = new ArrayList<List<Integer>>();
			for (List<Integer> individualSourceTargetGenes : sourceTargetAdjacvencyGenes) {

				List<Integer> neighborGenes = new ArrayList<Integer>();

				for (Integer gene : individualSourceTargetGenes) {
					neighborGenes.add(gene);
				}

				sourceTargetAdjacvencyGenesCopy.add(neighborGenes);
			}
			instructedIndividualGenerations.add(new InstructedIndividualPathGeneration(sourceTargetAdjacvencyGenesCopy,
					sourceTargetGenes.getFirst(), sourceTargetGenes.getSecond()));
		}

	}

	@Override
	public String getGenomeId() {

		return this.getClass().getName();
	}

	@Override
	public GraphGenome createInstance(List<List<Integer>> genes) {
		return new GraphGenome(genes, this.sourceTargetAdjacvencyGenes, this.targetSourceAdjacencyGenes,
				this.genesSourceTargets, this.instructionFactor);
	}

	@Override
	public GraphGenome createRandomInstance(Random random) {
		List<List<Integer>> chromosome = new ArrayList<List<Integer>>();

		for (Tuple<Integer, Integer> sourceTargetGene : genesSourceTargets) {
			List<Integer> chromosomePart = null;

			while (chromosomePart == null) {
				chromosomePart = generateIndividual(sourceTargetGene.getFirst(), sourceTargetGene.getSecond(), random);
			}
			chromosome.add(chromosomePart);
		}
		return createInstance(chromosome);
	}

	public List<Integer> generateIndividual(int sourceGene, int targetGene, Random random) {

		if (random.nextDouble() > instructionFactor)
			return generateRandomIndividual(sourceGene, targetGene, random);
		else {
			
			InstructedIndividualPathGeneration instructedPathGeneration = null;
			
			for (InstructedIndividualPathGeneration element : this.instructedIndividualGenerations) {
				
				if (element.getSourceGene() == sourceGene && element.getTargetGene() == targetGene) {
					instructedPathGeneration = element;
					break;
				}

			}
			return instructedPathGeneration.generateNewIndividual();
		}

	}

	private List<Integer> generateRandomIndividual(int sourceGene, int targetGene, Random random) {
		List<Integer> pathChromosome = new ArrayList<Integer>();
		List<Integer> visited = new ArrayList<Integer>();

		while (targetGene != sourceGene) {
			List<Integer> sinkGenes = new ArrayList<Integer>();
			sinkGenes.addAll(sourceTargetAdjacvencyGenes.get(sourceGene));
			sinkGenes.removeIf(l -> visited.contains(l));

			if (!sinkGenes.isEmpty()) {
				int newSource = sinkGenes.get(random.nextInt(sinkGenes.size()));

				visited.add(sourceGene);
				pathChromosome.add(sourceGene);
				sourceGene = newSource;
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

	public List<Integer> getOutgoingGenes(int index) {

		List<Integer> outgoingGenes = new ArrayList<Integer>();

		for (Integer outgoingGene : this.sourceTargetAdjacvencyGenes.get(index)) {
			outgoingGenes.add(outgoingGene);

		}
		return outgoingGenes;
	}

	public List<Integer> getIncomingGenes(int index) {

		List<Integer> incomingGenes = new ArrayList<Integer>();

		for (Integer incomingGene : this.targetSourceAdjacencyGenes.get(index)) {
			incomingGenes.add(incomingGene);

		}
		return incomingGenes;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.getGenes().toString();
	}

}