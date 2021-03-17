/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.accessibility;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface IShortcutAware {

    @Nullable
    String getShortcut();

}
