package program;

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
		List<List<Integer>> paths = extractPaths();
		int length = 0;
		for (List<Integer> list : paths)
		{
			length += list.size();

		}
		return length;
	}

	public List<List<Integer>> extractPaths()
	{
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		int startIndex = 0;
		int indexOfPathSeperator = this.indexOf(PathSeperator);

		while (indexOfPathSeperator != -1)
		{
			indexOfPathSeperator += startIndex;
			result.add(this.subList(startIndex, indexOfPathSeperator + 1));
			startIndex = indexOfPathSeperator + 1;
			indexOfPathSeperator = this.subList(startIndex, this.size()).indexOf(PathSeperator);
		}

		return result;
	}

	public double computeResidualTransmissionRate()
	{
		return 0.0;
	}
}