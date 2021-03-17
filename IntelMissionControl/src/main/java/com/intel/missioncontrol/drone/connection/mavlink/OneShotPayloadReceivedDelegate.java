/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;

/**
 * A delegate for one-time payload reception to be registered with a handler, providing a resultFuture that returns
 * successfully handled data, or fails indicating errors or timeout, whichever occurs first. Un-registers in any case.
 */
public class OneShotPayloadReceivedDelegate<TRes> implements IPayloadReceivedDelegate {
    private final Function<ReceivedPayload<?>, TRes> payloadReceivedFnc;
    private CancellationSource cancellationSource;
    private AtomicBoolean isExternallyCanceled;
    private FutureCompletionSource<TRes> fcs;
    private Exception resultException;
    private TRes result;

    OneShotPayloadReceivedDelegate(
            Function<ReceivedPayload<?>, TRes> payloadReceivedFnc,
            Duration timeout,
            CancellationSource externalCancellationSource) {
        this.payloadReceivedFnc = payloadReceivedFnc;
        isExternallyCanceled = new AtomicBoolean(false);
        cancellationSource = new CancellationSource();
        resultException = null;

        if (externalCancellationSource != null) {
            externalCancellationSource.addListener(
                mayInterruptIfRunning -> {
                    if (externalCancellationSource.isCancellationRequested()) {
                        isExternallyCanceled.set(true);
                        cancellationSource.cancel();
                    }
                });
        }

        fcs = new FutureCompletionSource<>();

        // timeout timer:
        Dispatcher.background()
            .runLaterAsync(() -> {}, timeout, cancellationSource)
            .whenDone(
                (f) -> {
                    if (f.isSuccess()) {
                        // timeout:
                        fcs.setException(new TimeoutException());
                    } else if (f.isCancelled()) {
                        if (resultException != null) {
                            // canceled because of exception in delegate handler.
                            var cause = resultException.getCause();
                            if (cause != null) {
                                fcs.setException(cause);
                            } else {
                                fcs.setException(resultException);
                            }
                        } else if (isExternallyCanceled.get()) {
                            // externally canceled
                            fcs.setCancelled();
                        } else {
                            // timeout itself was canceled, indicating payload was received successfully:
                            fcs.setResult(result);
                        }
                    } else if (f.isFailed()) {
                        //noinspection ConstantConditions
                        fcs.setException(f.getException().getCause());
                    }
                });
    }

    public Future<TRes> getResultFuture() {
        return fcs.getFuture();
    }

    @Override
    public boolean invoke(ReceivedPayload<?> receivedPayload) {
        boolean remove;

        if (payloadReceivedFnc == null || isExternallyCanceled.get()) {
            remove = true;
        } else {
            try {
                result = payloadReceivedFnc.apply(receivedPayload);
                remove = (result != null);
            } catch (Exception e) {
                remove = true;
                resultException = e;
            }
        }

        if (remove && cancellationSource != null) {
            // cancel timeout
            cancellationSource.cancel();
        }

        return remove;
    }
}
