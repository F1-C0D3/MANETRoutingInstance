package de.deterministic.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import de.deterministic.network.DeterministicMANET;
import de.jgraphlib.graph.algorithms.DijkstraShortestPath;
import de.jgraphlib.graph.elements.Path;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.DataRate;

public class DijkstraShortesFlow {
	MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet;

	public DijkstraShortesFlow(
			MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> manet) {
		this.manet = manet;
	}

	public Flow<Node, Link<LinkQuality>, LinkQuality> compute(Flow<Node, Link<LinkQuality>, LinkQuality> sp,
			Function<Tuple<Flow<Node, Link<LinkQuality>, LinkQuality>, LinkQuality>, Double> metric) {

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
				return (Flow<Node, Link<LinkQuality>, LinkQuality>) generateSP(predDist, sp);
			}

			for (Node neig : manet.getNextHopsOf(current)) {

				Flow<Node, Link<LinkQuality>, LinkQuality> rsp = new Flow<Node, Link<LinkQuality>, LinkQuality>(-1,
						source, current, sp.getDataRate());
				rsp.clear();
				rsp = (Flow<Node, Link<LinkQuality>, LinkQuality>) generateSP(predDist, rsp);
				Link<LinkQuality> currentLink = manet.getEdge(current, neig);
				rsp.add(new Tuple<Link<LinkQuality>, Node>(currentLink, neig));
				double edgeDist = metric.apply(new Tuple(rsp, currentLink.getWeight()));

				double newPathDist = edgeDist + predDist.get(current.getID()).getSecond();
				double oldPahtDist = predDist.get(neig.getID()).getSecond();
				manet.deployFlow(rsp);
				if (manet.getOverUtilization().get() > 0)
					newPathDist = manet.getCapacity().get() + 1L;
				manet.undeployFlow(rsp);

				if (newPathDist < oldPahtDist) {
					predDist.get(neig.getID()).setFirst(current);
					predDist.get(neig.getID()).setSecond(newPathDist);
				}

			}
		}
		sp.clear();
		return sp;
	}

	protected Path<Node, Link<LinkQuality>, LinkQuality> generateSP(List<Tuple<Node, Double>> predDist,
			Path<Node, Link<LinkQuality>, LinkQuality> sp) {
		Node t = sp.getTarget();
		List<Tuple<Link<LinkQuality>, Node>> copy = new ArrayList<Tuple<Link<LinkQuality>, Node>>();

		do {
			Node pred = predDist.get(t.getID()).getFirst();

			if (pred == null) {
				return sp;
			}

			copy.add(0, new Tuple<Link<LinkQuality>, Node>(manet.getEdge(pred, t), t));
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

	Flow<Node, Link<LinkQuality>, LinkQuality> generateReverseSP(Tuple<Node, Double> preDist,
			Flow<Node, Link<LinkQuality>, LinkQuality> flow) {

		return null;

	}

}
