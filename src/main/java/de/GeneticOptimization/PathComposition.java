package de.GeneticOptimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.geneticManet.GeneticManetGraph;
import de.manetmodel.graph.EdgeDistance;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.DataRate;
import de.manetmodel.util.Tuple;

public class PathComposition {

	GeneticManetGraph manet;
	public List<Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>> flows;

	public PathComposition(GeneticManetGraph manet,
			List<Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>> flows) {
		this.manet = manet;
		this.flows = flows;
	}

	public double getLength() {
		double size = 0;
		for (Flow f : this.flows) {
			size += f.size();
		}
		return size;
	}

	public DataRate computeResidualTransmissionRate() {

		return null;
	}

	/*
	 * Attempt: Using max(Variance) and standard deviation to determine link quality
	 * based on receptionPower
	 */
	public double minLinkReceptionVariance() {
		double receptionPowerMean = 0d;
		int numLinks = 0;
		List<Double> variance = new ArrayList<Double>();

		for (Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> flow : flows) {
			numLinks += flow.size() - 1;

			Iterator<Tuple<Link<EdgeDistance>, Node<EdgeDistance>>> linkIterator = flow.iterator();
			linkIterator.next();
			while (linkIterator.hasNext()) {
				Tuple<Link<EdgeDistance>, Node<EdgeDistance>> linkAndNode = linkIterator.next();
				receptionPowerMean += linkAndNode.getFirst().getReceptionPower();
			}
		}

		receptionPowerMean = receptionPowerMean / numLinks;
		for (

		Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> flow : flows) {

			Iterator<Tuple<Link<EdgeDistance>, Node<EdgeDistance>>> linkIterator = flow.iterator();
			linkIterator.next();
			while (linkIterator.hasNext()) {
				Tuple<Link<EdgeDistance>, Node<EdgeDistance>> linkAndNode = linkIterator.next();
				variance.add(Math.pow((linkAndNode.getFirst().getReceptionPower() - receptionPowerMean), 2));
			}
		}

		return Collections.min(variance);

	}

	public DataRate getManetCapacity() {
		return manet.getCapacity();
	}

	/*
	 * Attempt: Using max(Variance) and standard deviation to determine link quality
	 * based on receptionPower
	 */
	public double meanLinkReception() {
		double receptionPowerMean = 0d;
		int numLinks = 0;

		for (Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> flow : flows) {
			numLinks += flow.size() - 1;

			for (Tuple<Link<EdgeDistance>, Node<EdgeDistance>> linkAndNode : flow) {
				receptionPowerMean += linkAndNode.getFirst().getReceptionPower();
			}
		}

		return receptionPowerMean / numLinks;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		Iterator<Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>> fIterator = flows.iterator();

		while (fIterator.hasNext()) {
			Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> f = fIterator.next();
			buffer.append(f.toString());
		}
		return buffer.toString();
	}

	public DataRate overUtilization() {
		removeUtilization();
		DataRate overUtilization = new DataRate(0L);

		for (Link<EdgeDistance> l : manet.getEdges()) {
			DataRate tRate = l.getTransmissionRate();
			DataRate utilization = l.getUtilization();
			double oU = tRate.get() - utilization.get();
			overUtilization.set(oU < 0 ? overUtilization.get() + (long) Math.abs(oU) : overUtilization.get());
		}

		return overUtilization;
	}

	public DataRate getNetworUtilization() {
		removeUtilization();

		for (Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> flow : flows) {
			manet.addFlow(flow);
		}
		return manet.getUtilization();
	}

	private void removeUtilization() {
		manet.removeFlow(null);

	}
}