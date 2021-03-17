/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

public interface ISettingsManager {

    <T> T getSection(final Class<T> settingsType);

}
