package de.deterministic.network;

import java.util.List;
import java.util.function.Supplier;

import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.mobility.MobilityModel;
import de.manetmodel.network.mobility.MovementPattern;
import de.manetmodel.network.radio.IRadioModel;

public class DeterministicMANET
		extends MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> {

	public DeterministicMANET(Supplier<Node> vertexSupplier, Supplier<Link<LinkQuality>> edgeSupplier,
			Supplier<LinkQuality> edgreWeightSupplier,
			Supplier<Flow<Node, Link<LinkQuality>, LinkQuality>> flowSupplier, IRadioModel radioModel,
			MobilityModel mobilityModel) {
		super(vertexSupplier, edgeSupplier, edgreWeightSupplier, flowSupplier, radioModel, mobilityModel);
	}

	@Override
	public Link<LinkQuality> addEdge(Node source, Node target) {
		LinkQuality lq = new LinkQuality();
		lq.setDistance((double) lq.getUtilization().get());

		Link<LinkQuality> link = super.addEdge(source, target, lq);
		Tuple<Node, Node> sourceAndSink = getVerticesOf(link);

		link.getWeight().setSinkAndSourceMobility(new Tuple<List<MovementPattern>, List<MovementPattern>>(
				sourceAndSink.getFirst().getPrevMobility(), sourceAndSink.getSecond().getPrevMobility()));

		return link;
	}

}
