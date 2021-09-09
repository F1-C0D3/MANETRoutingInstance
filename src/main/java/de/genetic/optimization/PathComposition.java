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

	private ScalarRadioMANET manet;
	private List<ScalarRadioFlow> flows;

	public PathComposition(ScalarRadioMANET manet, List<ScalarRadioFlow> flows) {
		this.manet = manet;
		this.flows = flows;
	}

	public ScalarRadioMANET getManet() {
		return manet;
	}

	public void setManet(ScalarRadioMANET manet) {
		this.manet = manet;
	}

	public List<ScalarRadioFlow> getFlows() {
		return flows;
	}

	public void setFlows(List<ScalarRadioFlow> flows) {
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

	public double getDistance() {
		double distance = 0d;
		int numLinks = 0;

		for (ScalarRadioFlow flow : flows) {
			numLinks += flow.size() - 1;
			for (ScalarRadioLink link : flow.getEdges()) {
				distance += link.getWeight().getDistance();
			}
		}
		
		return 1-((distance)/(100d*numLinks));
	}
	
	public double getNumLinks() {
		int numLinks = 0;

		for (ScalarRadioFlow flow : flows) {
			numLinks += flow.size() - 1;
		}
		
		return numLinks;
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
				variance.add(Math.pow((linkAndNode.getFirst().getReceptionPower().get() - receptionPowerMean), 2));
			}
		}

		return Collections.min(variance);

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

			for (ScalarRadioLink link : flow.getEdges()) {
				receptionPowerMean += link.getWeight().getReceptionQuality();
			}
		}

		return receptionPowerMean / numLinks;
	}

	public double meanMobilityQuality() {
		double mobilityQualityMean = 0d;
		int numLinks = 0;

		for (ScalarRadioFlow flow : flows) {
			numLinks += flow.size() - 1;
			for (ScalarRadioLink link : flow.getEdges()) {
				mobilityQualityMean += link.getWeight().getMobilityQuality();
			}
		}
		return mobilityQualityMean / numLinks;
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

}