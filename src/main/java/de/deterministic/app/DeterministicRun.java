package de.deterministic.app;

import java.util.List;

import de.deterministic.optimization.MultipleDijkstraLinkQuality;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.Time;
import de.parallelism.Optimization;
import de.results.RunResultParameter;
import de.results.AverageResultParameter;
import de.results.MANETResultRecorder;
import de.results.MANETRunResultMapper;
import de.results.ResultParameter;
import de.runprovider.ExecutionCallable;

public class DeterministicRun extends
		ExecutionCallable<Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> {
	private Optimization<Void, MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>> op;
	private MANETResultRecorder<RunResultParameter> resultRecorder;
	private MANETRunResultMapper<RunResultParameter> runResultMapper;

	public DeterministicRun(
			Optimization<Void, MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>> op,
			MANETResultRecorder<RunResultParameter> resultRecorder,
			MANETRunResultMapper<RunResultParameter> runResultMapper) {
		this.op = op;
		this.resultRecorder = resultRecorder;
		this.runResultMapper = runResultMapper;
	}

	@Override
	public List<Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> call() {
		this.op.execute();
		Time duration = this.op.stop();
		this.resultRecorder.recordRun(op.getManet(), runResultMapper, duration);
		return op.getManet().getFlows();
	}

}
