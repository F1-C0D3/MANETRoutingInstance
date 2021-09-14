package de.genetic.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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

	public Tuple<List<List<Integer>>, List<List<Integer>>> manetGraphPhenotoGeno() {
		List<List<Integer>> sourceTargetResult = new ArrayList<List<Integer>>();
		List<List<Integer>> targetSourceResult = new ArrayList<List<Integer>>();
		TreeMap<Integer, ArrayList<Tuple<Integer, Integer>>> sourceTargetAdjacencies = manet
				.getSourceTargetAdjacencies();
		for (List<Tuple<Integer, Integer>> tuples : sourceTargetAdjacencies.values()) {
			List<Integer> adjacenies = new ArrayList<Integer>();

			for (Tuple<Integer, Integer> mapping : tuples) {

				adjacenies.add(mapping.getSecond());
			}
			sourceTargetResult.add(adjacenies);
		}

		TreeMap<Integer, ArrayList<Tuple<Integer, Integer>>> targetSourceAdjacencies = manet
				.getTargetSourceAdjacencies();
		for (List<Tuple<Integer, Integer>> tuples : targetSourceAdjacencies.values()) {
			List<Integer> adjacenies = new ArrayList<Integer>();

			for (Tuple<Integer, Integer> mapping : tuples) {

				adjacenies.add(mapping.getSecond());
			}
			targetSourceResult.add(adjacenies);
		}
		return new Tuple<List<List<Integer>>, List<List<Integer>>>(sourceTargetResult, targetSourceResult);
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
