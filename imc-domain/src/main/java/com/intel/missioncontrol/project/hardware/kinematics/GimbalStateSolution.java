/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

public class GimbalStateSolution {

    private final IGimbalStateVector stateVector;
    private final double angularDistanceDeg;
    private boolean exactSolutionFound;

    GimbalStateSolution(
            IGimbalStateVector stateVector,
            double angularDistanceDeg, boolean exactSolutionFound) {
        this.stateVector = stateVector;
        this.angularDistanceDeg = angularDistanceDeg;
        this.exactSolutionFound = exactSolutionFound;
    }

    public IGimbalStateVector getStateVector() {
        return stateVector;
    }

    public double getAngularDistanceDeg() {
        return angularDistanceDeg;
    }

    public boolean exactSolutionFound() {
        return exactSolutionFound;
    }
}
