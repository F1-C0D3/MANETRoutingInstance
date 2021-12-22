package de.heuristic.optimization.geneticprogramming;

import java.util.List;

import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.units.DataRate;
import de.terministic.serein.api.Individual;
import de.terministic.serein.core.fitness.AbstractFitnessFunction;

public class FlowDistributionFitness<W> extends AbstractFitnessFunction<PathComposition> {
	
	// Max possuble Utilization
	private double maxTheoreticalUtiliation;
	
	// over utilization weight
	private double ouW = 0.51;

	// mobility weight
	private double mW = 0.04;

	// reception power weight
	private double rcW = 0.10;

	private double uW = 0.35;


	public FlowDistributionFitness(DataRate maxPossibleUtilization) {
		this.maxTheoreticalUtiliation = maxPossibleUtilization.get();
	}

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

		double networkUtilization = manet.getUtilization().get();
		
		double utilizationNormalized=(networkUtilization/maxTheoreticalUtiliation)*uW;
		

//		/* Data rate over utilized */
		DataRate overUtilization = manet.getOverUtilization();
		double overUtilizationNormalized = 0d;
		if (overUtilization.get() > 0) 
			overUtilizationNormalized = 1d * ouW;
		

		double receptionPower = pc.meanReceptionConfidence() * rcW;
//
		double mobilityQuality = pc.meanMobilityQuality() * mW;
//
//		double distance = (pc.getNumLinks() / (double) manet.getEdges().size()) * dW;
		manet.undeployFlows();
		return overUtilizationNormalized +utilizationNormalized+receptionPower+mobilityQuality;
	}

}
