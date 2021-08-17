package de.approximation.optimization;

import java.util.List;
import java.util.Random;

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
		try (IloCplex cplex = new IloCplex()) {

			IloIntVar[][] x = new IloIntVar[manet.getFlows().size()][manet.getEdges().size()];
			IloIntVar[][] ulinks = new IloIntVar[manet.getEdges().size()][];
			IloIntVar[] y = new IloIntVar[manet.getEdges().size()];

			/*
			 * Decision varialbes initializatiopn:
			 */
			for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {
				for (Link<LinkQuality> link : manet.getEdges()) {

					x[f.getID()][link.getID()] = cplex.boolVar();
					x[f.getID()][link.getID()].setName(String.format("x^%d_[%d]_(%d,%d)", f.getID(), link.getID(),
							manet.getVerticesOf(link).getFirst().getID(),
							manet.getVerticesOf(link).getSecond().getID()));

					y[link.getID()] = cplex.boolVar();
					y[link.getID()].setName(
							String.format("y_[%d]_(%d,%d)", link.getID(), manet.getVerticesOf(link).getFirst().getID(),
									manet.getVerticesOf(link).getSecond().getID()));

				}

			}

			/*
			 * Guarantee
			 */
			for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {

				for (Node node : manet.getVertices()) {

					IloLinearNumExpr unsplittablePath = cplex.linearNumExpr();
					IloLinearNumExpr nodeEqualDemand = cplex.linearNumExpr();
					List<Link<LinkQuality>> incomingEdgesOf = manet.getIncomingEdgesOf(node);
					List<Link<LinkQuality>> outgoingEdgesOf = manet.getOutgoingEdgesOf(node);

					for (Link<LinkQuality> link : incomingEdgesOf) {
						nodeEqualDemand.addTerm(+(int) f.getDataRate().get(), x[f.getID()][link.getID()]);
						unsplittablePath.addTerm(+1, x[f.getID()][link.getID()]);
					}

					for (Link<LinkQuality> link : outgoingEdgesOf) {
						nodeEqualDemand.addTerm(-(int) f.getDataRate().get(), x[f.getID()][link.getID()]);
						unsplittablePath.addTerm(+1, x[f.getID()][link.getID()]);
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
			 * Capacity Constraint
			 */
			IloNumExpr[] max = new IloNumExpr[manet.getEdges().size()];
			for (Link<LinkQuality> currnetlink : manet.getEdges()) {

				IloNumExpr[][] hasActiveLink = new IloNumExpr[manet.getEdges().size()][manet.getFlows().size()];

				for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {

					cplex.add(cplex.ifThen(cplex.eq(x[f.getID()][currnetlink.getID()], 1),
							cplex.eq(y[currnetlink.getID()], 0)));
				}
			}

			for (Link<LinkQuality> currentlink : manet.getEdges()) {

				IloLinearIntExpr flowExpression = cplex.linearIntExpr();
				for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {
					for (Link<LinkQuality> ul : manet.getUtilizedLinksOf(currentlink)) {

						flowExpression.addTerm((int) f.getDataRate().get(), x[f.getID()][ul.getID()]);

						flowExpression.addTerm(-(int) (int) f.getDataRate().get(), y[currentlink.getID()]);

					}

				}
				cplex.addGe((int) currentlink.getWeight().getTransmissionRate().get(), flowExpression);

			}

			/*
			 * Path quality constraint
			 */
			IloNumExpr[] result = new IloNumExpr[manet.getFlows().size()];
			for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {

				IloNumExpr[] maxr = new IloNumExpr[manet.getEdges().size()];
				for (Link<LinkQuality> link : manet.getEdges()) {

					maxr[link.getID()] = cplex.prod(x[f.getID()][link.getID()], link.getWeight().getReceptionPower());
				}
				result[f.getID()] = cplex.max(maxr);
			}

			IloNumExpr minPathInstability = cplex.sum(result);
			/*
			 * Link quality constraint
			 */
			IloNumExpr[] result2 = new IloNumExpr[manet.getEdges().size()];
//
//			for (Link<LinkQuality> link : manet.getEdges()) {
//
//				result2[link.getID()] = cplex.prod(y[link.getID()], link.getWeight().getReceptionPower());
//			}
//
//			IloNumExpr minLinkinstability = cplex.sum(result2);

			IloNumExpr[] minUtilization = new IloNumExpr[manet.getEdges().size()];
			for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {

				IloNumExpr[] maxr = new IloNumExpr[manet.getEdges().size()];
				for (Link<LinkQuality> link : manet.getEdges()) {

					maxr[link.getID()] = cplex.prod(x[f.getID()][link.getID()], link.getWeight().getReceptionPower());
				}
				result[f.getID()] = cplex.max(maxr);
			}
			for (Link<LinkQuality> link : manet.getEdges()) {

				result2[link.getID()] = cplex.prod(y[link.getID()], link.getWeight().getReceptionPower());
			}
			/*
			 * Optimization
			 */
			double[][] flows = new double[manet.getFlows().size()][manet.getEdges().size()];

			for (int k = 0; k < flows.length; k++) {
				for (int i = 0; i < flows[k].length; i++) {
					flows[k][i] = manet.getEdge(i).getWeight().getReceptionPower();
				}
			}
			IloNumExpr[] result3 = new IloNumExpr[manet.getFlows().size()];

			for (int i = 0; i < flows.length; i++) {
				result3[i] = cplex.scalProd(flows[i], x[i]);

			}

//
//			for (Link<LinkQuality> currentlink : manet.getEdges()) {
//				IloLinearIntExpr[][] bla = new IloLinearIntExpr[manet.getFlows().size()][manet.getEdges().size()];
//
//				for (Flow<Node, Link<LinkQuality>, LinkQuality> f : manet.getFlows()) {
//
//					for (Link<LinkQuality> ul : manet.getUtilizedLinksOf(currentlink)) {
//
//						bla[f.getID()][currentlink.getID()].addTerm((int)f.getDataRate().get(), x[f.getID()][ul.getID()]); 
////						prod((int)f.getDataRate().get(), x[f.getID()][ul.getID()]);
//					}
//cplex.scalProd(bla[f.getID()], arg1)
//				}
//				
//				for (int i =0; i<manet.getFlows().size();i++) {
//					cplex.scalProd(bla[i], x[i]);
//				}
//
//			}

			cplex.addMinimize(cplex.sum(result3));
//			cplex.addMinimize(minLinkinstability);
//			cplex.setParam(IloCplex.Param.MIP.Limits.Solutions, 1);
			cplex.setParam(IloCplex.Param.Threads, Runtime.getRuntime().availableProcessors());

			cplex.exportModel("FixNet.lp");
			boolean res = cplex.solve();
			cplex.diff(cplex.sum(result3), 1);

			if (!res) {

			}
			// Write solution value and objective to the screen.
			System.out.println("Solution status: " + cplex.getStatus());
			System.out.println("Solution value  = " + cplex.getObjValue());
			System.out.println("Solution vector:");

			cplex.exportModel("FixNet.lp");

			for (int i = 0; i < x.length; i++) {
				Flow<Node, Link<LinkQuality>, LinkQuality> flow = manet.getFlow(i);
				int index = 0;
				Node node = flow.getSource();
				while (node.getID() != flow.getTarget().getID()) {

					List<Link<LinkQuality>> oLinks = manet.getOutgoingEdgesOf(node);

					for (Link<LinkQuality> link : oLinks) {

						if (cplex.getValue(x[i][link.getID()]) > 0) {
							manet.getTargetOf(link);
							flow.add(new Tuple<Link<LinkQuality>, Node>(link, manet.getTargetOf(link)));
							break;
						}

					}
					index++;
					node = flow.get(index).getSecond();

				}
				manet.deployFlow(flow);
			}
			for (Flow<Node, Link<LinkQuality>, LinkQuality> flow : manet.getFlows()) {
				System.out.println(flow.toString());
			}
		} catch (

		IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * node arc
		 */
//		try (IloCplex cplex = new IloCplex()) {
//
//			double[] q = new double[manet.getEdges().size()];
//			IloIntVar[][] x = new IloIntVar[manet.getFlows().size()][manet.getEdges().size()];
//
//			for (int i = 0; i < manet.getEdges().size(); i++) {
//				q[i] = 1;
//			}
//
//			for (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> f : manet.getFlows()) {
//				for (Link<MultipleDijkstraLinkQuality> link : manet.getEdges()) {
//
//					x[f.getId()][link.getID()] = cplex.intVar(0, (int) f.getDataRate().get());
//					x[f.getId()][link.getID()].setName(
//							String.format("x^%d_(%d,%d)", f.getId(), manet.getVerticesOf(link).getFirst().getID(),
//									manet.getVerticesOf(link).getSecond().getID()));
//				}
//			}
//
//			for (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> f : manet.getFlows()) {
//
//				for (Node node : manet.getVertices()) {
//
//					IloLinearNumExpr flowTraversalExpression = cplex.linearNumExpr();
//					List<Link<MultipleDijkstraLinkQuality>> incomingEdgesOf = manet.getIncomingEdgesOf(node);
//					List<Link<MultipleDijkstraLinkQuality>> outgoingEdgesOf = manet.getOutgoingEdgesOf(node);
//
//					for (Link<MultipleDijkstraLinkQuality> link : incomingEdgesOf) {
//						flowTraversalExpression.addTerm(+1, x[f.getId()][link.getID()]);
//					}
//
//					for (Link<MultipleDijkstraLinkQuality> link : outgoingEdgesOf) {
//						flowTraversalExpression.addTerm(-1, x[f.getId()][link.getID()]);
//					}
//
//					if (node.getID() == f.getSource().getID()) {
//						cplex.addGe(-f.getDataRate().get(), flowTraversalExpression);
//					} else if (node.getID() == f.getTarget().getID()) {
//						cplex.addGe(+f.getDataRate().get(), flowTraversalExpression);
//					} else {
//						cplex.addGe(0, flowTraversalExpression);
//					}
//				}
//
//			}
//
//			/*
//			 * Capacity Constraint
//			 */
//			for (Link<MultipleDijkstraLinkQuality> link : manet.getEdges()) {
//				IloLinearNumExpr flowExpression = cplex.linearNumExpr();
//
//				for (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> f : manet.getFlows()) {
//
//					for (int uLinkId : link.getUtilizedLinkIds()) {
//						flowExpression.addTerm(1, x[f.getId()][uLinkId]);
//					}
//
//					cplex.addGe(link.getWeight().getTransmissionRate().get(), flowExpression);
//
//				}
//			}
//
//			/*
//			 * Utilization constraint DECREASE RESIDUAL CAPACITY <
//			 */
//
//			/*
//			 * Optimization
//			 */
//			IloLinearNumExpr[] optExpression = new IloLinearNumExpr[manet.getFlows().size()];
//			for (Flow<Node, Link<MultipleDijkstraLinkQuality>, MultipleDijkstraLinkQuality> f : manet.getFlows()) {
//				IloLinearNumExpr linearNumExpr = cplex.linearNumExpr();
//				for (Link<MultipleDijkstraLinkQuality> link : manet.getEdges()) {
//					linearNumExpr.addTerm(1, x[f.getId()][link.getID()]);
//				}
//				optExpression[f.getId()] = linearNumExpr;
//			}
//			cplex.addMinimize(cplex.sum(optExpression));
//
//			cplex.solve();
//
//			// Write solution value and objective to the screen.
//			System.out.println("Solution status: " + cplex.getStatus());
//			System.out.println("Solution value  = " + cplex.getObjValue());
//			System.out.println("Solution vector:");
//			for (IloNumVar[] v : x) {
//				for (IloNumVar activeLink : v) {
//					System.out.println(String.format("%s, %15.6f", activeLink.getName(), cplex.getValue(activeLink)));
//				}
//			}
//
////				System.out.println(String.format("%10s: %15.6f", v.getName(), cplex.getValue(v)));
//
//			// Finally dump the model
//			cplex.exportModel("FixNet.lp");
//		} catch (
//
//		IloException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
