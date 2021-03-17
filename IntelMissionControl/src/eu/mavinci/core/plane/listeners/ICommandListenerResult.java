/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.CommandResultData;

public interface ICommandListenerResult extends IAirplaneListener {
    /* Communicate the acknowledgement for command and the results */

    public void recv_cmd_result(CommandResultData resultData);

}
