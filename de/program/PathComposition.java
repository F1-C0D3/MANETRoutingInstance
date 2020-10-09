package program;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.manet.graph.Flow;
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
		MANETGraph copy = G.returnMANETGraphCopy();
		for (List<Integer> path : this)
		{

			/*
			 * Gather flow parameters
			 */
			int sourceId = path.get(0);
			int targetId = path.get(path.size() - 1);
			String flowId = Helper.getFlowId(sourceId, targetId);
			Flow flow = Flows.stream().filter(f -> f.getId().equals(flowId)).findFirst().get();
			double flowTransmissionRate = flow.getTransmissionRate();

			for (int nodeId : path)
			{
				System.out.println(path);
				int interferenceFactor = Helper.getInterferenceFactor(nodeId, path);
				double linkUtilization = flowTransmissionRate * (interferenceFactor + 1);
				Node node = copy.vertexSet().stream().filter(v -> v.getId() == nodeId).findFirst().get();
				node.setIsPathPartisipant(true);
				copy.outgoingEdgesOf(node).forEach(l -> l.increaseUtilizationBy(linkUtilization));
			}

		}

		for (Node node : copy.vertexSet())
		{
			int numPathParticipants = 0;
			Set<Node> twoHopNeighbors = new HashSet<Node>();
			Set<Node> oneHopNeighbors = copy.outgoingEdgesOf(node).stream().map(l -> copy.getEdgeTarget(l))
					.collect(Collectors.toSet());

			twoHopNeighbors.addAll(oneHopNeighbors);
			for (Node n : oneHopNeighbors)
			{
				twoHopNeighbors.addAll(
						copy.outgoingEdgesOf(n).stream().map(l -> copy.getEdgeTarget(l)).collect(Collectors.toSet()));
			}
			for (Node potentialPathParticipant : twoHopNeighbors)
			{
				if (potentialPathParticipant.getIsPathPartisipant())
				{
					double linkUtilization = copy.outgoingEdgesOf(potentialPathParticipant).stream().findAny().get()
							.getUtilization();
					copy.outgoingEdgesOf(node).forEach(l -> l.increaseUtilizationBy(linkUtilization));
				} else
				{
					System.out.println(potentialPathParticipant);
				}
			}
		}

		return 0.0;
	}
}