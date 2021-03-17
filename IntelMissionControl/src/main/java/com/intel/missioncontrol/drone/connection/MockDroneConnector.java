/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.drone.MockDrone;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;

public class MockDroneConnector implements IConnector<MockDrone> {

    public interface Factory {
        MockDroneConnector create(MockConnectionItem connectionItem);
    }

    private final IHardwareConfigurationManager hardwareConfigurationManager;
    private final MockConnectionItem connectionItem;

    @Inject
    MockDroneConnector(
            IHardwareConfigurationManager hardwareConfigurationManager, @Assisted MockConnectionItem connectionItem) {
        this.hardwareConfigurationManager = hardwareConfigurationManager;
        this.connectionItem = connectionItem;
    }

    @Override
    public IConnectionItem getConnectionItem() {
        return connectionItem;
    }

    @Override
    public Future<MockDrone> connectAsync() {
        return Futures.successful(
            new MockDrone(hardwareConfigurationManager.getHardwareConfiguration(connectionItem.getDescriptionId())));
    }

    @Override
    public Future<Void> disconnectAsync() {
        return Futures.successful();
    }

}
