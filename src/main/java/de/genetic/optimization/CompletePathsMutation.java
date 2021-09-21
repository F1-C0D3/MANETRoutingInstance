package de.genetic.optimization;

import java.util.Random;

import de.terministic.serein.api.Mutation;
import de.terministic.serein.core.genome.ValueGenome;

public class CompletePathsMutation<G extends ValueGenome<?>> implements Mutation<G>
{

	public CompletePathsMutation()
	{
	}

	@Override
	public G mutate(G genome, Random random)
	{
		GraphGenome genes = (GraphGenome) genome;

		return (G) genes.createRandomInstance(random);
	}
}
