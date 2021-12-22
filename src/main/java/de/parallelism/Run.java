package de.parallelism;

import java.util.List;
import java.util.stream.Collectors;

import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;
import de.manetmodel.units.Time;

public class Run extends RunEcecutionCallable {

public Run(Optimization<ScalarRadioMANET> op,
		MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> runResultRecorder) {
	super(op, runResultRecorder);
}

	@Override
	public ScalarRadioMANET call() {

		this.op.start();
		ScalarRadioMANET manet = this.op.execute();
		Time duration = this.op.stop();

		if (isSuccessfull()) {
			this.resultRecorder.recordIndividual(manet, duration);
			this.resultRecorder.getRunResultContent().recordRun();
		}

		return manet;
	}
	

	private boolean isSuccessfull() {
		
		ScalarRadioMANET manet = op.getManet();
		
		List<ScalarRadioFlow> undeployedFlows = manet.getFlows().stream().filter(f -> !f.isComplete())
				.collect(Collectors.toList());

		return ((!manet.isOverutilized()) && (manet.getUtilization().get() > 0L) && (undeployedFlows.size() == 0));
	}

}