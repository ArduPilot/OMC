/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.DisplaySettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.analysis.tasks.CopyLogsTask;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.concurrent.Dispatcher;

public class AddFlightLogsViewModel extends DialogViewModel<CopyLogsTask, String> {

    private final StringProperty originPath = new SimpleStringProperty();
    private final ListProperty<FlightLogEntry> flightLogs =
        new SimpleListProperty<>(FXCollections.observableArrayList(a -> new Observable[] {a.selectedProperty()}));
    private final FilteredList<FlightLogEntry> selectedFlightLogs =
        new FilteredList<>(flightLogs, FlightLogEntry::isSelected);

    private final FilteredList<FlightLogEntry> notSelectedFlightLogs =
        new FilteredList<>(flightLogs, FlightLogEntry::isNotSelected);

    private final BooleanProperty isAnyLogSelected = new SimpleBooleanProperty();
    private final BooleanProperty isEveryLogSelected = new SimpleBooleanProperty();
    private final UIAsyncBooleanProperty eraseLogs = new UIAsyncBooleanProperty(this);

    private final Command browseCommand;
    private final Command openFolderCommand;
    private final Command copySelectedCommand;

    private final IApplicationContext applicationContext;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final IBackgroundTaskManager backgroundTaskManager;
    private final ISettingsManager settingsManager;
    private final IMapView mapView;
    private Property<Boolean> selectionCheckBoxProperty = new SimpleBooleanProperty();
    private boolean updatesLogList;

    @Inject
    public AddFlightLogsViewModel(
            IApplicationContext applicationContext,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            IBackgroundTaskManager backgroundTaskManager,
            ISettingsManager settingsManager,
            IMapView mapView) {
        this.applicationContext = applicationContext;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.backgroundTaskManager = backgroundTaskManager;
        this.settingsManager = settingsManager;
        this.mapView = mapView;

        eraseLogs.bindBidirectional(settingsManager.getSection(GeneralSettings.class).eraseLogsAfterCopyProperty());

        browseCommand = new DelegateCommand(this::handleBrowse);
        openFolderCommand = new DelegateCommand(this::handleOpenFolder, canOpenFolder());
        copySelectedCommand =
            new DelegateCommand(
                () -> {
                    handleCopySelectedLogs();
                    getCloseCommand().execute();
                },
                isAnyLogSelected);

        originPath.addListener(observable -> updateLogList());
        isAnyLogSelected.bind(Bindings.createBooleanBinding(() -> !selectedFlightLogs.isEmpty(), selectedFlightLogs));
        isEveryLogSelected.bind(
            Bindings.createBooleanBinding(() -> notSelectedFlightLogs.isEmpty(), notSelectedFlightLogs));

        isEveryLogSelected.addListener((observable, oldValue, newValue) -> updateSelectionCheckBox());

        selectedFlightLogs.addListener((ListChangeListener<? super FlightLogEntry>)change -> updatePreview());
        selectionCheckBoxProperty.addListener(
            (observable, oldValue, newValue) -> updateLogList(selectionCheckBoxProperty.getValue()));
    }

    private void updateSelectionCheckBox() {
        updatesLogList = true;
        selectionCheckBoxProperty.setValue(isEveryLogSelected.get());
        updatesLogList = false;
    }

    private void updateLogList(Boolean checkBoxPropertyValue) {
        if (!updatesLogList) {
            for (FlightLogEntry flightLog : flightLogs) {
                flightLog.setSelected(checkBoxPropertyValue);
            }
        }
    }

    public Property<Boolean> eraseLogsProperty() {
        return eraseLogs;
    }

    public ReadOnlyListProperty<FlightLogEntry> flightLogsProperty() {
        return flightLogs;
    }

    public StringProperty originPathProperty() {
        return originPath;
    }

    public String getOriginPath() {
        return originPath.get();
    }

    public void setOriginPath(String originPath) {
        this.originPath.set(originPath);
    }

    public ObservableList<FlightLogEntry> getFlightLogs() {
        return flightLogs.get();
    }

    public Command getBrowseCommand() {
        return browseCommand;
    }

    public Command getOpenFolderCommand() {
        return openFolderCommand;
    }

    public Command getCopySelectedCommand() {
        return copySelectedCommand;
    }

    @Override
    protected void initializeViewModel(String originPath) {
        super.initializeViewModel(originPath);

        this.originPath.set(originPath);
        updatePreview();
    }

    private void updatePreview() {
        Mission currentMission = applicationContext.getCurrentMission();
        if (currentMission == null) {
            return;
        }

        Matching matching = currentMission.getCurrentMatching();
        if (matching == null || matching.getStatus() != MatchingStatus.NEW) {
            return;
        }

        matching.previewLogfiles(new ArrayList<>(selectedFlightLogs), mapView);
    }

    private void updateLogList() {
        File logFolder = new File(originPath.get());

        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.run(
            () -> {
                List<FlightLogEntry> flightLogsNew = LogFileHelper.getLogsInFolder(logFolder, true);
                Dispatcher.platform().runLater(() -> flightLogs.setAll(flightLogsNew));
            });
    }

    private void handleBrowse() {
        Path selection =
            dialogService.requestDirectoryChooser(
                this,
                languageHelper.getString(AddFlightLogsViewModel.class.getName() + ".selectFlightLogFolder"),
                originPath.get() != null ? Paths.get(originPath.get()) : null);
        if (selection != null) {
            originPath.set(selection.toString());
        }
    }

    private void handleOpenFolder() {
        dialogService.openDirectory(originPath.get());
    }

    private BooleanBinding canOpenFolder() {
        return Bindings.createBooleanBinding(
            () -> originPath.get() != null && !originPath.get().isEmpty() && new File(originPath.get()).isDirectory(),
            originPath);
    }

    private void handleCopySelectedLogs() {
        CopyLogsTask task =
            new CopyLogsTask(
                selectedFlightLogs,
                applicationContext,
                languageHelper,
                dialogService,
                eraseLogs.get(),
                settingsManager.getSection(DisplaySettings.class).getWorkflowHints().dataTransferProperty());
        backgroundTaskManager.submitTask(task);
        setDialogResult(task);
    }

    public Property<Boolean> selectionCheckBoxProperty() {
        return selectionCheckBoxProperty;
    }
}
