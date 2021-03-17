/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics.provider;

import com.intel.missioncontrol.project.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.project.hardware.kinematics.IKinematicChain;

public interface IKinematicChainFactory {
    IKinematicChain createFromHardwareConfiguration(IHardwareConfiguration hardwareConfiguration);
}
