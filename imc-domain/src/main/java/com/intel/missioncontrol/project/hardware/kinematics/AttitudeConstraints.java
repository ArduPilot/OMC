/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.Arc;

public class AttitudeConstraints implements IAttitudeConstraints {

    private final Arc pitchRange;
    private final double rollDeg;

    AttitudeConstraints(Arc pitchRange, double rollDeg) {
        this.pitchRange = pitchRange;
        this.rollDeg = rollDeg;
    }

    @Override
    public Arc getPitchRange() {
        return pitchRange;
    }

    @Override
    public double getRollDeg() {
        return rollDeg;
    }
}
