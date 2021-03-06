package de.approximation.optimization;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.jgraphlib.util.Tuple;
import de.manetmodel.network.scalar.ScalarRadioFlow;
import de.manetmodel.network.scalar.ScalarRadioLink;
import de.manetmodel.network.scalar.ScalarRadioMANET;
import de.manetmodel.network.scalar.ScalarRadioNode;
import de.parallelism.Optimization;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;

public class CplexOptimization extends Optimization<ScalarRadioMANET> {

	private double timeConstraint;
	public CplexOptimization(ScalarRadioMANET manet,double constraint) {
		super(manet);
		this.timeConstraint = constraint;
	}

	@Override
	public ScalarRadioMANET execute() {

		return determinePaths();

	}

	private ScalarRadioMANET determinePaths() {

		System.out.println(duration.toString());
		/*
		 * path arc
		 */
		try (IloCplex cplex = new IloCplex()) {

			IloIntVar[][] x_f_l = new IloIntVar[manet.getFlows().size()][manet.getEdges().size()];
			IloIntVar[] a_l = new IloIntVar[manet.getEdges().size()];

			/*
			 * Decision varialbes initializatiopn:
			 */
			for (ScalarRadioFlow f : manet.getFlows()) {
				for (ScalarRadioLink link : manet.getEdges()) {

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

			for (ScalarRadioFlow f : manet.getFlows()) {

				for (ScalarRadioNode node : manet.getVertices()) {

					List<ScalarRadioLink> incomingEdgesOf = manet.getIncomingEdgesOf(node);
					List<ScalarRadioLink> outgoingEdgesOf = manet.getOutgoingEdgesOf(node);
					IloLinearIntExpr unsplittablePathIncoming = cplex.linearIntExpr();
					IloLinearIntExpr unsplittablePathOutgoing = cplex.linearIntExpr();
					IloLinearIntExpr nodeEqualDemand = cplex.linearIntExpr();

					for (ScalarRadioLink link : incomingEdgesOf) {
						nodeEqualDemand.addTerm(+(int) f.getDataRate().get(), x_f_l[f.getID()][link.getID()]);
						unsplittablePathIncoming.addTerm(+1, x_f_l[f.getID()][link.getID()]);
						unsplittablePathOutgoing.addTerm(0, x_f_l[f.getID()][link.getID()]);
					}

					for (ScalarRadioLink link : outgoingEdgesOf) {
						nodeEqualDemand.addTerm(-(int) f.getDataRate().get(), x_f_l[f.getID()][link.getID()]);
						unsplittablePathOutgoing.addTerm(+1, x_f_l[f.getID()][link.getID()]);
						unsplittablePathIncoming.addTerm(0, x_f_l[f.getID()][link.getID()]);
					}

					if (node.getID() == f.getSource().getID()) {
						cplex.addEq(-f.getDataRate().get(), nodeEqualDemand);
						cplex.addEq(1, unsplittablePathOutgoing);
						cplex.addEq(0, unsplittablePathIncoming);

					} else if (node.getID() == f.getTarget().getID()) {
						cplex.addEq(+f.getDataRate().get(), nodeEqualDemand);
						cplex.addEq(1, unsplittablePathIncoming);
						cplex.addEq(0, unsplittablePathOutgoing);
					} else {
						cplex.addEq(0, nodeEqualDemand);
						cplex.addGe(1, unsplittablePathIncoming);
						cplex.addGe(1, unsplittablePathOutgoing);
					}

				}

			}

			/*
			 * if x^f_l == 1 -> y_l ==0 Identifies if a link is an active link or not
			 */
			for (ScalarRadioLink currnetlink : manet.getEdges()) {

				for (ScalarRadioFlow f : manet.getFlows()) {

					cplex.add(cplex.ifThen(cplex.eq(x_f_l[f.getID()][currnetlink.getID()], 1),
							cplex.eq(a_l[currnetlink.getID()], 0)));
				}
			}

			/*
			 * Capacity constraint: Ensures that if a link is active, c_l of l is greater or
			 * equal the utilization
			 */
			for (ScalarRadioLink currentlink : manet.getEdges()) {

				IloLinearIntExpr flowExpression = cplex.linearIntExpr();

				for (ScalarRadioFlow f : manet.getFlows()) {

					for (ScalarRadioLink ul : manet.getUtilizedLinksOf(currentlink)) {

						flowExpression.addTerm((int) f.getDataRate().get(), x_f_l[f.getID()][ul.getID()]);
						flowExpression.addTerm(-(int) f.getDataRate().get(), a_l[currentlink.getID()]);

					}

				}

				cplex.addGe((int) currentlink.getTransmissionRate().get(), flowExpression);
			}

			/*
			 * Determines worst instabil path of all x_^f_l which equals 1
			 */
			IloNumExpr[] minPathStabilityArray = new IloNumExpr[manet.getFlows().size()];
			for (ScalarRadioFlow f : manet.getFlows()) {

				IloNumExpr[] maxr = new IloNumExpr[manet.getEdges().size()];
				for (ScalarRadioLink link : manet.getEdges()) {

					maxr[link.getID()] = cplex.prod(x_f_l[f.getID()][link.getID()],
							link.getWeight().getReceptionConfidence());
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

					individualLinkStabilityMatrix[k][i] = (0.60d
							* manet.getEdge(i).getWeight().getReceptionConfidence())
							+ (manet.getEdge(i).getWeight().getRelativeMobility() * 0.10d
									+ (0.30d * manet.getEdge(i).getWeight().getSpeedQuality()));
				}
			}

			IloNumExpr[] minLinkStabilityExpr = new IloNumExpr[manet.getFlows().size()];
			for (int i = 0; i < individualLinkStabilityMatrix.length; i++) {

				minLinkStabilityExpr[i] = cplex.scalProd(individualLinkStabilityMatrix[i], x_f_l[i]);

			}

			/*
			 * Individual Speed instability
			 */
			double[][] individualspeedStabilityMatrix = new double[manet.getFlows().size()][manet.getEdges().size()];

			for (int k = 0; k < individualspeedStabilityMatrix.length; k++) {

				for (int i = 0; i < individualspeedStabilityMatrix[k].length; i++) {

					individualspeedStabilityMatrix[k][i] = manet.getEdge(i).getWeight().getSpeedQuality();
				}
			}

			IloNumExpr[] minSpeedStabilityExpr = new IloNumExpr[manet.getFlows().size()];
			for (int i = 0; i < individualspeedStabilityMatrix.length; i++) {

				minSpeedStabilityExpr[i] = cplex.scalProd(individualspeedStabilityMatrix[i], x_f_l[i]);

			}

			/*
			 * Relative direction stability
			 */
			double[][] relativeDirectionStabilityMatrix = new double[manet.getFlows().size()][manet.getEdges().size()];

			for (int k = 0; k < relativeDirectionStabilityMatrix.length; k++) {

				for (int i = 0; i < relativeDirectionStabilityMatrix[k].length; i++) {

					relativeDirectionStabilityMatrix[k][i] = manet.getEdge(i).getWeight().getRelativeMobility();
				}
			}
			IloNumExpr[] minRelativeDirectionStabilityExpr = new IloNumExpr[manet.getFlows().size()];
			for (int i = 0; i < relativeDirectionStabilityMatrix.length; i++) {

				minRelativeDirectionStabilityExpr[i] = cplex.scalProd(relativeDirectionStabilityMatrix[i], x_f_l[i]);

			}

			/*
			 * Reduce number of 3 hop links which in total over utilize active links
			 */
			IloLinearNumExpr[] minHighUtilizedNearbyLinks = new IloLinearNumExpr[manet.getEdges().size()];
			for (ScalarRadioLink link : manet.getEdges()) {

				Set<ScalarRadioLink> nearbyLinks = new HashSet<ScalarRadioLink>();

				for (ScalarRadioLink ulink : manet.getUtilizedLinksOf(link)) {

					nearbyLinks.add(ulink);
					nearbyLinks.addAll(manet.getNeighboringEdgesOf(ulink));
				}

				minHighUtilizedNearbyLinks[link.getID()] = cplex.linearNumExpr();
				for (ScalarRadioFlow flow : manet.getFlows()) {

					for (ScalarRadioLink nearbyLink : nearbyLinks) {

						minHighUtilizedNearbyLinks[link.getID()].addTerm((int) flow.getDataRate().get(),
								x_f_l[flow.getID()][nearbyLink.getID()]);
						minHighUtilizedNearbyLinks[link.getID()].addTerm(-(int) flow.getDataRate().get(),
								a_l[link.getID()]);
					}

				}

			}

			cplex.addMinimize(cplex.sum(minLinkStabilityExpr));
			cplex.setParam(IloCplex.Param.RootAlgorithm, 3);
			cplex.setParam(IloCplex.Param.Threads, 1);
			cplex.setParam(IloCplex.Param.MIP.Display, 0);
			cplex.setParam(IloCplex.Param.TimeLimit, timeConstraint);
			cplex.setParam(IloCplex.Param.ClockType, 2);
			if (cplex.solve()) {

				for (int i = 0; i < x_f_l.length; i++) {
					ScalarRadioFlow flow = manet.getFlow(i);
					ScalarRadioNode node = flow.getSource();
					int index = 0;
					while (node.getID() != flow.getTarget().getID()) {
						boolean test = true;
						List<ScalarRadioLink> oLinks = manet.getOutgoingEdgesOf(node);
						for (ScalarRadioLink olink : oLinks) {

							if (cplex.getValue(x_f_l[i][olink.getID()]) >= 0.90) {
								flow.add(new Tuple<ScalarRadioLink, ScalarRadioNode>(olink, manet.getTargetOf(olink)));
								test = false;
								break;

							}

						}
						if (test) {
							System.out.println(String.format("flow ID: %d, Source node: %d, Target node: %d",
									flow.getID(), flow.getSource().getID(), flow.getTarget().getID()));
							for (ScalarRadioLink link : manet.getEdges()) {
								Tuple<ScalarRadioNode, ScalarRadioNode> verticesOf = manet.getVerticesOf(link);
								if (verticesOf.getSecond().getID() == 29 && flow.getID() == 8) {
									for (ScalarRadioLink lll : manet.getOutgoingEdgesOf(verticesOf.getSecond())) {
										System.out.println(cplex.getValue(x_f_l[i][lll.getID()]));
									}
								}
								if (cplex.getValue(x_f_l[i][link.getID()]) == 1d) {
									System.out.println(String.format("Source %d, Target %d, cplexValue: %f",
											manet.getVerticesOf(link).getFirst().getID(),
											manet.getVerticesOf(link).getSecond().getID(),
											cplex.getValue(x_f_l[i][link.getID()])));
								}
							}
						}

						index++;
						node = flow.get(index).getSecond();

					}

					manet.deployFlow(flow);

				}
				return manet;
			} else {
				System.out.println("noSolution Exists");
			}

		} catch (

		IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return manet;

	}

}
