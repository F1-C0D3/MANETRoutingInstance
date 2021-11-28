package de.result;

import java.util.List;

import com.opencsv.bean.ColumnPositionMappingStrategy;

import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.results.ResultParameter;
import de.manetmodel.results.RunResultContent;
import de.manetmodel.results.TotalResultMapper;
import de.manetmodel.results.TotalResultParameter;
import de.manetmodel.scenarios.Scenario;
import de.manetmodel.units.Time;

public class ScalarRadioTotalResultMapper
		extends TotalResultMapper<TotalResultParameter, IndividualRunResultParameter, AverageRunResultParameter> {

	public ScalarRadioTotalResultMapper(Scenario scenario,
			ColumnPositionMappingStrategy<TotalResultParameter> totalMappingStrategy) {
		super(scenario, totalMappingStrategy);
	}

	@Override
	public TotalResultParameter toalMapper(
			List<RunResultContent<IndividualRunResultParameter, AverageRunResultParameter>> runs) {
		TotalResultParameter totalResultParemeter = new TotalResultParameter();

		long meanOverUtilization = 0l;
		long meanUtilization = 0l;
		double activePathParticipants = 0d;
		double totalmeanLinkQuality = 0d;
		double meanConnectionStability = 0d;
		double minConnectionStability = 0d;
		double maxConnectionStability = 0d;
		double meanNumberOfUndeployedFlows = 0d;
		double meanAveragesimulationTime = 0d;
		double minAveragesimulationTime = Double.MAX_VALUE;
		double maxAveragesimulationTime = 0d;

		double actualRuns = 0;
		for (RunResultContent<IndividualRunResultParameter, AverageRunResultParameter> run : runs) {

			AverageRunResultParameter averageResultContent = run.getAverageResultContent();

			actualRuns++;
			totalmeanLinkQuality += averageResultContent.getLinkQuality();
			meanOverUtilization += averageResultContent.getOverUtilization();
			meanUtilization += averageResultContent.getUtilization();
			activePathParticipants += averageResultContent.getActivePathParticipants();
			meanConnectionStability+= averageResultContent.getMeanConnectionStability();;
			minConnectionStability+=averageResultContent.getMinConnectionStability();
			maxConnectionStability+=averageResultContent.getMaxConnectionStability();

			meanNumberOfUndeployedFlows += averageResultContent.getNumberOfUndeployedFlows();
			meanAveragesimulationTime +=averageResultContent.getSimulationTime();;

			if (minAveragesimulationTime > averageResultContent.getSimulationTime())
				minAveragesimulationTime=averageResultContent.getSimulationTime();

			if (maxAveragesimulationTime < averageResultContent.getSimulationTime())
				maxAveragesimulationTime =averageResultContent.getSimulationTime();

		}

		totalResultParemeter.setAverageOverUtilization(meanOverUtilization / actualRuns);
		totalResultParemeter.setAverageUtilization(meanUtilization / actualRuns);
		totalResultParemeter.setActivePathParticipants(activePathParticipants / actualRuns);
		totalResultParemeter
				.setMinAverageConnectionStability(minConnectionStability /  actualRuns);
		totalResultParemeter
				.setMaxAverageConnectionStability(maxConnectionStability / actualRuns);
		totalResultParemeter
				.setMeanAverageConnectionStability(meanConnectionStability /  actualRuns);
		totalResultParemeter.setNumberOfUndeployedFlows(meanNumberOfUndeployedFlows / actualRuns);
		totalResultParemeter
				.setMeanAverageSimulationTime(meanAveragesimulationTime /  actualRuns);

		totalResultParemeter.setMinAveragesimulationTime(minAveragesimulationTime);
		totalResultParemeter.setMaxAveragesimulationTime(maxAveragesimulationTime);
		
		totalResultParemeter.setLinkQuality(totalmeanLinkQuality/(double)actualRuns);
		totalResultParemeter.setFinishedRuns(runs.size());

		return totalResultParemeter;
	}

}
