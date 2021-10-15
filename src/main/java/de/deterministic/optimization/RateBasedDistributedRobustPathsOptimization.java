package de.deterministic.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.deterministic.algorithm.DijkstraShortestPenaltyFlow;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Link;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;

public class RateBasedDistributedRobustPathsOptimization extends DeterministicOptimization<ScalarRadioMANET> {

	private List<List<Integer>> activeCommonLinksByFlows;
	private List<List<Integer>> passiveCommonLinksByFlows;
	private List<List<Double>> rateParameters;
	private List<Integer> countLinkUsage;

	public RateBasedDistributedRobustPathsOptimization(ScalarRadioMANET manet) {
		super(manet);
		initialize();

	}

	private void initialize() {

		activeCommonLinksByFlows = new ArrayList<List<Integer>>(manet.getEdges().size());
		passiveCommonLinksByFlows = new ArrayList<List<Integer>>(manet.getEdges().size());
		rateParameters = new ArrayList<List<Double>>(manet.getEdges().size());

		for (ScalarRadioLink link : manet.getEdges()) {
			activeCommonLinksByFlows.add(new ArrayList<Integer>(manet.getFlows().size()));
			passiveCommonLinksByFlows.add(new ArrayList<Integer>(manet.getFlows().size()));
			rateParameters.add(new ArrayList<Double>(manet.getFlows().size()));
		}

		countLinkUsage = new ArrayList<Integer>();

	}

	@Override
	public ScalarRadioMANET execute() {

		Function<ScalarLinkQuality, Double> robustnessMetric = (linkQality) -> {
			return linkQality.getReceptionConfidence() * 0.6d + linkQality.getRelativeMobility() * 0.2d
					+ linkQality.getSpeedQuality() * 0.2d;
		};

		// Deploy paths based on metric in order to determine active and passive links
		this.deployPathsBasedOnMethric(manet.getFlowIDs(), robustnessMetric);

		// Determine list of common used links
		setSharedActiveLinks();
		setSharedPassiveLinks();

		for (ScalarRadioLink link : manet.getEdges()) {

			long residualTransmissionRate = link.getUtilization().get();

			for (ScalarRadioFlow flow : manet.getFlows()) {

				if (activeCommonLinksByFlows.get(link.getID()).isEmpty()&& passiveCommonLinksByFlows.get(link.getID()).isEmpty()) {

					rateParameters.get(link.getID()).add(flow.getID(), 1d);

				} else {
					rateParameters.get(link.getID())
							.add((flow.getDataRate().get() / (double) residualTransmissionRate));
				}

			}

			countLinkUsage.add(1);
		}

		manet.undeployFlows();

		for (ScalarRadioFlow flow : manet.getFlows()) {
			flow.clear();
		}

		// Metric to determine overlapping links
		Function<Tuple<ScalarRadioFlow, ScalarRadioLink>, Double> metric = (flowAndlinkQality) -> {

			ScalarRadioFlow currentFlow = flowAndlinkQality.getFirst();
			ScalarRadioLink currentLink = flowAndlinkQality.getSecond();

			long residualTransmissionRate = currentLink.getTransmissionRate().get()
					- currentLink.getUtilization().get();

			double rateParameter = rateParameters.get(currentLink.getID()).get(currentFlow.getID());
			
			if(rateParameter==1) {
				ScalarLinkQuality weight = currentLink.getWeight();
				rateParameter += 1-((weight.getReceptionConfidence()*0.6)+(weight.getRelativeMobility()*0.2)+(weight.getSpeedQuality()*0.2));
				
				}

			double result = countLinkUsage.get(currentLink.getID()) / (rateParameter * residualTransmissionRate);

			return result;
		};
		
		DijkstraShortestPenaltyFlow spf = new DijkstraShortestPenaltyFlow(manet);

		for (int i = 0; i< manet.getFlows().size();i++) {

			ScalarRadioFlow flow = manet.getFlow(i);
			flow = spf.compute(flow, metric);

			if (flow != null) {

				manet.deployFlow(flow);

				for (ScalarRadioLink l : flow.getEdges()) {
					countLinkUsage.set(l.getID(), countLinkUsage.get(l.getID()) + 1);
				}
			}

		}

		return manet;
	}

	private void setSharedActiveLinks() {

		for (ScalarRadioFlow flow : manet.getFlows()) {

			for (ScalarRadioLink activeLink : flow.getEdges()) {

				activeCommonLinksByFlows.get(activeLink.getID()).add(flow.getID());

			}
		}

	}

	private void setSharedPassiveLinks() {

		for (ScalarRadioFlow flow : manet.getFlows()) {

			for (ScalarRadioLink activeLink : flow.getEdges()) {

				for (ScalarRadioLink passiveLink : manet.getPassiveUtilizedLinksOf(activeLink)) {

					passiveCommonLinksByFlows.get(passiveLink.getID()).add(flow.getID());

				}
			}
		}

	}
}
