
package de.approximation.app;

import java.util.List;

import de.approximation.optimization.CplexOptimization;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageResultParameter;
import de.manetmodel.results.MANETResultRecorder;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.RunResultParameter;
import de.manetmodel.units.Time;
import de.parallelism.ExecutionCallable;
import de.parallelism.Optimization;

public class ApproximationRun
		extends ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> {
	private CplexOptimization op;
	private MANETResultRecorder<RunResultParameter, AverageResultParameter> resultRecorder;
	private RunResultMapper<RunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> runResultMapper;

	public ApproximationRun(CplexOptimization op,
			MANETResultRecorder<RunResultParameter, AverageResultParameter> resultRecorder,
			RunResultMapper<RunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> runResultMapper) {
		this.op = op;
		this.resultRecorder = resultRecorder;
		this.runResultMapper = runResultMapper;
	}

	@Override
	public ScalarRadioMANET call() {
		ScalarRadioMANET manet = this.op.execute();
		Time duration = this.op.stop();
		this.resultRecorder.recordRun(manet, runResultMapper, duration);
		return manet;
	}

}
