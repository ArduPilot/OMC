/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.sidepane.analysis.FlightLogEntry;
import com.intel.missioncontrol.ui.sidepane.analysis.LogFileHelper;
import eu.mavinci.core.helper.MProperties;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.hildan.fxgson.FxGson;
import org.hildan.fxgson.factories.JavaFxPropertyTypeAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissionInfoManager implements IMissionInfoManager {
    /** LEGACY */
    private static final String KEY_LOADED_FLIGHTPLAN = "loadedFlightplan.";

    private static final String SECTOR_MIN_LATITUDE = "sector.minLatitude";
    private static final String SECTOR_MAX_LATITUDE = "sector.maxLatitude";
    private static final String SECTOR_MIN_LONGITUDE = "sector.minLongitude";
    private static final String SECTOR_MAX_LONGITUDE = "sector.maxLongitude";
    private static final String APPLICATION_SRS = "Application.SRS";
    private static final String APPLICATION_SRS_NAME = "Application.SRS.name";
    private static final String APPLICATION_SRS_WKT = "Application.SRS.wkt";
    private static final String APPLICATION_SRS_ORIGIN = "Application.SRS.origin";
    private static final String FIRST_PIC_MATCHING_KEY_IN_RESOURCES = ".MapLayerVisibility.resource_pictureTaGging.";
    private String LEGACY_CONFIG_FILENAME_SUFFIX = ".mfs";
    private String LEGACY_CONFIG_FILENAME = "settings" + LEGACY_CONFIG_FILENAME_SUFFIX;

    ///////////////////////////////////////////////////////////////////

    private String CONFIG_FILENAME_SUFFIX = ".json";
    private String CONFIG_FILENAME = "settings" + CONFIG_FILENAME_SUFFIX;

    private static final Logger LOGGER = LoggerFactory.getLogger(MissionInfoManager.class);

    private ISrsManager srsManager;
    private final ISettingsManager settingsManager;

    private final Gson gson =
        FxGson.coreBuilder()
            .registerTypeAdapterFactory(new JavaFxPropertyTypeAdapterFactory())
            .serializeNulls()
            .create();

    @Inject
    public MissionInfoManager(ISettingsManager settingsManager, ISrsManager srsManager) {
        this.settingsManager = settingsManager;
        this.srsManager = srsManager;
    }

    public IMissionInfo readFromFile(Path folder) throws IOException {
        Path json = folder.resolve(CONFIG_FILENAME);
        boolean fileExists = Files.exists(json);
        MissionInfo missionSettings = null;

        if (fileExists) {
            try (InputStream inputStream = new FileInputStream(json.toFile());
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                missionSettings = gson.fromJson(reader, MissionInfo.class);
                if (!missionSettings.folderProperty().get().exists()) {
                    // change path if path/file was moved manually
                    missionSettings.folderProperty().set(folder.toFile());
                }

            } catch (Exception e) {
                // assuming the format of json has changed lets delete the file and create it again later
                LOGGER.error(e.getMessage(), e);
                Files.delete(json);
            }
        } else {
            return convertFromLegacySettings(folder);
        }

        return missionSettings;
    }

    public void saveToFile(IMissionInfo missionInfo) {
        final File settingsFile = missionInfo.getFolder().toPath().resolve(CONFIG_FILENAME).toFile();
        if (settingsFile == null) {
            return;
        }

        File dir = settingsFile.getParentFile();
        if (Files.notExists(dir.toPath())) {
            dir.mkdirs();
        }

        try (OutputStream outputStream = new FileOutputStream(settingsFile)) {
            JsonElement element = gson.toJsonTree(missionInfo);

            try (Writer streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                JsonWriter writer = new JsonWriter(streamWriter);
                writer.setIndent("\t");
                gson.toJson(element, writer);
            }
        } catch (IOException e) {
            MissionInfoManager.LOGGER.error(
                String.format("Cannot save mission settings to file %s. %s", settingsFile, e.getMessage()), e);
        }
    }

    public IMissionInfo convertFromLegacySettings(Path folder) throws IOException {
        MProperties settings = new MProperties();
        IMissionInfo missionSettings = new MissionInfo();
        missionSettings.setFolder(folder.toFile());
        missionSettings.setName(folder.getFileName().toString());
        missionSettings.setLastModified(new Date());

        Path legacy_config = folder.resolve(LEGACY_CONFIG_FILENAME);
        if (Files.exists(legacy_config)) {
            settings.loadFromXML(legacy_config.toFile());
            missionSettings.setLastModified(getLastModified(folder));

            /// setting legacy sector

            if (settings.getProperty(SECTOR_MIN_LATITUDE) != null) {
                missionSettings.setMinLatitude(Double.parseDouble(settings.getProperty(SECTOR_MIN_LATITUDE)));
                missionSettings.setMaxLatitude(Double.parseDouble(settings.getProperty(SECTOR_MAX_LATITUDE)));
                missionSettings.setMinLongitude(Double.parseDouble(settings.getProperty(SECTOR_MIN_LONGITUDE)));
                missionSettings.setMaxLongitude(Double.parseDouble(settings.getProperty(SECTOR_MAX_LONGITUDE)));
            }
            /// setting SRS
            if (settings.getProperty(APPLICATION_SRS) != null) {
                missionSettings.setSrsId(settings.getProperty(APPLICATION_SRS));
                missionSettings.setSrsName(settings.getProperty(APPLICATION_SRS_NAME));
                missionSettings.setSrsWkt(settings.getProperty(APPLICATION_SRS_WKT));
                missionSettings.setSrsOrigin(settings.getProperty(APPLICATION_SRS_ORIGIN));
            } else {
                // default SRS fallback
                MSpatialReference wgs84 = srsManager.getDefault();
                missionSettings.setSrsId(wgs84.id);
                missionSettings.setSrsName(wgs84.name);
                missionSettings.setSrsWkt(wgs84.wkt);
                missionSettings.setSrsOrigin(wgs84.getOrigin().toString());
            }
            ////////////////////////////////

            /// setting legacy flight plans
            int i = 0;
            String key = KEY_LOADED_FLIGHTPLAN + i;
            List<String> flightPlans = new ArrayList<>();
            while (settings.containsKey(key)) {
                String path = settings.getProperty(key);
                flightPlans.add(path);
                key = KEY_LOADED_FLIGHTPLAN + (++i);
            }

            missionSettings.setLoadedFlightPlans(flightPlans);
            ///////////////////////////

            /// setting legacy datasets
            i = 0;
            key = FIRST_PIC_MATCHING_KEY_IN_RESOURCES + i;
            List<String> dataSets = new ArrayList<>();
            while (settings.containsKey(key)) {
                String path = settings.getProperty(key);
                dataSets.add(path);
                key = FIRST_PIC_MATCHING_KEY_IN_RESOURCES + (++i);
            }

            missionSettings.setLoadedDataSets(dataSets);

            // parse flights
            parseFlightLogs(folder, missionSettings);
            saveToFile(missionSettings);

            /////////////////////////// 7
            return missionSettings;
        } else {
            throw new IOException(String.format("Legacy mission settings file %s does not exist", legacy_config));
        }
    }

    private Date getLastModified(Path folder) {
        ModifiedDateFileVisitor modifiedDateFileVisitor = new ModifiedDateFileVisitor();
        try {
            Files.walkFileTree(folder, modifiedDateFileVisitor);
        } catch (IOException e) {
            LOGGER.warn("Exception during calculating Mission date", e);
        }

        return modifiedDateFileVisitor.getDate();
    }

    private void parseFlightLogs(Path folder, IMissionInfo missionSettings) {
        File flightLogsFolder = MissionConstants.getFlightLogsFolder(folder.toFile());
        List<FlightLogEntry> logs = LogFileHelper.getLogsInFolder(flightLogsFolder, false);
        missionSettings.setFlightLogs(
            logs.stream()
                .map(element -> FileHelper.makeRelativePathSysIndep(flightLogsFolder, element.getPath()))
                .collect(Collectors.toList()));
    }

    private boolean isMissionFolder(File file) {
        return file.isDirectory() && (configExists(file) || legacyConfigExists(file));
    }

    public boolean configExists(File folder) {
        return Files.exists(getConfigFile(folder));
    }

    public boolean legacyConfigExists(File folder) {
        return Files.exists(getLegacyConfigFile(folder));
    }

    public Path getLegacyConfigFile(File base) {
        return base.toPath().resolve(LEGACY_CONFIG_FILENAME);
    }

    public Path getConfigFile(File base) {
        return base.toPath().resolve(CONFIG_FILENAME);
    }

}
