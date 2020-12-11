package de.GeneticOptimization;

import java.util.ArrayList;
import java.util.List;

import de.manetmodel.graph.Playground;
import de.manetmodel.graph.Playground.DoubleRange;
import de.manetmodel.graph.Playground.IntRange;
import de.manetmodel.graph.WeightedUndirectedGraph;
import de.manetmodel.graph.generator.GraphGenerator;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.Manet;
import de.manetmodel.network.ManetSupplier;
import de.manetmodel.network.Node;
import de.manetmodel.network.radio.IdealRadioOccupation;
import de.shortestpathoptimization.ShortestPathOptimization;

public class Program {
    public Program() {
	// TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
	Manet<Node, Link> manet = new Manet<Node, Link>(new ManetSupplier.ManetNodeSupplier(),
		new ManetSupplier.ManetLinkSupplier());
	WeightedUndirectedGraph<Node, Link> graph = manet.getGraph();
	GraphGenerator<Node, Link> generator = new GraphGenerator<Node, Link>(graph);
	Playground pg = new Playground();
	pg.height = new IntRange(0, 10000);
	pg.width = new IntRange(0, 10000);
	pg.edgeCount = new IntRange(1, 4);
	pg.vertexCount = new IntRange(60, 60);
	pg.edgeDistance = new DoubleRange(50d, 100d);
	generator.generateRandomGraph(pg);

//	generator.generateGridGraph(600, 1000, 100, 200);
	manet.setRadioOccupationModel(new IdealRadioOccupation(100d, 200d));
	manet.initialize();

	Node source1 = graph.getVertex(0);
	Node target1 = graph.getVertex(59);

//	Node source2 = graph.getVertex(1);
//	Node target2 = graph.getVertex(20);
	Flow<Node, Link> fSP1 = new Flow<Node, Link>(source1, target1, 1d);
//	Flow<Node, Link> fSP2 = new Flow<Node, Link>(source2, target2, 1d);

	List<Flow<Node, Link>> flowsSP = new ArrayList<Flow<Node, Link>>();
	flowsSP.add(fSP1);
//	flowsSP.add(fSP2);
	ShortestPathOptimization po = new ShortestPathOptimization(manet, flowsSP);
	List<Flow<Node, Link>> spr = po.execute(flowsSP);
	for (Flow<Node, Link> flow : spr) {
	    System.out.println(flow.toString());
	}
//	po.run();
	Flow<Node, Link> fGP1 = new Flow<Node, Link>(source1, target1, 1d);
//	Flow<Node, Link> fGP2 = new Flow<Node, Link>(source2, target2, 1d);
	List<Flow<Node, Link>> flowsGP = new ArrayList<Flow<Node, Link>>();

	flowsGP.add(fGP1);
//	flowsGP.add(fGP2);

	GeneticOptimization go = new GeneticOptimization(manet, flowsGP, 20001, 10);
	go.run();
	PathComposition pc = go.execute();
	for (Flow<Node, Link> flow : pc.flows) {
	    System.out.println(flow.toString());
	}

    }
}
