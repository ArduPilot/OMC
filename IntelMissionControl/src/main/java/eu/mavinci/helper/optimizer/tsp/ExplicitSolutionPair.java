/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper.optimizer.tsp;

/**
 * Datenstruktur um Kosten mit Loesungen zu assoziieren
 *
 * @author Marco Moeller
 */
class ExplicitSolutionPair {
    Object Solution;
    double cost;

    ExplicitSolutionPair() {}

    ExplicitSolutionPair(Object _Solution, double _cost) {
        Solution = _Solution;
        cost = _cost;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExplicitSolutionPair) {
            ExplicitSolutionPair tmp = (ExplicitSolutionPair)obj;
            return Solution.equals(tmp.Solution);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Solution.hashCode();
    }
}
