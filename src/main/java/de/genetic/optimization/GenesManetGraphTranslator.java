package de.genetic.optimization;

import java.util.ArrayList;
import java.util.List;

import de.genetic.network.GeneticMANET;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.terministic.serein.api.Translator;

public class GenesManetGraphTranslator implements Translator<PathComposition, GraphGenome> {
	private MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet;
	private List<Flow<Node, Link<LinkQuality>, LinkQuality>> flowMetaData;

	public GenesManetGraphTranslator(MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet) {
		this.manet = manet;
		this.flowMetaData = manet.getFlows();
	}

	@Override
	public PathComposition translate(GraphGenome genome) {

		List<Flow<Node, Link<LinkQuality>, LinkQuality>> flows = new ArrayList<Flow<Node, Link<LinkQuality>, LinkQuality>>();
		for (int i = 0; i < flowMetaData.size(); i++) {
			/* load flow fa at index i */
			Flow<Node, Link<LinkQuality>, LinkQuality> meta = flowMetaData.get(i);
			Flow<Node, Link<LinkQuality>, LinkQuality> f = new Flow<Node, Link<LinkQuality>, LinkQuality>(
					meta.getSource(), meta.getTarget(), meta.getDataRate());

			/* load chromosomePart at index i */
			List<Integer> chromosomePart = genome.getGenes().get(i);

			for (int index = 0; index < chromosomePart.size() - 1; index++) {
				Node sourceNode = manet.getVertex(chromosomePart.get(index));
				Node targetNode = manet.getVertex(chromosomePart.get(index + 1));

				f.add(new Tuple<Link<LinkQuality>, Node>(manet.getEdge(sourceNode, targetNode), targetNode));
			}
			flows.add(f);

		}

		return new PathComposition(manet, flows);
	}

	public List<List<Tuple<Integer, Integer>>> manetGraphPhenotoGeno() {
		List<List<Tuple<Integer, Integer>>> result = new ArrayList<List<Tuple<Integer, Integer>>>();
		List<ArrayList<Tuple<Integer, Integer>>> vertexAdjacencies = manet.getVertexAdjacencies();
		for (List<Tuple<Integer, Integer>> vertexAdjacency : vertexAdjacencies) {
			List<Tuple<Integer, Integer>> tupleList = new ArrayList<Tuple<Integer, Integer>>();

			for (Tuple<Integer, Integer> mapping : vertexAdjacency) {
				Tuple<Integer, Integer> t = new Tuple<Integer, Integer>(mapping.getFirst(), mapping.getSecond());
				tupleList.add(t);
			}
			result.add(tupleList);
		}
		return result;
	}

	public List<Tuple<Integer, Integer>> flowsPhenoToGeno() {
		List<Tuple<Integer, Integer>> genoFlows = new ArrayList<Tuple<Integer, Integer>>();

		for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : flowMetaData) {
			genoFlows.add(new Tuple<Integer, Integer>(flow.getSource().getID(), flow.getTarget().getID()));
		}

		return genoFlows;
	}

	public List<List<Integer>> manetVerticesPhenoToGeno() {
		List<Integer> genesVertices = new ArrayList<Integer>();
		List<Node> manetVertices = manet.getVertices();

		for (Node manetVertex : manetVertices) {
			genesVertices.add(manetVertex.getID());
		}

		List<List<Integer>> genesVerticesList = new ArrayList<List<Integer>>();
		genesVerticesList.add(genesVertices);

		return genesVerticesList;
	}

}
