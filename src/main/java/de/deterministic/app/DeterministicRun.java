package de.deterministic.app;

import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.units.Time;
import de.parallelism.ExecutionCallable;
import de.parallelism.Optimization;

public class DeterministicRun
		extends ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> {
	private Optimization<ScalarRadioMANET> op;
	private MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> resultRecorder;

	public DeterministicRun(Optimization<ScalarRadioMANET> op,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> resultRecorder) {
		this.op = op;
		this.resultRecorder = resultRecorder;
	}

	@Override
	public ScalarRadioMANET call() {
		ScalarRadioMANET manet = this.op.execute();
		Time duration = this.op.stop();
		this.resultRecorder.recordIndividual(op.getManet(), duration);

		return manet;
	}

}
