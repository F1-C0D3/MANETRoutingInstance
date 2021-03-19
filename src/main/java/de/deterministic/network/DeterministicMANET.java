package de.deterministic.network;

import java.util.List;
import java.util.function.Supplier;

import de.deterministic.optimization.MultipleDijkstraLinkQuality;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.mobility.MobilityModel;
import de.manetmodel.network.mobility.MovementPattern;
import de.manetmodel.network.radio.IRadioModel;

public class DeterministicMANET extends
		MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> {

	public DeterministicMANET(Supplier<Node> vertexSupplier, Supplier<Link<MultipleDijkstraLinkQuality>> edgeSupplier,
			Supplier<Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> flowSupplier,
			IRadioModel radioModel, MobilityModel mobilityModel) {
		super(vertexSupplier, edgeSupplier, flowSupplier, radioModel, mobilityModel);
	}

	@Override
	public Link<MultipleDijkstraLinkQuality> addEdge(Node source, Node target) {
		MultipleDijkstraLinkQuality lq = new MultipleDijkstraLinkQuality();
		lq.setDistance((double) lq.getUtilization().get());

		Link<MultipleDijkstraLinkQuality> link = super.addEdge(source, target, lq);
		Tuple<Node, Node> sourceAndSink = getVerticesOf(link);

		link.getWeight().setSinkAndSourceMobility(new Tuple<List<MovementPattern>, List<MovementPattern>>(
				sourceAndSink.getFirst().getPrevMobility(), sourceAndSink.getSecond().getPrevMobility()));

		return link;
	}

}
