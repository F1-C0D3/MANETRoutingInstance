package de.genetic.app;

import de.genetic.optimization.GeneticOptimization;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;
import de.manetmodel.results.RunResultMapper;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.units.Time;
import de.parallelism.RunEcecutionCallable;
import de.parallelism.Optimization;

public class GeneticRun extends RunEcecutionCallable {

	public GeneticRun(GeneticOptimization go,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> resultRecorder) {
		super(go, resultRecorder);
	}

	@Override
	public ScalarRadioMANET call() {
		
		this.op.start();
		ScalarRadioMANET manet = super.op.execute();
		Time duration = op.stop();

		if (super.isSuccessfull()) {
			this.resultRecorder.recordIndividual(manet, duration);
			this.resultRecorder.getRunResultContent().recordRun();
		}
		return manet;
	}
}
