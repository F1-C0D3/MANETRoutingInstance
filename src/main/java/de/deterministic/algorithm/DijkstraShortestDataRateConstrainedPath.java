package de.deterministic.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import de.deterministic.network.DeterministicMANET;
import de.deterministic.optimization.MultipleDijkstraLinkQuality;
import de.jgraphlib.graph.Path;
import de.jgraphlib.graph.algorithms.DijkstraShortestPath;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.DataRate;

public class DijkstraShortestDataRateConstrainedPath {
	MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> manet;

	public DijkstraShortestDataRateConstrainedPath(
			MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> manet) {
		this.manet = manet;
	}

	public Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> compute(
			Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> sp,
			Function<Tuple<MultipleDijkstraLinkQuality, DataRate>, Double> metric) {

		/* Initializaton */
		Node current = sp.getSource();
		Node source = sp.getSource();
		Node target = sp.getTarget();

		List<Integer> vertices = new ArrayList<Integer>();
		List<Tuple<Node, Double>> predDist = new ArrayList<Tuple<Node, Double>>();

		for (Node n : manet.getVertices()) {
			vertices.add(n.getID());

			if (n.getID() == current.getID()) {
				predDist.add(new Tuple<Node, Double>(null, 0d));
			} else {
				predDist.add(new Tuple<Node, Double>(null, Double.POSITIVE_INFINITY));
			}
		}

		while (!vertices.isEmpty()) {
			Integer nId = minDistance(predDist, vertices);
			vertices.remove(nId);
			current = manet.getVertex(nId);

			if (current.getID() == target.getID()) {
				return (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>) generateSP(predDist,
						sp);
			}

			for (Node neig : manet.getNextHopsOf(current)) {

				Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> rsp = new Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>(
						source, current, sp.getDataRate());
				rsp.clear();
				rsp = (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>) generateSP(predDist,
						rsp);
				Link<MultipleDijkstraLinkQuality> currentLink = manet.getEdge(current, neig);
				rsp.add(new Tuple<Link<MultipleDijkstraLinkQuality>, Node>(currentLink, neig));
				currentLink.getWeight().setReversePath(rsp);
				double edgeDist = metric.apply(
						new Tuple<MultipleDijkstraLinkQuality, DataRate>(currentLink.getWeight(), sp.getDataRate()));

				double oldPahtDist = predDist.get(neig.getID()).getSecond();
				double altPathDist = edgeDist + predDist.get(current.getID()).getSecond();

				if (altPathDist < oldPahtDist) {
					predDist.get(neig.getID()).setFirst(current);
					predDist.get(neig.getID()).setSecond(altPathDist);
				}
			}
		}
		sp.clear();
		return sp;
	}

	protected Path<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> generateSP(
			List<Tuple<Node, Double>> predDist,
			Path<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> sp) {
		Node t = sp.getTarget();
		List<Tuple<Link<MultipleDijkstraLinkQuality>, Node>> copy = new ArrayList<Tuple<Link<MultipleDijkstraLinkQuality>, Node>>();

		do {
			Node pred = predDist.get(t.getID()).getFirst();

			if (pred == null) {
				return sp;
			}

			copy.add(0, new Tuple<Link<MultipleDijkstraLinkQuality>, Node>(manet.getEdge(t, pred), t));
			t = pred;
		} while (t.getID() != sp.getSource().getID());

		sp.addAll(copy);

		return sp;
	}

	protected Integer minDistance(List<Tuple<Node, Double>> predT, List<Integer> v) {
		int id = -1;
		double result = Double.POSITIVE_INFINITY;
		ListIterator<Tuple<Node, Double>> it = predT.listIterator();

		while (it.hasNext()) {
			Tuple<Node, Double> pred = it.next();

			if (v.contains(it.previousIndex()) && pred.getSecond() < result) {
				result = pred.getSecond();
				id = it.previousIndex();
			}
		}
		return id;
	}

	Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> generateReverseSP(
			Tuple<Node, Double> preDist,
			Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> flow) {

		return null;

	}

}
