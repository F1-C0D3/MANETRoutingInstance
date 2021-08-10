package de.deterministic.network;

import java.util.function.Supplier;

import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;

public class DeterministicMANETSupplier
		implements Supplier<MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>> {

	public static class DeterministicMANETLinkSupplier implements Supplier<Link<LinkQuality>> {
		@Override
		public Link<LinkQuality> get() {
			return new Link<LinkQuality>();
		}
	}

	public static class DeterministicMANETNodeSupplier implements Supplier<Node> {
		@Override
		public Node get() {
			return new Node();
		}
	}

	public static class DeterministicMANETLinkQualitySupplier implements Supplier<LinkQuality> {
		@Override
		public LinkQuality get() {
			return new LinkQuality();
		}
	}

	public static class DeterministicMANETFlowSupplier implements Supplier<Flow<Node, Link<LinkQuality>, LinkQuality>> {
		@Override
		public Flow<Node, Link<LinkQuality>, LinkQuality> get() {
			return new Flow<Node, Link<LinkQuality>, LinkQuality>();
		}
	}

	@Override
	public MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>> get() {
		// TODO Auto-generated method stub
		return new MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>(
				new DeterministicMANETNodeSupplier(), new DeterministicMANETLinkSupplier(),
				new DeterministicMANETLinkQualitySupplier(), new DeterministicMANETFlowSupplier(), null, null);
	}

}