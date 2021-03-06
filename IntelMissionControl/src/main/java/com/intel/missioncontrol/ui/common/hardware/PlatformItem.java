/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common.hardware;

import com.intel.missioncontrol.hardware.IPlatformDescription;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PlatformItem {
    private IPlatformDescription description;
    private PlatformItemType itemType;
    private boolean isCaption;

    public PlatformItem(PlatformItemType itemType) {
        this.itemType = itemType;
        this.isCaption = true;
    }

    public PlatformItem(PlatformItemType itemType, IPlatformDescription description) {
        this.description = description;
        this.itemType = itemType;
        this.isCaption = false;
    }

    @Nullable
    public IPlatformDescription getDescription() {
        return description;
    }

    public PlatformItemType getItemType() {
        return itemType;
    }

    public boolean isCaption() {
        return isCaption;
    }

    @Override
    public String toString() {
        return description != null ? description.getName() : getClass().getName();
    }
}
