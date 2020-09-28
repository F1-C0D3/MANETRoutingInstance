package flow;

import java.text.DecimalFormat;

import de.terministic.serein.api.Individual;
import de.terministic.serein.core.fitness.AbstractFitnessFunction;

public class PathCompositionFitness extends AbstractFitnessFunction<PathComposition>
{

	final double _wValidPath = 0.6;

	@Override
	public boolean isNaturalOrder()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Double calculateFitness(Individual<PathComposition, ?> individual)
	{
		double res = 0;
		PathComposition pc = individual.getPhenotype();
		DecimalFormat df = new DecimalFormat("#.000");
		if (!pc.hasValidPaths())
		{
			res = 30;
		}

		res += pc.pathLengths();
		return res;
	}

}
