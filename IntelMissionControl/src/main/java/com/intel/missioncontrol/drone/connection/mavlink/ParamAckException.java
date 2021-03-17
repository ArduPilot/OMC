/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.ParamAck;

public class ParamAckException extends RuntimeException {
    private IMavlinkParameter parameter;
    private final ParamAck paramAck;

    ParamAckException(IMavlinkParameter parameter, ParamAck paramAck) {
        this.parameter = parameter;
        this.paramAck = paramAck;
    }

    public IMavlinkParameter getParameter() {
        return parameter;
    }

    public ParamAck getParamAck() {
        return paramAck;
    }
}
