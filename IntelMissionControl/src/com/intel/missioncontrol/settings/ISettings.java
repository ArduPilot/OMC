/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import eu.mavinci.core.obfuscation.IKeepAll;

/**
 * Identifies a class that contains settings. A class that implements this interface must also be annotated with the
 * {@link SettingsMetadata} annotation.
 */
public interface ISettings extends IKeepAll {

    /**
     * Occurs when SettingsManager has finished loading the settings class from disk. This method can be used to set up
     * initial state.
     */
    default void onLoaded() {}

}
