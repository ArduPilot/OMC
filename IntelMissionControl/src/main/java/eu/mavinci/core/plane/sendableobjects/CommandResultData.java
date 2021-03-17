/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.plane.UavCommand;
import eu.mavinci.core.plane.UavCommandResult;

public class CommandResultData extends MObject {
    /*
        Entries related to command ack. Command type, result and
        any other information associated with the ack.
     */
    public UavCommand uavCommand;
    public UavCommandResult uavCommandResult;
    public String otherInformation = "";

    public CommandResultData(UavCommand command, UavCommandResult resultData, String information) {
        this.uavCommand = command;
        this.uavCommandResult = resultData;
        this.otherInformation = information;
    }
}
