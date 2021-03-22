package de.parallelism;

import java.util.concurrent.Callable;

import de.deterministic.optimization.MultipleDijkstraLinkQuality;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.Node;

public abstract class Run<L, M, R>
		implements Callable<Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>> {

}
