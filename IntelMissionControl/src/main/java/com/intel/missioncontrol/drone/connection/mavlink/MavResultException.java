/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.MavResult;

public class MavResultException extends RuntimeException {
    private MavResult result;

    MavResultException(MavResult result) {
        this.result = result;
    }

    public MavResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "MavResultException (" + result + ")";
    }
}
