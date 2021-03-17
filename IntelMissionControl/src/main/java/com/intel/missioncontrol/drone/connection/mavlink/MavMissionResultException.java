/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.MavMissionResult;

public class MavMissionResultException extends RuntimeException {
    private MavMissionResult result;

    MavMissionResultException(MavMissionResult result) {
        this.result = result;
    }

    public MavMissionResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "MavMissionResultException (" + result + ")";
    }
}
