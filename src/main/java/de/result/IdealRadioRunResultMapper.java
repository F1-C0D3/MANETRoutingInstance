package de.result;

import java.util.List;

import com.opencsv.bean.ColumnPositionMappingStrategy;

import de.manetmodel.mobilitymodel.MobilityModel;
import de.manetmodel.mobilitymodel.MovementPattern;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.Node;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.scenarios.Scenario;
import de.manetmodel.units.Time;

public class IdealRadioRunResultMapper
		extends RunResultMapper<IndividualRunResultParameter,AverageRunResultParameter, Node, Link<LinkQuality>, LinkQuality> {

	public IdealRadioRunResultMapper(ColumnPositionMappingStrategy<IndividualRunResultParameter> individualMmappingStrategy,ColumnPositionMappingStrategy<AverageRunResultParameter> averageMmappingStrategy,
			Scenario scenario) {
		super(scenario, individualMmappingStrategy,averageMmappingStrategy);
	}

	@Override
	public IndividualRunResultParameter individualRunResultMapper(Node source, Node sink, Link<LinkQuality> link) {
		IndividualRunResultParameter resultParameter = new IndividualRunResultParameter();
		resultParameter.setlId(link.getID());
		resultParameter.setN1Id(source.getID());
		resultParameter.setN2Id(sink.getID());

		long transmissionrate = link.getTransmissionRate().get();
		long utilization = link.getUtilization().get();

		if (link.isActive()) {
			resultParameter.setPathParticipant(true);
			if (transmissionrate < utilization)
				resultParameter.setOverUtilization(utilization - transmissionrate);

//			double linkQuality = link.getWeight().getDistance();
//			resultParameter.setConnectionStability(linkQuality);
		}
		resultParameter.setUtilization(utilization);
		return resultParameter;
	}

	@Override
	public <F extends Flow<Node, Link<LinkQuality>, LinkQuality>> AverageRunResultParameter averageRunResultMapper(
			List<IndividualRunResultParameter> runParameters, List<F> flows, Time duration, int currentRun) {
		// TODO Auto-generated method stub
		return null;
	}

}
