/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import java.io.File;
import java.nio.file.Path;

/**
 * This class defines constants to be used when handling with a mission.
 *
 * @author aiacovici
 */
public class MissionConstants {

    /** Mission folder structure. */
    public static final String FOLDER_NAME_LOGFILES = "Recorder";

    public static final String FOLDER_NAME_FLIGHTPLANS = "Flight Plans";
    public static final String FOLDER_NAME_KML = "KML";
    public static final String FOLDER_NAME_PLANECONFIG = "UAV Configurations";
    public static final String FOLDER_NAME_SCREENSHOT = "Screenshots";
    public static final String FOLDER_NAME_MATCHINGS = "Datasets";
    public static final String FOLDER_NAME_FLIGHT_LOGS = "Flight Logs";
    public static final String FOLDER_NAME_AUTOSAVE = "Auto Save";

    public static final String FLIGHT_PLAN_EXT = ".fml";
    public static final String MATCHING_EXT = ".ptg";

    /** Mission configuration file. */
    public static final String LEGACY_CONFIG_FILENAME_SUFFIX = ".mfs";

    public static final String LEGACY_CONFIG_FILENAME = "settings" + LEGACY_CONFIG_FILENAME_SUFFIX;

    public static final String SETTINGS_TEMP_FILE_NAME = String.format("%s~", LEGACY_CONFIG_FILENAME);

    /** New mission configuration file. */
    public static final String NEW_CONFIG_FILENAME_SUFFIX = ".json";

    public static final String NEW_CONFIG_FILENAME = "settings" + NEW_CONFIG_FILENAME_SUFFIX;

    public static final String MISSION_SCREENSHOT_FILENAME = "main_view_screenshot.jpg";
    public static final String MISSION_SCREENSHOT_LOW_RES_FILENAME = "main_view_screenshot_lowRes.jpg";

    // legacy folders info support
    public static File getMatchingsFolder(File baseFolder) {
        return new File(baseFolder, FOLDER_NAME_MATCHINGS);
    }

    public static File getLogFolder(File baseFolder) {
        return new File(baseFolder, FOLDER_NAME_LOGFILES);
    }

    public static File getFlightplanFolder(File baseFolder) {
        return new File(baseFolder, FOLDER_NAME_FLIGHTPLANS);
    }

    public static File getFlightplanAutosaveFolder(File baseFolder) {
        return new File(baseFolder + File.separator + FOLDER_NAME_FLIGHTPLANS + File.separator + FOLDER_NAME_AUTOSAVE);
    }

    public static File getPlaneConfigFolder(File baseFolder) {
        return new File(baseFolder, FOLDER_NAME_PLANECONFIG);
    }

    public static File getPlaneConfigAutosaveFolder(File baseFolder) {
        return new File(baseFolder + File.separator + FOLDER_NAME_PLANECONFIG + File.separator + FOLDER_NAME_AUTOSAVE);
    }

    public static File getKMLFolder(File baseFolder) {
        return new File(baseFolder, FOLDER_NAME_KML);
    }

    public static File getFlightLogsFolder(File baseFolder) {
        return new File(baseFolder, FOLDER_NAME_FLIGHT_LOGS);
    }

    public static File getScreenshotFolder(File baseFolder) {
        return new File(baseFolder, FOLDER_NAME_SCREENSHOT);
    }

    // legacy folders info support with PATH
    public static File getMatchingsFolder(Path baseFolder) {
        return getMatchingsFolder(baseFolder.toFile());
    }

    public static File getLogFolder(Path baseFolder) {
        return getLogFolder(baseFolder.toFile());
    }

    public static File getFlightplanFolder(Path baseFolder) {
        return getFlightplanFolder(baseFolder.toFile());
    }

    public static File getFlightplanAutosaveFolder(Path baseFolder) {
        return getFlightplanAutosaveFolder(baseFolder.toFile());
    }

    public static File getPlaneConfigFolder(Path baseFolder) {
        return getPlaneConfigFolder(baseFolder.toFile());
    }

    public static File getPlaneConfigAutosaveFolder(Path baseFolder) {
        return getPlaneConfigAutosaveFolder(baseFolder.toFile());
    }

    public static File getKMLFolder(Path baseFolder) {
        return getKMLFolder(baseFolder.toFile());
    }

    public static File getFlightLogsFolder(Path baseFolder) {
        return getFlightLogsFolder(baseFolder.toFile());
    }

    public static File getScreenshotFolder(Path baseFolder) {
        return getScreenshotFolder(baseFolder.toFile());
    }

    ///////////////////////////

    public static Path getLegacyConfigFile(File baseFolder) {
        return getLegacyConfigFile(baseFolder.toPath());
    }

    public static Path getNewtConfigFile(File baseFolder) {
        return getNewtConfigFile(baseFolder.toPath());
    }

    public static Path getLegacyConfigFile(Path baseFolder) {
        return baseFolder.resolve(LEGACY_CONFIG_FILENAME);
    }

    public static Path getNewtConfigFile(Path baseFolder) {
        return baseFolder.resolve(NEW_CONFIG_FILENAME);
    }

}
