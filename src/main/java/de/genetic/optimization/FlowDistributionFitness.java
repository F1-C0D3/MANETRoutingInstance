package de.genetic.optimization;

import de.manetmodel.units.DataRate;
import de.terministic.serein.api.Individual;
import de.terministic.serein.core.fitness.AbstractFitnessFunction;

public class FlowDistributionFitness<W> extends AbstractFitnessFunction<PathComposition> {

	/* fitness weights */

	/* over utilization weight */
	double ouW = 0.51;
	/* receptionPower weight */
//	double rpW = 0.20;

	/* utilization weight */
	double uW = 0.49;

	@Override
	public boolean isNaturalOrder() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Double calculateFitness(Individual<PathComposition, ?> individual) {

		PathComposition pc = individual.getPhenotype();
		pc.deployFlows();
//		/* Compute entire manet capacity */
		DataRate manetCapacity = pc.getManetCapacity();
//		/* Data rate over utilized */
		DataRate overUtilization = pc.overUtilization();
		double overUtilizationNormalized = ((1d / manetCapacity.get()) * overUtilization.get()) * ouW;
////		System.out.println(overUtilizationNormalized);
//
		DataRate networkUtilization = pc.getNetworUtilization();
		double utilizationNormalized = ((1d / manetCapacity.get()) * networkUtilization.get()) * uW;

//		double receptionQuality = pc.minLinkReceptionVariance();
//		System.out.println(1 - (1d / receptionQuality));
//		double receptionQualityNormalized = receptionQuality * rpW;

		pc.undeployFlows();
		return utilizationNormalized + overUtilizationNormalized;
	}

}
