package de.genetic.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.jgraphlib.util.Tuple;
import de.terministic.serein.core.genome.ValueGenome;

public class GraphGenome extends ValueGenome<List<Integer>> {

	public List<List<Integer>> sourceTargetAdjacvencyGenes;
	public List<List<Integer>> targetSourceAdjacencyGenes;
	List<Tuple<Integer, Integer>> genesSourceTargets;

	public GraphGenome(List<List<Integer>> genes, List<List<Integer>> sourceTargetAdjacvencyGenes,List<List<Integer>> targetSourceAdjacencyGenes
			,
			List<Tuple<Integer, Integer>> genesSourceTargets) {
		super(genes);
		this.sourceTargetAdjacvencyGenes = sourceTargetAdjacvencyGenes;
		this.targetSourceAdjacencyGenes = targetSourceAdjacencyGenes;
		this.genesSourceTargets = genesSourceTargets;
	}

	@Override
	public String getGenomeId() {

		return this.getClass().getName();
	}

	@Override
	public GraphGenome createInstance(List<List<Integer>> genes) {
		return new GraphGenome(genes, sourceTargetAdjacvencyGenes,targetSourceAdjacencyGenes, genesSourceTargets);
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

	public List<Integer> generateRandomPath(int sourceGene, int targetGene, Random random) {
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
		
		List<Integer> outgoingGenesList = new ArrayList<Integer>();
		
		for(Integer outgoingGenes : this.sourceTargetAdjacvencyGenes.get(index)){
			outgoingGenesList.add(outgoingGenes);
			
		}
		return outgoingGenesList;
	}
	
	public List<Integer> getIncomingGenes(int index) {
		
		List<Integer> incomingGenesList = new ArrayList<Integer>();
		
		for(Integer incomingGenes : this.targetSourceAdjacencyGenes.get(index)){
			incomingGenesList.add(incomingGenes);
			
		}
		return incomingGenesList;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.getGenes().toString();
	}

}