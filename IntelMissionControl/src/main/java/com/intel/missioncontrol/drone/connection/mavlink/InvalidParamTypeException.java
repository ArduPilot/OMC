/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

public class InvalidParamTypeException extends RuntimeException {
    private IMavlinkParameter parameter;

    InvalidParamTypeException(IMavlinkParameter parameter) {
        super("Invalid MAVLink parameter type for " + parameter.toString());
        this.parameter = parameter;
    }

    public IMavlinkParameter getParameter() {
        return parameter;
    }
}
