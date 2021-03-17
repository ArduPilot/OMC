/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;

@SettingsMetadata(section = "paths")
public class PathSettings implements ISettings {

    private final AsyncListProperty<Path> referencedProjects =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<Path>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final AsyncObjectProperty<Path> projectFolder = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Path> agiSoftPhotoScanPath = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Path> contextCapturePath = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Path> pix4DPath = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Path> menciApsPath = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Path> threeDSurveyPath = new SimpleAsyncObjectProperty<>(this);

    private final transient IPathProvider pathProvider;

    @Inject
    public PathSettings(IPathProvider pathProvider) {
        this.pathProvider = pathProvider;
    }

    public AsyncListProperty<Path> referencedProjectsProperty() {
        return referencedProjects;
    }

    public AsyncObservableList<Path> getReferencedProjects() {
        return referencedProjects.get();
    }

    public void setReferencedProjects(AsyncObservableList<Path> missions) {
        this.referencedProjects.set(missions);
    }

    public AsyncObjectProperty<Path> projectFolderProperty() {
        return this.projectFolder;
    }

    public Path getProjectFolder() {
        return projectFolder.get();
    }

    public void setProjectFolder(Path projectFolder) {
        this.projectFolder.set(projectFolder);
    }

    public Boolean agiSoftIsMetashape() {
        if (agiSoftPhotoScanPathProperty().get() == null) {
            return false;
        }

        return (agiSoftPhotoScanPathProperty().get().toString().contains("metashape.exe"));
    }

    public ObservableValue<? extends Boolean> agiSoftIsMetashapeProperty() {
        return Bindings.createBooleanBinding(
            () -> {
                if (agiSoftPhotoScanPathProperty().get() == null) {
                    return false;
                }

                return (agiSoftPhotoScanPathProperty().get().toString().contains("metashape.exe"));
            },
            agiSoftPhotoScanPathProperty());
    }

    public ObservableValue<? extends Boolean> agiSoftIsPhotoscanProperty() {
        return Bindings.createBooleanBinding(
            () -> {
                if (agiSoftPhotoScanPathProperty().get() == null) {
                    return false;
                }

                return (!agiSoftPhotoScanPathProperty().get().toString().contains("metashape.exe"));
            },
            agiSoftPhotoScanPathProperty());
    }

    public AsyncObjectProperty<Path> agiSoftPhotoScanPathProperty() {
        return this.agiSoftPhotoScanPath;
    }

    public Path getAgiSoftPhotoScanPath() {
        return agiSoftPhotoScanPath.get();
    }

    public AsyncObjectProperty<Path> contextCapturePathProperty() {
        return this.contextCapturePath;
    }

    public Path getContextCapturePath() {
        return contextCapturePath.get();
    }

    public void setContextCapturePath(Path path) {
        contextCapturePath.set(path);
    }

    public AsyncObjectProperty<Path> pix4DPathProperty() {
        return this.pix4DPath;
    }

    public Path getPix4DPath() {
        return pix4DPath.get();
    }

    public void setPix4DPath(Path path) {
        pix4DPath.set(path);
    }

    public AsyncObjectProperty<Path> menciApsPathProperty() {
        return this.menciApsPath;
    }

    public Path getMenciApsPath() {
        return menciApsPath.get();
    }

    public void setMenciApsPath(Path path) {
        menciApsPath.set(path);
    }

    public AsyncObjectProperty<Path> threeDSurveyPathProperty() {
        return this.threeDSurveyPath;
    }

    public Path getThreeDSurveyPath() {
        return threeDSurveyPath.get();
    }

    public void setThreeDSurveyPath(Path path) {
        threeDSurveyPath.set(path);
    }

    @Override
    public void onLoaded() {
        // falling back to a default directory if the chosen before does not exist anymore
        if (projectFolder.get() == null || Files.notExists(projectFolder.get())) {
            File baseDir = pathProvider.getProjectsDirectory().toFile();
            baseDir.mkdirs();
            projectFolder.set(pathProvider.getProjectsDirectory());
        }
    }

}
