/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper.optimizer.tsp;

/**
 * Lokale Suche nehme als naechste Loesung einfach den ersten nachbarn, der besser ist als das Aktuelle falls kein
 * besserer Nachbar mehr gefunden wurde terminiert der Algorithmus
 *
 * @author Marco Moeller
 */
public class AlgoLocalSearch extends AlgoMeta {

    public AlgoLocalSearch(
            Object initSolution,
            CalculateCostsByPrototype prototypeCost,
            CalculateCostsByExplicit explicitCost,
            ConstructNeighborhood neighborhood) {
        super(initSolution, prototypeCost, explicitCost, neighborhood);
    }

    @Override
    protected void calcNext() {
        ExplicitSolutionPair tmp = mCurrent;

        mHasMore = false;
        // ersten Nachbarn suchen, der besser ist
        for (PrototypeIterator it = mNeighborhood.constructNeighborhood(tmp.Solution); it.hasMoreElements(); ) {
            Object nextSol = it.nextElement().ThisSolution();
            double nextCost = mCostByExplicit.calculateCostsByExplicit(nextSol);
            if (nextCost < mCurrent.cost) {
                mCurrent = new ExplicitSolutionPair(nextSol, nextCost);
                mHasMore = true;
                break;
            }
        }
    }

}
