/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.Heartbeat;
import io.dronefleet.mavlink.common.MavAutopilot;
import io.dronefleet.mavlink.common.MavModeFlag;
import io.dronefleet.mavlink.common.MavState;
import io.dronefleet.mavlink.common.MavType;
import java.time.Duration;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;

public class ConnectionProtocolSender extends PayloadSender {
    public ConnectionProtocolSender(
            MavlinkEndpoint recipient, MavlinkHandler handler, CancellationSource cancellationSource) {
        super(recipient, handler, cancellationSource);
    }

    public Future<Void> sendHeartbeatAsync() {
        return sendMavlinkPacketWithPayloadAsync(
            new Heartbeat.Builder()
                .type(MavType.MAV_TYPE_GCS)
                .autopilot(MavAutopilot.MAV_AUTOPILOT_INVALID)
                .baseMode(MavModeFlag.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED)
                .customMode(0)
                .systemStatus(MavState.MAV_STATE_ACTIVE)
                .mavlinkVersion(3)
                .build());
    }

    public Future<Void> startSendingHeartbeatsAsync() {
        FutureCompletionSource<Void> fcs = new FutureCompletionSource<>(this.cancellationSource);
        CancellationSource cts = new CancellationSource();
        cancellationSource.addListener(cts::cancel);

        // Start sending own heartbeats once per second:
        Dispatcher dispatcher = Dispatcher.background();
        dispatcher
            .runLaterAsync(
                () ->
                    sendHeartbeatAsync()
                        .whenFailed(
                            e -> {
                                if (!cts.isCancellationRequested()) {
                                    fcs.setException(e.getCause());
                                    cts.cancel();
                                }
                            }),
                Duration.ZERO,
                Duration.ofSeconds(1),
                cts)
            .whenFailed(e -> fcs.setException(e.getCause()))
            .whenSucceeded(v -> fcs.setResult(null));

        return fcs.getFuture();
    }
}
