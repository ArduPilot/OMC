/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import com.intel.missioncontrol.drone.SpecialDuration;
import io.dronefleet.mavlink.common.FlightInformation;
import io.dronefleet.mavlink.common.Statustext;
import io.dronefleet.mavlink.common.StorageInformation;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;

public class TelemetryReceiver extends PayloadReceiver {

    private CancellationSource cancellationSource;

    public TelemetryReceiver(
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

    public Future<Void> registerStatusTextHandlerAsync(Consumer<ReceivedPayload<Statustext>> payloadReceivedFnc) {
        return registerPayloadTypeCallbackAsync(
            Statustext.class, payloadReceivedFnc, SpecialDuration.INDEFINITE, null, cancellationSource);
    }

    private <TResponse> Future<Void> periodicallyRequestTelemetryAsync(
            Duration period,
            Function<CommandProtocolSender, Future<TResponse>> requestCommandAsyncFnc,
            Consumer<ReceivedPayload<TResponse>> onReceivedFnc,
            Runnable onTimeout) {
        FutureCompletionSource<Void> fcs = new FutureCompletionSource<>(this.cancellationSource);

        CancellationSource cts = new CancellationSource();
        cancellationSource.addListener(mayInterruptIfRunning -> cts.cancel());

        CommandProtocolSender commandProtocolSender = new CommandProtocolSender(targetEndpoint, handler, cts);

        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.runLaterAsync(
            () -> {
                requestCommandAsyncFnc
                    .apply(commandProtocolSender)
                    .whenSucceeded(response -> onReceivedFnc.accept(new ReceivedPayload<>(response, targetEndpoint)))
                    .whenFailed(
                        e -> {
                            if (e.getCause() instanceof TimeoutException) {
                                onTimeout.run();
                            } else {
                                fcs.setException(e.getCause());
                            }
                        });
            },
            Duration.ofSeconds(0),
            period,
            cts);

        return fcs.getFuture().whenDone(f -> cts.cancel());
    }

    public Future<Void> periodicallyRequestFlightInformationAsync(
            Duration period, Consumer<ReceivedPayload<FlightInformation>> onReceivedFnc, Runnable onTimeout) {
        return periodicallyRequestTelemetryAsync(
            period, CommandProtocolSender::requestFlightInformationAsync, onReceivedFnc, onTimeout);
    }

    public Future<Void> periodicallyRequestStorageInformationAsync(
            Duration period, Consumer<ReceivedPayload<StorageInformation>> onReceivedFnc, Runnable onTimeout) {
        return periodicallyRequestTelemetryAsync(
            period, CommandProtocolSender::requestStorageInformationAsync, onReceivedFnc, onTimeout);
    }

}
