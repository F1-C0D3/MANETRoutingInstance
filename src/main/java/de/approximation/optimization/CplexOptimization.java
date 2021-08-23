package de.approximation.optimization;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.deterministic.algorithm.DijkstraShortesFlow;
import de.jgraphlib.util.Tuple;
import de.manetmodel.network.Flow;
import de.manetmodel.network.Link;
import de.manetmodel.network.LinkQuality;
import de.manetmodel.network.MANET;
import de.manetmodel.network.Node;
import de.parallelism.Optimization;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

public class CplexOptimization<M extends MANET<Node, Link<LinkQuality>, LinkQuality, Flow<Node, Link<LinkQuality>, LinkQuality>>>
		extends Optimization<Void, M> {

	private Random random;

	public CplexOptimization(M manet) {
		super(manet);
		this.random = new Random();
	}

	public Void execute() {

		determinePaths();

		return null;
	}

	private void determinePaths() {

		/*
		 * path arc
		 */
		System.out.println(manet.getEdge(46).getWeight().isActive());
		try (IloCplex cplex = new IloCplex()) {

			IloIntVar[][] x_f_l = new IloIntVar[manet.getFlows().size()][manet.getEdges().size()];
			IloIntVar[][] ulinks = new IloIntVar[manet.getEdges().size()][];
			IloIntVar[] a_l = new IloIntVar[manet.getEdges().size()];

			/*
			 * Decision varialbes initializatiopn:
			 */
			for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {
				for (Link<LinkQuality> link : manet.getEdges()) {

					x_f_l[f.getID()][link.getID()] = cplex.boolVar();
					x_f_l[f.getID()][link.getID()].setName(String.format("x^%d_[%d]_(%d,%d)", f.getID(), link.getID(),
							manet.getVerticesOf(link).getFirst().getID(),
							manet.getVerticesOf(link).getSecond().getID()));

					a_l[link.getID()] = cplex.boolVar();
					a_l[link.getID()].setName(String.format("a_l_[%d]_(%d,%d)", link.getID(),
							manet.getVerticesOf(link).getFirst().getID(),
							manet.getVerticesOf(link).getSecond().getID()));

				}

			}

			/*
			 * Guarantee same amount at incoming edges goes out at outgoing edges This also
			 * includes unsplittable path guarantee
			 */
			for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {

				for (Node node : manet.getVertices()) {

					IloLinearNumExpr unsplittablePath = cplex.linearNumExpr();
					IloLinearNumExpr nodeEqualDemand = cplex.linearNumExpr();
					List<Link<LinkQuality>> incomingEdgesOf = manet.getIncomingEdgesOf(node);
					List<Link<LinkQuality>> outgoingEdgesOf = manet.getOutgoingEdgesOf(node);

					for (Link<LinkQuality> link : incomingEdgesOf) {
						nodeEqualDemand.addTerm(+(int) f.getDataRate().get(), x_f_l[f.getID()][link.getID()]);
						unsplittablePath.addTerm(+1, x_f_l[f.getID()][link.getID()]);
					}

					for (Link<LinkQuality> link : outgoingEdgesOf) {
						nodeEqualDemand.addTerm(-(int) f.getDataRate().get(), x_f_l[f.getID()][link.getID()]);
						unsplittablePath.addTerm(+1, x_f_l[f.getID()][link.getID()]);
					}

					if (node.getID() == f.getSource().getID()) {
						cplex.addGe(-(int) f.getDataRate().get(), nodeEqualDemand);
					} else if (node.getID() == f.getTarget().getID()) {
						cplex.addGe(+(int) f.getDataRate().get(), nodeEqualDemand);
					} else {
						cplex.addGe(0, nodeEqualDemand);
					}

					cplex.addGe(2, unsplittablePath);
				}

			}

			/*
			 * if x^f_l == 1 -> y_l ==0 Identifies if a link is an active link or not
			 */
			IloNumExpr[] max = new IloNumExpr[manet.getEdges().size()];
			for (Link<LinkQuality> currnetlink : manet.getEdges()) {

				IloNumExpr[][] hasActiveLink = new IloNumExpr[manet.getEdges().size()][manet.getFlows().size()];

				for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {

					cplex.add(cplex.ifThen(cplex.eq(x_f_l[f.getID()][currnetlink.getID()], 1),
							cplex.eq(a_l[currnetlink.getID()], 0)));
				}
			}

			/*
			 * Capacity constraint: Ensures that if a link is active, c_l of l is greater or
			 * equal the utilization
			 */
			for (Link<LinkQuality> currentlink : manet.getEdges()) {

				IloLinearIntExpr flowExpression = cplex.linearIntExpr();

				for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {

					for (Link<LinkQuality> ul : manet.getUtilizedLinksOf(currentlink)) {

						flowExpression.addTerm((int) f.getDataRate().get(), x_f_l[f.getID()][ul.getID()]);
						flowExpression.addTerm(-(int) f.getDataRate().get(), a_l[currentlink.getID()]);

					}

				}

				cplex.addGe((int) currentlink.getWeight().getTransmissionRate().get(), flowExpression);
			}

			/*
			 * Determines worst instabil path of all x_^f_l which equals 1
			 */
			IloNumExpr[] minPathStabilityArray = new IloNumExpr[manet.getFlows().size()];
			for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {

				IloNumExpr[] maxr = new IloNumExpr[manet.getEdges().size()];
				for (Link<LinkQuality> link : manet.getEdges()) {

					maxr[link.getID()] = cplex.prod(x_f_l[f.getID()][link.getID()],
							link.getWeight().getReceptionPower());
				}
				minPathStabilityArray[f.getID()] = cplex.max(maxr);
			}

			IloNumExpr minPathInstabilityExpr = cplex.sum(minPathStabilityArray);

			/*
			 * Individual link instability
			 */
			double[][] individualLinkStabilityMatrix = new double[manet.getFlows().size()][manet.getEdges().size()];

			for (int k = 0; k < individualLinkStabilityMatrix.length; k++) {

				for (int i = 0; i < individualLinkStabilityMatrix[k].length; i++) {

					individualLinkStabilityMatrix[k][i] = manet.getEdge(i).getWeight().getReceptionPower();
				}
			}

			IloNumExpr[] minLinkStabilityExpr = new IloNumExpr[manet.getFlows().size()];
			for (int i = 0; i < individualLinkStabilityMatrix.length; i++) {

				minLinkStabilityExpr[i] = cplex.scalProd(individualLinkStabilityMatrix[i], x_f_l[i]);

			}

			/*
			 * Reduce number of 3 hop links which in total over utilize active links
			 */
			IloLinearNumExpr[] minHighUtilizedNearbyLinks = new IloLinearNumExpr[manet.getEdges().size()];
			IloNumExpr[] minHighUtilizedNearbyLinksExpr = new IloNumExpr[manet.getEdges().size()];
			for (Link<LinkQuality> link : manet.getEdges()) {

				Set<Link<LinkQuality>> nearbyLinks = new HashSet<Link<LinkQuality>>();

				for (Link<LinkQuality> ulink : manet.getUtilizedLinksOf(link)) {

					nearbyLinks.add(ulink);
					nearbyLinks.addAll(manet.getNeighboringEdgesOf(ulink));
				}

				minHighUtilizedNearbyLinks[link.getID()] = cplex.linearNumExpr();
				for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : manet.getFlows()) {

					for (Link<LinkQuality> nearbyLink : nearbyLinks) {

						minHighUtilizedNearbyLinks[link.getID()].addTerm((int) flow.getDataRate().get(),
								x_f_l[flow.getID()][nearbyLink.getID()]);
						minHighUtilizedNearbyLinks[link.getID()].addTerm(-(int) flow.getDataRate().get(),
								a_l[link.getID()]);
					}

				}

//				minHighUtilizedNearbyLinksRange[link.getID()] = cplex.sum(minHighUtilizedNearbyLinks);
				minHighUtilizedNearbyLinksExpr[link.getID()] = cplex.ge(link.getWeight().getTransmissionRate().get(),
						minHighUtilizedNearbyLinks[link.getID()]);
			}

//			cplex.addMinimize(cplex.sum(minPathInstabilityExpr, cplex.sum(minLinkStabilityExpr)));
//			cplex.addMinimize(cplex.sum(cplex.sum(cplex.sum(minHighUtilizedNearbyLinksExpr), cplex.sum(minLinkStabilityExpr)),minPathInstabilityExpr));
			cplex.addMinimize(cplex.sum(minLinkStabilityExpr));
//			cplex.setParam(IloCplex.Param.MIP.Limits.Solutions, 1);
			cplex.setParam(IloCplex.Param.Threads, Runtime.getRuntime().availableProcessors());
//			cplex.setParam(IloCplex.Param.Tune.TimeLimit,1);

			cplex.solve();
			// Write solution value and objective to the screen.
			System.out.println("Solution status: " + cplex.getStatus());
			System.out.println("Solution value  = " + cplex.getObjValue());
			System.out.println("Solution vector:");

			cplex.exportModel("FixNet.lp");

			for (int i = 0; i < x_f_l.length; i++) {
				Flow<Node, Link<LinkQuality>, LinkQuality> flow = manet.getFlow(i);
				int index = 0;
				Node node = flow.getSource();
				while (node.getID() != flow.getTarget().getID()) {

					List<Link<LinkQuality>> oLinks = manet.getOutgoingEdgesOf(node);

					for (Link<LinkQuality> link : oLinks) {
						if (cplex.getValue(x_f_l[i][link.getID()]) > 0) {
							flow.add(new Tuple<Link<LinkQuality>, Node>(link, manet.getTargetOf(link)));
							break;
						}

					}
					index++;
					node = flow.get(index).getSecond();

				}

				manet.deployFlow(flow);
			}
		} catch (

		IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
