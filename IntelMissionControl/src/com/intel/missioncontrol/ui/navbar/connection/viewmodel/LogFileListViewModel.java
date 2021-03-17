/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navbar.connection.LogFileListItem;
import com.intel.missioncontrol.ui.navbar.connection.scope.LogFilePlayerScope;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LogFileListViewModel extends ViewModelBase {
    private final ObservableList<LogFileListItem> logFileList = FXCollections.observableArrayList();

    @InjectScope
    private MainScope mainScope;

    @InjectScope
    private LogFilePlayerScope logFilePlayerScope;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private IApplicationContext applicationContext;

    public static final long FILE_SIZE_LIMIT = 20 * 1024;

    private List<FileChooser.ExtensionFilter> logFileExtensions;

    private final BooleanProperty isFilteredFilesShowed = new SimpleBooleanProperty(false);

    private void loadMissionLogFiles() {
        Collection<LogFileListItem> logItems =
            getLogFiles()
                .stream()
                .filter(file -> !isFilteredFilesShowed.get() || file.length() > FILE_SIZE_LIMIT)
                .map(LogFileListItem::new)
                .sorted((item1, item2) -> Long.compare(item2.getDate().getTime(), item1.getDate().getTime()))
                .collect(Collectors.toList());

        getLogFileListProperty().setAll(logItems);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        applicationContext
            .currentMissionProperty()
            .addListener((observable, oldValue, newValue) -> loadMissionLogFiles());

        isFilteredFilesShowed.addListener((observable, oldValue, newValue) -> loadMissionLogFiles());

        logFileExtensions =
            ImmutableList.of(
                new FileChooser.ExtensionFilter(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.connection.view.LogFileListView.Extension.vlg.Text"),
                    "*.vlg"),
                new FileChooser.ExtensionFilter(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.connection.view.LogFileListView.Extension.vlg.zip.Text"),
                    "*.vlg.zip"),
                new FileChooser.ExtensionFilter(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.connection.view.LogFileListView.Extension.alg.Text"),
                    "*.alg"));
    }

    public void onBrowse() {
        FileChooser fpFileChooser = new FileChooser();
        fpFileChooser.getExtensionFilters().addAll(logFileExtensions);

        fpFileChooser.setTitle(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.connection.view.LogFileListView.OpenFileDialog.Title"));
        fpFileChooser.setInitialDirectory(applicationContext.getCurrentMission().getDirectory().toFile());

        File chosedFile = fpFileChooser.showOpenDialog(new Stage());

        if (null != chosedFile) {
            onLoadLogFile(chosedFile.getAbsolutePath());
        }
    }

    public void onLoadLogFile(String selectedLogFileName) {
        logFilePlayerScope.switchToPlayerView(applicationContext.getCurrentMission(), selectedLogFileName);
    }

    public ObservableList<LogFileListItem> getLogFileListProperty() {
        return logFileList;
    }

    public BooleanProperty getIsFilteredFilesShowedProperty() {
        return isFilteredFilesShowed;
    }

    private Collection<File> getLogFiles() {
        Mission currentMission = applicationContext.getCurrentMission();
        if (null == currentMission) {
            return Collections.<File>emptyList();
        }

        return currentMission.getImcFlightLogFiles();
    }
}
