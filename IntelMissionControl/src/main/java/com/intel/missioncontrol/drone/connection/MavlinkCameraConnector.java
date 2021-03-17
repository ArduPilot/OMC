/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.drone.MavlinkCamera;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkEndpoint;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkHandler;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import io.dronefleet.mavlink.common.Heartbeat;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;

/** An IConnector that allows creating a MavlinkCamera instance from a MavlinkCameraConnectionItem. */
public class MavlinkCameraConnector extends MavlinkConnector<MavlinkCamera, MavlinkCameraConnectionItem> {

    public interface Factory {
        MavlinkCameraConnector create(MavlinkCameraConnectionItem connectionItem);
    }

    @Inject
    MavlinkCameraConnector(
            IConnectionListenerService droneConnectionListenerService,
            IHardwareConfigurationManager hardwareConfigurationManager,
            @Assisted MavlinkCameraConnectionItem connectionItem) {
        super(
            connectionItem,
            droneConnectionListenerService,
            hardwareConfigurationManager,
            connectionItem.getComponentId());
    }

    @Override
    protected MavlinkCamera create(
            Heartbeat heartbeat,
            MavlinkCameraConnectionItem connectionItem,
            MavlinkHandler mavlinkHandler,
            MavlinkEndpoint targetEndpoint,
            CancellationSource cancellationSource,
            ConnectionProtocolSender connectionProtocolSender,
            Future<Void> heartbeatSenderFuture) {
        String descriptionId = connectionItem.getDescriptionId();
        IGenericCameraDescription cameraDescription = hardwareConfigurationManager.getCameraDescription(descriptionId);

        MavlinkCameraConnection conn = new MavlinkCameraConnection(connectionItem, mavlinkHandler, cancellationSource);

        return new MavlinkCamera(conn, cameraDescription);
    }
}
