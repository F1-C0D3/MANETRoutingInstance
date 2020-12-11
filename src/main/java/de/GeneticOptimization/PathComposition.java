package de.GeneticOptimization;

import java.util.Iterator;
import java.util.List;

import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.Manet;
import de.manetmodel.network.Node;

public class PathComposition {
    Manet<Node, Link> manet;
    List<Flow<Node, Link>> flows;

    public PathComposition(Manet<Node, Link> manet, List<Flow<Node, Link>> flows) {
	this.manet = manet;
	this.flows = flows;
    }

    public double computeResidualTransmissionRate() {

//	System.out.println((double) manet.utilization(flows));
	return (double) manet.utilization(flows);
    }

    @Override
    public String toString() {
	StringBuffer buffer = new StringBuffer();

	Iterator<Flow<Node, Link>> fIterator = flows.iterator();

	while (fIterator.hasNext()) {
	    Flow<Node, Link> f = fIterator.next();
	    buffer.append(f.toString());
	}
	return buffer.toString();
    }

}