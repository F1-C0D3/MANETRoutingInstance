package de.genetic.app;

import de.genetic.optimization.PathComposition;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.parallelism.Optimization;
import de.parallelism.Run;
import de.results.MANETRunResult;
import de.results.ParameterRecorder;
import de.results.ResultRecorder;
import de.results.RunResult;

public class GeneticRun extends
		Run<PathComposition, MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>, ResultRecorder<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>, RunResult>> {
	Optimization<PathComposition, MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>> op;
	ResultRecorder<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>, MANETRunResult> parameterRecorder;

	public GeneticRun(
			Optimization<PathComposition, MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>> op,
			ResultRecorder<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>, MANETRunResult> geneticEvalRecorder) {
		this.op = op;
		this.parameterRecorder = geneticEvalRecorder;
	}

	@Override
	public void run() {
		PathComposition pc = this.op.execute();
		MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet = op.getManet();

		for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : pc.flows) {
			manet.deployFlow(flow);
		}

		this.parameterRecorder.recordRun(manet);
		manet.eraseFlows();

	}

}
