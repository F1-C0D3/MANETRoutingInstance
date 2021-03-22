package de.deterministic.app;

import java.util.List;

import de.deterministic.optimization.MultipleDijkstraLinkQuality;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.parallelism.Optimization;
import de.parallelism.Run;
import de.results.MANETResultParameter;
import de.results.MANETResultRecorder;
import de.results.MANETRunResultMapper;
import de.results.ResultParameter;
import de.runprovider.ExecutionCallable;

public class DeterministicRun extends
		ExecutionCallable<Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> {
	private Optimization<Void, MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>> op;
	private MANETResultRecorder<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, MANETResultParameter> resultRecorder;
	private MANETRunResultMapper<MultipleDijkstraLinkQuality, MANETResultParameter> runResultMapper;

	public DeterministicRun(
			Optimization<Void, MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>> op,
			MANETResultRecorder<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, MANETResultParameter> resultRecorder,
			MANETRunResultMapper<MultipleDijkstraLinkQuality, MANETResultParameter> runResultMapper) {
		this.op = op;
		this.resultRecorder = resultRecorder;
		this.runResultMapper = runResultMapper;
	}

	@Override
	public List<Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> call() {
		this.op.execute();
		this.resultRecorder.recordRun(op.getManet(), runResultMapper);
		return op.getManet().getFlows();
	}

}
