package de.genetic.optimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.jgraphlib.util.Tuple;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.units.DataRate;

public class PathComposition {

	ScalarRadioMANET manet;
	public List<ScalarRadioFlow> flows;

	public PathComposition(ScalarRadioMANET manet, List<ScalarRadioFlow> flows) {
		this.manet = manet;
		this.flows = flows;
	}

	public double getLength() {
		double size = 0;
		for (ScalarRadioFlow f : this.flows) {
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

		for (ScalarRadioFlow flow : flows) {
			numLinks += flow.size() - 1;

			Iterator<Tuple<ScalarRadioLink, ScalarRadioNode>> linkIterator = flow.iterator();
			linkIterator.next();
			while (linkIterator.hasNext()) {
				Tuple<ScalarRadioLink, ScalarRadioNode> linkAndNode = linkIterator.next();
				receptionPowerMean += linkAndNode.getFirst().getReceptionPower().get();
			}
		}

		receptionPowerMean = receptionPowerMean / numLinks;
		for (

				ScalarRadioFlow flow : flows) {

			Iterator<Tuple<ScalarRadioLink, ScalarRadioNode>> linkIterator = flow.iterator();
			linkIterator.next();
			while (linkIterator.hasNext()) {
				Tuple<ScalarRadioLink, ScalarRadioNode> linkAndNode = linkIterator.next();
				variance.add(
						Math.pow((linkAndNode.getFirst().getReceptionPower().get() - receptionPowerMean), 2));
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

		for (ScalarRadioFlow flow : flows) {
			numLinks += flow.size() - 1;

			for (Tuple<ScalarRadioLink, ScalarRadioNode> linkAndNode : flow) {
				receptionPowerMean += linkAndNode.getFirst().getReceptionPower().get();
			}
		}

		return receptionPowerMean / numLinks;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		Iterator<ScalarRadioFlow> fIterator = flows.iterator();

		while (fIterator.hasNext()) {
			ScalarRadioFlow f = fIterator.next();
			buffer.append(f.toString());
		}
		return buffer.toString();
	}

	public DataRate overUtilization() {
		DataRate overUtilization = new DataRate(0L);

		for (ScalarRadioLink l : manet.getEdges()) {
//			System.out.println("v1: " + manet.getVerticesOf(l).getFirst().getID() + ",  v2: "
//					+ manet.getVerticesOf(l).getSecond().getID() + ", u= " + l.getUtilization().toString());
			DataRate tRate = l.getTransmissionRate();
			DataRate utilization = l.getUtilization();
			double oU = tRate.get() - utilization.get();
			overUtilization.set(oU < 0 ? overUtilization.get() + (long) Math.abs(oU) : overUtilization.get());
		}

		return overUtilization;
	}

	public DataRate getNetworUtilization() {
		return manet.getUtilization();
	}

	public void undeployFlows() {
		for (ScalarRadioFlow flow : flows) {

			manet.undeployFlow(flow);
		}

	}

	public void deployFlows() {
		for (ScalarRadioFlow flow : flows) {
			manet.deployFlow(flow);
		}

	}
}