/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.model;

import com.intel.missioncontrol.hardware.IPlatformDescription;

/** @author Vladimir Iordanov */
public class LocalSimulationItem {
    private final IPlatformDescription platformDescription;

    public LocalSimulationItem(IPlatformDescription platformDescription) {
        this.platformDescription = platformDescription;
    }

    public String getLabel() {
        return platformDescription.getName();
    }

    public IPlatformDescription getPlatformDescription() {
        return platformDescription;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LocalSimulationItem that = (LocalSimulationItem)o;

        return platformDescription.equals(that.platformDescription);
    }

    @Override
    public int hashCode() {
        return platformDescription.hashCode();
    }
}
