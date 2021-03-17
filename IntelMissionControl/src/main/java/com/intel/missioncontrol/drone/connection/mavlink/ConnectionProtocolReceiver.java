/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import com.intel.missioncontrol.drone.SpecialDuration;
import io.dronefleet.mavlink.common.Heartbeat;
import java.time.Duration;
import java.util.function.Consumer;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;

public class ConnectionProtocolReceiver extends PayloadReceiver {
    private static final double defaultLinkLostTimeoutSeconds = 5.0;

    private final CancellationSource cancellationSource;

    public ConnectionProtocolReceiver(
            MavlinkEndpoint targetEndpoint, MavlinkHandler handler, CancellationSource cancellationSource) {
        super(targetEndpoint, handler);
        this.cancellationSource = cancellationSource;
    }

    public Future<Void> registerHeartbeatHandlerAsync(
            Consumer<ReceivedPayload<Heartbeat>> payloadReceivedFnc, Runnable onTimeout) {
        return registerHeartbeatHandlerAsync(payloadReceivedFnc, defaultLinkLostTimeoutSeconds, onTimeout);
    }

    public Future<Void> registerHeartbeatHandlerAsync(
            Consumer<ReceivedPayload<Heartbeat>> payloadReceivedFnc,
            double linkLostTimeoutSeconds,
            Runnable onTimeout) {
        return registerPayloadTypeCallbackAsync(
            Heartbeat.class,
            payloadReceivedFnc,
            linkLostTimeoutSeconds > 0
                ? Duration.ofMillis((long)(linkLostTimeoutSeconds * 1000))
                : SpecialDuration.INDEFINITE,
            onTimeout,
            cancellationSource);
    }
}
