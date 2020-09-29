package genetic;

import de.terministic.serein.api.Translator;
import program.PathComposition;

public class PathTranslator implements Translator<PathComposition, MANETGenome>
{

	@Override
	public PathComposition translate(MANETGenome genome)
	{
		PathComposition result = new PathComposition(genome.getGenes(), genome.G, genome.SourceTarget);
		return result;
	}

}
