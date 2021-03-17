/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

import eu.mavinci.core.obfuscation.IKeepAll;

public enum AirplaneConnectorState implements IKeepAll {
    unconnected,
    connectingTCP,
    connectedTCP, // means waiting for portlist
    portlistReceived,
    connectingDevice,
    waitingForLogreplayStart,
    fullyConnected
}
