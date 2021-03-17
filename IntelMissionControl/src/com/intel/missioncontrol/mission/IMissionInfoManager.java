/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.beans.property.AsyncListProperty;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javafx.beans.property.ListProperty;

public interface IMissionInfoManager {

    IMissionInfo readFromFile(Path folder) throws IOException;

    void saveToFile(IMissionInfo missionInfo);

    IMissionInfo convertFromLegacySettings(Path folder) throws IOException;

    boolean configExists(File file);

    Path getLegacyConfigFile(File base);

    Path getConfigFile(File base);

}
