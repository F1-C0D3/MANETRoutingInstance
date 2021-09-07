package de.app;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.deterministic.algorithm.DijkstraShortesFlow;
import de.jgraphlib.util.RandomNumbers;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.scenarios.Scenario;
import de.manetmodel.units.DataRate;

public class HighUtilizedMANETSecenario extends Scenario {

	private enum DataType {
		HD_Video, SD_Video, VoIP
	}

	private List<Tuple<DataType, Long>> flowCharacteristics;

	public HighUtilizedMANETSecenario(String individualName, int numFlows, int numNodes, int runs) {
		super(individualName, numFlows, numNodes, runs);
		this.flowCharacteristics = new ArrayList<Tuple<DataType, Long>>();
	}

	protected void generateSourceTargetPairs(int runs) {
		List<Integer> exclusionList = new ArrayList<Integer>();

		for (int i = 0; i < (numFlows * 2); i++) {
			int randomNodeId = RandomNumbers.getInstance(runs).getRandomNotInE(0, numNodes, exclusionList);
			exclusionList.add(randomNodeId);
		}

		Tuple<Integer, Integer> stTriple = null;
		for (int i = 0; i < exclusionList.size(); i++) {

			if (i % 2 == 0) {
				stTriple = new Tuple<Integer, Integer>();
				stTriple.setFirst(exclusionList.get(i));
			} else {
				stTriple.setSecond(exclusionList.get(i));
				flowSourceTargetPairs.add(stTriple);
			}
		}

	}

	Function<ScalarLinkQuality, Double> metric = (linkQuality) -> {
		System.out.println(linkQuality.getScore());
		return linkQuality.getScore();

	};

	public List<ScalarRadioFlow> generatePaths(ScalarRadioMANET manet, int runs, DataRate step) {

		DijkstraShortesFlow sp = new DijkstraShortesFlow(manet);

		int index = 0;

		for (Tuple<Integer, Integer> tuple : this.flowSourceTargetPairs) {

			double random = RandomNumbers.getInstance(runs).getRandom(0d, 1d);
			if (random >= 0 && random < 0.2) {
				flowCharacteristics.add(new Tuple<HighUtilizedMANETSecenario.DataType, Long>(DataType.HD_Video, null));

			} else if (random >= 0.2 && random < 0.55) {
				flowCharacteristics.add(new Tuple<HighUtilizedMANETSecenario.DataType, Long>(DataType.SD_Video, null));

			} else if (random >= 0.55 && random <= 1d) {
				flowCharacteristics.add(new Tuple<HighUtilizedMANETSecenario.DataType, Long>(DataType.VoIP, null));

			}

			manet.addFlow(manet.getVertex(tuple.getFirst()), manet.getVertex(tuple.getSecond()), new DataRate());
		}

		initializeDataRateRelations(step);

		for (ScalarRadioFlow flow : manet.getFlows()) {

			sp.compute(flow, metric);
		}

		while (true) {

			if (manet.getOverUtilization().get() != 0L) {
				System.out.println(manet.getOverUtilization().toString());
				manet.undeployFlows();
				break;
			}
			manet.undeployFlows();

			for (ScalarRadioFlow f : manet.getFlows()) {

				Long rate = flowCharacteristics.get(f.getID()).getSecond();

				f.setDataRate(new DataRate(f.getDataRate().get() + rate));

				manet.deployFlow(f);

				if (manet.getOverUtilization().get() > 0)
					f.setDataRate(new DataRate(f.getDataRate().get() - rate));

				manet.undeployFlow(f);

			}

			for (ScalarRadioFlow f : manet.getFlows()) {
				manet.deployFlow(f);
			}

		}

		for (ScalarRadioFlow f : manet.getFlows()) {
			f.clear();
			System.out.println(f.toString());
		}
		return manet.getFlows();
	}

	private void initializeDataRateRelations(DataRate step) {

		int numFlowTypeOne = 0;
		int numFlowTypeTwo = 0;
		int numFlowTypeThree = 0;

		for (Tuple<DataType, Long> tuple : flowCharacteristics) {

			DataType dataType = tuple.getFirst();

			switch (dataType) {

			case HD_Video:
				numFlowTypeOne++;
				break;
			case SD_Video:
				numFlowTypeTwo++;
				break;
			case VoIP:
				numFlowTypeThree++;
				break;
			default:
				break;
			}
		}

		for (Tuple<DataType, Long> tuple : flowCharacteristics) {

			DataType dataType = tuple.getFirst();
			long amount = 0;

			switch (dataType) {

			case HD_Video:

				amount = (long) (step.get() * 0.6);
				tuple.setSecond(amount / numFlowTypeOne);

				break;
			case SD_Video:

				amount = (long) (step.get() * 0.3);
				tuple.setSecond(amount / numFlowTypeTwo);

				break;
			case VoIP:

				amount = (long) (step.get() * 0.1);
				tuple.setSecond(amount / numFlowTypeThree);

				break;
			default:
				break;
			}

		}
	}

//	private void adaptDataRate(List<Flow<Node, Link<LinkQuality>, LinkQuality>> flows, DataRate step) {
//
//		int numFlowTypeOne = 0;
//		int numFlowTypeTwo = 0;
//		int numFlowTypeThree = 0;
//
//		for (Triple<Integer, Integer, Integer> triple : flowSourceTargetPairs) {
//			int flowType = triple.getThird();
//
//			switch (flowType) {
//
//			case 1:
//				numFlowTypeOne++;
//				break;
//			case 2:
//				numFlowTypeTwo++;
//				break;
//			case 3:
//				numFlowTypeThree++;
//				break;
//			default:
//				break;
//			}
//		}
//		int index = 0;
//		for (Triple<Integer, Integer, Integer> triple : flowSourceTargetPairs) {
//
//			int flowType = triple.getThird();
//			long amount = 0;
//
//			switch (flowType) {
//
//			case 1:
//				amount = (long) (step.get() * 0.6);
//				amount = amount / numFlowTypeThree;
//				break;
//			case 2:
//				amount = (long) (step.get() * 0.3);
//				amount = amount / numFlowTypeThree;
//
//				break;
//			case 3:
//				amount = (long) (step.get() * 0.1);
//				amount = amount / numFlowTypeThree;
//				break;
//			default:
//				break;
//			}
//
//			DataRate currentDataRate = flows.get(index).getDataRate();
//			flows.get(index).setDataRate(new DataRate(amount + currentDataRate.get()));
//			index++;
//		}
//
//	}

}
