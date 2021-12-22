package de.parallelism;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.results.AverageRunResultParameter;
import de.manetmodel.results.IndividualRunResultParameter;
import de.manetmodel.results.MANETRunResultRecorder;

public class RunEcecutionCallable
		implements Callable<ScalarRadioMANET> {
	
	protected Optimization<ScalarRadioMANET> op;
	protected MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter,ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality,ScalarRadioFlow> resultRecorder;
	
	public RunEcecutionCallable(Optimization<ScalarRadioMANET> op,
			MANETRunResultRecorder<IndividualRunResultParameter, AverageRunResultParameter, ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality, ScalarRadioFlow> resultRecorder) {
		this.op=op;
		this.resultRecorder=resultRecorder;
	}
	

	@Override
	public ScalarRadioMANET call() {
		// TODO Auto-generated method stub
		return null;
	}

}
