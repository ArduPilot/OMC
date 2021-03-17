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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavlinkCamera implements ICamera {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkCamera.class);

    private final AsyncStringProperty name = new SimpleAsyncStringProperty(this);
    private final AsyncObjectProperty<IGenericCameraDescription> cameraDescription =
        new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<MavlinkVideoStream> videoStream = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<ICamera.Status> status =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<Status>().initialValue(Status.UNKNOWN).create());

    private final MavlinkCameraConnection cameraConnection;

    public MavlinkCamera(MavlinkCameraConnection cameraConnection, IGenericCameraDescription cameraDescription) {
        this.cameraConnection = cameraConnection;
        this.cameraDescription.setValue(cameraDescription);

        name.set(cameraConnection.getCameraConnectionItem().getName());

        // video stream one time setup
        updateVideoStreamAsync()
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

    private Future<Void> updateVideoStreamAsync() {
        Function<Integer, Future<Void>> startStreamingFnc =
            streamId -> cameraConnection.getCommandProtocolSender().startStreamingAsync(streamId);
        Function<Integer, Future<Void>> stopStreamingFnc =
            streamId -> cameraConnection.getCommandProtocolSender().stopStreamingAsync(streamId);

        videoStream.addListener((o, oldValue, newValue) -> LOGGER.debug("VideoStream changed to " + newValue));

        String videoStreamUri = getCameraDescription().getVideoStreamUri();
        if (videoStreamUri != null) {
            videoStream.set(new MavlinkVideoStream(videoStreamUri, startStreamingFnc, stopStreamingFnc));
        }

        return cameraConnection
            .getCameraProtocolSender()
            .requestVideoStreamInformationAsync()
            .whenSucceeded(
                videoStreamInformationList ->
                    videoStreamInformationList
                        .stream()
                        .map(
                            videoStreamInformation ->
                                new MavlinkVideoStream(videoStreamInformation, startStreamingFnc, stopStreamingFnc))
                        .findFirst()
                        .ifPresent(stream -> PropertyHelper.setValueSafe(this.videoStream, stream)))
            .thenGet(() -> null);
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
    public ReadOnlyAsyncObjectProperty<MavlinkVideoStream> videoStreamProperty() {
        return videoStream;
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
