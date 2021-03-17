/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.common;

import java.nio.file.Path;

public interface IPathProvider {

    Path getSettingsDirectory();

    Path getProjectsDirectory();

    Path getProfilingDirectory();

    Path getUserHomeDirectory();

    Path getErrorUploadDirectory();

    Path getUpdatesDirectory();

    Path getSettingsFile();

    Path getLegacySettingsFile();

    Path getCacheDirectory();

    Path getGazetteerCacheDirectory();

    Path getLogDirectory();

    Path getExternalRtkConfigDirectory();

    Path getCameraDescriptionsDirectory();

    Path getLensDescriptionsDirectory();

    Path getPlatformDescriptionsDirectory();

    Path getTemplatesDirectory();

    Path getLicenseSettingsFile();

    Path getGeoidDirectory();

    Path getLocalAirspacesFolder();

    Path getWebviewCacheFolder();

}
