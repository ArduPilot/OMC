/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;

/**
 * A MAVLink message receiver, allowing to listen for and reply to messages from a target endpoint. Usually used for
 * listening and responding to communication initiated by the target, as part of a MAVLink microservice protocol
 * implementation.
 */
class PayloadReceiver {

    protected final MavlinkEndpoint targetEndpoint;
    protected final MavlinkHandler handler;

    PayloadReceiver(MavlinkEndpoint targetEndpoint, MavlinkHandler handler) {
        this.targetEndpoint = targetEndpoint;
        this.handler = handler;
    }

    /**
     * Register a callback for all received payloads from the correct endpoint matching the given payload type, as well
     * as a timeout callback (counted since last package reception). Returns a future which fires when de-registered.
     * The future indicates success when cancelled, or fails in case of errors.
     */
    <TPayload> Future<Void> registerPayloadTypeCallbackAsync(
            Class<TPayload> payloadType,
            Consumer<ReceivedPayload<TPayload>> onReceivedFnc,
            Duration timeout,
            Runnable onTimeoutFnc,
            CancellationSource cancellationSource) {
        return registerPayloadCallbackAsync(
            payloadType,
            p -> {
                // Apply if correct target endpoint:
                return targetEndpoint.equals(MavlinkEndpoint.UnspecifiedUdp)
                    || p.getSenderEndpoint().getAddress().equals(targetEndpoint.getAddress())
                        && p.getSenderEndpoint().getSystemId() == targetEndpoint.getSystemId()
                        && (targetEndpoint.getComponentId() == MavlinkEndpoint.AllComponentIds
                            || p.getSenderEndpoint().getComponentId() == MavlinkEndpoint.AllComponentIds
                            || p.getSenderEndpoint().getComponentId() == targetEndpoint.getComponentId());
            },
            onReceivedFnc,
            timeout,
            onTimeoutFnc,
            cancellationSource);
    }

    private <TPayload> Future<Void> registerPayloadCallbackAsync(
            Class<TPayload> payloadType,
            Function<ReceivedPayload<TPayload>, Boolean> isApplicableFnc,
            Consumer<ReceivedPayload<TPayload>> onReceivedFnc,
            Duration timeout,
            Runnable onTimeoutFnc,
            CancellationSource cancellationSource) {
        ContinuousPayloadReceivedDelegate payloadReceivedDelegate =
            new ContinuousPayloadReceivedDelegate(
                receivedPayload -> {
                    if (onReceivedFnc == null || isApplicableFnc == null) {
                        return false;
                    }

                    if (payloadType.isAssignableFrom(receivedPayload.getPayload().getClass())) {
                        //noinspection unchecked
                        ReceivedPayload<TPayload> p = (ReceivedPayload<TPayload>)receivedPayload;
                        if (isApplicableFnc.apply(p)) {
                            onReceivedFnc.accept(p);
                            return true;
                        }
                    }

                    return false;
                },
                onTimeoutFnc,
                timeout,
                cancellationSource);
        handler.addPayloadReceivedDelegate(payloadReceivedDelegate);

        return payloadReceivedDelegate.getResultFuture();
    }

    static <TPayload> Function<ReceivedPayload<?>, TPayload> createPayloadTypeReceiverFnc(
            Class<TPayload> payloadMessageType,
            Function<TPayload, Boolean> isApplicableFnc,
            int targetSystem,
            int targetComponent) {
        return createPayloadTypeReceiverFnc(payloadMessageType, isApplicableFnc, x -> x, targetSystem, targetComponent);
    }

    static <TPayload, TRes> Function<ReceivedPayload<?>, TRes> createPayloadTypeReceiverFnc(
            Class<TPayload> payloadMessageType,
            Function<TPayload, Boolean> isApplicableFnc,
            Function<TPayload, TRes> selectResult,
            int targetSystem,
            int targetComponent) {
        return (receivedPayload) -> {
            if (!payloadMessageType.isAssignableFrom(receivedPayload.getPayload().getClass())) {
                return null;
            }

            @SuppressWarnings("unchecked")
            TPayload payload = (TPayload)receivedPayload.getPayload();

            int senderSystemId = receivedPayload.getSenderEndpoint().getSystemId();
            int senderComponentId = receivedPayload.getSenderEndpoint().getComponentId();

            if ((senderSystemId == targetSystem || senderSystemId == 0 || targetSystem == 0)
                    && (senderComponentId == targetComponent
                        || senderComponentId == MavlinkEndpoint.AllComponentIds
                        || targetComponent == MavlinkEndpoint.AllComponentIds)
                    && isApplicableFnc.apply(payload)) {
                return selectResult.apply(payload);
            } else {
                return null;
            }
        };
    }

}
