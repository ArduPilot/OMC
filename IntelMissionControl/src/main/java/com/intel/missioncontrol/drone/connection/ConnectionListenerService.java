/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.google.inject.Inject;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.settings.ISettingsManager;

public class ConnectionListenerService implements IConnectionListenerService {
    // We currently have only a Mavlink (UDP) listener. More might be added here (exposing a list of
    // IDroneConnectionListeners).
    private final IConnectionListener connectionListener;

    @Inject
    public ConnectionListenerService(
            ISettingsManager settingsManager, IHardwareConfigurationManager hardwareConfigurationManager) {
        connectionListener = new MavlinkConnectionListener(settingsManager, hardwareConfigurationManager);
    }

    @Override
    public IConnectionListener getConnectionListener() {
        return connectionListener;
    }
}
