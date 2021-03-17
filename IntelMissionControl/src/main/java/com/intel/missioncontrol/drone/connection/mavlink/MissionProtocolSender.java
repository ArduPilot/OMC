/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.MavMissionResult;
import io.dronefleet.mavlink.common.MavMissionType;
import io.dronefleet.mavlink.common.MissionAck;
import io.dronefleet.mavlink.common.MissionClearAll;
import io.dronefleet.mavlink.common.MissionCount;
import io.dronefleet.mavlink.common.MissionItem;
import io.dronefleet.mavlink.common.MissionItemInt;
import io.dronefleet.mavlink.common.MissionRequest;
import io.dronefleet.mavlink.common.MissionRequestInt;
import io.dronefleet.mavlink.common.MissionRequestList;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;

/** implements mavlink mission protocol https://mavlink.io/en/services/mission.html */
public class MissionProtocolSender extends PayloadSender {

    /** Receiver for individual mission items. */
    private static class MissionItemReceiver extends PayloadReceiver {
        private final Duration missionRequestTimeout = Duration.ofSeconds(1);

        MissionItemReceiver(MavlinkEndpoint targetEndpoint, MavlinkHandler handler) {
            super(targetEndpoint, handler);
        }

        Future<Void> registerMissionRequestHandlerAsync(
                Consumer<ReceivedPayload<MissionRequest>> payloadReceivedFnc,
                Runnable onTimeout,
                CancellationSource cancellationSource) {
            return registerPayloadTypeCallbackAsync(
                MissionRequest.class, payloadReceivedFnc, missionRequestTimeout, onTimeout, cancellationSource);
        }

        Future<Void> registerMissionRequestIntHandlerAsync(
                Consumer<ReceivedPayload<MissionRequestInt>> payloadReceivedFnc,
                Runnable onTimeout,
                CancellationSource cancellationSource) {
            return registerPayloadTypeCallbackAsync(
                MissionRequestInt.class, payloadReceivedFnc, missionRequestTimeout, onTimeout, cancellationSource);
        }
    }

    /** Sender for individual mission items */
    private static class MissionItemSender extends PayloadSender {

        MissionItemSender(MavlinkEndpoint recipient, MavlinkHandler handler, CancellationSource cancellationSource) {
            super(recipient, handler, cancellationSource);
        }

        /** Send a single mission item (unacknowledged) */
        Future<Void> sendMissionItemAsync(MissionItem missionItem) {
            return sendMavlinkPacketWithPayloadAsync(missionItem);
        }

        /** Send a single mission item (int) (unacknowledged) */
        Future<Void> sendMissionItemIntAsync(MissionItemInt missionItem) {
            return sendMavlinkPacketWithPayloadAsync(missionItem);
        }

        /** Request a single mission item */
        Future<MavlinkMissionItem> requestMissionItemAsync(MavMissionType mavMissionType, int itemIndex) {
            MissionRequestInt payloadToSend =
                MissionRequestInt.builder()
                    .seq(itemIndex)
                    .targetSystem(targetEndpoint.getSystemId())
                    .targetComponent(targetEndpoint.getComponentId())
                    .missionType(mavMissionType)
                    .build();

            Function<ReceivedPayload<?>, MissionItemInt> receiverFnc =
                PayloadReceiver.createPayloadTypeReceiverFnc(
                    MissionItemInt.class,
                    missionItem ->
                        missionItem.missionType().entry() == mavMissionType && missionItem.seq() == itemIndex,
                    targetEndpoint.getSystemId(),
                    targetEndpoint.getComponentId());

            return sendAndExpectResponseWithRetriesAsync(payloadToSend, receiverFnc)
                .thenApply(MavlinkMissionItem::fromMissionItemInt);
        }
    }

    /** Sender for Mission count notifications & requests */
    private static class MissionCountSender extends PayloadSender {
        MissionCountSender(MavlinkEndpoint recipient, MavlinkHandler handler, CancellationSource cancellationSource) {
            super(recipient, handler, cancellationSource);
        }

        /** Send mission item count and wait for mission ack. */
        Future<MissionAck> sendMissionListCountAsync(MavMissionType mavMissionType, int count) {
            MissionCount payloadToSend =
                MissionCount.builder()
                    .missionType(mavMissionType)
                    .count(count)
                    .targetSystem(targetEndpoint.getSystemId())
                    .targetComponent(targetEndpoint.getComponentId())
                    .build();

            Function<ReceivedPayload<?>, MissionAck> receiverFnc =
                PayloadReceiver.createPayloadTypeReceiverFnc(
                    MissionAck.class,
                    p -> mavMissionType == p.missionType().entry(),
                    targetEndpoint.getSystemId(),
                    targetEndpoint.getComponentId());

            int repetitions = 10;
            Duration responseTimeoutPerRepetition =
                PayloadSender.defaultResponseTimeoutPerRepetition.multipliedBy((count + 1) * 3);

            return sendAndExpectResponseWithRetriesAsync(
                payloadToSend, receiverFnc, repetitions, responseTimeoutPerRepetition);
        }

        /** Request mission items count */
        Future<Integer> requestMissionListCountAsync(MavMissionType mavMissionType) {
            MissionRequestList payloadToSend =
                MissionRequestList.builder()
                    .missionType(mavMissionType)
                    .targetSystem(targetEndpoint.getSystemId())
                    .targetComponent(targetEndpoint.getComponentId())
                    .build();

            Function<ReceivedPayload<?>, Integer> receiverFnc =
                PayloadReceiver.createPayloadTypeReceiverFnc(
                    MissionCount.class,
                    p -> mavMissionType == p.missionType().entry(),
                    MissionCount::count,
                    targetEndpoint.getSystemId(),
                    targetEndpoint.getComponentId());

            return sendAndExpectResponseWithRetriesAsync(payloadToSend, receiverFnc);
        }
    }

    public MissionProtocolSender(
            MavlinkEndpoint targetEndpoint, MavlinkHandler handler, CancellationSource externalCancellationSource) {
        super(targetEndpoint, handler, externalCancellationSource);
    }

    /**
     * Send mission item count, expect mission item requests and wait for mission ack. No clearing of remote items. No
     * retries on our side (although the drone might retry requesting single items). Implements
     * https://mavlink.io/en/services/mission.html#uploading_mission .
     */
    public Future<Void> sendMissionItemsAsync(MavMissionType mavMissionType, List<MavlinkMissionItem> missionItems) {
        FutureCompletionSource<Void> fcs = new FutureCompletionSource<>(super.cancellationSource);

        CancellationSource senderCS = new CancellationSource();
        fcs.getFuture().whenDone((Runnable)senderCS::cancel);

        MissionItemSender missionItemSender = new MissionItemSender(targetEndpoint, handler, senderCS);

        Runnable onMissionItemRequestTimeout =
            () -> fcs.setException(new TimeoutException("Mission item request timeout"));

        // Prepare receiver for MissionItem and MissionItemInt requests. Receiver timeout will cancel sender as well.
        MissionItemReceiver missionItemReceiver = new MissionItemReceiver(targetEndpoint, handler);
        MissionItemReceiver missionItemIntReceiver = new MissionItemReceiver(targetEndpoint, handler);

        CancellationSource missionItemReceiverCS = new CancellationSource();
        fcs.getFuture().whenDone(f -> missionItemReceiverCS.cancel());

        CancellationSource missionItemIntReceiverCts = new CancellationSource();
        fcs.getFuture().whenDone(f -> missionItemIntReceiverCts.cancel());

        // Receive MissionItems:
        missionItemReceiver
            .registerMissionRequestHandlerAsync(
                p -> {
                    if (p.getPayload().missionType().entry() == mavMissionType) {
                        int seq = p.getPayload().seq();
                        if (seq < 0 || seq >= missionItems.size()) {
                            fcs.setException(new IllegalStateException("Requested item sequence number out of bounds"));
                            return;
                        }

                        // we are receiving MissionItems, cancel MissionItemInt receiver:
                        missionItemIntReceiverCts.cancel();

                        // reply with MissionItem:
                        MissionItem item = missionItems.get(seq).asMissionItemForRecipient(targetEndpoint);
                        missionItemSender.sendMissionItemAsync(item).whenFailed(fcs::setException);
                    }
                },
                onMissionItemRequestTimeout,
                missionItemReceiverCS)
            .whenFailed(e -> fcs.setException(e.getCause()));

        // Might alternatively receive MissionRequestInt:
        missionItemIntReceiver
            .registerMissionRequestIntHandlerAsync(
                p -> {
                    if (p.getPayload().missionType().entry() == mavMissionType) {
                        int seq = p.getPayload().seq();
                        if (seq < 0 || seq >= missionItems.size()) {
                            fcs.setException(new IllegalStateException("Requested item sequence number out of bounds"));
                            return;
                        }

                        // we are receiving MissionItemInts, cancel MissionItem receiver:
                        missionItemReceiverCS.cancel();

                        // reply with MissionItemInt:
                        MissionItemInt item = missionItems.get(seq).asMissionItemIntForRecipient(targetEndpoint);
                        missionItemSender.sendMissionItemIntAsync(item).whenFailed(fcs::setException);
                    }
                },
                onMissionItemRequestTimeout,
                missionItemIntReceiverCts)
            .whenFailed(e -> fcs.setException(e.getCause()));

        // Send Mission List Count to initiate upload, and wait for mission ack.
        MissionCountSender missionCountSender = new MissionCountSender(targetEndpoint, handler, senderCS);
        missionCountSender
            .sendMissionListCountAsync(mavMissionType, missionItems.size())
            .whenSucceeded(
                missionAck -> {
                    // Successfully received ack
                    MavMissionResult mavMissionResult =
                        missionAck.type() == null ? MavMissionResult.MAV_MISSION_ACCEPTED : missionAck.type().entry();
                    if (mavMissionResult == MavMissionResult.MAV_MISSION_ACCEPTED) {
                        // successfully transmitted mission
                        fcs.setResult(null);
                    } else {
                        fcs.setException(new MavMissionResultException(mavMissionResult));
                    }
                })
            .whenFailed(e -> fcs.setException(e.getCause()));

        return fcs.getFuture();
    }

    /** Implements https://mavlink.io/en/services/mission.html#download_mission . */
    public Future<List<MavlinkMissionItem>> requestMissionItemsAsync(MavMissionType mavMissionType) {
        MissionCountSender missionCountSender = new MissionCountSender(targetEndpoint, handler, cancellationSource);
        MissionItemSender missionItemSender = new MissionItemSender(targetEndpoint, handler, cancellationSource);

        return missionCountSender
            .requestMissionListCountAsync(mavMissionType)
            .thenApplyAsync(
                count ->
                    // Ask for all MissionItems
                    requestMultipleItemsSequentiallyAsync(
                        count, i -> missionItemSender.requestMissionItemAsync(mavMissionType, i)))
            .thenApplyAsync(
                array ->
                    // Send ack:
                    sendMavlinkPacketWithPayloadAsync(
                            new MissionAck.Builder()
                                .targetSystem(targetEndpoint.getSystemId())
                                .targetComponent(targetEndpoint.getComponentId())
                                .missionType(mavMissionType)
                                .type(MavMissionResult.MAV_MISSION_ACCEPTED)
                                .build())
                        .thenGet(() -> array));
    }

    /** Clear the mission currently stored on the drone. */
    public Future<Void> sendClearMissionAsync() {
        return sendClearMissionAsync(MavMissionType.MAV_MISSION_TYPE_MISSION)
            .thenRunAsync(() -> sendClearMissionAsync(MavMissionType.MAV_MISSION_TYPE_FENCE))
            .thenRunAsync(() -> sendClearMissionAsync(MavMissionType.MAV_MISSION_TYPE_RALLY));
    }

    private Future<Void> sendClearMissionAsync(MavMissionType mavMissionType) {
        MissionClearAll payloadToSend =
            MissionClearAll.builder()
                .missionType(mavMissionType)
                .targetSystem(targetEndpoint.getSystemId())
                .targetComponent(targetEndpoint.getComponentId())
                .build();

        Function<ReceivedPayload<?>, MissionAck> receiverFnc =
            PayloadReceiver.createPayloadTypeReceiverFnc(
                MissionAck.class,
                payload -> payload.missionType().entry() == mavMissionType,
                targetEndpoint.getSystemId(),
                targetEndpoint.getComponentId());

        return sendAndExpectResponseWithRetriesAsync(payloadToSend, receiverFnc)
            .thenAccept(
                missionAck -> {
                    if (missionAck.type().entry() != MavMissionResult.MAV_MISSION_ACCEPTED) {
                        throw new MavMissionResultException(missionAck.type().entry());
                    }
                });
    }

}
