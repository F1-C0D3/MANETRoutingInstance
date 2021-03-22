package de.genetic.app;

import de.genetic.optimization.PathComposition;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.parallelism.Optimization;
import de.parallelism.Run;
import de.results.MANETResultParameter;
import de.results.MANETResultRecorder;
import de.results.MANETRunResultMapper;
import de.results.ResultParameter;

public class GeneticRun extends
		Run<PathComposition, MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>, MANETResultRecorder<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>, ResultParameter>> {
	Optimization<PathComposition, MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>> op;
	MANETResultRecorder<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>, MANETResultParameter> parameterRecorder;
	MANETRunResultMapper<LinkQuality, MANETResultParameter> runResultMapper;
	public GeneticRun(
			Optimization<PathComposition, MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>> op,
			MANETResultRecorder<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>, MANETResultParameter> geneticEvalRecorder,
			MANETRunResultMapper<LinkQuality, MANETResultParameter> runResultMapper) {
		this.op = op;
		this.parameterRecorder = geneticEvalRecorder;
		this.runResultMapper=runResultMapper;
	}

	@Override
	public void run() {
		PathComposition pc = this.op.execute();
		MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet = op.getManet();

		for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : pc.flows) {
			manet.deployFlow(flow);
		}

		this.parameterRecorder.recordRun(op.getManet(),runResultMapper);
		manet.eraseFlows();

	}

}
