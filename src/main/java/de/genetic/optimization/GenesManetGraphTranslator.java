package de.genetic.optimization;

import java.util.ArrayList;
import java.util.List;

import de.jgraphlib.util.Tuple;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.terministic.serein.api.Translator;

public class GenesManetGraphTranslator implements Translator<PathComposition, GraphGenome> {
	private ScalarRadioMANET manet;
	private List<ScalarRadioFlow> flowMetaData;

	public GenesManetGraphTranslator(ScalarRadioMANET manet) {
		this.manet = manet;
		this.flowMetaData = manet.getFlows();
	}

	@Override
	public PathComposition translate(GraphGenome genome) {

		List<ScalarRadioFlow> flows = new ArrayList<ScalarRadioFlow>();
		for (int i = 0; i < flowMetaData.size(); i++) {
			/* load flow fa at index i */
			ScalarRadioFlow meta = flowMetaData.get(i);
			ScalarRadioFlow f = new ScalarRadioFlow(meta.getSource(), meta.getTarget(), meta.getDataRate());

			/* load chromosomePart at index i */
			List<Integer> chromosomePart = genome.getGenes().get(i);

			for (int index = 0; index < chromosomePart.size() - 1; index++) {
				ScalarRadioNode sourceNode = manet.getVertex(chromosomePart.get(index));
				ScalarRadioNode targetNode = manet.getVertex(chromosomePart.get(index + 1));

				f.add(new Tuple<ScalarRadioLink, ScalarRadioNode>(manet.getEdge(sourceNode, targetNode), targetNode));
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

		for (ScalarRadioFlow flow : flowMetaData) {
			genoFlows.add(new Tuple<Integer, Integer>(flow.getSource().getID(), flow.getTarget().getID()));
		}

		return genoFlows;
	}

	public List<List<Integer>> manetVerticesPhenoToGeno() {
		List<Integer> genesVertices = new ArrayList<Integer>();
		List<ScalarRadioNode> manetVertices = manet.getVertices();

		for (ScalarRadioNode manetVertex : manetVertices) {
			genesVertices.add(manetVertex.getID());
		}

		List<List<Integer>> genesVerticesList = new ArrayList<List<Integer>>();
		genesVerticesList.add(genesVertices);

		return genesVerticesList;
	}

}
