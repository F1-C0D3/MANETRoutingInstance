package de.app;

import de.approximation.app.ApproximationRun;
import de.approximation.optimization.CplexOptimization;
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

public class CplexApp extends App {

	private double constraint;
	public CplexApp(double constraint, Scenario scenario,RandomNumbers random,boolean visual) {
		super(scenario,random,visual);
		this.constraint = constraint;
	}


	@Override
	public RunEcecutionCallable configureRun(
			ScalarRadioMANET manet,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> resultRecorder) {
		
		CplexOptimization co = new CplexOptimization(
				manet,this.constraint);
		return new ApproximationRun(co, resultRecorder);
	}
}
