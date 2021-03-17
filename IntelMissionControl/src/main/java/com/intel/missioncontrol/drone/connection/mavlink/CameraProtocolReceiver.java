/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import java.time.Duration;
import java.util.function.Consumer;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;

public class CameraProtocolReceiver extends PayloadReceiver {

    private CancellationSource cancellationSource;

    public CameraProtocolReceiver(
            MavlinkEndpoint targetEndpoint, MavlinkHandler handler, CancellationSource cancellationSource) {
        super(targetEndpoint, handler);
        this.cancellationSource = cancellationSource;
    }

    public <TPayload> Future<Void> registerTelemetryCallbackAsync(
            Class<TPayload> payloadType,
            Consumer<ReceivedPayload<TPayload>> onReceivedFnc,
            Duration timeout,
            Runnable onTimeoutFnc) {
        return registerPayloadTypeCallbackAsync(payloadType, onReceivedFnc, timeout, onTimeoutFnc, cancellationSource);
    }

}
