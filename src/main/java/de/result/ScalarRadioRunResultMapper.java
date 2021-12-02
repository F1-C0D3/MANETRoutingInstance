package de.result;

import java.util.List;
import java.util.stream.Collectors;

import com.opencsv.bean.ColumnPositionMappingStrategy;

import de.jgraphlib.graph.elements.Position2D;
import de.manetmodel.mobilitymodel.MobilityModel;
import de.manetmodel.mobilitymodel.MovementPattern;
import de.manetmodel.network.Flow;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioModel;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.scenarios.Scenario;
import de.manetmodel.units.Time;
import de.manetmodel.units.Watt;

public class ScalarRadioRunResultMapper extends
		RunResultMapper<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> {

	MobilityModel mobilityModel;

	public ScalarRadioRunResultMapper(
			ColumnPositionMappingStrategy<IndividualRunResultParameter> individualMappingStrategy,
			ColumnPositionMappingStrategy<AverageRunResultParameter> averageMappingStrategy, Scenario scenario,
			MobilityModel mobilityModel) {
		super(scenario, individualMappingStrategy, averageMappingStrategy);
		this.mobilityModel = mobilityModel;
	}

	private double getLinkStability(ScalarRadioNode source, ScalarRadioNode sink, ScalarRadioLink link) {

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
		return new Time(stabilityIterator * mobilityModel.getTickDuration().value).getSeconds();
	}

	private double nodeDistance(Position2D nodeOne, Position2D nodeTwo) {
		return Math.sqrt(Math.pow(nodeOne.x() - nodeTwo.x(), 2) + Math.pow(nodeOne.y() - nodeTwo.y(), 2));
	}

	@Override
	public IndividualRunResultParameter individualRunResultMapper(ScalarRadioNode source, ScalarRadioNode sink,
			ScalarRadioLink link) {
		IndividualRunResultParameter runResultParameter = new IndividualRunResultParameter();
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
			ScalarLinkQuality linkQuality = link.getWeight();
			runResultParameter.setLinkQuality((linkQuality.getReceptionConfidence() * 0.6)
					+ (linkQuality.getSpeedQuality() * 0.3) + (linkQuality.getRelativeMobility() * 0.1));
		}

		runResultParameter.setUtilization(link.getUtilization().get());

		return runResultParameter;
	}

	@Override
	public <F extends Flow<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>> AverageRunResultParameter averageRunResultMapper(
			List<IndividualRunResultParameter> runParameters, List<F> flows, Time duration, int currentRun) {

		AverageRunResultParameter averageRunParemeter = new AverageRunResultParameter();

		long overUtilization = 0l;
		long averageUtilization = 0l;
		double linkQuality = 0d;
		double activeLinks = 0d;
		double averageConnectivityStability = 0d;
		double minConnectionStability = Double.MAX_VALUE;
		double maxConnectionStability = 0d;

		for (IndividualRunResultParameter runParameter : runParameters) {

			if (runParameter.isPathParticipant()) {

				if (runParameter.getOverUtilization() != 0l) {
					overUtilization += runParameter.getOverUtilization();
				}

				if (runParameter.getConnectionStability() != 0d) 
					averageConnectivityStability
							+=runParameter.getConnectionStability();

				if (minConnectionStability > runParameter.getConnectionStability())
					minConnectionStability=runParameter.getConnectionStability();

				if (maxConnectionStability < runParameter.getConnectionStability())
					maxConnectionStability=runParameter.getConnectionStability();
				
				linkQuality +=runParameter.getLinkQuality();
				activeLinks++;
			}

			if (runParameter.getUtilization() != 0l) {
				averageUtilization += runParameter.getUtilization();
			}
			

		}
		averageRunParemeter.setOverUtilization(overUtilization);
		averageRunParemeter.setUtilization(averageUtilization);
		averageRunParemeter
				.setMeanConnectionStability(averageConnectivityStability / activeLinks);
		averageRunParemeter.setMinConnectionStability(minConnectionStability);
		averageRunParemeter.setMaxConnectionStability(maxConnectionStability);
		averageRunParemeter.setActivePathParticipants(activeLinks);
		averageRunParemeter.setLinkQuality(linkQuality/(double)activeLinks);
		averageRunParemeter.setSimulationTime(duration.getSeconds());
		averageRunParemeter.setRunNumber(currentRun);

		averageRunParemeter.setNumberOfUndeployedFlows(
				flows.stream().filter(f -> !f.isComplete()).collect(Collectors.toList()).size());

		return averageRunParemeter;
	}

}
