/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import com.intel.missioncontrol.drone.SpecialDuration;
import io.dronefleet.mavlink.Mavlink2Message;
import io.dronefleet.mavlink.annotations.MavlinkMessageInfo;
import io.dronefleet.mavlink.common.Statustext;
import io.dronefleet.mavlink.protocol.MavlinkPacket;
import io.dronefleet.mavlink.serialization.payload.MavlinkPayloadSerializer;
import io.dronefleet.mavlink.serialization.payload.reflection.ReflectionPayloadSerializer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;
import org.asyncfx.concurrent.Futures;

/**
 * A cancellable MAVLink message sender with a target endpoint, usually used for initiating communication as part of a
 * MAVLink microservice protocol implementation.
 */
class PayloadSender {
    static final int defaultRepetitions = 30;
    static final Duration defaultResponseTimeoutPerRepetition = Duration.ofSeconds(1);

    final int systemId = 255;
    final int componentId = 250;
    final MavlinkEndpoint targetEndpoint;
    final MavlinkHandler handler;
    protected final CancellationSource cancellationSource;
    private final MavlinkPayloadSerializer serializer = new ReflectionPayloadSerializer();
    private int sequence = 0;

    PayloadSender(MavlinkEndpoint targetEndpoint, MavlinkHandler handler, CancellationSource cancellationSource) {
        if (targetEndpoint.equals(MavlinkEndpoint.UnspecifiedUdp)) {
            throw new IllegalArgumentException("Sender targetEndpoint must be specified");
        }

        this.targetEndpoint = targetEndpoint;
        this.handler = handler;
        this.cancellationSource = cancellationSource;
    }

    <TPayload, TRes> Future<TRes> sendAndExpectResponseWithRetriesAsync(
            TPayload payloadToSend, Function<ReceivedPayload<?>, TRes> receiverFnc) {
        return sendAndExpectResponseWithRetriesAsync(
            r -> payloadToSend, receiverFnc, defaultRepetitions, defaultResponseTimeoutPerRepetition);
    }

    <TPayload, TRes> Future<TRes> sendAndExpectResponseWithRetriesAsync(
            TPayload payloadToSend,
            Function<ReceivedPayload<?>, TRes> receiverFnc,
            int repetitions,
            Duration responseTimeoutPerRepetition) {
        return sendAndExpectResponseWithRetriesAsync(
            r -> payloadToSend, receiverFnc, repetitions, responseTimeoutPerRepetition);
    }

    <TPayload, TRes> Future<TRes> sendAndExpectResponseWithRetriesAsync(
            Function<Integer, TPayload> payloadForRepetitionFnc,
            Function<ReceivedPayload<?>, TRes> receiverFnc,
            int repetitions,
            Duration responseTimeoutPerRepetition) {
        CancellationSource cs = new CancellationSource();
        this.cancellationSource.addListener(cs::cancel);

        // prepare receiver delegate
        OneShotPayloadReceivedDelegate<TRes> payloadReceivedDelegate =
            new OneShotPayloadReceivedDelegate<>(
                receiverFnc, responseTimeoutPerRepetition.multipliedBy(repetitions), cs);

        // Start receiver:
        handler.addPayloadReceivedDelegate(payloadReceivedDelegate);

        Future<TRes> payloadReceivedFuture = payloadReceivedDelegate.getResultFuture();
        payloadReceivedFuture.whenDone((Runnable)cs::cancel);

        // Send command (repeatedly):
        AtomicInteger repetition = new AtomicInteger();
        Dispatcher.background()
            .runLaterAsync(
                () -> {
                    int rep = repetition.getAndIncrement();
                    if (rep >= repetitions) {
                        cs.cancel();
                    }

                    sendMavlinkPacketWithPayloadAsync(payloadForRepetitionFnc.apply(rep));
                },
                Duration.ZERO,
                responseTimeoutPerRepetition,
                cs);

        return payloadReceivedFuture;
    }

    <TPayload> Future<Void> sendMavlinkPacketWithPayloadAsync(TPayload payload) {
        Mavlink2Message<TPayload> sendMsg = new Mavlink2Message<>(0, 0, systemId, componentId, payload);

        MavlinkMessageInfo msgInfo = sendMsg.getPayload().getClass().getAnnotation(MavlinkMessageInfo.class);
        byte[] serializedPayload = serializer.serialize(sendMsg.getPayload());

        MavlinkPacket sendPkt =
            MavlinkPacket.create(
                sendMsg.getIncompatibleFlags(),
                sendMsg.getCompatibleFlags(),
                sequence++,
                sendMsg.getOriginSystemId(),
                sendMsg.getOriginComponentId(),
                msgInfo.id(),
                msgInfo.crc(),
                serializedPayload);

        return handler.writePacketAsync(sendPkt, targetEndpoint);
    }

    /**
     * Simultaneously executes the given request function the given number of times, with a parameter from 0 to (count -
     * 1). Succeeds when all requests got a response. Fails if any of the requests failed.
     */
    <TResItem> Future<List<TResItem>> requestMultipleItemsSimultaneouslyAsync(
            int count, Function<Integer, Future<TResItem>> requestByIndexFnc) {
        if (count == 0) {
            return Futures.successful(new ArrayList<>());
        }

        FutureCompletionSource<List<TResItem>> fcs = new FutureCompletionSource<>(this.cancellationSource);

        AtomicInteger doneCount = new AtomicInteger(0);

        List<TResItem> array = Collections.synchronizedList(new ArrayList<>(Collections.nCopies(count, null)));

        for (int i = 0; i < count; i++) {
            final int currentIndex = i;
            requestByIndexFnc
                .apply(i)
                .whenSucceeded(p -> array.set(currentIndex, p))
                .whenFailed(fcs::setException)
                .whenDone(
                    f -> {
                        if (doneCount.incrementAndGet() == count) {
                            fcs.setResult(array);
                        }
                    });
        }

        return fcs.getFuture();
    }

    /**
     * Sequentially executes the given request function the given number of times, with a parameter from 0 to (count -
     * 1). Succeeds when all requests got a response. Fails if any of the requests failed.
     */
    <TResItem> Future<List<TResItem>> requestMultipleItemsSequentiallyAsync(
            int count, Function<Integer, Future<TResItem>> requestByIndexFnc) {
        return FutureHelper.repeatSequentiallyAsync(requestByIndexFnc, count, this.cancellationSource);
    }

    /**
     * Run the given asynchronous callable. Succeeds if the callable succeeds, or fails if the callable fails or a
     * status text matching the given filter is received, whichever occurs first. In the latter case, the callable is
     * cancelled. Status text filtering is done via String.matches(failOnStatusText), which takes a regular expression.
     */
    <TRes> Future<TRes> runAndFailOnStatusTextAsync(
            Callable<Future<TRes>> asyncFnc, StatusTextFilter failOnStatusText) {
        FutureCompletionSource<TRes> fcs = new FutureCompletionSource<>(this.cancellationSource);

        CancellationSource statusTextCts = new CancellationSource();
        cancellationSource.addListener(statusTextCts::cancel);

        // register status text handler:
        Function<Statustext, Boolean> isApplicableFnc =
            (Statustext statusText) -> failOnStatusText.match(statusText.text());

        Function<ReceivedPayload<?>, Statustext> receiverFnc =
            PayloadReceiver.createPayloadTypeReceiverFnc(
                Statustext.class, isApplicableFnc, targetEndpoint.getSystemId(), targetEndpoint.getComponentId());

        OneShotPayloadReceivedDelegate<Statustext> payloadReceivedDelegate =
            new OneShotPayloadReceivedDelegate<>(receiverFnc, SpecialDuration.INDEFINITE, statusTextCts);

        // Start receiver:
        handler.addPayloadReceivedDelegate(payloadReceivedDelegate);

        // execute callable:
        Future<TRes> fncFuture;
        try {
            fncFuture =
                asyncFnc.call()
                    .whenSucceeded(fcs::setResult)
                    .whenFailed(throwable -> fcs.setException(throwable.getCause()))
                    .whenDone(f -> statusTextCts.cancel());
        } catch (Exception e) {
            statusTextCts.cancel();
            return Futures.failed(e);
        }

        payloadReceivedDelegate
            .getResultFuture()
            .whenSucceeded(statusText -> fcs.setException(new StatusTextException(statusText)))
            .whenFailed(throwable -> fcs.setException(throwable.getCause()))
            .whenDone(v -> fncFuture.cancel(false));

        return fcs.getFuture();
    }
}
