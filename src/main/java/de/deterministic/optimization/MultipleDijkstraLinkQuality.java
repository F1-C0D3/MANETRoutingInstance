package de.deterministic.optimization;

import java.util.List;

import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.Node;
import de.manetmodel.network.mobility.MovementPattern;

public class MultipleDijkstraLinkQuality extends LinkQuality {

	Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> reversePath;

	Tuple<List<MovementPattern>, List<MovementPattern>> sinkAndSourceMobility;

	public MultipleDijkstraLinkQuality() {
		// TODO Auto-generated constructor stub
	}

	public Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> getReversePath() {
		return reversePath;
	}

	public void setReversePath(Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> reversePath) {
		this.reversePath = reversePath;
	}


}
