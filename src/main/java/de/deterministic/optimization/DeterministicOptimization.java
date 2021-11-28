package de.deterministic.optimization;

import java.util.List;
import java.util.function.Function;

import de.deterministic.algorithm.DijkstraShortestFlow;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.parallelism.Optimization;

public class DeterministicOptimization<M extends ScalarRadioMANET> extends Optimization<M> {

	protected DijkstraShortestFlow sp;

	public DeterministicOptimization(M manet) {
		super(manet);
		sp = new DijkstraShortestFlow(manet);
	}

	@Override
	public M execute() {
		// TODO Auto-generated method stub
		return null;
	}

	protected double evaluateSolution() {

		double receptionConfidence = 0d;
		double sinkSpeed = 0d;
		double relativeDistance = 0d;

		for (ScalarRadioLink link : manet.getActiveUtilizedLinks()) {
			receptionConfidence += link.getWeight().getReceptionConfidence() * 0.6d;
			sinkSpeed += link.getWeight().getSpeedQuality() * 0.3d;
			relativeDistance += link.getWeight().getRelativeMobility() * 0.1d;
		}

		return (receptionConfidence + sinkSpeed + relativeDistance) / manet.getActiveUtilizedLinks().size();
	}

	protected void deployPathsBasedOnMethric(List<Integer> flowIds,Function<ScalarLinkQuality, Double> metric) {
		manet.undeployFlows();
		for (int fId : flowIds) {
			ScalarRadioFlow flow = manet.getFlow(fId);
			flow.clear();
			flow = sp.compute(flow, metric);
			manet.deployFlow(flow);
		}

	}

}
