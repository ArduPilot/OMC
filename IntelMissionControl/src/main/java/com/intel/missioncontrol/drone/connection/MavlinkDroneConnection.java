/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.drone.MavlinkCamera;
import com.intel.missioncontrol.drone.SpecialDuration;
import com.intel.missioncontrol.drone.connection.mavlink.CameraProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.CommandProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolReceiver;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkEndpoint;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkHandler;
import com.intel.missioncontrol.drone.connection.mavlink.MissionProtocolReceiver;
import com.intel.missioncontrol.drone.connection.mavlink.MissionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.ParameterProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.ReceivedPayload;
import com.intel.missioncontrol.drone.connection.mavlink.TelemetryReceiver;
import io.dronefleet.mavlink.MavlinkDialect;
import io.dronefleet.mavlink.common.MavType;
import java.time.Duration;
import java.util.function.Consumer;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides mavlink-specific handles for communication with a connected MavlinkDrone. Allows cancellation of all ongoing
 * transfers via its CancellationToken, effectively disconnecting the drone.
 */
public class MavlinkDroneConnection implements IMavlinkDroneConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkDroneConnection.class);

    private final CommandProtocolSender commandProtocolSender;
    private final ConnectionProtocolSender connectionProtocolSender;
    private final MissionProtocolSender missionProtocolSender;
    private final ConnectionProtocolReceiver connectionProtocolReceiver;
    private final ParameterProtocolSender parameterProtocolSender;
    private final CameraProtocolSender cameraProtocolSender;
    private final TelemetryReceiver telemetryReceiver;
    private final Future<Void> heartbeatSenderFuture;

    private final MavlinkDroneConnectionItem connectionItem;
    private final MavType mavType;
    private final MavlinkEndpoint targetEndpoint;
    private final MavlinkHandler mavlinkHandler;
    private final CancellationSource externalCancellationSource;

    private final MavlinkCameraListener mavlinkCameraListener;

    MavlinkDroneConnection(
            MavlinkDroneConnectionItem connectionItem,
            MavlinkHandler mavlinkHandler,
            MavType mavType,
            MavlinkDialect dialect,
            MavlinkEndpoint targetEndpoint,
            CancellationSource cancellationSource,
            ConnectionProtocolSender connectionProtocolSender,
            Future<Void> heartbeatSenderFuture,
            MavlinkCameraListener mavlinkCameraListener) {
        this.connectionItem = connectionItem;
        this.mavType = mavType;
        this.targetEndpoint = targetEndpoint;
        this.mavlinkHandler = mavlinkHandler;
        externalCancellationSource = cancellationSource;

        this.mavlinkCameraListener = mavlinkCameraListener;

        this.connectionProtocolSender = connectionProtocolSender;
        commandProtocolSender = new CommandProtocolSender(targetEndpoint, mavlinkHandler, cancellationSource);
        missionProtocolSender = initMissionProtocolSender(mavlinkHandler, targetEndpoint, cancellationSource);
        parameterProtocolSender = new ParameterProtocolSender(targetEndpoint, mavlinkHandler, cancellationSource);
        connectionProtocolReceiver = new ConnectionProtocolReceiver(targetEndpoint, mavlinkHandler, cancellationSource);
        cameraProtocolSender = new CameraProtocolSender(targetEndpoint, mavlinkHandler, cancellationSource);
        telemetryReceiver = new TelemetryReceiver(targetEndpoint, mavlinkHandler, cancellationSource);
        this.heartbeatSenderFuture = heartbeatSenderFuture;

        int sysId = targetEndpoint.getSystemId();
        mavlinkHandler.registerSystemDialect(sysId, dialect);

        // TODO: use cleaner pattern?
        cancellationSource.addListener(b -> mavlinkHandler.unRegisterSystemDialect(sysId));
    }

    @NonNull
    protected MissionProtocolSender initMissionProtocolSender(
            MavlinkHandler mavlinkHandler, MavlinkEndpoint targetEndpoint, CancellationSource cancellationSource) {
        return new MissionProtocolSender(targetEndpoint, mavlinkHandler, cancellationSource);
    }

    /**
     * Registers a receiver for the given telemetry payload type, obtaining the current message interval for this type
     * from the drone in order to set the timeout. If the message interval cannot be obtained, a default timeout value
     * is used.
     */
    public <TPayload> Future<Void> registerTelemetryWithAutoTimeoutAsync(
            Class<TPayload> payloadType, Consumer<ReceivedPayload<TPayload>> payloadReceivedFnc, Runnable onTimeout) {
        return commandProtocolSender
            .getMessageIntervalAsync(payloadType)
            .thenFinallyAcceptAsync(
                (msgInterval, ex) -> {
                    Duration interval =
                        (ex == null
                                && msgInterval != null
                                && !msgInterval.equals(SpecialDuration.UNKNOWN)
                                && msgInterval.compareTo(Duration.ofMillis(100)) >= 0)
                            ? msgInterval.multipliedBy(5) // define timeout as 5 times the scheduled interval
                            : Duration.ofSeconds(5); // default;

                    return telemetryReceiver.registerTelemetryCallbackAsync(
                        payloadType, payloadReceivedFnc, interval, onTimeout);
                });
    }

    @Override
    public MissionProtocolReceiver createMissionProtocolReceiver(CancellationSource cancellationSource) {
        CancellationSource cts = new CancellationSource();
        cancellationSource.addListener(cts::cancel);
        externalCancellationSource.addListener(cts::cancel);
        return new MissionProtocolReceiver(targetEndpoint, mavlinkHandler, cts);
    }

    @Override
    public ConnectionProtocolSender getConnectionProtocolSender() {
        return connectionProtocolSender;
    }

    @Override
    public MissionProtocolSender getMissionProtocolSender() {
        return missionProtocolSender;
    }

    @Override
    public CommandProtocolSender getCommandProtocolSender() {
        return commandProtocolSender;
    }

    @Override
    public ConnectionProtocolReceiver getConnectionProtocolReceiver() {
        return connectionProtocolReceiver;
    }

    @Override
    public ParameterProtocolSender getParameterProtocolSender() {
        return parameterProtocolSender;
    }

    @Override
    public TelemetryReceiver getTelemetryReceiver() {
        return telemetryReceiver;
    }

    @Override
    public Future<Void> getHeartbeatSenderFuture() {
        return heartbeatSenderFuture;
    }

    @Override
    public CameraProtocolSender getCameraProtocolSender() {
        return cameraProtocolSender;
    }

    @Override
    public MavlinkDroneConnectionItem getConnectionItem() {
        return connectionItem;
    }

    @Override
    public ReadOnlyAsyncListProperty<MavlinkCamera> connectedCamerasProperty() {
        return mavlinkCameraListener.connectedCamerasProperty();
    }

    @Override
    public CancellationSource getCancellationSource() {
        return externalCancellationSource;
    }

    @Override
    public MavType getMavType() {
        return mavType;
    }
}
