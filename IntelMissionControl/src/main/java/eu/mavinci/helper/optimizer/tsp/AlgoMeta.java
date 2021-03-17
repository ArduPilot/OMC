/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper.optimizer.tsp;

/**
 * Abstrakter Algorithmus. Es muss nur noch eine Methode fuer das Auffinden der naechsten Loesung implementiert werden
 *
 * @author Marco Moeller
 */
public abstract class AlgoMeta {

    // Beste überhaupt gesehene loesung
    private ExplicitSolutionPair mBest;

    // beste aktuelle loesung
    protected ExplicitSolutionPair mCurrent;

    // FunktionsKontainer zum Berechnen
    protected final CalculateCostsByPrototype mCostByPrototype;
    protected final CalculateCostsByExplicit mCostByExplicit;
    protected ConstructNeighborhood mNeighborhood;

    // Zahl der schritte
    protected int mStepCount;

    // Letzter Schritt(nummer) mit verbesserung + mStepUnchangeMax
    protected int mStepLastGood;

    // Zahl der maximalen Schritte (0->unbeschränkt)
    protected int mStepMax;

    // Zahl der maximalen Schritte OHNE verbesserung (0->nicht berücksichtigen)
    protected int mStepMaxUnchange;

    // ist Algo Terminiert (bzw.) zuviele Steps
    protected boolean mHasMore;

    // Stoppen des Fast modusses
    public boolean mStop;

    /** @return Anzahl der berechneten Schritte */
    public int StepCount() {
        return mStepCount;
    }

    // Konstruktor
    public AlgoMeta(
            Object initSolution,
            CalculateCostsByPrototype prototypeCost,
            CalculateCostsByExplicit explicitCost,
            ConstructNeighborhood neighborhood) {
        mCostByPrototype = prototypeCost;
        mCostByExplicit = explicitCost;
        mStepMax = 0;
        mStepMaxUnchange = 0;
        ResetStartSolution(initSolution, neighborhood);
    }

    // neue anfangsloesung setzen / ReINIT
    public void ResetStartSolution(Object initSolution, ConstructNeighborhood neighborhood) {
        mNeighborhood = neighborhood;
        mStop = true;
        mStepCount = 0;
        mStepLastGood = mStepMaxUnchange;
        mHasMore = true;

        double cost = mCostByExplicit.calculateCostsByExplicit(initSolution);
        mCurrent = mBest = new ExplicitSolutionPair(initSolution, cost);

        // Algo Speziefische zusatzinizialisierungen
        ResetStartSolution();
    }

    // kann bei bedarf ueberladen werden
    protected void ResetStartSolution() {}

    // Fast modus ohne call back
    public void ComputeUntilEnd() {
        ComputeUntilEnd(null);
    }

    // fast modus mit callback
    public void ComputeUntilEnd(AlgoCallBack callBack) {
        mStop = false;
        if (callBack == null) {
            while (mHasMore && !mStop) {
                nextElement();
            }
        } else {
            while (mHasMore && !mStop) {
                callBack.NewSolution(nextElement());
            }
        }
    }

    // muss von den einzelnen Algos ueberladen werden
    protected abstract void calcNext();

    // grobe rahmenhandlung vorgeben
    public Object nextElement() {
        // falls noch nicht am ende-> weitersuchen
        if (mHasMore) {
            mStepCount++;
            calcNext();
        }
        // evtl. neuen alltime besten heraussuchen
        if (mCurrent.cost < mBest.cost) {
            mStepLastGood = mStepCount + mStepMaxUnchange;
            mBest = mCurrent;
        }
        // Maximale schrittanzahl prüfen
        if (mStepMax != 0 && mStepCount >= mStepMax) {
            mHasMore = false;
        }
        // Maximale Zeit ohne verbesserung prüfen
        if (mStepMaxUnchange != 0 && mStepLastGood <= mStepCount) {
            mHasMore = false;
        }
        // falls am ende angelangt den alltime besten zurückgeben
        if (!mHasMore) {
            mCurrent = mBest;
        }

        return mCurrent.Solution;
    }

    public boolean hasMoreElements() {
        return mHasMore;
    }

    public double getCurrentCost() {
        return mCurrent.cost;
    }

    public Object getCurrentSolution() {
        return mCurrent.Solution;
    }

}
