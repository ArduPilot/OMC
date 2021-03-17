/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.drone.MavlinkCamera;
import com.intel.missioncontrol.drone.connection.mavlink.CameraProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.CommandProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolReceiver;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.MissionProtocolReceiver;
import com.intel.missioncontrol.drone.connection.mavlink.MissionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.ParameterProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.ReceivedPayload;
import com.intel.missioncontrol.drone.connection.mavlink.TelemetryReceiver;
import io.dronefleet.mavlink.common.MavType;
import java.util.function.Consumer;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;

public interface IMavlinkDroneConnection {
    MissionProtocolReceiver createMissionProtocolReceiver(CancellationSource cancellationSource);

    ConnectionProtocolSender getConnectionProtocolSender();

    MissionProtocolSender getMissionProtocolSender();

    CommandProtocolSender getCommandProtocolSender();

    ConnectionProtocolReceiver getConnectionProtocolReceiver();

    ParameterProtocolSender getParameterProtocolSender();

    CameraProtocolSender getCameraProtocolSender();

    TelemetryReceiver getTelemetryReceiver();

    Future<Void> getHeartbeatSenderFuture();

    MavlinkDroneConnectionItem getConnectionItem();

    ReadOnlyAsyncListProperty<MavlinkCamera> connectedCamerasProperty();

    CancellationSource getCancellationSource();

    MavType getMavType();

    <TPayload> Future<Void> registerTelemetryWithAutoTimeoutAsync(
            Class<TPayload> payloadType, Consumer<ReceivedPayload<TPayload>> payloadReceivedFnc, Runnable onTimeout);
}
