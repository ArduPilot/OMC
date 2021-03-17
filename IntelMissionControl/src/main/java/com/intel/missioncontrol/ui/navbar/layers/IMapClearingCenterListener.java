/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

public interface IMapClearingCenterListener {
    void clearUavImageCache();

    void clearTrackLog();

    void clearOldTrackCache();
}
