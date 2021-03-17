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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;
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

    private final MavlinkCameraConnection cameraConnection;

    public MavlinkCamera(MavlinkCameraConnection cameraConnection, IGenericCameraDescription cameraDescription) {
        this.cameraConnection = cameraConnection;
        this.cameraDescription.setValue(cameraDescription);

        name.set(cameraConnection.getCameraConnectionItem().getName());

        // get camera information
        // TODO use
        cameraConnection
            .getCommandProtocolSender()
            .requestCameraInformationAsync()
            .whenSucceeded(
                camInfo -> {
                    LOGGER.info("CAMERA_INFORMATION received: " + camInfo.toString());
                })
            .whenFailed(e -> LOGGER.error("Error requesting CAMERA_INFORMATION", e));

        // video stream one time setup
        updateVideoStreamsAsync()
            .whenFailed(e -> LOGGER.error("MavlinkDrone: Cannot obtain video stream information", e));

        // set mavlink params
        updateMavlinkParamsAsync()
            .whenFailed(
                e -> {
                    LOGGER.error("MavlinkDrone: Error setting camera parameters", e);
                    status.set(Status.PARAMETER_ERROR);
                })
            .whenSucceeded(v -> status.set(Status.OK));
    }

    private Future<Void> updateVideoStreamsAsync() {
        Function<Integer, Future<Void>> startStreamingFnc =
            streamId -> cameraConnection.getCommandProtocolSender().startStreamingAsync(streamId);
        Function<Integer, Future<Void>> stopStreamingFnc =
            streamId -> cameraConnection.getCommandProtocolSender().stopStreamingAsync(streamId);

        videoStreams.addListener((o, oldValue, newValue) -> LOGGER.debug("VideoStream list changed"));

        String videoStreamUri = getCameraDescription().getVideoStreamUri();
        if (videoStreamUri != null) {
            videoStreams.add(new MavlinkVideoStream(videoStreamUri, startStreamingFnc, stopStreamingFnc));
        }

        return cameraConnection
            .getCameraProtocolSender()
            .requestVideoStreamInformationAsync()
            .thenApply(
                videoStreamInformationList ->
                    videoStreamInformationList
                        .stream()
                        .map(
                            videoStreamInformation ->
                                new MavlinkVideoStream(videoStreamInformation, startStreamingFnc, stopStreamingFnc))
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

    private Future<Void> updateMavlinkParamsAsync() {
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
}
