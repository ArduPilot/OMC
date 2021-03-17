/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.drone.ArduCopterDrone;
import com.intel.missioncontrol.drone.DroneConnectionException;
import com.intel.missioncontrol.drone.MavlinkDrone;
import com.intel.missioncontrol.drone.PX4Drone;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkEndpoint;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkHandler;
import com.intel.missioncontrol.hardware.IConnectionProperties;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IMavlinkConnectionProperties;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import io.dronefleet.mavlink.common.Heartbeat;
import io.dronefleet.mavlink.common.MavAutopilot;
import io.dronefleet.mavlink.common.MavComponent;
import io.dronefleet.mavlink.common.MavType;
import io.dronefleet.mavlink.util.EnumValue;
import org.apache.commons.lang3.NotImplementedException;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An IConnector that allows creating a MavlinkDrone subclass instance from a MavlinkDroneConnectionItem. */
public class MavlinkDroneConnector extends MavlinkConnector<MavlinkDrone, MavlinkDroneConnectionItem> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkDroneConnector.class);

    public interface Factory {
        MavlinkDroneConnector create(MavlinkDroneConnectionItem connectionItem);
    }

    private final MavlinkCameraListener.Factory mavlinkCameraListenerFactory;

    private final ILanguageHelper languageHelper;

    @Inject
    MavlinkDroneConnector(
            IConnectionListenerService droneConnectionListenerService,
            IHardwareConfigurationManager hardwareConfigurationManager,
            MavlinkCameraListener.Factory mavlinkCameraListenerFactory,
            ILanguageHelper languageHelper,
            @Assisted MavlinkDroneConnectionItem connectionItem) {
        super(
            connectionItem,
            droneConnectionListenerService,
            hardwareConfigurationManager,
            EnumValue.of(MavComponent.MAV_COMP_ID_AUTOPILOT1).value());
        this.mavlinkCameraListenerFactory = mavlinkCameraListenerFactory;
        this.languageHelper = languageHelper;
    }

    @Override
    protected MavlinkDrone create(
            Heartbeat heartbeat,
            MavlinkDroneConnectionItem connectionItem,
            MavlinkHandler mavlinkHandler,
            MavlinkEndpoint targetEndpoint,
            CancellationSource cancellationSource,
            ConnectionProtocolSender connectionProtocolSender,
            Future<Void> heartbeatSenderFuture) {
        MavAutopilot mavAutopilot = heartbeat.autopilot().entry();
        MavType mavType = heartbeat.type().entry();

        var mavlinkCameraListener = mavlinkCameraListenerFactory.create(connectionItem, cancellationSource);

        MavlinkDroneConnection conn =
            new MavlinkDroneConnection(
                connectionItem,
                mavlinkHandler,
                targetEndpoint,
                cancellationSource,
                connectionProtocolSender,
                heartbeatSenderFuture,
                mavlinkCameraListener);

        String platformId = conn.getConnectionItem().getPlatformId();
        IPlatformDescription platformDescription = hardwareConfigurationManager.getPlatformDescription(platformId);

        IConnectionProperties connectionProperties = platformDescription.getConnectionProperties();

        var invalidPlatformException =
            new DroneConnectionException(
                MavlinkDroneConnector.class,
                false,
                languageHelper.getString(MavlinkDroneConnector.class, "incompatibleModel"));

        if (!(connectionProperties instanceof IMavlinkConnectionProperties)) {
            throw invalidPlatformException;
        }

        String mavTypeString = ((IMavlinkConnectionProperties)connectionProperties).getMavlinkType();
        MavType platformDefMavType;
        try {
            platformDefMavType = MavType.valueOf(mavTypeString);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error in platform definition for " + platformId + ": " + mavTypeString, e);
            throw invalidPlatformException;
        }

        String mavAutopilotString = ((IMavlinkConnectionProperties)connectionProperties).getMavlinkAutopilot();
        MavAutopilot platformDefMavAutopilot;
        try {
            platformDefMavAutopilot = MavAutopilot.valueOf(mavAutopilotString);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error in platform definition for " + platformId + ": " + mavAutopilotString, e);
            throw invalidPlatformException;
        }

        if (mavType != platformDefMavType || mavAutopilot != platformDefMavAutopilot) {
            throw invalidPlatformException;
        }

        String droneType = connectionProperties.getDroneType();

        switch (droneType) {
        case "PX4Drone":
            return PX4Drone.create(conn, platformDescription);
        case "ArduCopterDrone":
            return ArduCopterDrone.create(conn, platformDescription);
        default:
            throw new NotImplementedException("Drone type " + droneType + " is not supported");
        }
    }
}