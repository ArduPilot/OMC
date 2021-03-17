/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import java.io.File;

public interface ISaveable {

    String getName();

    boolean hasUnsavedChanges();

    boolean canBeSaved();

    File getResourceFile();

    void save();

}
