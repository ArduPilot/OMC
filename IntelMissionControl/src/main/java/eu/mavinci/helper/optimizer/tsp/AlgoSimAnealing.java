/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper.optimizer.tsp;

/**
 * Simulated Anealing - Simmuliertes Abkuehlen es wird in den Nachbarn der aktuell besten Loesung gesucht falls ein
 * solcher Nachbar besser ist, wird er direkt genommen. Falls er schlechter ist nur mit einer gewissen
 * wahrscheinlichkeit x dieses x haengt von den relativen kosten des nachbarn und von der Temperatur T des Systems ab.
 * dieses T wird im Laufe der Zeit gesenkt. Der Algorithmus wird also immer Ã¤hnlicher wie die lokale suche. Terminiert
 * wird der Algorithmus nach einer festen maximalen schritteanzahl
 *
 * @author Marco Moeller
 */
public class AlgoSimAnealing extends AlgoMeta {

    public AlgoSimAnealing(
            Object initSolution,
            CalculateCostsByPrototype prototypeCost,
            CalculateCostsByExplicit explicitCost,
            ConstructNeighborhood neighborhood) {
        super(initSolution, prototypeCost, explicitCost, neighborhood);
        mStepMaxUnchange = 25000;
        mStepMax = 40000;
    }

    @Override
    protected void calcNext() {

        // Ein erster versuch die Temperatur sinken zu lassen
        double curTemp = 0.01 * mStepMax / mStepCount;

        // ersten Nachbarn suchen, der besser ist
        for (PrototypeIterator it = mNeighborhood.constructNeighborhood(mCurrent.Solution); it.hasMoreElements(); ) {
            Object nextSol = it.nextElement().ThisSolution();
            double nextCost = mCostByExplicit.calculateCostsByExplicit(nextSol);

            // bessere immer nehmen
            if (nextCost < mCurrent.cost) {
                mCurrent = new ExplicitSolutionPair(nextSol, nextCost);
                return;
            }
            // schlechtere mit gewisser wahrscheinlichkeit trotzdem nehmen
            double prop = Math.exp((getCurrentCost() - nextCost) / curTemp);
            prop = -1;
            if (Math.random() <= prop) {
                mCurrent = new ExplicitSolutionPair(nextSol, nextCost);
                return;
            }
        }

        mHasMore = false;
    }

}
