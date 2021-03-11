package de.geneticOptimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.DataRate;
import de.network.GeneticMANET;

public class PathComposition {

	GeneticMANET manet;
	public List<Flow<Node, Link<LinkQuality>, LinkQuality>> flows;

	public PathComposition(GeneticMANET manet, List<Flow<Node, Link<LinkQuality>, LinkQuality>> flows) {
		this.manet = manet;
		this.flows = flows;
	}

	public double getLength() {
		double size = 0;
		for (Flow<Node, Link<LinkQuality>, LinkQuality> f : this.flows) {
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

		for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : flows) {
			numLinks += flow.size() - 1;

			Iterator<Tuple<Link<LinkQuality>, Node>> linkIterator = flow.iterator();
			linkIterator.next();
			while (linkIterator.hasNext()) {
				Tuple<Link<LinkQuality>, Node> linkAndNode = linkIterator.next();
				receptionPowerMean += linkAndNode.getFirst().getWeight().getReceptionPower();
			}
		}

		receptionPowerMean = receptionPowerMean / numLinks;
		for (

		Flow<Node, Link<LinkQuality>, LinkQuality> flow : flows) {

			Iterator<Tuple<Link<LinkQuality>, Node>> linkIterator = flow.iterator();
			linkIterator.next();
			while (linkIterator.hasNext()) {
				Tuple<Link<LinkQuality>, Node> linkAndNode = linkIterator.next();
				variance.add(
						Math.pow((linkAndNode.getFirst().getWeight().getReceptionPower() - receptionPowerMean), 2));
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

		for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : flows) {
			numLinks += flow.size() - 1;

			for (Tuple<Link<LinkQuality>, Node> linkAndNode : flow) {
				receptionPowerMean += linkAndNode.getFirst().getWeight().getReceptionPower();
			}
		}

		return receptionPowerMean / numLinks;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		Iterator<Flow<Node, Link<LinkQuality>, LinkQuality>> fIterator = flows.iterator();

		while (fIterator.hasNext()) {
			Flow<Node, Link<LinkQuality>, LinkQuality> f = fIterator.next();
			buffer.append(f.toString());
		}
		return buffer.toString();
	}

	public DataRate overUtilization() {
		DataRate overUtilization = new DataRate(0L);

		for (Link<LinkQuality> l : manet.getEdges()) {
//			System.out.println("v1: " + manet.getVerticesOf(l).getFirst().getID() + ",  v2: "
//					+ manet.getVerticesOf(l).getSecond().getID() + ", u= " + l.getUtilization().toString());
			DataRate tRate = l.getWeight().getTransmissionRate();
			DataRate utilization = l.getWeight().getUtilization();
			double oU = tRate.get() - utilization.get();
			overUtilization.set(oU < 0 ? overUtilization.get() + (long) Math.abs(oU) : overUtilization.get());
		}

		return overUtilization;
	}

	public DataRate getNetworUtilization() {
		return manet.getUtilization();
	}

	public void undeployFlows() {
		for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : flows) {

			manet.undeployFlow(flow);
		}

	}

	public void deployFlows() {
		for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : flows) {
			manet.deployFlow(flow);
		}

	}
}