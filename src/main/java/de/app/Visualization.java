package de.app;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.WindowConstants;

import de.jgraphlib.gui.VisualGraph;
import de.jgraphlib.gui.VisualGraphFrame;
import de.jgraphlib.gui.VisualGraphStyle;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;

public class Visualization<N extends Node, L extends Link<W>, W extends LinkQuality, F extends Flow<N, L, W>>
		implements Runnable {
	private VisualGraphFrame<N, L> frame;
	private VisualGraph<N, L> visualGraph;
	private MANET<N, L, W, F> manet;

	public Visualization(MANET<N, L, W, F> manet) {
		this.manet = manet;
		visualGraph = new VisualGraph<N, L>(this.manet, new VisualGraphStyle(true));
		frame = new VisualGraphFrame<N, L>(visualGraph);
	}

	@Override
	public void run() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setPreferredSize(
				new Dimension((int) screenSize.getWidth() * 3 / 4, (int) screenSize.getHeight() * 3 / 4));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	public void printPath(F flow) {
		visualGraph.addVisualPath(flow);
		frame.getVisualGraphPanel().repaint();
	}
}
