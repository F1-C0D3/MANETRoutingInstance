package de.approximation.app;

import java.util.List;

import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.Time;
import de.parallelism.Optimization;
import de.results.RunResultParameter;
import de.results.MANETResultRecorder;
import de.results.RunResultMapper;
import de.runprovider.ExecutionCallable;

public class ApproximationRun
		extends ExecutionCallable<Flow<Node, Link<LinkQuality>, LinkQuality>, Node, Link<LinkQuality>, LinkQuality> {
	private Optimization<Void, MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>> op;
	private MANETResultRecorder<RunResultParameter> resultRecorder;
	private RunResultMapper<RunResultParameter> runResultMapper;

	public ApproximationRun(
			Optimization<Void, MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>> op,
			MANETResultRecorder<RunResultParameter> resultRecorder,
			RunResultMapper<RunResultParameter> runResultMapper) {
		this.op = op;
		this.resultRecorder = resultRecorder;
		this.runResultMapper = runResultMapper;
	}

	@Override
	public Void call() {
		this.op.execute();
		Time duration = this.op.stop();
		this.resultRecorder.recordRun(op.getManet(), runResultMapper, duration);
		return null;
	}

}
