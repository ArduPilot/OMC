/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

public interface IMapClearingCenter extends IMapClearingCenterListener {

    void addWeakListener(IMapClearingCenterListener listener);

    void removeWeakListener(IMapClearingCenterListener listener);

    default void clearAllCaches() {
        clearUavImageCache();
        clearTrackLog();
        clearOldTrackCache();
    }
}
