package de.geneticManet;
import java.util.function.Supplier;

import de.manetmodel.graph.EdgeDistance;
import de.manetmodel.network.Link;
import de.manetmodel.network.Manet;
import de.manetmodel.network.Node;

public class GeneticManetGraphSupplier implements Supplier<Manet<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>> {

	public static class ManetLinkSupplier implements Supplier<Link<EdgeDistance>> {
		@Override
		public Link<EdgeDistance> get() {
			return new Link<EdgeDistance>();
		}
	}

	public static class ManetNodeSupplier implements Supplier<Node<EdgeDistance>> {
		@Override
		public Node<EdgeDistance> get() {
			return new Node<EdgeDistance>();
		}
	}

	@Override
	public Manet<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> get() {
		// TODO Auto-generated method stub
		return new Manet<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance>(new ManetNodeSupplier(),
				new ManetLinkSupplier(), null);
	}

}