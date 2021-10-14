
package de.approximation.app;

import de.approximation.optimization.CplexOptimization;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;
import de.manetmodel.results.ResultParameter;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.units.Time;
import de.parallelism.ExecutionCallable;

public class ApproximationRun
		extends ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> {
	private CplexOptimization op;
	private MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> runResultRecorder;

	public ApproximationRun(CplexOptimization op,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> runResultRecorder) {
		this.op = op;
		this.runResultRecorder = runResultRecorder;
	}

	@Override
	public ScalarRadioMANET call() {
		ScalarRadioMANET manet = this.op.execute();
		Time duration = this.op.stop();
		this.runResultRecorder.recordIndividual(manet, duration);
		return manet;
	}

}
