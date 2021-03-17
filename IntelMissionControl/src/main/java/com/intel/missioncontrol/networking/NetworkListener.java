/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

public interface NetworkListener {

    void networkActivityStarted(Object tag);

    void networkActivityStopped(Object tag);

}
