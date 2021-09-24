/*
 *
 * Copyright (c) 2004-2008 Arizona State University.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ARIZONA STATE UNIVERSITY ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL ARIZONA STATE UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.genetic.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import de.jgraphlib.util.Tuple;

public class InstructedIndividualPathGeneration {
	private List<List<Integer>> graphGenes;
	private List<List<Integer>> candidates;
	ArrayList<List<Integer>> ksp;
	List<Set<Integer>> removedEdges;
	private int sourceGene;
	private int targetGene;

	private int spurPos;
	private int round;

	public InstructedIndividualPathGeneration(List<List<Integer>> graphGenes, int sourceGene, int targetGene) {
		this.graphGenes = graphGenes;
		this.sourceGene = sourceGene;
		this.targetGene = targetGene;
		_init();
	}

	private void _init() {

		this.spurPos = 0;
		this.round = 0;
		this.candidates = new ArrayList<List<Integer>>();
		this.ksp = new ArrayList<List<Integer>>();
		List<Integer> individuals = instructedIndividual(sourceGene, targetGene);
		ksp.add(individuals);

		this.removedEdges = new ArrayList<Set<Integer>>();
		for (int i = 0; i < graphGenes.size(); i++) {
			removedEdges.add(new HashSet<Integer>());
		}
	}

	public int getSourceGene() {
		return sourceGene;
	}

	public void setSourceGene(int sourceGene) {
		this.sourceGene = sourceGene;
	}

	public int getTargetGene() {
		return targetGene;
	}

	public void setTargetGene(int targetGene) {
		this.targetGene = targetGene;
	}

	private List<Integer> instructedIndividual(int sourceGene, int targetGene) {

		int currentGene = sourceGene;
		List<Integer> genes = new ArrayList<Integer>();
		List<Tuple<Integer, Integer>> predDist = new ArrayList<Tuple<Integer, Integer>>();
		for (int i = 0; i < graphGenes.size(); i++) {
			genes.add(i);

			if (i == sourceGene) {
				predDist.add(new Tuple<Integer, Integer>(-1, 0));
			} else {
				predDist.add(new Tuple<Integer, Integer>(-1, Integer.MAX_VALUE));
			}
		}

		while (!genes.isEmpty()) {
			Integer nId = minDistance(predDist, genes);
			genes.remove(nId);
			currentGene = nId;

			if (currentGene == targetGene) {
				return generateSP(predDist, sourceGene, targetGene);
			}

			for (int i = 0; i < graphGenes.get(currentGene).size(); i++) {
				int neighborGene = graphGenes.get(currentGene).get(i);

				int oldPahtDist = predDist.get(neighborGene).getSecond();
				int altPathDist = 1 + predDist.get(currentGene).getSecond();

				if (altPathDist < oldPahtDist) {
					predDist.get(neighborGene).setFirst(currentGene);
					predDist.get(neighborGene).setSecond(altPathDist);
				}
			}
		}
		return null;
	}

	protected List<Integer> generateSP(List<Tuple<Integer, Integer>> predDist, int sourceGene, int targetGene) {
		List<Integer> individuals = new ArrayList<Integer>();

		do {
			int predGene = predDist.get(targetGene).getFirst();

			individuals.add(0, targetGene);

			targetGene = predGene;
		} while (targetGene != sourceGene);

		individuals.add(0, sourceGene);
		return individuals;
	}

	protected Integer minDistance(List<Tuple<Integer, Integer>> predGenes, List<Integer> genes) {
		int id = -1;
		double result = Integer.MAX_VALUE;
		ListIterator<Tuple<Integer, Integer>> it = predGenes.listIterator();

		while (it.hasNext()) {
			Tuple<Integer, Integer> pred = it.next();

			if (genes.contains(it.previousIndex()) && pred.getSecond() < result) {
				result = pred.getSecond();
				id = it.previousIndex();
			}
		}
		return id;
	}

	public List<Integer> generateNewIndividual() {

		List<Integer> previousPath = ksp.get(round);

		int spurNode = previousPath.get(spurPos);

		// Root path = prefix portion of the (k-1)st path up to the spur node
		List<Integer> rootPath = new ArrayList<Integer>(previousPath.subList(0, spurPos));

		/* Iterate over all of the (k-1) shortest paths */
		for (List<Integer> p : ksp) {

			List<Integer> stub = new ArrayList<Integer>(p.subList(0, spurPos));
			// Check to see if this path has the same prefix/root as the (k-1)st shortest
			// path
			if (rootPath.equals(stub)) {
				/*
				 * If so, eliminate the next edge in the path from the graph (later on, this
				 * forces the spur node to connect the root path with an un-found suffix path)
				 */
				if (p.size() - 1 != spurPos) {
					int sourceGene = p.get(spurPos);
					int sinkGene = p.get(spurPos + 1);

					List<Integer> sourceNeighborGenes = this.graphGenes.get(sourceGene);

					for (int l = 0; l < sourceNeighborGenes.size(); l++) {
						if (sourceNeighborGenes.get(l) == sinkGene) {
							sourceNeighborGenes.remove(l);
							removedEdges.get(sourceGene).add(sinkGene);
							break;
						}
					}

				}

				for (Integer rootPathGene : rootPath) {

					if (rootPathGene != spurNode) {
						removedEdges.get(rootPathGene)
								.addAll(new ArrayList<Integer>(this.graphGenes.get(rootPathGene)));
						this.graphGenes.get(rootPathGene).clear();
					}
				}
			}
		}


		spurPos++;
		if (this.graphGenes.get(spurNode).size() != 0) {
			List<Integer> spurPath = instructedIndividual(spurNode, targetGene);

			// If a new spur path was identified...
			if (spurPath != null) {
				// Concatenate the root and spur paths to form the new candidate path
				List<Integer> totalPath = new ArrayList<Integer>(rootPath);
				totalPath.addAll(spurPath);

				for (int i = 0; i < removedEdges.size(); i++) {
					for (Integer neighborGene : removedEdges.get(i)) {

						List<Integer> neighborGeneList = this.graphGenes.get(i);

						if (!neighborGeneList.contains(neighborGene))
							neighborGeneList.add(neighborGene);
					}
				}
				// If candidate path has not been generated previously, add it
				if (!candidates.contains(totalPath)) {
					candidates.add(totalPath);

					if (ksp.get(round).size() - 1 == spurPos) {

						candidates.sort((a, b) -> a.size() - b.size());
						ksp.add(++round, candidates.get(0));
						candidates.remove(0);
						spurPos = 0;
					}
					return totalPath;
				}

			}
		}
		return null;

	}

}
