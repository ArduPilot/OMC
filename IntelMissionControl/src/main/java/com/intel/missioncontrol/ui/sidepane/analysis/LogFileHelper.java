/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import static eu.mavinci.desktop.helper.MFileFilter.photoLogFilter;
import static eu.mavinci.desktop.helper.MFileFilter.vlgFilter;
import static eu.mavinci.desktop.helper.MFileFilter.vlgZipFilter;

import com.intel.missioncontrol.concurrent.BackgroundComputeCache;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ITaggingAlgorithm;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.TaggingAlgorithmA;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.asyncfx.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFileHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileHelper.class);

    private static BackgroundComputeCache<LogParserCacheEntry, File> parserCacheEntrySet =
        new BackgroundComputeCache<>(
            2,
            (logFile) -> {
                ITaggingAlgorithm logParser = TaggingAlgorithmA.createNewDefaultTaggingAlgorithm();
                LogParserCacheEntry cacheEntry2 = new LogParserCacheEntry();
                logParser.clearLogfiles();

                try {
                    logParser.loadLogfile(logFile, true);
                } catch (Exception e) {
                    LOGGER.error("issues parsing logfile: " + logFile, e);
                }

                cacheEntry2.allLogs = logParser.getLogsAll();
                cacheEntry2.hasRtk = logParser.isRtkPosAvaliable();
                return cacheEntry2;
            });

    public static class LogParserCacheEntry {
        TreeSet<CPhotoLogLine> allLogs;
        boolean hasRtk;
    }

    public static void setLogsIdentical(File file, File target) {
        parserCacheEntrySet.setIdentical(file, target);
    }

    public static Future<LogParserCacheEntry> parseLogAsync(File path) {
        return parserCacheEntrySet.get(path);
    }

    public static List<FlightLogEntry> getLogsInFolder(File folder, boolean needToParse) {
        List<FlightLogEntry> flightLogsNew = new ArrayList<>();

        if (!folder.exists()) {
            return flightLogsNew;
        }

        // Search for supported file types
        File[] logs =
            folder.listFiles(
                (dir, name) -> photoLogFilter.accept(name) || vlgZipFilter.accept(name) || vlgFilter.accept(name));

        if (logs != null && logs.length > 0) {
            for (File log : logs) {
                FlightLogEntry logEntry = new FlightLogEntry(log, needToParse);
                flightLogsNew.add(logEntry);
            }
        }

        // Search for AscTec folders starting from the root folder
        List<File> logFolders = new ArrayList<>();
        FileHelper.scanSubFoldersForFiles(folder, logFolders, 1, MFileFilter.ascTecLogFolder::acceptTrinityLog);

        // since jsons are not matching on a folder but on a file in a folder, we have to scan one level deeper than on
        // falcon logs
        FileHelper.scanSubFoldersForFiles(folder, logFolders, 2, MFileFilter.photoJsonFilter::acceptWithoutFolders);

        if (!logFolders.isEmpty()) {
            for (File logFolder : logFolders) {
                FlightLogEntry logEntry = new FlightLogEntry(logFolder, needToParse);
                flightLogsNew.add(logEntry);
            }
        }

        int countWithLogs = 0;
        for (FlightLogEntry logEntry : flightLogsNew) {
            if (logEntry.getImageFolder() != null) {
                countWithLogs++;
            }
        }

        List<FlightLogEntry> flightLogsNew2 = new ArrayList<>();
        if (countWithLogs > 0 && countWithLogs < flightLogsNew.size()) {
            // we cant deal with mixed datasets with included images and without at the same time?
            // drop the once without
            for (FlightLogEntry logEntry : flightLogsNew) {
                if (logEntry.getImageFolder() != null) {
                    flightLogsNew2.add(logEntry);
                }
            }

            flightLogsNew = flightLogsNew2;
        }

        if (flightLogsNew.isEmpty()) {
            LOGGER.info("No photo logs found in folder {}.", folder);
        } else {
            // flightLogsNew.sort(FlightLogEntry::compareTo);
        }

        return flightLogsNew;
    }

}
