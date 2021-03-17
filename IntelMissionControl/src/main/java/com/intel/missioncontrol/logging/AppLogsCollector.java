/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.logging;

import eu.mavinci.desktop.helper.FileHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.apache.commons.io.FilenameUtils;

public class AppLogsCollector {
    private static  final String LOG = "log";
    //some safety margin of time in ms from the first possible logging and as a consequence first possible log rollover
    private static final long MARGIN = 60000;
    private final long startTimestamp;
    private final Path logConfigurationFilePath;

    public AppLogsCollector(Path logConfigurationFilePath) {
        startTimestamp = System.currentTimeMillis() - MARGIN;
        this.logConfigurationFilePath = logConfigurationFilePath;
    }

    public List<File> getCurrentLogFiles() {
        ArrayList<File> logs = new ArrayList<>();
        Arrays.stream(logConfigurationFilePath.toFile().listFiles())
            .forEach(
                e -> {
                    if (isLog(e) && e.lastModified() > startTimestamp) {
                        logs.add(e);
                    }
                });
        return logs;
    }

    private boolean isLog(File e) {
        return FilenameUtils.getExtension(e.getName()).equals(LOG);
    }

    public Vector<File> getCurrentLogFilesSnapShot() throws IOException {
        Vector<File> tmp = new Vector<>();
        List<File> logs = getCurrentLogFiles();
        for (File f : logs) {
            File fNew = FileHelper.zipSingleFile(f);
            fNew.deleteOnExit();
            tmp.add(fNew);
        }

        return tmp;
    }
}
