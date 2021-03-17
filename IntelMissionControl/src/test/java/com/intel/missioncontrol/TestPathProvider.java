/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.intel.missioncontrol.common.IPathProvider;
import eu.mavinci.desktop.helper.FileHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.checkerframework.checker.initialization.qual.UnderInitialization;

public class TestPathProvider implements IPathProvider {

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

    public TestPathProvider() {
        initialize(Paths.get(System.getProperty("java.io.tmpdir")));
    }

    public TestPathProvider(Path baseDir) {
        initialize(baseDir);
    }

    private void initialize(@UnderInitialization TestPathProvider this, Path appSettingsDirectory) {
        this.appSettingsDirectory = appSettingsDirectory;
        licenseSettingsFile = appSettingsDirectory.resolve("licence");
        projectsDirectory = FileHelper.getMyDocumentsFolder().toPath().resolve("projects");
        appUpdatesDirectory = appSettingsDirectory.resolve("updates");
        settingsFile = appSettingsDirectory.resolve("settings.json");
        legacySettingsFile = appSettingsDirectory.resolve("appSettings.xml");
        wwjCacheDirectory = appSettingsDirectory.resolve("cache");
        gazetteerCacheDirectory = appSettingsDirectory.resolve("gazetteerCache");
        logFolder = appSettingsDirectory.resolve("appLogs");
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
        };

        try {
            FileHelper.mkdirs(Arrays.stream(folders).map(Path::toFile).toArray(File[]::new));
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        return Paths.get(System.getProperty("java.io.tmpdir"));
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
        return wwjCacheDirectory;
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
}
