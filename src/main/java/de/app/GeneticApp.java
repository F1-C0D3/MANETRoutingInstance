package de.app;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.deterministic.optimization.MultipleDijkstraLinkQuality;
import de.genetic.app.GeneticRun;
import de.genetic.network.GeneticMANETSupplier;
import de.genetic.optimization.GeneticOptimization;
import de.jgraphlib.graph.generator.NetworkGraphProperties;
import de.jgraphlib.util.Triple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.mobility.MobilityModel;
import de.manetmodel.network.radio.IRadioModel;
import de.manetmodel.network.unit.DataRate;
import de.manetmodel.network.unit.DataUnit;
import de.manetmodel.network.unit.DataRate.DataRateRange;
import de.results.RunResultParameter;
import de.results.RunResultParameterSupplier;
import de.results.AverageResultMapper;
import de.results.AverageResultParameter;
import de.results.AverageResultParameterSupplier;
import de.results.MANETAverageResultMapper;
import de.results.MANETResultRecorder;
import de.results.MANETRunResultMapper;
import de.results.RunResultMapper;
import de.runprovider.Program;

public class GeneticApp extends App {

	public GeneticApp(int runs, int numNodes, List<Triple<Integer, Integer, DataRate>> flowSourceTargetIds,
			DataRate meanTransmissionRate, String appName) {
		super(runs, numNodes, flowSourceTargetIds, meanTransmissionRate, appName);
	}

	public void start() throws InterruptedException, ExecutionException {
		Program<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> program = new Program<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>(
				new GeneticMANETSupplier.GeneticMANETNodeSupplier(),
				new GeneticMANETSupplier.GeneticMANETLinkSupplier(),
				new GeneticMANETSupplier.GeneticMANETLinkQualitySupplier(),
				new GeneticMANETSupplier.GeneticMANETFlowSupplier());

		MANETResultRecorder<RunResultParameter> resultRecorder = program.setResultRecorder(appName);

		MANETAverageResultMapper<AverageResultParameter> totalResultMapper = program.setTotalResultMapper(
				new AverageResultParameterSupplier(), appName, numNodes, flowSourceTargetIds.size(),
				meanTransmissionRate);
		totalResultMapper.getMappingStrategy().setType(AverageResultParameter.class);
		Visualization<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> visualization = null;
		while (runs > 0) {

			MobilityModel mobilityModel = program.setMobilityModel(runs);
			IRadioModel radioModel = program.setRadioModel();

			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet = program
					.createMANET(mobilityModel, radioModel);
			NetworkGraphProperties networkProperties = program.generateNetwork(manet, runs, numNodes);
			RunResultMapper<RunResultParameter> runResultMapper = program.setIndividualRunResultMapper(
					new RunResultParameterSupplier(), networkProperties, mobilityModel, radioModel, appName, numNodes,
					flowSourceTargetIds.size(), meanTransmissionRate);
			runResultMapper.getMappingStrategy().setType(RunResultParameter.class);
			
			if (manet.getVertices().size() == (numNodes + 1)) {
				/* Visialization */
//				visualization = new Visualization<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>(
//						manet);
//				visualization.run();
				if (flowSourceTargetIds.get(0).getThird().get() == -1)
					flowSourceTargetIds = program.generateFlowSourceTargetPairs(manet.getVertices().size(),
							flowSourceTargetIds.size(), new DataRateRange(flowSourceTargetIds.get(0).getFirst(),
									flowSourceTargetIds.get(0).getSecond(), DataUnit.Type.bit),
							runs);
				program.addFlows(manet, flowSourceTargetIds, runs);

				/* Evaluation of each run starts here */
				GeneticOptimization go = new GeneticOptimization(manet, 20000, 12);
				GeneticRun geneticRun = new GeneticRun(go, resultRecorder, runResultMapper);
				Future<List<Flow<Node, Link<LinkQuality>, LinkQuality>>> futureFlows = executor.submit(geneticRun);

//				for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : futureFlows.get())
//					visualization.printPath(flow);
				runs--;
			}

		}
		executor.shutdown();
		try {
			executor.awaitTermination(1L, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		resultRecorder.finish(totalResultMapper);
	}
}
