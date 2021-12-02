package de.app;

import de.deterministic.app.DeterministicRun;
import de.deterministic.optimization.KMostDisjointPathsOptimization;
import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;
import de.manetmodel.scenarios.Scenario;
import de.parallelism.RunEcecutionCallable;

public class KMostDiscjointPathApp extends App {

	public KMostDiscjointPathApp(Scenario scenario, RandomNumbers random,boolean visual) {
		super(scenario, random,visual);
	}

	@Override
	public RunEcecutionCallable configureRun(
			ScalarRadioMANET manet, MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> resultRecorder) {
		KMostDisjointPathsOptimization go = new KMostDisjointPathsOptimization(manet,4,RandomNumbers.getInstance(3));
		return new DeterministicRun(go, resultRecorder);
	}

}
