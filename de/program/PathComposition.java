package program;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.MANETGraph;

public class PathComposition extends ArrayList<Integer>
{
	MANETGraph G;
	Pair<Integer, Integer> SourceTarget;

	public PathComposition(List<Integer> list)
	{
		super(list);
	}

	public PathComposition(List<Integer> list, MANETGraph g, Pair<Integer, Integer> sourceTarget)
	{
		this(list);
		this.G = g;
		this.SourceTarget = sourceTarget;
	}

	public int pathLengths()
	{
		return this.size();
	}

//	public boolean hasValidPaths()
//	{
//
//		int result = this.size();
//		Set<Integer> duplicates = new HashSet<Integer>();
//		boolean foundTarget = false;
//		for (int i = 0; i < this.size() - 1; i++)
//		{
//
//			if (i == 0)
//			{
//				if (G.isDirectNeighbor(this.SourceTarget.getFirst(), this.get(0)))
//				{
//					result--;
//					duplicates.add(this.SourceTarget.getFirst());
//				} else
//				{
//					return this.size();
//				}
//			}
//
//			if (G.isDirectNeighbor(this.get(i), this.get(i + 1)))
//			{
//				if (!duplicates.add(this.get(i)))
//				{
//					return this.size();
//				}
//				if (get(i + 1) == SourceTarget.getSecond())
//				{
//					return this.size() - (result - 1);
//				}
//				result--;
//			} else
//			{
//				return this.size();
//			}
//
//		}

//		for (int i = this.size() - 1; i != 0; i--)
//		{
//			if (i == this.size() - 1)
//			{
//				if (G.isDirectNeighbor(this.get(i), this.SourceTarget.getSecond()))
//				{
//					result--;
//				} else
//				{
//					break;
//				}
//
//			}
//
//			if (G.isDirectNeighbor(this.get(i - 1), this.get(i)))
//			{
//				result--;
//			} else
//			{
//				return result;
//			}
//
//		}
//
//		return result;
//
//	}

	public double ContainingValidLinksRelation()
	{
		int validLinks = 0;
		for (int index = 0; index < (this.size() - 1); index++)
		{
			if (G.isDirectNeighbor(this.get(index), this.get(index + 1)))
			{
				validLinks++;
			}
		}
		return ((this.size()) - validLinks) / ((double) this.size());
	}

	public double containsSourceTarget()
	{
		double containsSourceTarget = 1;
		if (this.contains(this.SourceTarget.getFirst()))
		{
			containsSourceTarget = containsSourceTarget - 0.5;
		}
		if (this.contains(this.SourceTarget.getSecond()))
		{
			containsSourceTarget = containsSourceTarget - 0.5;
		}

		return containsSourceTarget;
	}

	public double isValidPath()
	{
		double isValidPath = 1;
		int sourceIdIndex = -1;
		int targetIdIndex = -1;

		if (containsSourceTarget() == 0)
		{
			sourceIdIndex = this.indexOf(this.SourceTarget.getFirst());
			targetIdIndex = this.indexOf(this.SourceTarget.getSecond());

			if (sourceIdIndex > targetIdIndex)
			{
				int tmpTarget = sourceIdIndex;
				sourceIdIndex = targetIdIndex;
				targetIdIndex = tmpTarget;
			}

			isValidPath = 0;
			for (int index = sourceIdIndex; index < targetIdIndex; index++)
			{
				if (!G.isDirectNeighbor(this.get(index), this.get(index + 1)))
				{
					isValidPath = 1;
					break;
				}
			}
			return isValidPath;
		} else
		{
			return isValidPath;
		}
	}

}
