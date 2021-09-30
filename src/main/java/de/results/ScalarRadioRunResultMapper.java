package de.results;

import com.opencsv.bean.ColumnPositionMappingStrategy;

import de.jgraphlib.graph.elements.Position2D;
import de.manetmodel.mobilitymodel.MobilityModel;
import de.manetmodel.mobilitymodel.MovementPattern;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioModel;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.RunResultParameter;
import de.manetmodel.scenarios.Scenario;
import de.manetmodel.units.Watt;

public class ScalarRadioRunResultMapper
		extends RunResultMapper<RunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> {

	MobilityModel mobilityModel;

	public ScalarRadioRunResultMapper(ColumnPositionMappingStrategy<RunResultParameter> mappingStrategy,
			Scenario scenario, MobilityModel mobilityModel) {
		super(scenario, mappingStrategy);
		this.mobilityModel = mobilityModel;
	}

	private int getLinkStability(ScalarRadioNode source, ScalarRadioNode sink, ScalarRadioLink link) {

		MovementPattern nodeOneMobilityPattern = source.getPreviousMobilityPattern();
		MovementPattern nodeTwoMobilityPattern = sink.getPreviousMobilityPattern();
		double currentDistance = nodeDistance(nodeOneMobilityPattern.getPostion(), nodeTwoMobilityPattern.getPostion());
		Watt requiredReceptionPower = sink.getReceptionThreshold();

		Watt currentReceptionPower = link.getReceptionPower();

		int stabilityIterator = 0;

		while (currentReceptionPower.get() >= requiredReceptionPower.get()) {

			MovementPattern newNodeOneMobilityPattern = mobilityModel
					.computeNextMovementPattern(nodeOneMobilityPattern);

			MovementPattern newNodeTwoMobilityPattern = mobilityModel
					.computeNextMovementPattern(nodeTwoMobilityPattern);

			currentDistance = nodeDistance(newNodeOneMobilityPattern.getPostion(),
					newNodeTwoMobilityPattern.getPostion());

			currentReceptionPower = ScalarRadioModel.Propagation.receptionPower(currentDistance,
					source.getTransmissionPower(), source.getCarrierFrequency());

			nodeOneMobilityPattern = newNodeOneMobilityPattern;
			nodeTwoMobilityPattern = newNodeTwoMobilityPattern;
			stabilityIterator++;
		}
		return (int) (stabilityIterator * mobilityModel.timeStamp.value);
	}

	private double nodeDistance(Position2D nodeOne, Position2D nodeTwo) {
		return Math.sqrt(Math.pow(nodeOne.x() - nodeTwo.x(), 2) + Math.pow(nodeOne.y() - nodeTwo.y(), 2));
	}

	@Override
	public RunResultParameter individualRunResultMapper(ScalarRadioNode source, ScalarRadioNode sink,
			ScalarRadioLink link) {
		RunResultParameter runResultParameter = new RunResultParameter();
		runResultParameter.setlId(link.getID());
		runResultParameter.setN1Id(source.getID());
		runResultParameter.setN2Id(sink.getID());

		long transmissionrate = link.getTransmissionRate().get();
		long utilization = link.getUtilization().get();

		if (link.isActive()) {

			runResultParameter.setPathParticipant(true);

			if (transmissionrate < utilization)
				runResultParameter.setOverUtilization(utilization - transmissionrate);

			runResultParameter.setConnectionStability(getLinkStability(source, sink, link));
		}

		runResultParameter.setUtilization(link.getUtilization().get());

		return runResultParameter;
	}

}
