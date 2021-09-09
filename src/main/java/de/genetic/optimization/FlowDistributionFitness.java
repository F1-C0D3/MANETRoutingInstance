package de.genetic.optimization;

import java.util.List;

import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.units.DataRate;
import de.terministic.serein.api.Individual;
import de.terministic.serein.core.fitness.AbstractFitnessFunction;

public class FlowDistributionFitness<W> extends AbstractFitnessFunction<PathComposition> {

	// over utilization weight
	double ouW = 0.51;

	// mobility weight
	double mW = 0.49;

	// reception power weight
	double rpW = 0.20;
	
	double uW = 0.49;

	@Override
	public boolean isNaturalOrder() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Double calculateFitness(Individual<PathComposition, ?> individual) {

		PathComposition pc = individual.getPhenotype();

		ScalarRadioMANET manet = pc.getManet();
		List<ScalarRadioFlow> flows = pc.getFlows();
		manet.deployFlows(flows);

//		/* Compute entire manet capacity */
		DataRate manetCapacity = manet.getCapacity();

		
		DataRate networkUtilization = manet.getUtilization();
		double utilizationNormalized = ((1d / manetCapacity.get()) * networkUtilization.get()) * uW;
		
//		/* Data rate over utilized */
		DataRate overUtilization = manet.getOverUtilization();
		double overUtilizationNormalized=0d;
		if(overUtilization.get()>0)
		 overUtilizationNormalized = 1 * ouW;

		
		double receptionPower = pc.meanLinkReception() * rpW;

		double mobilityQuality = pc.meanMobilityQuality() * mW;
		
		double distance = pc.getNumLinks();
		manet.undeployFlows();
		return utilizationNormalized;
	}

}
