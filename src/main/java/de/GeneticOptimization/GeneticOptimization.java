package de.GeneticOptimization;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import de.manetmodel.app.gui.WeightedUndirectedGraphPanel;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.Manet;
import de.manetmodel.network.Node;
import de.manetmodel.util.Tuple;
import de.terministic.serein.api.EvolutionEnvironment;
import de.terministic.serein.api.Mutation;
import de.terministic.serein.api.Population;
import de.terministic.serein.api.Recombination;
import de.terministic.serein.api.TerminationCondition;
import de.terministic.serein.core.AlgorithmFactory;
import de.terministic.serein.core.BasicIndividual;
import de.terministic.serein.core.Populations;
import de.terministic.serein.core.StatsListener;
import de.terministic.serein.core.selection.individual.TournamentSelection;
import de.terministic.serein.core.termination.TerminationConditionGenerations;

public class GeneticOptimization implements Runnable {

    private Manet<Node, Link> manet;
    private List<Flow<Node, Link>> flows;
    private GenesManetGraphTranslator translator;

    final int populationSize;
    final int generations;

    public GeneticOptimization(Manet manet, List<Flow<Node, Link>> flows, int populationSize, int generations) {

	this.generations = generations;
	this.populationSize = populationSize;
	this.manet = manet;
	this.flows = flows;
	translator = new GenesManetGraphTranslator(manet, flows);

    }

    public PathComposition execute() {

	Random random = new Random(1233);
	List<List<Tuple<Integer, Integer>>> manetGraphPhenotoGeno = translator.manetGraphPhenotoGeno();
	List<Tuple<Integer, Integer>> flowsPhenoToGeno = translator.flowsPhenoToGeno();
	List<List<Integer>> manetVerticesPhenoToGeno = translator.manetVerticesPhenoToGeno();
	Mutation<GraphGenome> mutation = new MultiplePathSingleMutation<GraphGenome>();

	Recombination<GraphGenome> recombination = new UniformCrossoverPathSeperator();
	FlowDistributionFitness fitness = new FlowDistributionFitness();
	TerminationCondition<PathComposition> termination = new TerminationConditionGenerations<PathComposition>(
		generations);

	// Initial individual
	GraphGenome genome = new GraphGenome(manetVerticesPhenoToGeno, manetGraphPhenotoGeno, flowsPhenoToGeno);
	BasicIndividual<PathComposition, GraphGenome> initialIndividual = new BasicIndividual<PathComposition, GraphGenome>(
		genome, translator);
	initialIndividual.<Double>setProperty("ProbabilityOfMutation", 0.0, true);
	initialIndividual.setRecombination(recombination);
	initialIndividual.setMutation(mutation);
	initialIndividual.setMateSelection(new TournamentSelection<>(fitness, 3));
	Population<PathComposition> initialPopulation = Populations.generatePopulation(initialIndividual,
		populationSize, random);
	// assembling the metaheuristic
	AlgorithmFactory<PathComposition> factory = new AlgorithmFactory<>();
	factory.termination = termination;
	EvolutionEnvironment<PathComposition> algo = factory.createReferenceEvolutionaryAlgorithm(fitness,
		initialPopulation, random);

	StatsListener<PathComposition> listener = new StatsListener<PathComposition>(fitness, 20);
	algo.addListener(listener);

	// run optimization
	algo.evolve();

	// result
	return algo.getFittest().getPhenotype();

    }

    public static int getRandomWithExclusion(Random rnd, int start, int end, List<Integer> exclude) {
	int random = start + rnd.nextInt(end - start + 1 - exclude.size());
	for (int ex : exclude) {
	    if (random < ex) {
		break;
	    }
	    random++;
	}
	return random;
    }

    @Override
    public void run() {

//	Path<Vertex, Edge> path = new Path<Vertex, Edge>(source);
//	path.add(new Tuple<Edge, Vertex>(sourceToB, b));
//	path.add(new Tuple<Edge, Vertex>(bToC, c));
//	path.add(new Tuple<Edge, Vertex>(cToTarget, target));

	WeightedUndirectedGraphPanel<Node, Link> panel = new WeightedUndirectedGraphPanel<Node, Link>(
		manet.getGraph().toVisualGraph());
//	panel.getVisualGraph().addPath(path, Color.RED);

	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	int width = (int) screenSize.getWidth() * 3 / 4;
	int height = (int) screenSize.getHeight() * 3 / 4;
	panel.setPreferredSize(new Dimension(width, height));
	panel.setFont(new Font("Consolas", Font.PLAIN, 16));
	panel.setLayout(null);
	JFrame frame = new JFrame("VisualGraphPanel");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.getContentPane().add(panel);
	frame.pack();
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);

    }
}
