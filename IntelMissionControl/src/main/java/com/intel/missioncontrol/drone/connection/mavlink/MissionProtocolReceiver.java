/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.MissionCurrent;
import io.dronefleet.mavlink.common.MissionItemReached;
import java.time.Duration;
import java.util.function.Consumer;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;

public class MissionProtocolReceiver extends PayloadReceiver {
    private final CancellationSource cancellationSource;

    public MissionProtocolReceiver(
            MavlinkEndpoint targetEndpoint, MavlinkHandler handler, CancellationSource cancellationSource) {
        super(targetEndpoint, handler);
        this.cancellationSource = cancellationSource;
    }

    /** Receives MissionCurrent (upcoming waypoint update) messages. */
    public Future<Void> registerMissionCurrentHandlerAsync(
            Consumer<ReceivedPayload<MissionCurrent>> payloadReceivedFnc, Runnable onTimeout) {
        return registerPayloadTypeCallbackAsync(
            MissionCurrent.class, payloadReceivedFnc, Duration.ofSeconds(5), onTimeout, cancellationSource);
    }

    /** Receives MissionItemReached (reached waypoint update) messages. */
    public Future<Void> registerMissionItemReachedHandlerAsync(
            Consumer<ReceivedPayload<MissionItemReached>> payloadReceivedFnc, Runnable onTimeout) {
        return registerPayloadTypeCallbackAsync(
            MissionItemReached.class, payloadReceivedFnc, Duration.ofSeconds(5), onTimeout, cancellationSource);
    }
}
