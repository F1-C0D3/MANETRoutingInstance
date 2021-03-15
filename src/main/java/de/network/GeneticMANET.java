package de.network;

import java.util.function.Supplier;

import de.manetmodel.mobility.MobilityModel;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.radio.IRadioModel;

public class GeneticMANET
		extends MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> {

	public GeneticMANET(Supplier<Node> vertexSupplier, Supplier<Link<LinkQuality>> edgeSupplier,
			Supplier<Flow<Node, Link<LinkQuality>, LinkQuality>> flowSupplier, IRadioModel radioModel,
			MobilityModel mobilityModel) {
		super(vertexSupplier, edgeSupplier, flowSupplier, radioModel, mobilityModel);
	}

	@Override
	public Link<LinkQuality> addEdge(Node source, Node target) {
		LinkQuality lq = new LinkQuality();
		lq.setDistance((double) lq.getUtilization().get());
		return super.addEdge(source, target, lq);
	}

}
