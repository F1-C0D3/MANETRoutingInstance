package de.program;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.WindowConstants;

import de.geneticOptimization.GeneticOptimization;
import de.geneticOptimization.PathComposition;
import de.jgraphlib.graph.generator.GraphProperties.DoubleRange;
import de.jgraphlib.graph.generator.GraphProperties.IntRange;
import de.jgraphlib.graph.generator.NetworkGraphGenerator;
import de.jgraphlib.graph.generator.NetworkGraphProperties;
import de.jgraphlib.gui.VisualGraph;
import de.jgraphlib.gui.VisualGraphFrame;
import de.jgraphlib.gui.VisualGraphMarkUp;
import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.radio.Propagation;
import de.manetmodel.network.radio.ScalarRadioModel;
import de.manetmodel.network.unit.DataRate;
import de.manetmodel.network.unit.Unit;
import de.network.GeneticMANET;
import de.network.GeneticMANETSupplier;
import de.results.MANETParameterRecorder;
import de.results.MANETResultRunSupplier;
import de.results.MANETRunResult;
import de.results.ResultRecorder;
import de.results.Scenario;

public class Program implements Runnable {

	MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet;
	private VisualGraphFrame<Node, Link<LinkQuality>> frame;
	VisualGraph<Node, Link<LinkQuality>> visualGraph;

	public Program(MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet) {
		this.manet = manet;
		visualGraph = new VisualGraph<Node, Link<LinkQuality>>(this.manet, new VisualGraphMarkUp());
		frame = new VisualGraphFrame<Node, Link<LinkQuality>>(visualGraph);
	}

	public static void main(String[] args) {

		MANETParameterRecorder<LinkQuality, MANETRunResult> runRecorder = new MANETParameterRecorder<LinkQuality, MANETRunResult>(
				new MANETResultRunSupplier());
		ResultRecorder<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>, MANETRunResult> recorder = new ResultRecorder<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>, MANETRunResult>(
				GeneticMANET.class, runRecorder);

		for (int i = 0; i < 1; i++) {

			GeneticMANET manet = new GeneticMANET(new GeneticMANETSupplier.GeneticMANETNodeSupplier(),
					new GeneticMANETSupplier.GeneticMANETLinkSupplier(),
					new GeneticMANETSupplier.GeneticMANETFlowSupplier(),
					new ScalarRadioModel(Propagation.pathLoss(100d, Propagation.waveLength(2412000000d)), 0.002d, 1e-11,
							2000000d, 2412000000d));

//			GridGraphGenerator<Node, Link<LinkQuality>, LinkQuality> generator = new GridGraphGenerator<Node, Link<LinkQuality>, LinkQuality>(
//					manet, new RandomNumbers(i));
//			GridGraphProperties properties = new GridGraphProperties(1000, 1000, 100, 100);
//			generator.generate(properties);
			NetworkGraphGenerator<Node, Link<LinkQuality>, LinkQuality> generator = new NetworkGraphGenerator<Node, Link<LinkQuality>, LinkQuality>(
					manet, new RandomNumbers(i));
			NetworkGraphProperties properties = new NetworkGraphProperties(/* width */ 1000, /* height */ 1000,
					/* vertices */ new IntRange(100, 100), /* vertex distance */ new DoubleRange(55d, 100d),
					/* edge distance */ 100);
			generator.generate(properties);
			Program program = new Program(manet);
			program.run();

			Node source1 = manet.getVertex(2);
			Node target1 = manet.getVertex(51);
			DataRate rate1 = new DataRate(1.2d, Unit.Type.megabit);

			Node source2 = manet.getVertex(100);
			Node target2 = manet.getVertex(83);
			DataRate rate2 = new DataRate(1.8d, Unit.Type.megabit);

			Node source3 = manet.getVertex(68);
			Node target3 = manet.getVertex(10);
			DataRate rate3 = new DataRate(1.2d, Unit.Type.megabit);

			Node source4 = manet.getVertex(54);
			Node target4 = manet.getVertex(27);
			DataRate rate4 = new DataRate(1.4d, Unit.Type.megabit);

			Flow<Node, Link<LinkQuality>, LinkQuality> flow2 = manet.addFlow(source1, target1, rate1);
			Flow<Node, Link<LinkQuality>, LinkQuality> flow1 = manet.addFlow(source2, target2, rate2);

			Flow<Node, Link<LinkQuality>, LinkQuality> flow3 = manet.addFlow(source3, target3, rate3);
			Flow<Node, Link<LinkQuality>, LinkQuality> flow4 = manet.addFlow(source4, target4, rate4);

			GeneticOptimization go = new GeneticOptimization(manet, 70000, 10);
			PathComposition pc = go.execute();
			runRecorder.setScenario(new Scenario(manet.getFlows().size(), manet.getVertices().size()));

			for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : pc.flows) {
				manet.deployFlow(flow);
				program.printPath(flow);
//
			}

			recorder.recordRun(manet);
			manet.eraseFlows();
		}

		recorder.finish();
	}

	@Override
	public void run() {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setPreferredSize(
				new Dimension((int) screenSize.getWidth() * 3 / 4, (int) screenSize.getHeight() * 3 / 4));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void printPath(Flow<Node, Link<LinkQuality>, LinkQuality> flow) {
		visualGraph.addVisualPath(flow);
		frame.getVisualGraphPanel().repaint();
	}
}
