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
		Time meanConnectionStability = new Time();
		Time minConnectionStability = new Time();
		Time maxConnectionStability = new Time();
		double meanNumberOfUndeployedFlows = 0d;
		Time meanAveragesimulationTime = new Time();
		Time minAveragesimulationTime = new Time(Long.MAX_VALUE);
		Time maxAveragesimulationTime = new Time(0L);

		double actualRuns = 0;
		for (RunResultContent<IndividualRunResultParameter, AverageRunResultParameter> run : runs) {

			AverageRunResultParameter averageResultContent = run.getAverageResultContent();

				actualRuns++;

				meanOverUtilization += averageResultContent.getOverUtilization();
				meanUtilization += averageResultContent.getUtilization();
				activePathParticipants += averageResultContent.getActivePathParticipants();
				meanConnectionStability.set(meanConnectionStability.getMillis()
						+ averageResultContent.getMeanConnectionStability().getMillis());
				minConnectionStability.set(minConnectionStability.getMillis()
						+ averageResultContent.getMinConnectionStability().getMillis());
				
				maxConnectionStability.set(maxConnectionStability.getMillis()
						+ averageResultContent.getMaxConnectionStability().getMillis());
				
				meanNumberOfUndeployedFlows += averageResultContent.getNumberOfUndeployedFlows();
				meanAveragesimulationTime.set(
						meanAveragesimulationTime.getMillis() + averageResultContent.getSimulationTime().getMillis());

				if (minAveragesimulationTime.getMillis() > averageResultContent.getSimulationTime().getMillis())
					minAveragesimulationTime.set(averageResultContent.getSimulationTime().getMillis());

				if (maxAveragesimulationTime.getMillis() < averageResultContent.getSimulationTime().getMillis())
					maxAveragesimulationTime.set(averageResultContent.getSimulationTime().getMillis());

		}
		
		totalResultParemeter.setAverageOverUtilization(meanOverUtilization / actualRuns);
		totalResultParemeter.setAverageUtilization(meanUtilization / actualRuns);
		totalResultParemeter.setActivePathParticipants(activePathParticipants / actualRuns);
		totalResultParemeter
				.setMinAverageConnectionStability(new Time(minConnectionStability.getMillis() / (long) actualRuns));
		totalResultParemeter
		.setMaxAverageConnectionStability(new Time(maxConnectionStability.getMillis() / (long) actualRuns));
		totalResultParemeter
				.setMeanAverageConnectionStability(new Time(meanConnectionStability.getMillis() / (long) actualRuns));
		totalResultParemeter.setNumberOfUndeployedFlows(meanNumberOfUndeployedFlows / actualRuns);
		totalResultParemeter
				.setMeanAverageSimulationTime(new Time(meanAveragesimulationTime.getMillis() / (long) actualRuns));

		totalResultParemeter.setMinAveragesimulationTime(minAveragesimulationTime);
		totalResultParemeter.setMaxAveragesimulationTime(maxAveragesimulationTime);
		totalResultParemeter.setFinishedRuns(runs.size());

		return totalResultParemeter;
	}

}
