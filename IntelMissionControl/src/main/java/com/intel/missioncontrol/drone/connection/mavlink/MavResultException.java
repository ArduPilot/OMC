/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.CommandLong;
import io.dronefleet.mavlink.common.MavResult;

public class MavResultException extends RuntimeException {
    private final CommandLong commandLong;
    private final MavResult result;

    MavResultException(CommandLong commandLong, MavResult result) {
        super(getMessage(commandLong, result));

        this.commandLong = commandLong;
        this.result = result;
    }

    public CommandLong getCommandLong() {
        return commandLong;
    }

    public MavResult getResult() {
        return result;
    }

    private static String getMessage(CommandLong commandLong, MavResult result)
    {
        return "Command failed: " + commandLong.command().entry().toString() + ", result: " + result + ")";
    }

    @Override
    public String toString() {
        return super.getMessage();
    }
}
