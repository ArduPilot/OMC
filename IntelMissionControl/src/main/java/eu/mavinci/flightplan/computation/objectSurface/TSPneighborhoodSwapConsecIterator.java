/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import eu.mavinci.helper.optimizer.tsp.PrototypeIterator;
import eu.mavinci.helper.optimizer.tsp.PrototypeSolution;

public class TSPneighborhoodSwapConsecIterator implements PrototypeIterator {

    int a;
    int aInit;

    private TSPsolution mOtherSolution;

    boolean mHasMore;
    int vecSize;

    public TSPneighborhoodSwapConsecIterator(Object solution) {
        mOtherSolution = (TSPsolution)solution;
        vecSize = mOtherSolution.idx.length;
        mHasMore = vecSize > 1;
        a = -1;
    }

    public PrototypeSolution nextElement() {
        mHasMore = true;
        if (a == -1) {
            a = (int)(Math.random() * (vecSize));
            aInit = a;
        } else {
            a = (a + 1) % vecSize;
            if (a == aInit) mHasMore = false;
        }

        TSPsolution s = mOtherSolution.clone();
        int aM = a;
        int bM = (a + 1) % vecSize;
        s.idx[aM] = mOtherSolution.idx[bM];
        s.idx[bM] = mOtherSolution.idx[aM];

        return s;
    }

    public boolean hasMoreElements() {
        return mHasMore;
    }
}
