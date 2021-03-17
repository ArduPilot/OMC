/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.drone.connection.MavlinkCameraConnection;
import com.intel.missioncontrol.drone.connection.MavlinkCameraConnectionItem;
import com.intel.missioncontrol.drone.connection.mavlink.IMavlinkParameter;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkParameterFactory;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import io.dronefleet.mavlink.common.CameraImageCaptured;
import io.dronefleet.mavlink.common.VideoStreamInformation;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavlinkCamera implements ICamera {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkCamera.class);

    private final AsyncStringProperty name = new SimpleAsyncStringProperty(this);
    private final AsyncObjectProperty<IGenericCameraDescription> cameraDescription =
        new SimpleAsyncObjectProperty<>(this);
    private final AsyncListProperty<MavlinkVideoStream> videoStreams =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<MavlinkVideoStream>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());
    private final AsyncObjectProperty<ICamera.Status> status =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<Status>().initialValue(Status.UNKNOWN).create());
    private final AsyncIntegerProperty imageCount = new SimpleAsyncIntegerProperty(this);

    private final MavlinkCameraConnection cameraConnection;

    public MavlinkCamera(MavlinkCameraConnection cameraConnection, IGenericCameraDescription cameraDescription) {
        this.cameraConnection = cameraConnection;
        this.cameraDescription.setValue(cameraDescription);

        name.set(cameraConnection.getCameraConnectionItem().getName());

        // image captured telemetry
        cameraConnection
            .getCameraProtocolReceiver()
            .registerTelemetryCallbackAsync(
                CameraImageCaptured.class,
                receivedPayload -> {
                    CameraImageCaptured cameraImageCaptured = receivedPayload.getPayload();
                    if (cameraImageCaptured.captureResult() != 0) {
                        imageCount.set(imageCount.get() + 1);
                    }
                },
                // ignore timeout
                SpecialDuration.INDEFINITE,
                () -> {})
            .whenFailed(e -> LOGGER.error("MavlinkCamera: Error in image captured telemetry handler", e));

        // video stream one time setup
        initializeVideoStreamsAsync()
            .whenFailed(e -> LOGGER.error("MavlinkCamera: Cannot obtain video stream information", e));

        // set mavlink params
        initializeMavlinkParamsAsync()
            .whenFailed(
                e -> {
                    LOGGER.error("MavlinkCamera: Error setting camera parameters", e);
                    status.set(Status.PARAMETER_ERROR);
                })
            .whenSucceeded(v -> status.set(Status.OK));
    }

    private Future<Void> initializeVideoStreamsAsync() {
        Function<Integer, Future<Void>> startStreamingFnc =
            streamId -> cameraConnection.getCommandProtocolSender().startStreamingAsync(streamId);
        Function<Integer, Future<Void>> stopStreamingFnc =
            streamId -> cameraConnection.getCommandProtocolSender().stopStreamingAsync(streamId);

        String videoStreamUri = getCameraDescription().getVideoStreamUri();
        if (videoStreamUri != null) {
            MavlinkVideoStream stream = new MavlinkVideoStream(videoStreamUri, startStreamingFnc, stopStreamingFnc);
            stream.setStreamUUID(UUID.nameUUIDFromBytes(videoStreamUri.getBytes()));
            videoStreams.add(stream);
        }

        String connectionID =
            "SysID:"
                + cameraConnection.getCameraConnectionItem().getSystemId()
                + ";CompID:"
                + cameraConnection.getCameraConnectionItem().getComponentId();

        return cameraConnection
            .getCameraProtocolSender()
            .requestVideoStreamInformationAsync()
            .thenApply(
                videoStreamInformationList ->
                    videoStreamInformationList
                        .stream()
                        .map(
                            videoStreamInformation ->
                                mapToMavlinkVideoStream(
                                    videoStreamInformation, connectionID, startStreamingFnc, stopStreamingFnc))
                        .collect(Collectors.toList()))
            .thenAccept((Consumer<List<MavlinkVideoStream>>)this.videoStreams::addAll);
    }

    @Override
    public ReadOnlyAsyncStringProperty nameProperty() {
        return name;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IGenericCameraDescription> cameraDescriptionProperty() {
        return cameraDescription;
    }

    @Override
    public ReadOnlyAsyncListProperty<? extends IVideoStream> videoStreamsProperty() {
        return videoStreams;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Status> statusProperty() {
        return status;
    }

    @Override
    public AsyncIntegerProperty imageCountProperty() {
        return imageCount;
    }

    private Future<Void> initializeMavlinkParamsAsync() {
        List<com.intel.missioncontrol.hardware.MavlinkParam> mavlinkParams = cameraDescription.get().getMavlinkParams();
        if (mavlinkParams == null || mavlinkParams.isEmpty()) {
            return Futures.successful(null);
        }

        List<IMavlinkParameter> parameters =
            mavlinkParams
                .stream()
                .map(MavlinkParameterFactory::createFromMavlinkParam)
                .collect(Collectors.toUnmodifiableList());
        return cameraConnection.getParameterProtocolSender().setParamsAsync(parameters);
    }

    public MavlinkCameraConnectionItem getConnectionItem() {
        return cameraConnection.getCameraConnectionItem();
    }

    private MavlinkVideoStream mapToMavlinkVideoStream(
            VideoStreamInformation info,
            String connectionID,
            Function<Integer, Future<Void>> startStreamingFnc,
            Function<Integer, Future<Void>> stopStreamingFnc) {
        MavlinkVideoStream videoStream = new MavlinkVideoStream(info, startStreamingFnc, stopStreamingFnc);
        String streamID = connectionID + "StreamID:" + info.streamId();
        videoStream.setStreamUUID(UUID.nameUUIDFromBytes(streamID.getBytes()));
        if (info.streamId() == 1 && cameraConnection.getCameraConnectionItem().getCameraNumber() == 1) {
            videoStream.setDefaultStream();
        }

        return videoStream;
    }
}
