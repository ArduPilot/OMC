/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model.discovery;

public interface DiscoveryCallback<T> {
    public void onStopped(Exception e);

    public void onStarted();

    public void onDeviceDiscovered(T device);
}
