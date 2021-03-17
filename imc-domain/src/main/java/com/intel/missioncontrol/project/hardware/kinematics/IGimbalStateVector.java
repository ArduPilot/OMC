/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

/** Gimbal state vector (e.g. array of joint angles) for an IPayloadConfiguration. */
public interface IGimbalStateVector {
    double getDistanceMeasure(IGimbalStateVector stateVector);
}
