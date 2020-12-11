package de.shortestpathoptimization;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.swing.JFrame;

import de.manetmodel.algo.DijkstraShortestPath;
import de.manetmodel.app.gui.WeightedUndirectedGraphPanel;
import de.manetmodel.graph.Path;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.Manet;
import de.manetmodel.network.Node;
import de.manetmodel.util.Tuple;

public class ShortestPathOptimization implements Runnable {

    private Manet<Node, Link> manet;

    public ShortestPathOptimization(Manet manet, List<Flow<Node, Link>> flows) {
	this.manet = manet;
    }

    public List<Flow<Node, Link>> execute(List<Flow<Node, Link>> flows) {
	DijkstraShortestPath<Node, Link> sp = new DijkstraShortestPath(manet.getGraph());

	Iterator<Flow<Node, Link>> iterator = flows.iterator();

	Function<Tuple<Link, Node>, Double> metric = (Tuple<Link, Node> p) -> {

	    Node n = manet.getGraph().getTargetOf(p.getSecond(), p.getFirst());
	    n.getInterferedLinks();
	    Set<Link> iLinks = new HashSet<Link>(n.getInterferedLinks());
	    iLinks.addAll(p.getFirst().inReceptionRange());
	    return (double) iLinks.size();
	};
	double u = 0d;
	while (iterator.hasNext()) {
	    Flow<Node, Link> f = iterator.next();
	    Path<Node, Link> p = (sp.compute(f.getSource(), f.getTarget(), metric));
	    Iterator<Tuple<Link, Node>> iter = p.listIterator(1);
	    while (iter.hasNext()) {
		Tuple<Link, Node> next = iter.next();
		f.add(next);
	    }
	}
	u = manet.utilization(flows);
	System.out.println(u);
	return flows;
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