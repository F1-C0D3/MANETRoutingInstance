package program;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.manet.graph.Flow;
import de.manet.graph.Link;
import de.manet.graph.MANETGraph;
import de.manet.graph.Node;
import de.manet.util.Helper;

public class PathComposition extends ArrayList<List<Integer>>
{
	MANETGraph G;
	List<Flow> Flows;

	public PathComposition(List<List<Integer>> paths)
	{
		super(paths);
	}

	public PathComposition(List<List<Integer>> paths, MANETGraph g, List<Flow> flows)
	{
		this(paths);
		this.G = g;
		this.Flows = flows;

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
		double linkUtilizations = G.get_EntireUtilizationCapacity();

		for (List<Integer> path : this)
		{
			/*
			 * Gather flow parameters
			 */
			int sourceId = path.get(0);
			int targetId = path.get(path.size() - 1);
			String flowId = Helper.getFlowId(sourceId, targetId);
			Flow flow = Flows.stream().filter(f -> f.getId().equals(flowId)).findFirst().get();

			/*
			 * Load affected networksegment
			 */
			Set<Integer> oneHopNeighborNetworkSegment = new HashSet<Integer>();
			Set<Set<Integer>> oneHopNeighborsSets = path.stream().map(p -> G.getOutgoingNeighbors(p))
					.collect(Collectors.toSet());

			for (Set<Integer> neighborIds : oneHopNeighborsSets)
			{
				oneHopNeighborNetworkSegment.addAll(neighborIds);
			}

			Set<Link> segmentLinks = null;
			for (Integer segmentNode : oneHopNeighborNetworkSegment)
			{
				Node n = G.vertexSet().stream().filter(v -> v.getId() == segmentNode).findFirst().get();
				segmentLinks.addAll(G.outgoingEdgesOf(n));
				segmentLinks.addAll(G.incomingEdgesOf(n));
			}

			/*
			 * Adapt/decrease transmission capacityter
			 */
			for (Link segmentLink : segmentLinks)
			{
				segmentLink.setTransmissionCapacity(segmentLink.getTransmissionCapacity() - flow.getTransmissionRate());
			}
		}
		return 0.0;
	}
}