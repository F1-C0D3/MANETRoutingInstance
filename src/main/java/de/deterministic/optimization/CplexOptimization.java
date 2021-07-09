package de.deterministic.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import de.deterministic.algorithm.DijkstraShortestDataRateConstrainedPath;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.manetmodel.network.unit.DataRate;
import de.parallelism.Optimization;
import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class CplexOptimization<M extends MANET<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality, Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality>>>
		extends Optimization<Void, M> {

	private Random random;
	protected DijkstraShortestDataRateConstrainedPath sp;

	public CplexOptimization(M manet) {
		super(manet);
		this.sp = new DijkstraShortestDataRateConstrainedPath(manet);
		this.random = new Random();
	}

	private Function<Tuple<MultipleDijkstraLinkQuality, DataRate>, Double> metric = (tuple) -> {
		MultipleDijkstraLinkQuality linkQuality = tuple.getFirst();
		DataRate rate = tuple.getSecond();
		Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> reversePath = linkQuality
				.getReversePath();

		Tuple<Link<MultipleDijkstraLinkQuality>, Node> current = reversePath.getLast();
		double cost = current.getFirst().getWeight().getNumUtilizedLinks() * rate.get();
		manet.deployFlow(reversePath);

		if (manet.getOverUtilizedLinks().get() != 0) {
			cost = manet.getCapacity().get() + 1L;
		}
		manet.undeployFlow(reversePath);
		return cost;

	};

	public Void execute() {

		try (IloCplex cplex = new IloCplex()) {

			double[] q = new double[manet.getEdges().size()];
			IloIntVar[][] x = new IloIntVar[manet.getFlows().size()][manet.getEdges().size()];

			for (int i = 0; i < manet.getEdges().size(); i++) {
				q[i] = manet.getEdge(i).getWeight().getReceptionPower();
			}

			for (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> f : manet.getFlows()) {
				for (Link<MultipleDijkstraLinkQuality> link : manet.getEdges()) {

					x[f.getId()][link.getID()] = cplex.boolVar();
					x[f.getId()][link.getID()].setName(
							String.format("x^%d_(%d,%d)", f.getId(), manet.getVerticesOf(link).getFirst().getID(),
									manet.getVerticesOf(link).getSecond().getID()));
				}
			}

			for (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> f : manet.getFlows()) {

				for (Node node : manet.getVertices()) {

					IloLinearNumExpr flowTraversalExpression = cplex.linearNumExpr();
					List<Link<MultipleDijkstraLinkQuality>> incomingEdgesOf = manet.getIncomingEdgesOf(node);
					List<Link<MultipleDijkstraLinkQuality>> outgoingEdgesOf = manet.getOutgoingEdgesOf(node);

					for (Link<MultipleDijkstraLinkQuality> link : incomingEdgesOf) {
						flowTraversalExpression.addTerm(+1, x[f.getId()][link.getID()]);
					}

					for (Link<MultipleDijkstraLinkQuality> link : outgoingEdgesOf) {
						flowTraversalExpression.addTerm(-1, x[f.getId()][link.getID()]);
					}

					if (node.getID() == f.getSource().getID()) {
						cplex.addGe(-1, flowTraversalExpression);
					} else if (node.getID() == f.getTarget().getID()) {
						cplex.addGe(+1, flowTraversalExpression);
					} else {
						cplex.addGe(0, flowTraversalExpression);
					}
				}

			}

			/*
			 * Capacity Constraint
			 */
			for (Link<MultipleDijkstraLinkQuality> link : manet.getEdges()) {
				IloLinearNumExpr flowExpression = cplex.linearNumExpr();
				for (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> f : manet.getFlows()) {

					for (int uLinkId : link.getUtilizedLinkIds()) {
						flowExpression.addTerm(1, x[f.getId()][uLinkId]);
					}

				}
				cplex.addGe(2, flowExpression);
			}

			/*
			 * Utilization constraint DECREASE RESIDUAL CAPACITY <
			 */

			/*
			 * Optimization
			 */
			IloLinearNumExpr[] optExpression = new IloLinearNumExpr[manet.getFlows().size()];
			for (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> f : manet.getFlows()) {
				IloLinearNumExpr linearNumExpr = cplex.linearNumExpr();
				for (Link<MultipleDijkstraLinkQuality> link : manet.getEdges()) {
					if (link.getID() == 5) {

						linearNumExpr.addTerm(2, x[f.getId()][link.getID()]);
					} else {
						linearNumExpr.addTerm(1, x[f.getId()][link.getID()]);
					}
				}
				optExpression[f.getId()] = linearNumExpr;
			}
			cplex.addMinimize(cplex.sum(optExpression));

			cplex.solve();

			// Write solution value and objective to the screen.
			System.out.println("Solution status: " + cplex.getStatus());
			System.out.println("Solution value  = " + cplex.getObjValue());
			System.out.println("Solution vector:");
			for (IloNumVar[] v : x) {
				for (IloNumVar activeLink : v) {
					System.out.println(String.format("%s, %15.6f", activeLink.getName(), cplex.getValue(activeLink)));
				}
			}

//				System.out.println(String.format("%10s: %15.6f", v.getName(), cplex.getValue(v)));

			// Finally dump the model
			cplex.exportModel("FixNet.lp");
		} catch (

		IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
