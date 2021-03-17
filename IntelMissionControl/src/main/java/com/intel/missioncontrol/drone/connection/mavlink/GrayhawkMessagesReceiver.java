/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.obstacle_avoidance.ObstacleAvoidanceStatus;
import java.time.Duration;
import java.util.function.Consumer;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;

public class GrayhawkMessagesReceiver extends PayloadReceiver {
    private final CancellationSource cancellationSource;

    public GrayhawkMessagesReceiver(
            MavlinkEndpoint targetEndpoint, MavlinkHandler handler, CancellationSource cancellationSource) {
        super(targetEndpoint, handler);
        this.cancellationSource = cancellationSource;
    }

    /** Receives ObstacleAvoidanceStatus messages. */
    public Future<Void> registerObstacleAvoidanceStatusHandlerAsync(
            Consumer<ReceivedPayload<ObstacleAvoidanceStatus>> payloadReceivedFnc, Runnable onTimeout) {
        return registerPayloadTypeCallbackAsync(
            ObstacleAvoidanceStatus.class, payloadReceivedFnc, Duration.ofSeconds(5), onTimeout, cancellationSource);
    }
}
