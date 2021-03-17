/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.GrayhawkMessagesReceiver;
import com.intel.missioncontrol.drone.connection.mavlink.GrayhawkMissionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkEndpoint;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkHandler;
import com.intel.missioncontrol.drone.connection.mavlink.MissionProtocolSender;
import io.dronefleet.mavlink.MavlinkDialect;
import io.dronefleet.mavlink.common.MavType;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides grayhawk-specific handles for communication with a connected GrayhawkDrone. Allows cancellation of all
 * ongoing transfers via its CancellationToken, effectively disconnecting the drone.
 */
public class GrayhawkDroneConnection extends MavlinkDroneConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrayhawkDroneConnection.class);

    private final GrayhawkMessagesReceiver grayhawkMessagesReceiver;

    GrayhawkDroneConnection(
            MavlinkDroneConnectionItem connectionItem,
            MavlinkHandler mavlinkHandler,
            MavType mavType,
            MavlinkDialect dialect,
            MavlinkEndpoint targetEndpoint,
            CancellationSource cancellationSource,
            ConnectionProtocolSender connectionProtocolSender,
            Future<Void> heartbeatSenderFuture,
            MavlinkCameraListener mavlinkCameraListener) {
        super(
            connectionItem,
            mavlinkHandler,
            mavType,
            dialect,
            targetEndpoint,
            cancellationSource,
            connectionProtocolSender,
            heartbeatSenderFuture,
            mavlinkCameraListener);
        grayhawkMessagesReceiver = new GrayhawkMessagesReceiver(targetEndpoint, mavlinkHandler, cancellationSource);
    }

    public GrayhawkMessagesReceiver getGrayhawkMessagesReceiver() {
        return grayhawkMessagesReceiver;
    }

    @Override
    protected MissionProtocolSender initMissionProtocolSender(
            MavlinkHandler mavlinkHandler, MavlinkEndpoint targetEndpoint, CancellationSource cancellationSource) {
        return new GrayhawkMissionProtocolSender(targetEndpoint, mavlinkHandler, cancellationSource);
    }
}
