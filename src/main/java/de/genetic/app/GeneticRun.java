package de.genetic.app;

import de.genetic.optimization.GeneticOptimization;
import de.genetic.optimization.PathComposition;
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

public class GeneticRun
		extends ExecutionCallable<ScalarRadioFlow, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> {
	private Optimization<ScalarRadioMANET> op;
	private MANETResultRecorder<RunResultParameter, AverageResultParameter> resultRecorder;
	private RunResultMapper<RunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> runResultMapper;

	public GeneticRun(GeneticOptimization go,
			MANETResultRecorder<RunResultParameter, AverageResultParameter> resultRecorder,
			RunResultMapper<RunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> runResultMapper) {
		this.op = go;
		this.resultRecorder = resultRecorder;
		this.runResultMapper = runResultMapper;
	}

	@Override
	public ScalarRadioMANET call() {
		ScalarRadioMANET manet = this.op.execute();
		Time duration = op.stop();
		this.resultRecorder.recordRun(manet, runResultMapper, duration);
		return manet;
	}
}
