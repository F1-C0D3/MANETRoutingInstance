package de.GeneticOptimization;

import de.terministic.serein.api.Individual;
import de.terministic.serein.core.fitness.AbstractFitnessFunction;

public class FlowDistributionFitness extends AbstractFitnessFunction<PathComposition> {

    @Override
    public boolean isNaturalOrder() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    protected Double calculateFitness(Individual<PathComposition, ?> individual) {
	PathComposition pc = individual.getPhenotype();
	double occupationfactor = pc.computeResidualTransmissionRate();
	return occupationfactor;
    }

}
