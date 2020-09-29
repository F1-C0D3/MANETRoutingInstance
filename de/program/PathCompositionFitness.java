package program;

import java.text.DecimalFormat;

import de.terministic.serein.api.Individual;
import de.terministic.serein.core.fitness.AbstractFitnessFunction;

public class PathCompositionFitness extends AbstractFitnessFunction<PathComposition>
{

	final double _wValidPath = 0.7;
	final double _wValidLinks = 0.1;
	final double _wSourceTarget = 0.2;

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
		DecimalFormat df = new DecimalFormat("#.000");
		double validLinksRelations = pc.ContainingValidLinksRelation();
		validLinksRelations = (validLinksRelations * _wValidLinks);
		double sourceTargetInPath = pc.containsSourceTarget();
		sourceTargetInPath = sourceTargetInPath * _wSourceTarget;
		double isValidPath = pc.isValidPath();
		isValidPath = (isValidPath * _wValidPath);
		return validLinksRelations + sourceTargetInPath + isValidPath;
	}

}
