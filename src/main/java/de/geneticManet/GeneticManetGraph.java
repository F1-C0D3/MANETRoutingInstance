package de.geneticManet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

import de.manetmodel.graph.EdgeDistance;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.Manet;
import de.manetmodel.network.Node;
import de.manetmodel.network.radio.IRadioModel;
import de.manetmodel.network.unit.DataRate;
import de.manetmodel.network.unit.Unit;
import de.manetmodel.util.Tuple;

public class GeneticManetGraph extends Manet<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> {
	private Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> flows;

	public GeneticManetGraph(Supplier<Node<EdgeDistance>> vertexSupplier, Supplier<Link<EdgeDistance>> edgeSupplier,
			IRadioModel radioModel) {
		super(vertexSupplier, edgeSupplier, radioModel);
		// TODO Auto-generated constructor stub
	}

	public void addFlow(Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> flow) {
		Iterator<Tuple<Link<EdgeDistance>, Node<EdgeDistance>>> flowIterator = flow.listIterator(1);

		while (flowIterator.hasNext()) {
			Tuple<Link<EdgeDistance>, Node<EdgeDistance>> linkAndNode = flowIterator.next();
			Link<EdgeDistance> l = linkAndNode.getFirst();
			l.setIsActive(true);
			increaseUtilizationBy(l, flow.getDataRate());
		}
	}

	public void increaseUtilizationBy(Link<EdgeDistance> l, DataRate r) {
		Set<Link<EdgeDistance>> interferedLinks = new HashSet<Link<EdgeDistance>>(l.inReceptionRange());
		Iterator<Link<EdgeDistance>> iLinkIterator = interferedLinks.iterator();
		while (iLinkIterator.hasNext()) {
			this.utilization.set(this.utilization.get() + r.get());
			Link<EdgeDistance> interferedLink = iLinkIterator.next();
			interferedLink.increaseUtilizationBy(r);
		}
	}

	public DataRate aggregateOverUtilizedLinks() {
		DataRate overUtilization = new DataRate(0L);

		for (Link<EdgeDistance> l : this.getEdges()) {
			if (l.getIsActive()) {
				DataRate tRate = l.getTransmissionRate();
				DataRate utilization = l.getUtilization();
				double oU = tRate.get() - utilization.get();
				System.out.println(getVerticesOf(l).getFirst() + "--" + getVerticesOf(l).getSecond() + ", oU: "
						+ new DataRate(oU, Unit.Type.bit).toString());
				overUtilization.set(oU < 0 ? overUtilization.get() + (long) Math.abs(oU) : overUtilization.get());
			}
		}

		return overUtilization;
	}

	/*
	 * Must be implemented
	 */
	public void removeFlow(Flow<Node<EdgeDistance>, Link<EdgeDistance>, EdgeDistance> flow) {
		for (Link<EdgeDistance> l : this.getEdges()) {
			l.setUtilization(new DataRate(0L));
			l.setIsActive(false);
		}
		utilization = new DataRate(0L);
	}
}
