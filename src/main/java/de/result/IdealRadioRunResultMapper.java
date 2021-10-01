package de.result;

import com.opencsv.bean.ColumnPositionMappingStrategy;

import de.manetmodel.mobilitymodel.MobilityModel;
import de.manetmodel.mobilitymodel.MovementPattern;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.Node;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.RunResultParameter;
import de.manetmodel.scenarios.Scenario;

public class IdealRadioRunResultMapper
		extends RunResultMapper<RunResultParameter, Node, Link<LinkQuality>, LinkQuality> {

	public IdealRadioRunResultMapper(ColumnPositionMappingStrategy<RunResultParameter> mappingStrategy,
			Scenario scenario) {
		super(scenario, mappingStrategy);
	}

	@Override
	public RunResultParameter individualRunResultMapper(Node source, Node sink, Link<LinkQuality> link) {
		RunResultParameter resultParameter = new RunResultParameter();
		resultParameter.setlId(link.getID());
		resultParameter.setN1Id(source.getID());
		resultParameter.setN2Id(sink.getID());

		long transmissionrate = link.getTransmissionRate().get();
		long utilization = link.getUtilization().get();

		if (link.isActive()) {
			resultParameter.setPathParticipant(true);
			if (transmissionrate < utilization)
				resultParameter.setOverUtilization(utilization - transmissionrate);

			MovementPattern nodeOneMobilityPattern = source.getPreviousMobilityPattern();
			MovementPattern nodeTwoMobilityPattern = sink.getPreviousMobilityPattern();
			double linkQuality = link.getWeight().getDistance();
			resultParameter.setConnectionStability(linkQuality);
		}
		resultParameter.setUtilization(utilization);
		return resultParameter;
	}

}
