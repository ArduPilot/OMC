/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import io.dronefleet.mavlink.common.VideoStreamInformation;
import io.dronefleet.mavlink.common.VideoStreamStatusFlags;
import java.util.UUID;
import java.util.function.Function;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MavlinkVideoStream implements IVideoStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkVideoStream.class);

    private final AsyncBooleanProperty isRunning = new SimpleAsyncBooleanProperty(this);
    private final AsyncStringProperty streamName = new SimpleAsyncStringProperty(this);
    private final AsyncStringProperty streamURI = new SimpleAsyncStringProperty(this);

    private final Function<Integer, Future<Void>> startStreamingFnc;
    private final Function<Integer, Future<Void>> stopStreamingFnc;

    private int streamId;

    private UUID streamUUID;
    private boolean defaultStream = false;

    MavlinkVideoStream(
            VideoStreamInformation videoStreamInformation,
            Function<Integer, Future<Void>> startStreamingFnc,
            Function<Integer, Future<Void>> stopStreamingFnc) {
        this.startStreamingFnc = startStreamingFnc;
        this.stopStreamingFnc = stopStreamingFnc;
        set(videoStreamInformation);
    }

    MavlinkVideoStream(
            String videoStreamUri,
            Function<Integer, Future<Void>> startStreamingFnc,
            Function<Integer, Future<Void>> stopStreamingFnc) {
        isRunning.set(true);
        streamName.set(videoStreamUri);
        streamURI.set(videoStreamUri);
        streamId = 0; // all streams
        this.startStreamingFnc = startStreamingFnc;
        this.stopStreamingFnc = stopStreamingFnc;
    }

    public void set(VideoStreamInformation videoStreamInformation) {
        streamId = videoStreamInformation.streamId();
        isRunning.set(
            videoStreamInformation.flags().flagsEnabled(VideoStreamStatusFlags.VIDEO_STREAM_STATUS_FLAGS_RUNNING));
        streamName.set(videoStreamInformation.name());
        streamURI.set(videoStreamInformation.uri());
    }

    @Override
    public String toString() {
        return "MavlinkVideoStream{"
            + "streamName="
            + getStreamName()
            + ", streamURI="
            + getStreamURI()
            + ", streamId="
            + getStreamId()
            + '}';
    }

    @Override
    public ReadOnlyAsyncBooleanProperty isRunningProperty() {
        return isRunning;
    }

    @Override
    public ReadOnlyAsyncStringProperty streamNameProperty() {
        return streamName;
    }

    @Override
    public ReadOnlyAsyncStringProperty streamURIProperty() {
        return streamURI;
    }

    @Override
    public Future<Void> StartStreamingAsync() {
        if (startStreamingFnc == null) {
            // ignore
            return Futures.successful(null);
        }

        return startStreamingFnc.apply(streamId);
    }

    @Override
    public Future<Void> StopStreamingAsync() {
        if (stopStreamingFnc == null) {
            // ignore
            return Futures.successful(null);
        }

        return stopStreamingFnc.apply(streamId);
    }

    int getStreamId() {
        return streamId;
    }

    @Override
    public boolean isDefaultStream() {
        return defaultStream;
    }

    @Override
    public boolean isSameVideoStream(IVideoStream other) {
        if (!(other instanceof MavlinkVideoStream)) return false;

        MavlinkVideoStream o = (MavlinkVideoStream) other;

        return o.streamUUID.equals(this.streamUUID);
    }

    void setStreamUUID(UUID streamUUID) {
        this.streamUUID = streamUUID;
    }

    void setDefaultStream() {
        this.defaultStream = true;
    }
}
