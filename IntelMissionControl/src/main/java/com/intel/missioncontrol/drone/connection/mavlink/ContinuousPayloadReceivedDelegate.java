/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import java.time.Duration;
import java.util.function.Function;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A delegate for continuous payload reception to be registered with a handler, providing an additional callback
 * indicating timeouts. Stays registered in case of timeouts. Provides a resultFuture that never succeeds but fails in
 * case of errors. Un-registers in case of errors or cancellation.
 */
public class ContinuousPayloadReceivedDelegate implements IPayloadReceivedDelegate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContinuousPayloadReceivedDelegate.class);

    private final Function<ReceivedPayload<?>, Boolean> payloadReceivedFnc;
    private final CancellationSource cancellationSource;
    private final RefreshableTimeout refreshableTimeout;
    private final FutureCompletionSource<Void> resultFutureCompletionSource;
    private final Future<Void> resultFuture;

    ContinuousPayloadReceivedDelegate(
            Function<ReceivedPayload<?>, Boolean> payloadReceivedFnc,
            Runnable onTimeoutFnc,
            Duration timeout,
            CancellationSource externalCancellationSource) {
        this.payloadReceivedFnc = payloadReceivedFnc;
        cancellationSource = new CancellationSource();

        resultFutureCompletionSource = new FutureCompletionSource<>(cancellationSource);

        if (externalCancellationSource != null) {
            externalCancellationSource.addListener(mayInterruptIfRunning -> cancellationSource.cancel());
        }

        refreshableTimeout =
            new RefreshableTimeout(
                () -> {
                    try {
                        if (onTimeoutFnc == null) {
                            return;
                        }

                        onTimeoutFnc.run();
                    } catch (Exception e) {
                        resultFutureCompletionSource.setException(e);
                    }
                },
                timeout,
                cancellationSource);

        refreshableTimeout.refreshOrStart();
        resultFuture = resultFutureCompletionSource.getFuture();

        resultFuture.whenCancelled(cancellationSource::cancel);
    }

    public Future<Void> getResultFuture() {
        return resultFuture;
    }

    @Override
    public boolean invoke(ReceivedPayload<?> receivedPayload) {
        if (resultFuture.isDone()) {
            return true;
        }

        boolean remove = false;
        if (payloadReceivedFnc != null) {
            try {
                boolean handled = payloadReceivedFnc.apply(receivedPayload);
                if (handled) {
                    refreshableTimeout.refreshOrStart();
                }
            } catch (Exception e) {
                LOGGER.error("Error in mavlink payload handler", e);
                resultFutureCompletionSource.setException(e);
                remove = true;
            }
        }

        if (remove) {
            // cancel timeout
            cancellationSource.cancel();
        }

        return remove;
    }

}
