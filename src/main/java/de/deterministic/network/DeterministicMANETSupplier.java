package de.deterministic.network;

import java.util.function.Supplier;

import de.deterministic.optimization.MultipleDijkstraLinkQuality;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;

public class DeterministicMANETSupplier implements
		Supplier<MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>> {

	public static class DeterministicMANETLinkSupplier implements Supplier<Link<MultipleDijkstraLinkQuality>> {
		@Override
		public Link<MultipleDijkstraLinkQuality> get() {
			return new Link<MultipleDijkstraLinkQuality>();
		}
	}

	public static class DeterministicMANETNodeSupplier implements Supplier<Node> {
		@Override
		public Node get() {
			return new Node();
		}
	}

	public static class DeterministicMANETLinkQualitySupplier implements Supplier<MultipleDijkstraLinkQuality> {
		@Override
		public MultipleDijkstraLinkQuality get() {
			return new MultipleDijkstraLinkQuality();
		}
	}

	public static class DeterministicMANETFlowSupplier
			implements Supplier<Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> {
		@Override
		public Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> get() {
			return new Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>();
		}
	}

	@Override
	public MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> get() {
		// TODO Auto-generated method stub
		return new MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>(
				new DeterministicMANETNodeSupplier(), new DeterministicMANETLinkSupplier(),
				new DeterministicMANETFlowSupplier(), null, null);
	}

}