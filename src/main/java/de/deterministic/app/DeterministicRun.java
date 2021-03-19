package de.deterministic.app;

import de.deterministic.optimization.MultipleDijkstraLinkQuality;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.parallelism.Optimization;
import de.parallelism.Run;
import de.results.MANETRunResult;
import de.results.ResultRecorder;
import de.results.RunResult;

public class DeterministicRun extends
		Run<Void, MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>, ResultRecorder<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, RunResult>> {
	Optimization<Void, MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>> op;
	ResultRecorder<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, MANETRunResult> resultRecorder;

	public DeterministicRun(
			Optimization<Void, MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>> op,
			ResultRecorder<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>, MANETRunResult> resultRecorder) {
		this.op = op;
		this.resultRecorder = resultRecorder;
	}

	@Override
	public void run() {
		this.op.execute();
		this.resultRecorder.recordRun(op.getManet());

	}

}
