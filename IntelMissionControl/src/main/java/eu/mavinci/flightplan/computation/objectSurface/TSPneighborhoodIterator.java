/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import eu.mavinci.helper.optimizer.tsp.PrototypeIterator;
import eu.mavinci.helper.optimizer.tsp.PrototypeSolution;

public class TSPneighborhoodIterator implements PrototypeIterator {

    int a;
    int b;
    int aInit;
    int bInit;

    private TSPsolution mOtherSolution;

    boolean mHasMore;
    int vecSize;

    public TSPneighborhoodIterator(Object solution) {
        mOtherSolution = (TSPsolution)solution;
        vecSize = mOtherSolution.idx.length;
        mHasMore = vecSize > 1;
        a = -1;
    }

    public PrototypeSolution nextElement() {
        mHasMore = true;
        if (a == -1) {

            // mit zufälliger änderung beginnen, damit die algorithmen
            // vernünftig funktionieren
            int a = (int)(Math.random() * (vecSize - 1));
            int b =
                a
                    + 1
                    + (int)(Math.random() * (vecSize - a - 1)); // so umgehe ich, dass eine kante doppelt gewürfelt wird
            this.aInit = a;
            this.bInit = b;
            this.a = a;
            this.b = b;
        } else {
            // die beiden punkte müssen immer noch
            // Rechten punkt weiter schieben
            // es soll immer gelten 0<=a < b <vecSize
            b++;
            // falls über den Rand hinaus, den linken nachziehen, und..
            if (b == vecSize) {
                a++;

                // .. den rechten 2 davon entfernt inizialisieren
                b = a + 1;
                // Wenn jetzt wieder der Rand erreicht wars das.. einmal rum!
                if (b == vecSize) {
                    a = 0;
                    b = 1;
                }
            }

            if (a == aInit && b == bInit) mHasMore = false;
        }

        TSPsolution s = mOtherSolution.clone();
        int aM = a;
        int bM = b;
        for (; aM < bM; aM++, bM--) {
            s.idx[aM] = mOtherSolution.idx[bM];
            s.idx[bM] = mOtherSolution.idx[aM];
        }

        return s;
    }

    public boolean hasMoreElements() {
        return mHasMore;
    }

}
