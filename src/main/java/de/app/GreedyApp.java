package de.app;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.deterministic.app.DeterministicRun;
import de.deterministic.network.DeterministicMANETSupplier;
import de.deterministic.optimization.GreedyCombinationOptimization;
import de.deterministic.optimization.MultipleDijkstraLinkQuality;
import de.genetic.optimization.GeneticOptimization;
import de.jgraphlib.graph.generator.NetworkGraphProperties;
import de.jgraphlib.util.Triple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
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
import de.results.ResultParameter;
import de.runprovider.Program;

public class GreedyApp extends App {

	public GreedyApp(int runs, int numNodes, List<Triple<Integer, Integer, DataRate>> flowSourceTargetIds,
			DataRate meanTransmissionRate, String appName) {
		super(runs, numNodes, flowSourceTargetIds, meanTransmissionRate, appName);
	}

	public void start() throws InterruptedException, ExecutionException {
		Program<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> program = new Program<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>(
				new DeterministicMANETSupplier.DeterministicMANETNodeSupplier(),
				new DeterministicMANETSupplier.DeterministicMANETLinkSupplier(),
				new DeterministicMANETSupplier.DeterministicMANETLinkQualitySupplier(),
				new DeterministicMANETSupplier.DeterministicMANETFlowSupplier());

		MANETResultRecorder<RunResultParameter> resultRecorder = program.setResultRecorder(appName);

		AverageResultMapper<AverageResultParameter> totalResultMapper = program.setTotalResultMapper(
				new AverageResultParameterSupplier(), appName, numNodes, flowSourceTargetIds.size(),
				meanTransmissionRate);
		totalResultMapper.getMappingStrategy().setType(AverageResultParameter.class);
		Visualization<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> visualization = null;
		while (runs > 0) {

			MobilityModel mobilityModel = program.setMobilityModel(runs);
			IRadioModel radioModel = program.setRadioModel();

			MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> manet = program
					.createMANET(mobilityModel, radioModel);
			NetworkGraphProperties networkProperties = program.generateNetwork(manet, runs, numNodes);
			MANETRunResultMapper<RunResultParameter> runResultMapper = program.setIndividualRunResultMapper(
					new RunResultParameterSupplier(), networkProperties, mobilityModel, radioModel, appName, numNodes,
					flowSourceTargetIds.size(), meanTransmissionRate);
			runResultMapper.getMappingStrategy().setType(RunResultParameter.class);

			if (manet.getVertices().size() == (numNodes + 1)) {
				/* Visialization */
//				visualization = new Visualization<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>(
//						manet);
//				visualization.run();
				if (flowSourceTargetIds.get(0).getThird().get() == -1)
					flowSourceTargetIds = program.generateFlowSourceTargetPairs(manet.getVertices().size(),
							flowSourceTargetIds.size(), new DataRateRange(flowSourceTargetIds.get(0).getFirst(),
									flowSourceTargetIds.get(0).getSecond(), DataUnit.Type.bit),
							runs);
				program.addFlows(manet, flowSourceTargetIds, runs);

				/* Evaluation of each run starts here */
				GreedyCombinationOptimization<MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>> go = new GreedyCombinationOptimization<MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>>(
						manet);
				DeterministicRun greedyHeuristicRun = new DeterministicRun(go, resultRecorder, runResultMapper);
				Future<List<Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>> futureFlows = executor
						.submit(greedyHeuristicRun);
				for (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> flow : futureFlows
						.get())
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
