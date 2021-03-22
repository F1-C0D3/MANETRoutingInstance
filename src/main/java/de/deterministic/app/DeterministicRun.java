package de.deterministic.app;


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

public class DeterministicRun extends
		Run<Void, MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>, MANETResultRecorder<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, ResultParameter>> {
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
	public void run() {
		this.op.execute();
		this.resultRecorder.recordRun(op.getManet(), runResultMapper);

	}

}
