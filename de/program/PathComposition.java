package program;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.MANETGraph;

public class PathComposition extends ArrayList<Integer>
{
	MANETGraph G;
	Pair<Integer, Integer> SourceTarget;
	List<List<Integer>> suitablePath;

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

	public int validHops()
	{
		Set<Integer> duplicates = new HashSet<Integer>();
		List<Integer> sourceIndexes = new ArrayList<Integer>();
		List<Integer> targetIndexes = new ArrayList<Integer>();
		List<Integer> copy = new ArrayList<Integer>(this);
		for (int i = 0; i < copy.size(); i++)
		{
			if (this.get(i) == SourceTarget.getFirst())
			{
				sourceIndexes.add(copy.indexOf(copy.get(i)));
				copy.set(i, -1);

			} else if (this.get(i) == SourceTarget.getSecond())
			{
				targetIndexes.add(copy.indexOf(copy.get(i)));
				copy.set(i, -1);
			}
		}

		int pathLength = 0;
//		System.out.println(this);
		for (int sourceIndex : sourceIndexes)
		{
//			System.out.println("source: " + sourceIndexes);
//			System.out.println("target: " + targetIndexes);
//			pathLength = 0;
			for (int innerSourceIndex = sourceIndex; innerSourceIndex < this.size() - 1; innerSourceIndex++)
			{
				duplicates.add(SourceTarget.getFirst());

				if (G.isDirectNeighbor(this.get(innerSourceIndex), this.get(innerSourceIndex + 1)))
				{
					if (duplicates.add(this.get(innerSourceIndex + 1)))
					{
						pathLength++;
					}
				} else
				{
					duplicates.clear();
					break;
				}
			}

			for (int innerSourceIndex = sourceIndex; innerSourceIndex > 1; innerSourceIndex--)
			{
				duplicates.add(SourceTarget.getFirst());

				if (G.isDirectNeighbor(this.get(innerSourceIndex), this.get(innerSourceIndex - 1)))
				{
					if (duplicates.add(this.get(innerSourceIndex - 1)))
					{
						pathLength++;
					}
				} else
				{
					duplicates.clear();
					break;
				}
			}
		}

		duplicates.clear();
		int resultTargetToSource = 0;
		for (int targetIndex : targetIndexes)
		{
			pathLength = 0;
			for (int innerTargetIndex = targetIndex; innerTargetIndex < this.size() - 1; innerTargetIndex++)
			{
				duplicates.add(SourceTarget.getSecond());

				if (G.isDirectNeighbor(this.get(innerTargetIndex), this.get(innerTargetIndex + 1)))
				{
					if (duplicates.add(this.get(innerTargetIndex + 1)))
					{
						pathLength++;
					}
				} else
				{
					duplicates.clear();
					break;
				}
			}

			for (int innerTargetIndex = targetIndex; innerTargetIndex > 1; innerTargetIndex--)
			{
				duplicates.add(SourceTarget.getSecond());

				if (G.isDirectNeighbor(this.get(innerTargetIndex), this.get(innerTargetIndex - 1)))
				{
					if (duplicates.add(this.get(innerTargetIndex - 1)))
					{
						pathLength++;
					}
				} else
				{
					duplicates.clear();
					break;
				}
			}
		}
//		System.out.println(pathLength);
		return pathLength;

	}

	public Pair<Set<List<Integer>>, Pair<Integer, Set<Integer>>> returnValidPathWithCriteria(int criteria)
	{

		List<Integer> sourceIndexes = new ArrayList<Integer>();
		List<Integer> targetIndexes = new ArrayList<Integer>();
		List<Integer> copy = new ArrayList<Integer>(this);
//		System.out.println(copy);
		for (int i = 0; i < copy.size(); i++)
		{
			if (this.get(i) == SourceTarget.getFirst())
			{
				sourceIndexes.add(copy.indexOf(copy.get(i)));
				copy.set(i, -1);

			} else if (this.get(i) == SourceTarget.getSecond())
			{
				targetIndexes.add(copy.indexOf(copy.get(i)));
				copy.set(i, -1);
			}
		}
		int pathLength = 0;
		Set<Integer> duplicates = new HashSet<Integer>();
		Set<Integer> resultDuplicates = new HashSet<Integer>();
		Set<List<Integer>> validPathSet = new HashSet<List<Integer>>();
//		System.out.println(this);
		for (int sourceIndex : sourceIndexes)
		{
			resultDuplicates.addAll(duplicates);
			duplicates.clear();
			for (int innerSourceIndex = sourceIndex; innerSourceIndex < this.size() - 1; innerSourceIndex++)
			{
				duplicates.add(SourceTarget.getFirst());

				if (G.isDirectNeighbor(this.get(innerSourceIndex), this.get(innerSourceIndex + 1)))
				{
					if (duplicates.add(this.get(innerSourceIndex + 1)))
					{
						pathLength++;
						if (this.get(innerSourceIndex + 1) == SourceTarget.getSecond())
						{
							validPathSet.add(this.subList(sourceIndex, innerSourceIndex + 2));
							break;

						}
					}
				} else
				{
					break;
				}
			}

			resultDuplicates.addAll(duplicates);
//			duplicates.clear();
//			for (int innerSourceIndex = sourceIndex; innerSourceIndex > 1; innerSourceIndex--)
//			{
//				duplicates.add(SourceTarget.getFirst());
//
//				if (G.isDirectNeighbor(this.get(innerSourceIndex), this.get(innerSourceIndex - 1)))
//				{
//					if (duplicates.add(this.get(innerSourceIndex - 1)))
//					{
//						pathLength++;
//						if (this.get(innerSourceIndex - 1) == SourceTarget.getSecond())
//						{
//							validPathSet.add(this.subList(innerSourceIndex - 1, sourceIndex + 1));
//							break;
//
//						}
//					}
//				} else
//				{
//
//					break;
//				}
//			}
		}

		for (int targetIndex : targetIndexes)
		{
			pathLength = 0;
			resultDuplicates.addAll(duplicates);
			duplicates.clear();
			for (int innerTargetIndex = targetIndex; innerTargetIndex < this.size() - 1; innerTargetIndex++)
			{
				duplicates.add(SourceTarget.getSecond());

				if (G.isDirectNeighbor(this.get(innerTargetIndex), this.get(innerTargetIndex + 1)))
				{
					if (duplicates.add(this.get(innerTargetIndex + 1)))
					{
						pathLength++;
						if (this.get(innerTargetIndex + 1) == SourceTarget.getFirst())
						{
							validPathSet.add(this.subList(targetIndex, innerTargetIndex + 2));
							break;
						}
					}
				} else
				{
					break;
				}
			}
			resultDuplicates.addAll(duplicates);
//			duplicates.clear();
//			for (int innerTargetIndex = targetIndex; innerTargetIndex > 1; innerTargetIndex--)
//			{
//				duplicates.add(SourceTarget.getSecond());
//
//				if (G.isDirectNeighbor(this.get(innerTargetIndex), this.get(innerTargetIndex - 1)))
//				{
//					if (duplicates.add(this.get(innerTargetIndex - 1)))
//					{
//						pathLength++;
//						if (this.get(innerTargetIndex - 1) == SourceTarget.getFirst())
//						{
//							validPathSet.add(this.subList(innerTargetIndex - 1, targetIndex + 1));
//							break;
//
//						}
//					}
//				} else
//				{
//					break;
//				}
//			}
		}

//		suitablePath = validPathSet;
		Pair<Set<List<Integer>>, Pair<Integer, Set<Integer>>> resultPair = new Pair<Set<List<Integer>>, Pair<Integer, Set<Integer>>>(
				validPathSet, new Pair<Integer, Set<Integer>>(pathLength, resultDuplicates));
		return resultPair;
	}

	public List<List<Integer>> validPath()
	{

		Set<Integer> duplicates = new HashSet<Integer>();
		Set<Integer> resultDuplicates = new HashSet<Integer>();
		List<List<Integer>> validPath = new ArrayList<List<Integer>>();

		Pair<List<Integer>, List<Integer>> stPairList = sourceTargetCounter();
		List<Integer> sourceIndexList = stPairList.getFirst();

		for (int sourceIndex : sourceIndexList)
		{
			duplicates.clear();
			duplicates.add(SourceTarget.getFirst());
//			System.out.println(this);
			for (int innerSourceIndex = sourceIndex; innerSourceIndex < this.size() - 1; innerSourceIndex++)
			{
				int nextNode = this.get(innerSourceIndex + 1);
				int currentNode = this.get(innerSourceIndex);
//				System.out.println("current node : " + currentNode + ", next node: " + nextNode);
				if (duplicates.add(nextNode))
				{
					if (G.isDirectNeighbor(currentNode, nextNode))
					{
						if (nextNode == SourceTarget.getSecond())
						{
							validPath.add(this.subList(sourceIndex, innerSourceIndex + 2));
							break;
						}
					} else if (innerSourceIndex - sourceIndex >= 1)
					{
						if (nextNode == SourceTarget.getSecond())
						{
							validPath.clear();
							break;
						}
						validPath.add(this.subList(sourceIndex, innerSourceIndex + 1));
						break;

					} else
					{
						break;
					}
				} else
				{
					validPath.clear();
					break;
				}
//				if (G.isDirectNeighbor(this.get(innerSourceIndex), this.get(innerSourceIndex + 1)))
//				{
//
//					if (duplicates.add(this.get(innerSourceIndex + 1)))
//					{
//
//						if (this.get(innerSourceIndex + 1) == SourceTarget.getSecond())
//						{
//							validPath.add(this.subList(sourceIndex, innerSourceIndex + 2));
//							break;
//						}
//					} else
//					{
//						break;
//					}
//				} else
//				{
//					/*
//					 * maybe plus 2
//					 */
//					validPath.add(this.subList(sourceIndex, innerSourceIndex + 2));
//					break;
//				}

			}
		}
		this.suitablePath = validPath;
		return validPath;

	}

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
		return validLinks;
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

	public double computeDiversity(List<List<Integer>> paths)
	{
		Set result = new HashSet<Integer>(this);
		for (List<Integer> list : paths)
		{
			result.removeAll(list);
		}

		result.remove(this.SourceTarget.getFirst());
		result.remove(this.SourceTarget.getSecond());
		return result.size();
	}

	public Pair<List<Integer>, List<Integer>> sourceTargetCounter()
	{
		List<Integer> sourceIndexes = new ArrayList<Integer>();
		List<Integer> targetIndexes = new ArrayList<Integer>();
		List<Integer> copy = new ArrayList<Integer>(this);
		for (int i = 0; i < copy.size(); i++)
		{
			if (this.get(i) == SourceTarget.getFirst())
			{
				sourceIndexes.add(copy.indexOf(copy.get(i)));
				copy.set(i, -1);

			} else if (this.get(i) == SourceTarget.getSecond())
			{
				targetIndexes.add(copy.indexOf(copy.get(i)));
				copy.set(i, -1);
			}
		}
		Pair<List<Integer>, List<Integer>> result = new Pair<List<Integer>, List<Integer>>(sourceIndexes,
				targetIndexes);
		return result;
	}

	public double preferedSourceDestPositions(int numPairs)
	{
		int genomeSize = this.size() - numPairs;
		int range = genomeSize / numPairs;
		int[] result = new int[numPairs * 2];
		result[0] = 0;
		for (int i = 1; i < result.length; i++)
		{
			if (i % 2 == 0)
			{
				result[i] = result[i - 1] + 1;
			} else
			{
				result[i] = result[i - 1] + range;
			}
		}

		double rank = 0;
		for (int i = 0; i < result.length; i++)
		{
			if (i % 2 == 0)
			{
				if (this.get(result[i]) == this.SourceTarget.getFirst())
				{
					rank++;
				}
			} else
			{
				if (this.get(result[i]) == this.SourceTarget.getSecond())
				{
					rank++;
				}
			}
		}
		return 1 - (rank / (double) (numPairs * 2));
	}

}
