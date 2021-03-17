/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.MavMissionResult;
import io.dronefleet.mavlink.common.MavMissionType;
import io.dronefleet.mavlink.common.MissionItemInt;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrayhawkMissionProtocolSender extends MissionProtocolSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrayhawkMissionProtocolSender.class);
    private static final int BULK_TRANSMISSION_BLOCK_SIZE = 25;
    // 1.5x grayhawk's 250ms resend period
    private static final int QUIESCENCE_MILLISECONDS = 375;

    public GrayhawkMissionProtocolSender(
            MavlinkEndpoint targetEndpoint, MavlinkHandler handler, CancellationSource externalCancellationSource) {
        super(targetEndpoint, handler, externalCancellationSource);
    }

    private static final class TransmissionProgress {
        public int maxSent = -1;
        public Calendar quietUntil = Calendar.getInstance();
    }

    @Override
    public Future<Void> sendMissionItemsAsync(MavMissionType mavMissionType, List<MavlinkMissionItem> missionItems) {
        LOGGER.debug("Grayhawk mission upload start at {}.", Calendar.getInstance().getTime());
        long startTime = System.currentTimeMillis();

        FutureCompletionSource<Void> fcs = new FutureCompletionSource<>(super.cancellationSource);

        CancellationSource cancellationSource = new CancellationSource();
        fcs.getFuture().whenDone((Runnable)cancellationSource::cancel);

        MissionItemSender missionItemSender = new MissionItemSender(targetEndpoint, handler, cancellationSource);

        Runnable onMissionItemRequestTimeout =
            () -> fcs.setException(new TimeoutException("Mission item request timeout"));

        Consumer<Integer> updateProgress =
                seq -> {
                    int count = missionItems.size();
                    double progress = count == 0 ? 1.0 : (((double)seq + 1.0) / count);
                    fcs.setProgress(progress);
                };

        // Prepare receiver for MissionItemInt requests.
        MissionItemReceiver missionItemIntReceiver = new MissionItemReceiver(targetEndpoint, handler);

        TransmissionProgress progress = new TransmissionProgress();

        // receive MissionRequestInt:
        int missionItemCount = missionItems.size();
        missionItemIntReceiver
            .registerMissionRequestIntHandlerAsync(
                p -> {
                    if (p.getPayload().missionType().entry() == mavMissionType) {
                        int seq = p.getPayload().seq();
                        if (seq < 0 || seq >= missionItemCount) {
                            fcs.setException(new IllegalStateException("Requested item sequence number out of bounds"));
                            return;
                        }

                        int sendUpTo = -1;
                        synchronized (progress) {
                            Calendar now = Calendar.getInstance();
                            Date receivedAt = now.getTime();
                            if (now.after(progress.quietUntil) || seq > progress.maxSent) {
                                // reply with MissionItemInt:
                                sendUpTo = Math.min(seq + BULK_TRANSMISSION_BLOCK_SIZE, missionItemCount) - 1;
                                now.add(Calendar.MILLISECOND, QUIESCENCE_MILLISECONDS);
                                progress.quietUntil = now;
                                progress.maxSent = sendUpTo;
                                LOGGER.debug(
                                    "At {}: answering request for mission item {} with up to item {} and setting quiescence period until {}",
                                    receivedAt,
                                    seq,
                                    progress.maxSent,
                                    progress.quietUntil.getTime());
                            } else {
                                LOGGER.debug(
                                    "At {}: ignoring request for mission item {} due to quiescence period up to item {} until {}.",
                                    receivedAt,
                                    seq,
                                    progress.maxSent,
                                    progress.quietUntil.getTime());
                            }
                        }

                        updateProgress.accept(seq);
                        for (int i = seq; i <= sendUpTo; i++) {
                            MissionItemInt item = missionItems.get(i).asMissionItemIntForRecipient(targetEndpoint);
                            missionItemSender.sendMissionItemIntAsync(item).whenFailed(fcs::setException);
                        }
                    }
                },
                onMissionItemRequestTimeout,
                cancellationSource)
            .whenFailed(e -> fcs.setException(e.getCause()));

        // Send Mission List Count to initiate upload, and wait for mission ack.
        MissionCountSender missionCountSender = new MissionCountSender(targetEndpoint, handler, cancellationSource);
        missionCountSender
            .sendMissionListCountAsync(mavMissionType, missionItemCount)
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

        return fcs.getFuture()
            .whenDone(
                unused ->
                    LOGGER.debug("Grayhawk mission upload done after {} ms", System.currentTimeMillis() - startTime));
    }
}
