package program;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.alg.util.Pair;

import de.manet.graph.MANETGraph;

public class PathComposition extends ArrayList<List<Integer>>
{
	MANETGraph G;
	List<Pair<Integer, Integer>> SourceTarget;
	int PathSeperator;
	private double EntireMANETTransmissionCapacity;

	public PathComposition(List<List<Integer>> paths)
	{
		super(paths);
	}

	public PathComposition(List<List<Integer>> paths, MANETGraph g, List<Pair<Integer, Integer>> sourceTargetPairs,
			int pathSeperator)
	{
		this(paths);
		this.G = g;
		this.PathSeperator = pathSeperator;
		this.SourceTarget = sourceTargetPairs;
		this.EntireMANETTransmissionCapacity = G.computeEntireTransmissionCapacity();

	}

	public int pathLengths()
	{
		int length = 0;
		for (List<Integer> list : this)
		{
			length += list.size();
		}
		return length;
	}

	public double computeResidualTransmissionRate()
	{
		double linkUtilizations = G.edgeSet().parallelStream().mapToDouble(l -> l.getTransmissionCapacity()).sum();

		List<Double> nodeBitrates = G.vertexSet().stream().map(v -> v.getBitrate()).collect(Collectors.toList());

		return 0.0;
	}
}