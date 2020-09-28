package flow;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.MANETGraph;

public class PathComposition extends ArrayList<Integer>
{
	MANETGraph G;
	List<Pair<Integer, Integer>> SourceTarget;
	int PathSeperator;

	public PathComposition(List<Integer> list)
	{
		super(list);
	}

	public PathComposition(List<Integer> list, MANETGraph g, List<Pair<Integer, Integer>> sourceTargetPairs,
			int pathSeperator)
	{
		this(list);
		this.G = g;
		this.PathSeperator = pathSeperator;
		this.SourceTarget = sourceTargetPairs;
	}

	public int pathLengths()
	{
		return this.size();
	}

	public boolean hasValidPaths()
	{
		for (int nodeId : this)
		{
			if (nodeId != PathSeperator)
			{
				int successorId = this.get(this.indexOf(nodeId) + 1);
				if (successorId != PathSeperator)
				{
					if (!G.isDirectNeighbor(nodeId, successorId))
					{
						return false;
					}
				}
			}
		}
		return true;

	}

}
