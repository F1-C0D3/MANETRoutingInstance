package program;

import java.text.DecimalFormat;
import java.util.List;

import org.jgrapht.alg.util.Pair;

import de.terministic.serein.api.Individual;
import de.terministic.serein.core.fitness.AbstractFitnessFunction;

public class PathCompositionFitness extends AbstractFitnessFunction<PathComposition>
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
		DecimalFormat df = new DecimalFormat("#.000");

		double result = 0;

		double stPositionValue = pc.preferedSourceDestPositions(3);
		result += stPositionValue * _wSTPosition;
		List<List<Integer>> path = pc.validPath();
//		Pair<Set<List<Integer>>, Pair<Integer, Set<Integer>>> resultPair = pc.returnValidPathWithCriteria(1);
//		Set<List<Integer>> validPaths = resultPair.getFirst();
//		Set<Integer> duplicates = resultPair.getSecond().getSecond();
//		int validHops = duplicates.size();

		Pair<List<Integer>, List<Integer>> counter = pc.sourceTargetCounter();
		int sourceTargetCounter = counter.getFirst().size() + counter.getSecond().size();

		if (sourceTargetCounter == 0 || sourceTargetCounter > 3 * 2)
		{
			result += 1 * _wSourceTarget;
		}
		int pathSizes = 0;
		boolean containsValidPath = false;
		for (List<Integer> list : path)
		{
			pathSizes += list.size();
			containsValidPath = list.contains(pc.SourceTarget.getFirst()) && list.contains(pc.SourceTarget.getSecond())
					? true
					: false;

		}

		if (!containsValidPath)
		{
			result += 1 * _wValidPath;
			result += (pc.size() - pathSizes) / ((double) pc.size()) * _wValidLinks;
		}
		/*
		 * Rank robustnes of partial paths
		 */
//		double diversity = 1 - (pc.computeDiversity(path) / (double) pc.size());
//		result += diversity * _wDiversity;
		/*
		 * Do we already have partial routes for either starting from source or target,
		 * respectively?
		 */
//		result = (pc.size() - validHops) / ((double) pc.size()) * _wValidLinks;

		/*
		 * Is source and target present in the genome
		 */
//		result += (pc.containsSourceTarget() * _wSourceTarget);

		/*
		 * Do we have detected valid paths in the current genome?
		 */
//		if (validPaths.size() > 0)
//
//		{
//			/*
//			 * Take the the shortest path
//			 */
//			int min = validPaths.stream().min(Comparator.comparingInt(List::size)).orElse(new ArrayList<Integer>())
//					.size();
//
//			/*
//			 * Increase rank of shortest path
//			 */
//			result += (min / (double) pc.size()) * _wValidPath;
//		} else
//		{
//			result += 1 * _wValidPath;
//		}

		return result;
//		double validLinksRelations = pc.ContainingValidLinksRelation();
//		validLinksRelations = (validLinksRelations * _wValidLinks);
//		double sourceTargetInPath = pc.containsSourceTarget();
//		sourceTargetInPath = sourceTargetInPath * _wSourceTarget;
//		double isValidPath = pc.isValidPath();
//		isValidPath = (isValidPath * _wValidPath);
//		return validLinksRelations + sourceTargetInPath + isValidPath;
	}

}
