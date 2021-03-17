/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import com.google.inject.Singleton;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/** @author Vladimir Iordanov */
@Singleton
public class FileUtils {
    public String extractFilename(String fullPath) {
        Path path = Paths.get(fullPath);
        Path fileName = path.getFileName();
        return fileName != null ? fileName.toString() : null;
    }

    public String extractFilename(File file) {
        Path fileName = file.toPath().getFileName();
        return fileName != null ? fileName.toString() : null;
    }
}
