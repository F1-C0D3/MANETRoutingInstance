package program;

import de.terministic.serein.api.Individual;
import de.terministic.serein.core.fitness.AbstractFitnessFunction;

public class FlowDistributionFitness extends AbstractFitnessFunction<PathComposition>
{

	final double _wValidPath = 0.51;
	final double _wValidLinks = 0.27;
	final double _wSourceTarget = 0.11;
	final double _wSTPosition = 0.10;
//	final double _wDiversity = 0.10;

	@Override
	public boolean isNaturalOrder()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Double calculateFitness(Individual<PathComposition, ?> individual)
	{
		PathComposition pc = individual.getPhenotype();
//		pc.computeResidualTransmissionRate();
		return (double) pc.pathLengths();
	}

}
