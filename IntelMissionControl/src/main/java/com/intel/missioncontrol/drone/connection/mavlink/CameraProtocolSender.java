/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import io.dronefleet.mavlink.common.VideoStreamInformation;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;

public class CameraProtocolSender extends PayloadSender {

    public CameraProtocolSender(
            MavlinkEndpoint targetEndpoint, MavlinkHandler handler, CancellationSource cancellationSource) {
        super(targetEndpoint, handler, cancellationSource);
    }

    /** Receiver for video stream information */
    private class VideoStreamInformationReceiver extends PayloadReceiver {
        private final Duration videoStreamInformationRequestTimeout = Duration.ofMillis(500);

        VideoStreamInformationReceiver(MavlinkEndpoint targetEndpoint, MavlinkHandler handler) {
            super(targetEndpoint, handler);
        }

        Future<Void> registerVideoStreamInformationHandlerAsync(
                Consumer<ReceivedPayload<VideoStreamInformation>> payloadReceivedFnc,
                Runnable onTimeout,
                CancellationSource cancellationSource) {
            return registerPayloadTypeCallbackAsync(
                VideoStreamInformation.class,
                payloadReceivedFnc,
                videoStreamInformationRequestTimeout,
                onTimeout,
                cancellationSource);
        }
    }

    /** Request video stream information of all streams and asynchronously return a list. timeout. */
    public Future<List<VideoStreamInformation>> requestVideoStreamInformationAsync() {
        FutureCompletionSource<List<VideoStreamInformation>> futureCompletionSource = new FutureCompletionSource<>();

        super.cancellationSource.addListener(mayInterruptIfRunning -> futureCompletionSource.setCancelled());

        // Prepare receiver for VideoStreamInformation requests.
        VideoStreamInformationReceiver videoStreamInformationReceiver =
            new VideoStreamInformationReceiver(targetEndpoint, handler);

        CancellationSource videoStreamInformationCts = new CancellationSource();
        futureCompletionSource.getFuture().whenDone((Runnable)videoStreamInformationCts::cancel);

        // map stream ID to VideoStreamInformation
        Map<Integer, VideoStreamInformation> videoStreams = new HashMap<>();

        // Receive VideoStreamInformation:
        videoStreamInformationReceiver
            .registerVideoStreamInformationHandlerAsync(
                p -> {
                    synchronized (videoStreams) {
                        VideoStreamInformation videoStreamInformation = p.getPayload();

                        videoStreams.put(videoStreamInformation.streamId(), videoStreamInformation);

                        if (videoStreams.size() >= videoStreamInformation.count()) {
                            // finished
                            futureCompletionSource.setResult(
                                videoStreams.values().stream().collect(Collectors.toUnmodifiableList()));
                        }
                    }
                },
                // timeout
                () -> futureCompletionSource.setException(new TimeoutException()),
                videoStreamInformationCts)
            .whenFailed(throwable -> futureCompletionSource.setException(throwable.getCause()));

        // Send request
        CommandProtocolSender sender = new CommandProtocolSender(targetEndpoint, handler, videoStreamInformationCts);
        sender.sendVideoStreamInformationRequestAsync()
            .whenFailed(
                throwable -> {
                    if (throwable.getCause() instanceof CancellationException) {
                        return;
                    }

                    futureCompletionSource.setException(throwable.getCause());
                });

        return futureCompletionSource.getFuture();
    }

}
