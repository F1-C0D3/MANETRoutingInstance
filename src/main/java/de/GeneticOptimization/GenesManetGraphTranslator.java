package de.GeneticOptimization;

import java.util.ArrayList;
import java.util.List;

import de.manetmodel.graph.EdgeVertexMapping;
import de.manetmodel.graph.Vertex;
import de.manetmodel.graph.VertexAdjacency;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.Manet;
import de.manetmodel.network.Node;
import de.manetmodel.util.Tuple;
import de.terministic.serein.api.Translator;

public class GenesManetGraphTranslator implements Translator<PathComposition, GraphGenome> {
    private Manet<Node, Link> manet;
    private List<Flow<Node, Link>> flowMetaData;

    public GenesManetGraphTranslator(Manet manet, List<Flow<Node, Link>> flowMetaData) {
	this.manet = manet;
	this.flowMetaData = flowMetaData;
    }

    @Override
    public PathComposition translate(GraphGenome genome) {

	List<Flow<Node, Link>> flows = new ArrayList<Flow<Node, Link>>();
	for (int i = 0; i < flowMetaData.size(); i++) {
	    /* load flow fa at index i */
	    Flow<Node, Link> meta = flowMetaData.get(i);
	    Flow f = new Flow<Node, Link>(meta.getSource(), meta.getTarget(), meta.getBitrate());

	    /* load chromosomePart at index i */
	    List<Integer> chromosomePart = genome.getGenes().get(i);

	    for (int index = 0; index < chromosomePart.size() - 1; index++) {
		Node sourceNode = manet.getGraph().getVertex(chromosomePart.get(index));
		Node targetNode = manet.getGraph().getVertex(chromosomePart.get(index + 1));

		f.add(new Tuple<Link, Node>(manet.getGraph().getEdge(sourceNode, targetNode), targetNode));
	    }
	    flows.add(f);

	}
	return new PathComposition(manet, flows);
    }

    public List<List<Tuple<Integer, Integer>>> manetGraphPhenotoGeno() {
	List<List<Tuple<Integer, Integer>>> result = new ArrayList<List<Tuple<Integer, Integer>>>();
	List<VertexAdjacency> vertexAdjacencies = manet.getGraph().getVertexAdjacencies();
	for (VertexAdjacency vertexAdjacency : vertexAdjacencies) {
	    List<Tuple<Integer, Integer>> tupleList = new ArrayList<Tuple<Integer, Integer>>();

	    for (EdgeVertexMapping mapping : vertexAdjacency.getEdgeVertexMappings()) {
		Tuple<Integer, Integer> t = new Tuple<Integer, Integer>(mapping.getEdgeID(), mapping.getVertexID());
		tupleList.add(t);
	    }
	    result.add(tupleList);
	}
	return result;
    }

    public List<Tuple<Integer, Integer>> flowsPhenoToGeno() {
	List<Tuple<Integer, Integer>> genoFlows = new ArrayList<Tuple<Integer, Integer>>();

	for (Flow<Node, Link> flow : flowMetaData) {
	    genoFlows.add(new Tuple<Integer, Integer>(flow.getSource().getID(), flow.getTarget().getID()));
	}

	return genoFlows;
    }

    public List<List<Integer>> manetVerticesPhenoToGeno() {
	List<Integer> genesVertices = new ArrayList<Integer>();
	List<Node> manetVertices = manet.getGraph().getVertices();

	for (Vertex manetVertex : manetVertices) {
	    genesVertices.add(manetVertex.getID());
	}

	List<List<Integer>> genesVerticesList = new ArrayList<List<Integer>>();
	genesVerticesList.add(genesVertices);

	return genesVerticesList;
    }

}
