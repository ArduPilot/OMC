/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.drone.MavlinkCamera;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncListChangeListener;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.CancellationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Auto-connects and keeps track of mavlink cameras associated with a mavlink drone connection. */
public class MavlinkCameraListener {

    public interface Factory {
        MavlinkCameraListener create(
                MavlinkDroneConnectionItem droneConnectionItem, CancellationSource cancellationSource);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkCameraListener.class);

    private final AsyncListProperty<MavlinkCamera> connectedCameras =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<MavlinkCamera>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final MavlinkCameraConnector.Factory cameraConnectorFactory;
    private final IHardwareConfigurationManager hardwareConfigurationManager;

    private final MavlinkDroneConnectionItem droneConnectionItem;

    private final CancellationSource cancellationSource;

    @Inject
    MavlinkCameraListener(
            IConnectionListenerService connectionListenerService,
            MavlinkCameraConnector.Factory cameraConnectorFactory,
            IHardwareConfigurationManager hardwareConfigurationManager,
            @Assisted MavlinkDroneConnectionItem droneConnectionItem,
            @Assisted CancellationSource cancellationSource) {
        this.cameraConnectorFactory = cameraConnectorFactory;
        this.droneConnectionItem = droneConnectionItem;
        this.cancellationSource = cancellationSource;
        this.hardwareConfigurationManager = hardwareConfigurationManager;

        final AsyncListChangeListener<? super IConnectionItem> availableCameraConnectionItemsChangeListener =
            change -> {
                while (change.next()) {
                    change.getAddedSubList().forEach(this::onCameraAdded);
                    change.getRemoved().forEach(this::onCameraRemoved);
                }
            };

        AsyncObservableList<IConnectionItem> availableCameraConnectionItems =
            connectionListenerService.getConnectionListener().onlineCameraConnectionItemsProperty();

        try (LockedList<IConnectionItem> list = availableCameraConnectionItems.lock()) {
            for (IConnectionItem connItem : list) {
                onCameraAdded(connItem);
            }

            availableCameraConnectionItems.addListener(availableCameraConnectionItemsChangeListener);
        }

        cancellationSource.addListener(
            mayInterruptIfRunning ->
                availableCameraConnectionItems.removeListener(availableCameraConnectionItemsChangeListener));
    }

    private void onCameraAdded(IConnectionItem cameraConnItem) {
        if (!(cameraConnItem instanceof MavlinkCameraConnectionItem)) {
            return;
        }

        MavlinkCameraConnectionItem camItem = (MavlinkCameraConnectionItem)cameraConnItem;

        // check if this camItem belongs to this drone connection:
        // TODO define how to associate cameras with drones. For now, use systemId.
        if (camItem.getSystemId() != droneConnectionItem.getSystemId()) {
            return;
        }

        MavlinkCameraConnector cameraConnector = cameraConnectorFactory.create(camItem);
        cameraConnector
            .connectAsync()
            .whenSucceeded(
                cam -> {
                    LOGGER.debug("Camera added: " + cam);
                    connectedCameras.add(cam);
                })
            .whenFailed(e -> LOGGER.error("Failed to connect to camera", e.getCause()));

        // disconnect camera when cancelled
        cancellationSource.addListener(mayInterruptIfRunning -> cameraConnector.disconnectAsync());
    }

    private void onCameraRemoved(IConnectionItem cameraConnItem) {
        if (!(cameraConnItem instanceof MavlinkCameraConnectionItem)) {
            return;
        }

        try (LockedList<MavlinkCamera> cams = connectedCameras.lock()) {
            cams.stream()
                .filter(cam -> cam.getConnectionItem().isSameConnection(cameraConnItem))
                .findFirst()
                .ifPresent(cams::remove);
        }
    }

    public ReadOnlyAsyncListProperty<MavlinkCamera> connectedCamerasProperty() {
        return connectedCameras;
    }

}
