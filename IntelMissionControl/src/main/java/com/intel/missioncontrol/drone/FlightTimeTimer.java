/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;

public class FlightTimeTimer {

    private final AsyncObjectProperty<Duration> flightTime = new SimpleAsyncObjectProperty<>(this);

    private final CancellationSource externalCancellationSource;
    private final AtomicReference<CancellationSource> cancellationSource = new AtomicReference<>(null);

    public FlightTimeTimer(CancellationSource externalCancellationSource) {
        flightTime.set(null);
        this.externalCancellationSource = externalCancellationSource;
    }

    public void update(Instant flightStartTime) {
        if (flightStartTime == null) {
            CancellationSource cs = cancellationSource.getAndSet(null);
            if (cs != null) cs.cancel();

            flightTime.set(Duration.ZERO);
        } else {
            CancellationSource cs = new CancellationSource();
            cancellationSource.set(cs);
            externalCancellationSource.addListener(cs::cancel);

            // update flight time periodically:
            Dispatcher.background()
                .runLaterAsync(
                    () -> flightTime.set(Duration.between(flightStartTime, Instant.now())),
                    Duration.ofMillis(500),
                    Duration.ofMillis(500),
                    cs);
        }
    }

    public ReadOnlyAsyncObjectProperty<Duration> flightTimeProperty() {
        return flightTime;
    }
}
