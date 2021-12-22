package de.heuristic.optimization;

import de.jgraphlib.util.RandomNumbers;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.parallelism.Optimization;

public class ACOOptimization extends Optimization<ScalarRadioMANET> {

	private RandomNumbers random;

	public ACOOptimization(ScalarRadioMANET manet, RandomNumbers random) {
		super(manet);
		this.random = random;

	}

	@Override
	public ScalarRadioMANET execute() {

		// Entire optimization is executed in this method.
		// Returns ScalarRadioMANET with DEPLOYED flows with paths.
		
		return null;

	}
}
