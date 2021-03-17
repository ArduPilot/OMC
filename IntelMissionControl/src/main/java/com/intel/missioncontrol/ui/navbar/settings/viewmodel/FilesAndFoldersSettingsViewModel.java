/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.export.ExternalApplication;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.settings.PathSettings;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.DialogResult;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import eu.mavinci.desktop.helper.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.asyncfx.beans.binding.Converters;
import org.asyncfx.beans.property.UIAsyncStringProperty;

public class FilesAndFoldersSettingsViewModel extends ViewModelBase {

    private final UIAsyncStringProperty projectFolder = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty agiSoftPhotoScanPath = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty contextCapturePath = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty pix4DPath = new UIAsyncStringProperty(this);

    private final ParameterizedCommand<ExternalApplication> browseCommand;
    private final ParameterizedCommand<ExternalApplication> openFolderCommand;

    private final IDialogService dialogService;
    private final PathSettings pathSettings;

    @Inject
    public FilesAndFoldersSettingsViewModel(
            IMissionManager missionManager,
            PathSettings pathSettings,
            IDialogService dialogService,
            ILanguageHelper languageHelper) {
        this.dialogService = dialogService;
        this.pathSettings = pathSettings;

        browseCommand =
            new ParameterizedDelegateCommand<>(
                payload -> {
                    if (payload == null) {
                        var path = dialogService.requestDirectoryChooser(this);
                        if (path != null && Files.exists(path) && !path.toString().equals(projectFolder.get())) {
                            DialogResult confirmed =
                                dialogService.requestCancelableConfirmation(
                                    languageHelper.getString(
                                        "com.intel.missioncontrol.ui.navbar.settings.view.FilesAndFoldersSettingsView.sessionDefaultBaseFolder.title"),
                                    languageHelper.getString(
                                        "com.intel.missioncontrol.ui.navbar.settings.view.FilesAndFoldersSettingsView.sessionDefaultBaseFolder.message",
                                        projectFolder.get(),
                                        path.toString()),
                                    new ButtonType[] {
                                        new ButtonType(
                                            languageHelper.getString(
                                                "com.intel.missioncontrol.ui.navbar.settings.view.FilesAndFoldersSettingsView.sessionDefaultBaseFolder.yes.button"),
                                            ButtonBar.ButtonData.YES),
                                        new ButtonType(
                                            languageHelper.getString(
                                                "com.intel.missioncontrol.ui.navbar.settings.view.FilesAndFoldersSettingsView.sessionDefaultBaseFolder.no.button"),
                                            ButtonBar.ButtonData.NO),
                                        ButtonType.CANCEL
                                    });
                            if (confirmed == DialogResult.YES) {
                                missionManager.moveRecentMissions(projectFolder.get(), path.toString());
                                projectFolder.set(path.toString());
                                missionManager.refreshRecentMissionInfos();
                                missionManager.refreshRecentMissionListItems();
                            } else if (confirmed == DialogResult.NO) {
                                projectFolder.set(path.toString());
                                missionManager.refreshRecentMissionInfos();
                                missionManager.refreshRecentMissionListItems();
                            }
                        }
                    } else {
                        switch (payload) {
                        case AGI_SOFT_PHOTO_SCAN:
                            requestFileChooserAndSetValue(
                                FileFilter.AGISOFT_PHOTOSCAN_APPLICATION,
                                FileFilter.AGISOFT_METASHAPE_APPLICATION,
                                agiSoftPhotoScanPath);
                            break;
                        case PIX_4_DESKTOP:
                            requestFileChooserAndSetValue(FileFilter.PIX4D_APPLICATION, pix4DPath);
                            break;
                        case CONTEXT_CAPTURE:
                            requestFileChooserAndSetValue(FileFilter.CONTEXTCAPTURE_APPLICATION, contextCapturePath);
                            break;
                        }
                    }
                });

        openFolderCommand =
            new ParameterizedDelegateCommand<>(
                payload -> {
                    String path = null;
                    if (payload == null) {
                        path = projectFolder.get();
                    } else if (payload == ExternalApplication.AGI_SOFT_PHOTO_SCAN) {
                        path = agiSoftPhotoScanPath.get();
                    } else if (payload == ExternalApplication.PIX_4_DESKTOP) {
                        path = pix4DPath.get();
                    } else if (payload == ExternalApplication.CONTEXT_CAPTURE) {
                        path = contextCapturePath.get();
                    }

                    if (path != null) {
                        Path p = Paths.get(path);
                        if (Files.isRegularFile(p)) {
                            path = p.getParent().toString();
                        }

                        dialogService.openDirectory(path);
                    }
                });
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        projectFolder.bindBidirectional(pathSettings.projectFolderProperty(), Converters.pathToString());
        agiSoftPhotoScanPath.bindBidirectional(pathSettings.agiSoftPhotoScanPathProperty(), Converters.pathToString());
        contextCapturePath.bindBidirectional(pathSettings.contextCapturePathProperty(), Converters.pathToString());
        pix4DPath.bindBidirectional(pathSettings.pix4DPathProperty(), Converters.pathToString());

        if (agiSoftPhotoScanPath.get() == null || Files.notExists(pathSettings.agiSoftPhotoScanPathProperty().get())) {
            agiSoftPhotoScanPath.set(findExecutableLocation(ExternalApplication.AGI_SOFT_PHOTO_SCAN));
        }

        if (pix4DPath.get() == null || Files.notExists(pathSettings.pix4DPathProperty().get())) {
            pix4DPath.set(findExecutableLocation(ExternalApplication.PIX_4_DESKTOP));
        }

        if (contextCapturePath.get() == null || Files.notExists(pathSettings.contextCapturePathProperty().get())) {
            contextCapturePath.set(findExecutableLocation(ExternalApplication.CONTEXT_CAPTURE));
        }
    }

    public ParameterizedCommand<ExternalApplication> getBrowseCommand() {
        return browseCommand;
    }

    public ParameterizedCommand<ExternalApplication> getOpenFolderCommand() {
        return openFolderCommand;
    }

    public UIAsyncStringProperty projectFolderProperty() {
        return projectFolder;
    }

    public UIAsyncStringProperty agiSoftPhotoScanPathProperty() {
        return agiSoftPhotoScanPath;
    }

    public UIAsyncStringProperty contextCapturePathProperty() {
        return contextCapturePath;
    }

    public UIAsyncStringProperty pix4DesktopPathProperty() {
        return pix4DPath;
    }

    private String findExecutableLocation(ExternalApplication externalApplication) {
        for (var path : externalApplication.getDefaultExecutablePaths()) {
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        return null;
    }

    private void requestFileChooserAndSetValue(FileFilter fileFilter, UIAsyncStringProperty property) {
        var path = dialogService.requestFileOpenDialog(this, null, null, fileFilter);
        if (path == null || !Files.exists(path)) {
            return;
        }

        property.set(path.toString());
    }

    private void requestFileChooserAndSetValue(
            FileFilter fileFilter, FileFilter fileFilter2, UIAsyncStringProperty property) {
        var path = dialogService.requestFileOpenDialog(this, null, null, fileFilter, fileFilter2);
        if (path == null || !Files.exists(path)) {
            return;
        }

        property.set(path.toString());
    }

}
