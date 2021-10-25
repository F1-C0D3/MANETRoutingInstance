package de.deterministic.app;

import java.util.List;
import java.util.stream.Collectors;

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
import de.parallelism.RunEcecutionCallable;
import de.parallelism.Optimization;

public class DeterministicRun
		extends RunEcecutionCallable {
	

	public DeterministicRun(Optimization<ScalarRadioMANET> op,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> resultRecorder) {
		super(op, resultRecorder);
	}

	@Override
	public ScalarRadioMANET call() {
		ScalarRadioMANET manet = this.op.execute();
		Time duration = this.op.stop();

	if(super.isSuccessfull()) {
			this.resultRecorder.recordIndividual(op.getManet(), duration);
			this.resultRecorder.getRunResultContent().recordRun();
	}

		return manet;
	}

}
