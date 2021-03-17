/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.drone.connection.mavlink.CameraProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.CommandProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkEndpoint;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkHandler;
import com.intel.missioncontrol.drone.connection.mavlink.ParameterProtocolSender;
import java.net.InetSocketAddress;
import org.asyncfx.concurrent.CancellationSource;

public class MavlinkCameraConnection {
    private final CommandProtocolSender commandProtocolSender;
    private final CameraProtocolSender cameraProtocolSender;
    private final ParameterProtocolSender parameterProtocolSender;
    private final MavlinkCameraConnectionItem cameraConnectionItem;

    MavlinkCameraConnection(
            MavlinkCameraConnectionItem cameraConnectionItem,
            MavlinkHandler mavlinkHandler,
            CancellationSource cancellationSource) {
        this.cameraConnectionItem = cameraConnectionItem;

        MavlinkEndpoint targetEndpoint =
            new MavlinkEndpoint(
                cameraConnectionItem.getTransportType(),
                new InetSocketAddress(cameraConnectionItem.getHost(), cameraConnectionItem.getPort()),
                cameraConnectionItem.getSystemId(),
                cameraConnectionItem.getComponentId());

        commandProtocolSender = new CommandProtocolSender(targetEndpoint, mavlinkHandler, cancellationSource);
        cameraProtocolSender = new CameraProtocolSender(targetEndpoint, mavlinkHandler, cancellationSource);
        parameterProtocolSender = new ParameterProtocolSender(targetEndpoint, mavlinkHandler, cancellationSource);
    }

    public ParameterProtocolSender getParameterProtocolSender() {
        return parameterProtocolSender;
    }

    public MavlinkCameraConnectionItem getCameraConnectionItem() {
        return cameraConnectionItem;
    }

    public CommandProtocolSender getCommandProtocolSender() {
        return commandProtocolSender;
    }

    public CameraProtocolSender getCameraProtocolSender() {
        return cameraProtocolSender;
    }
}
