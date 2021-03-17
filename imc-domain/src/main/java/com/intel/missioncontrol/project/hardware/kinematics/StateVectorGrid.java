/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.Arc;
import java.util.ArrayList;
import java.util.List;

/**
 * A grid over all combinations of angles in a gimbal state vector, with resulting yaw/pitch/roll of the optical axis.
 */
class StateVectorGrid {
    private final double angularResolutionDeg;
    private final List<double[]> stateVectors;

    /**
     * Span an n-dimensional grid over the given constraint arcs corresponding to state vector angles, with the
     * given angular resolution.
     */
    StateVectorGrid(List<Arc> constraintArcs, double angularResolutionDeg) {
        this.angularResolutionDeg = angularResolutionDeg;

        // For each controllable gimbal segment, span an array of allowed joint angles, with the given minimal angular
        // resolution. Outer dimension refers to the controllable gimbal segment index
        List<double[]> linSpaceDeg = createAngularGridDeg(constraintArcs, angularResolutionDeg);

        int nSegments = linSpaceDeg.size();

        // Create state vector array for each combination:
        stateVectors = new ArrayList<>();
        int[] currentIndex = new int[nSegments];
        double[] stateVector = new double[nSegments];
        for (int i = 0; i < nSegments; i++) {
            stateVector[i] = linSpaceDeg.get(i)[0];
        }

        int i = 0;
        boolean ok;
        do {
            stateVectors.add(stateVector.clone());

            ok = false;

            while (i < nSegments) {
                if (currentIndex[i] < linSpaceDeg.get(i).length - 1) {
                    currentIndex[i]++;
                    stateVector[i] = linSpaceDeg.get(i)[currentIndex[i]];
                    i = 0;
                    ok = true;
                    break;
                }

                currentIndex[i] = 0;
                stateVector[i] = linSpaceDeg.get(i)[currentIndex[i]];

                i++;
            }
        } while (ok);
    }

    /**
     * Create a grid of angles, with the given minimal angular resolution. Outer dimension refers to the constraint arc
     * index.
     */
    private List<double[]> createAngularGridDeg(List<Arc> constraintArcs, double angularResolutionDeg) {
        int n = constraintArcs.size();

        List<double[]> linSpaceDeg = new ArrayList<>(n);
        for (Arc constraintArc : constraintArcs) {
            linSpaceDeg.add(constraintArc.toLinearlySpacedAnglesDeg(angularResolutionDeg));
        }

        return linSpaceDeg;
    }

    List<double[]> getStateVectors() {
        return stateVectors;
    }

    public double getAngularResolutionDeg() {
        return angularResolutionDeg;
    }
}
