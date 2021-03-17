/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.Arc;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GriddedAngularOptimizer {

    public static class Result {
        private final double[] stateVectorDeg;
        private final double cost;

        public Result(double[] stateVectorDeg, double cost) {
            this.stateVectorDeg = stateVectorDeg;
            this.cost = cost;
        }

        public double[] getStateVectorDeg() {
            return stateVectorDeg;
        }

        public double getCost() {
            return cost;
        }
    }

    private final StateVectorGrid outerGrid;
    private final List<Arc> constraintArcs;

    GriddedAngularOptimizer(List<Arc> constraintArcs) {
        // Span an n-dimensional grid over the full allowed range of all angles in the given arcs, with
        // the given angular resolution.
        this.constraintArcs = constraintArcs;
        outerGrid = new StateVectorGrid(constraintArcs, 30.0);
    }

    /**
     * Find a stzate vector (list of angles compatible with constraint arcs) that minimizes the given cost function. The
     * optimizer spans a coarse grid of state vector angles, picks the optimum, and recursively continues with
     * finer-grained grids around the optimum until the target resolution is reached.
     */
    Result optimize(
            Function<double[], Double> costFunctionForAnglesDeg,
            @SuppressWarnings("SameParameterValue") double targetResolutionDeg) {
        return optimizeImpl(costFunctionForAnglesDeg, outerGrid, targetResolutionDeg);
    }

    private Result optimizeImpl(
            Function<double[], Double> costFunction, StateVectorGrid grid, double targetResolutionDeg) {
        double[] optimum = null;
        double minCost = Double.MAX_VALUE;

        for (double[] stateVectorDeg : grid.getStateVectors()) {
            double distanceDeg = costFunction.apply(stateVectorDeg);

            if (distanceDeg <= minCost) {
                minCost = distanceDeg;
                optimum = stateVectorDeg;
            }
        }

        if (optimum == null) {
            return null;
        }

        double resolutionDeg = grid.getAngularResolutionDeg();
        if (resolutionDeg <= targetResolutionDeg) {
            return new Result(optimum, minCost);
        }

        // recursively continue with higher resolution around current solution, until target resolution is reached
        int n = optimum.length;
        ArrayList<Arc> subConstraintArcs = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            double aDeg = optimum[i];

            Arc c = constraintArcs.get(i);
            double a1 = c.limitToArcDeg(aDeg - resolutionDeg / 2.0);
            double a2 = c.limitToArcDeg(aDeg + resolutionDeg / 2.0);

            subConstraintArcs.add(Arc.fromAnglesDeg(a1, a2));
        }

        double newResolutionDeg = resolutionDeg / 5; // TODO optimize for performance

        StateVectorGrid newGrid = new StateVectorGrid(subConstraintArcs, newResolutionDeg);
        return optimizeImpl(costFunction, newGrid, targetResolutionDeg);
    }
}
