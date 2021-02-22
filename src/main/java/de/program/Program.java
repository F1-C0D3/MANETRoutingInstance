package de.program;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.WindowConstants;

import de.GeneticOptimization.GeneticOptimization;
import de.GeneticOptimization.PathComposition;
import de.geneticManet.GeneticManetGraph;
import de.geneticManet.GeneticManetGraphSupplier;
import de.manetmodel.app.gui.VisualGraphFrame;
import de.manetmodel.app.gui.visualgraph.VisualGraph;
import de.manetmodel.app.gui.visualgraph.VisualGraphMarkUp;
import de.manetmodel.graph.EdgeDistance;
import de.manetmodel.graph.SampleTopologies.TrainingTopology;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.Manet;
import de.manetmodel.network.Node;
import de.manetmodel.network.radio.Propagation;
import de.manetmodel.network.radio.ScalarRadioModel;
import de.manetmodel.network.unit.DataRate;
import de.manetmodel.network.unit.Unit;

public class Program implements Runnable {

	Manet<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> manet;
	private VisualGraphFrame<Node<EdgeDistance>, Link<EdgeDistance>> frame;
	VisualGraph<Node<EdgeDistance>, Link<EdgeDistance>> visualGraph;

	public Program(Manet<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> manet) {
		this.manet = manet;
		visualGraph = new VisualGraph<Node<EdgeDistance>, Link<EdgeDistance>>(this.manet, new VisualGraphMarkUp());
		frame = new VisualGraphFrame<Node<EdgeDistance>, Link<EdgeDistance>>(visualGraph);
	}

	public static void main(String[] args) {
		GeneticManetGraph manet = new GeneticManetGraph(new GeneticManetGraphSupplier.ManetNodeSupplier(),
				new GeneticManetGraphSupplier.ManetLinkSupplier(),
				new ScalarRadioModel(Propagation.pathLoss(100d, Propagation.waveLength(2412000000d)), 0.002d, 1e-11,
						2000000d, 2412000000d));

//		Manet<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> manet = new Manet<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>(
//				new ManetSupplier.ManetNodeSupplier(), new ManetSupplier.ManetLinkSupplier(),
//				new IdealRadioModel(100, new DataRate(11, Type.megabit)));
//		GridGraphGenerator<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> generator = new GridGraphGenerator<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>(
//				manet);
//		GridGraphProperties properties = new GridGraphProperties(1000, 1000, 100, 100);
//		generator.generate(properties);

		TrainingTopology tTopoogy = new TrainingTopology(manet);
		tTopoogy.create();
		Program program = new Program(manet);
		program.run();

		Node<EdgeDistance> source1 = manet.getVertex(18);
		Node<EdgeDistance> target1 = manet.getVertex(9);

		Node<EdgeDistance> source2 = manet.getVertex(10);
		Node<EdgeDistance> target2 = manet.getVertex(15);
//
//		Node<EdgeDistance> source3 = manet.getVertex(10);
//		Node<EdgeDistance> target3 = manet.getVertex(15);
////
//		Node<EdgeDistance> source4 = manet.getVertex(0);
//		Node<EdgeDistance> target4 = manet.getVertex(76);
//
//		Node<EdgeDistance> source5 = manet.getVertex(10);
//		Node<EdgeDistance> target5 = manet.getVertex(3);
//
//		Node<EdgeDistance> source6 = manet.getVertex(7);
//		Node<EdgeDistance> target6 = manet.getVertex(22);

		Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> f1 = new Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>(
				source1, target1, new DataRate(1.0d, Unit.Type.megabit));

		Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> f2 = new Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>(
				source2, target2, new DataRate(4.90d, Unit.Type.megabit));
//
//		Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> f3 = new Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>(
//				source3, target3, new DataRate(1d, Unit.Type.megabit));

//		Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> f4 = new Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>(
//				source4, target4, new DataRate(1d, Unit.Type.megabit));
//
//		Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> fGP5 = new Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>(
//				source5, target5, new DataRate(2d, Unit.Type.megabit));
//
//		Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> fGP6 = new Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>(
//				source6, target6, new DataRate(1.5d, Unit.Type.megabit));

		List<Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>> flowsGP = new ArrayList<Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>>();

		flowsGP.add(f1);
		flowsGP.add(f2);
//		flowsGP.add(f3);
//		flowsGP.add(f4);
//		flowsGP.add(fGP5);
//		flowsGP.add(fGP6);
		GeneticOptimization go = new GeneticOptimization(manet, flowsGP, 20000, 10);
		PathComposition pc = go.execute();

//		System.out.println((1.0 / manet.getCapacity().get()) * manet.getUtilization().get() + ", OverUtilization: "
//				+ pc.overUtilization().get());
//		ShortestPathOptimization spo = new ShortestPathOptimization(manet, flowsGP);
//		List<Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>> pc = spo.execute(flowsGP);

		for (Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> flow : pc.flows) {
			program.printPath(flow);

		}
		System.out.println("overUtilization: " + manet.aggregateOverUtilizedLinks().toString());
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

	public void printPath(Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> flow) {
		visualGraph.addVisualPath(flow);
		frame.getVisualGraphPanel().repaint();
	}
}
