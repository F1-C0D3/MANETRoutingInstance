package genetic;

import de.terministic.serein.api.Translator;
import program.PathComposition;

public class PathTranslator implements Translator<PathComposition, GraphGenome>
{

	@Override
	public PathComposition translate(GraphGenome genome)
	{
		PathComposition result = new PathComposition(genome.getGenes(), genome.G, genome.Flows);
		return result;
	}

}
