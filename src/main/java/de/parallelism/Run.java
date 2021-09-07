package de.parallelism;

import java.util.concurrent.Callable;

import de.manetmodel.network.Flow;
import de.manetmodel.network.scalar.ScalarLinkQuality;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioNode;

public abstract class Run<L, M, R>
		implements Callable<Flow<ScalarRadioNode, ScalarRadioLink, ScalarLinkQuality>> {

}
