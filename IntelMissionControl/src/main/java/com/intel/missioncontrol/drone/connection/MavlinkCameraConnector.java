/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.drone.MavlinkCamera;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkEndpoint;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkHandler;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.hardware.MavlinkCameraSpecification;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import io.dronefleet.mavlink.common.CameraInformation;
import io.dronefleet.mavlink.common.Heartbeat;
import java.util.Arrays;
import java.util.Optional;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An IConnector that allows creating a MavlinkCamera instance from a MavlinkCameraConnectionItem. */
public class MavlinkCameraConnector extends MavlinkConnector<MavlinkCamera, MavlinkCameraConnectionItem> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkCameraConnector.class);
    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;

    public interface Factory {
        MavlinkCameraConnector create(MavlinkCameraConnectionItem connectionItem);
    }

    @Inject
    MavlinkCameraConnector(
            IConnectionListenerService droneConnectionListenerService,
            IHardwareConfigurationManager hardwareConfigurationManager,
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            @Assisted MavlinkCameraConnectionItem connectionItem) {
        super(
            connectionItem,
            droneConnectionListenerService,
            hardwareConfigurationManager,
            connectionItem.getComponentId());
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
    }

    @Override
    protected Future<MavlinkCamera> createComponentAsync(
            Heartbeat heartbeat,
            MavlinkCameraConnectionItem connectionItem,
            MavlinkHandler mavlinkHandler,
            MavlinkEndpoint targetEndpoint,
            CancellationSource cancellationSource,
            ConnectionProtocolSender connectionProtocolSender,
            Future<Void> heartbeatSenderFuture) {
        MavlinkCameraConnection conn = new MavlinkCameraConnection(connectionItem, mavlinkHandler, cancellationSource);

        return conn.getCommandProtocolSender()
            .requestCameraInformationAsync()
            .thenFinallyApply(
                this::checkCameraInformation,
                err -> {
                    LOGGER.error("Error requesting CAMERA_INFORMATION", err);
                    return null;
                })
            .thenApply(
                descriptionId -> {
                    try {
                        IGenericCameraDescription cameraDescription =
                            hardwareConfigurationManager.getCameraDescription(descriptionId);
                        return new MavlinkCamera(conn, cameraDescription);
                    } catch (Exception err) {
                        LOGGER.error("Couldn't identify the camera");
                        throw err;
                    }
                });
    }

    private String checkCameraInformation(CameraInformation camInfo) {
        String model = new String(camInfo.modelName());
        String vendor = new String(camInfo.vendorName());

        Optional<String> camId =
            Arrays.stream(hardwareConfigurationManager.getCameras())
                .filter(
                    desc -> {
                        for (MavlinkCameraSpecification spec : desc.getMavlinkCameraSpecifications()) {
                            if (spec.getModel().equals(model) && spec.getVendor().equals(vendor)) return true;
                        }

                        return false;
                    })
                .map(IGenericCameraDescription::getId)
                .findFirst();

        if (camId.isPresent()) {
            return camId.get();
        }

        /* FIXME: these UI dependencies should be propagated upwards via an exception */
        String notFoundText =
            languageHelper.getString(MavlinkCameraConnector.class, "incompatibleModel", vendor, model);
        applicationContext.addToast(Toast.of(ToastType.INFO).setText(notFoundText).create());
        return null;
    }
}
