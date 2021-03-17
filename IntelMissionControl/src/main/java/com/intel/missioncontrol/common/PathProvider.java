/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.common;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathProvider implements IPathProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathProvider.class);

    private static final String SETTINGS_FOLDER_NAME = "Intel Mission Control";
    private static final String PROJECTS_FOLDER_NAME = "Intel Mission Control Projects";
    private static final String APP_LOGS_DIRECTORY = "appLogs";

    private final Path homeFolder = getWindowsSpecialFolder("5E6C858F-0E22-4760-9AFE-EA3317B67173");
    private final Path myDocumentsFolder = getWindowsSpecialFolder("FDD39AD0-238F-46AF-ADB4-6C85480369C7");
    private final Path myDownloadsFolder = getWindowsSpecialFolder("374DE290-123F-4565-9164-39C4925E467B");
    private final Path appDataFolder = Paths.get(System.getenv("APPDATA"));

    private Path appSettingsDirectory;
    private Path projectsDirectory;
    private Path appUpdatesDirectory;
    private Path settingsFile;
    private Path legacySettingsFile;
    private Path wwjCacheDirectory;
    private Path gazetteerCacheDirectory;
    private Path logFolder;
    private Path errorUploadFolder;
    private Path profilingFolder;
    private Path externalRtkConfigFolder;
    private Path hwDescriptionsFolderCameras;
    private Path hwDescriptionsFolderLenses;
    private Path hwDescriptionsFolderPlatforms;
    private Path templatesFolder;
    private Path licenseSettingsFile;
    private Path geoidFolder;
    private Path localAirspacesFolder;
    private Path webviewFolder;

    public PathProvider() {
        appSettingsDirectory = appDataFolder.resolve(SETTINGS_FOLDER_NAME);
        licenseSettingsFile = appSettingsDirectory.resolve("licence");
        projectsDirectory = myDocumentsFolder.resolve(PROJECTS_FOLDER_NAME);
        appUpdatesDirectory = appSettingsDirectory.resolve("updates");
        settingsFile = appSettingsDirectory.resolve("settings.json");
        legacySettingsFile = appSettingsDirectory.resolve("appSettings.xml");
        wwjCacheDirectory = appSettingsDirectory.resolve("cache");
        gazetteerCacheDirectory = appSettingsDirectory.resolve("gazetteerCache");
        logFolder = appSettingsDirectory.resolve(APP_LOGS_DIRECTORY);
        profilingFolder = appSettingsDirectory.resolve("performanceLogs");
        errorUploadFolder = appSettingsDirectory.resolve("errorReports");
        geoidFolder = appSettingsDirectory.resolve("geoids");
        externalRtkConfigFolder = appSettingsDirectory.resolve("externalRtkConfigs");
        webviewFolder = appSettingsDirectory.resolve("webview");

        Path hwDescriptionsFolder = appSettingsDirectory.resolve("hwDescriptions");
        hwDescriptionsFolderCameras = hwDescriptionsFolder.resolve("cameras");
        hwDescriptionsFolderLenses = hwDescriptionsFolder.resolve("lenses");
        hwDescriptionsFolderPlatforms = hwDescriptionsFolder.resolve("platforms");
        templatesFolder = appSettingsDirectory.resolve("templates");
        localAirspacesFolder =
            appSettingsDirectory.resolve("airspaces" + File.separator + "bundles" + File.separator + "openair");

        // if folder structure doesn't exist, create it now!
        Path[] folders = {
            appSettingsDirectory,
            appUpdatesDirectory,
            wwjCacheDirectory,
            gazetteerCacheDirectory,
            logFolder,
            errorUploadFolder,
            profilingFolder,
            externalRtkConfigFolder,
            templatesFolder,
            geoidFolder,
            hwDescriptionsFolder,
            hwDescriptionsFolderCameras,
            hwDescriptionsFolderLenses,
            hwDescriptionsFolderPlatforms,
            localAirspacesFolder,
            webviewFolder,
        };

        try {
            mkdirs(Arrays.stream(folders).map(Path::toFile).toArray(File[]::new));
        } catch (IOException e) {
            LOGGER.error("cant create folder", e);
        }
    }

    @Override
    public Path getSettingsDirectory() {
        return appSettingsDirectory;
    }

    @Override
    public Path getProjectsDirectory() {
        return projectsDirectory;
    }

    @Override
    public Path getProfilingDirectory() {
        return profilingFolder;
    }

    @Override
    public Path getUserHomeDirectory() {
        return homeFolder;
    }

    @Override
    public Path getErrorUploadDirectory() {
        return errorUploadFolder;
    }

    @Override
    public Path getUpdatesDirectory() {
        return appUpdatesDirectory;
    }

    @Override
    public Path getSettingsFile() {
        return settingsFile;
    }

    @Override
    public Path getLegacySettingsFile() {
        return legacySettingsFile;
    }

    @Override
    public Path getCacheDirectory() {
        return wwjCacheDirectory.toAbsolutePath();
    }

    @Override
    public Path getGazetteerCacheDirectory() {
        return gazetteerCacheDirectory;
    }

    @Override
    public Path getLogDirectory() {
        return logFolder;
    }

    @Override
    public Path getExternalRtkConfigDirectory() {
        return externalRtkConfigFolder;
    }

    @Override
    public Path getCameraDescriptionsDirectory() {
        return hwDescriptionsFolderCameras;
    }

    @Override
    public Path getLensDescriptionsDirectory() {
        return hwDescriptionsFolderLenses;
    }

    @Override
    public Path getPlatformDescriptionsDirectory() {
        return hwDescriptionsFolderPlatforms;
    }

    @Override
    public Path getTemplatesDirectory() {
        return templatesFolder;
    }

    @Override
    public Path getLicenseSettingsFile() {
        return licenseSettingsFile;
    }

    @Override
    public Path getGeoidDirectory() {
        return geoidFolder;
    }

    @Override
    public Path getLocalAirspacesFolder() {
        return localAirspacesFolder;
    }

    @Override
    public Path getWebviewCacheFolder() {
        return webviewFolder;
    }

    private static void mkdirs(File[] folders) throws IOException {
        for (File file : folders) {
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new IOException("Could not create folder: " + file);
                }
            }
        }
    }

    private static Path getWindowsSpecialFolder(String guid) {
        PointerByReference ptrRef = new PointerByReference();
        Shell32.INSTANCE.SHGetKnownFolderPath(Guid.GUID.fromString(guid), 0, new WinNT.HANDLE(Pointer.NULL), ptrRef);
        Pointer ptr = ptrRef.getValue();
        String path = ptr.getWideString(0);
        Ole32.INSTANCE.CoTaskMemFree(ptr);
        return Paths.get(path);
    }

}
