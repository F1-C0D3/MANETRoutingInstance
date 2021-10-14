package de.deterministic.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import de.jgraphlib.graph.elements.Path;
import de.jgraphlib.util.Triple;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.manetmodel.units.DataRate;

public class DijkstraShortestPenaltyFlow {
	ScalarRadioMANET manet;

	public DijkstraShortestPenaltyFlow(ScalarRadioMANET manet) {
		this.manet = manet;
	}

	public ScalarRadioFlow compute(ScalarRadioFlow sp,
			Function<Tuple<ScalarRadioFlow, ScalarRadioLink>, Double> metric) {

		/* Initializaton */
		ScalarRadioNode current = sp.getSource();
		ScalarRadioNode source = sp.getSource();
		ScalarRadioNode target = sp.getTarget();

		List<Integer> vertices = new ArrayList<Integer>();
		List<Tuple<ScalarRadioNode, Double>> predDist = new ArrayList<Tuple<ScalarRadioNode, Double>>();

		for (ScalarRadioNode n : manet.getVertices()) {
			vertices.add(n.getID());

			if (n.getID() == current.getID()) {
				predDist.add(new Tuple<ScalarRadioNode, Double>(null, 0d));
			} else {
				predDist.add(new Tuple<ScalarRadioNode, Double>(null, Double.POSITIVE_INFINITY));
			}
		}

		while (!vertices.isEmpty()) {
			Integer nId = minDistance(predDist, vertices);

			if (nId == -1)
				return null;
			vertices.remove(nId);
			current = manet.getVertex(nId);

			if (current.getID() == target.getID()) {
				return (ScalarRadioFlow) generateSP(predDist, sp);
			}

			for (ScalarRadioNode neig : manet.getNextHopsOf(current)) {

				ScalarRadioFlow rsp = new ScalarRadioFlow(-1, source, current, sp.getDataRate());
				rsp.clear();
				rsp = (ScalarRadioFlow) generateSP(predDist, rsp);
				ScalarRadioLink currentLink = manet.getEdge(current, neig);
				rsp.add(new Tuple<ScalarRadioLink, ScalarRadioNode>(currentLink, neig));
				double edgeDist = metric.apply(new Tuple<ScalarRadioFlow, ScalarRadioLink>(sp, currentLink));

				double newPathDist = edgeDist + predDist.get(current.getID()).getSecond();
				double oldPahtDist = predDist.get(neig.getID()).getSecond();

				manet.deployFlow(rsp);

				if (!manet.isOverutilized() && newPathDist < oldPahtDist) {
					predDist.get(neig.getID()).setFirst(current);
					predDist.get(neig.getID()).setSecond(newPathDist);
				}
				manet.undeployFlow(rsp);

			}
		}
		sp.clear();
		return sp;
	}

	protected Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> generateSP(
			List<Tuple<ScalarRadioNode, Double>> predDist,
			Path<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality> sp) {
		ScalarRadioNode t = sp.getTarget();
		List<Tuple<ScalarRadioLink, ScalarRadioNode>> copy = new ArrayList<Tuple<ScalarRadioLink, ScalarRadioNode>>();

		do {
			ScalarRadioNode pred = predDist.get(t.getID()).getFirst();

			if (pred == null) {
				return sp;
			}

			copy.add(0, new Tuple<ScalarRadioLink, ScalarRadioNode>(manet.getEdge(pred, t), t));
			t = pred;
		} while (t.getID() != sp.getSource().getID());

		sp.addAll(copy);

		return sp;
	}

	protected Integer minDistance(List<Tuple<ScalarRadioNode, Double>> predT, List<Integer> v) {
		int id = -1;
		double result = Double.POSITIVE_INFINITY;
		ListIterator<Tuple<ScalarRadioNode, Double>> it = predT.listIterator();

		while (it.hasNext()) {
			Tuple<ScalarRadioNode, Double> pred = it.next();

			if (v.contains(it.previousIndex()) && pred.getSecond() < result) {
				result = pred.getSecond();
				id = it.previousIndex();
			}
		}
		return id;
	}

	ScalarRadioFlow generateReverseSP(Tuple<ScalarRadioNode, Double> preDist, ScalarRadioFlow flow) {

		return null;

	}

}
