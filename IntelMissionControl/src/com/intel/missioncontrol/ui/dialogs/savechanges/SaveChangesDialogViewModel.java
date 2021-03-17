/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.savechanges;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.ISaveable;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.PathSettings;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import java.util.Collection;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SaveChangesDialogViewModel extends DialogViewModel<Object, SaveChangesDialogViewModel.Payload> {

    public static class Payload {
        private final Collection<ISaveable> items;
        private final Mission mission;
        private final DialogTypes dialogType;

        public Payload(Collection<ISaveable> items, Mission mission, DialogTypes dialogType) {
            this.items = items;
            this.mission = mission;
            this.dialogType = dialogType;
        }
    }

    public enum DialogTypes {
        close,
        sendSupport,
        demo;
    }

    private final IMissionManager missionManager;
    private final IApplicationContext applicationContext;

    @Inject
    public SaveChangesDialogViewModel(IMissionManager missionManager, IApplicationContext applicationContext) {
        this.missionManager = missionManager;
        this.applicationContext = applicationContext;
    }

    private ReadOnlyBooleanWrapper shouldProceedWrapper = new ReadOnlyBooleanWrapper(false);
    private ReadOnlyBooleanWrapper shouldSaveChanges = new ReadOnlyBooleanWrapper(false);

    private final ObjectProperty<DialogTypes> dialogType = new SimpleObjectProperty<>(DialogTypes.close);
    private ObservableList<ChangedItemViewModel> changedItems = FXCollections.observableArrayList();
    private StringProperty missionName = new SimpleStringProperty();
    private Mission mission;

    @Override
    protected void initializeViewModel(Payload payload) {
        super.initializeViewModel(payload);

        addItems(payload.items, payload.mission);
        setDialogType(payload.dialogType);
    }

    public void addItems(Collection<ISaveable> items, Mission mission) {
        this.mission = mission;
        missionName.setValue(mission.getName());
        changedItems.addAll(items.stream().map(ChangedItemViewModel::new).collect(Collectors.toList()));
    }

    public ObservableList<ChangedItemViewModel> changedItemsProperty() {
        return changedItems;
    }

    public StringProperty nameProperty() {
        return missionName;
    }

    public ReadOnlyBooleanProperty shouldProceedProperty() {
        return shouldProceedWrapper.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty shouldSaveChangesProperty() {
        return shouldSaveChanges.getReadOnlyProperty();
    }

    public void setDialogType(DialogTypes dialogType) {
        this.dialogType.set(dialogType);
    }

    public void saveAllOnAction(boolean b) {
        changedItems.stream().forEach(item -> item.needsToSaveProperty().setValue(b));
    }

    public void saveAndExitOnAction() {
        shouldProceedWrapper.set(true);
        shouldSaveChanges.set(true);
        getCloseCommand().execute();
    }

    public void closeButtonOnAction() {
        shouldProceedWrapper.set(false);
        shouldSaveChanges.set(false);
        getCloseCommand().execute();
    }

    public void proceedWithoutSavingOnAction() {
        shouldProceedWrapper.set(true);
        shouldSaveChanges.set(false);
        changedItems.stream().forEach(item -> item.needsToSaveProperty().setValue(false));
        getCloseCommand().execute();
    }

    public boolean onMissionNameChanged(String name) {
        if (name == null || name.isEmpty() || name.equals(Mission.DEMO_MISSION_NAME)) {
            return true;
        }

        if (!mission.getName().equals(name)) {
            var projectFolder =
                DependencyInjector.getInstance()
                    .getInstanceOf(ISettingsManager.class)
                    .getSection(PathSettings.class)
                    .getProjectFolder();
            var targetMissionFolder = projectFolder.resolve(name);
            if (targetMissionFolder.toFile().exists()) {
                return true;
            }
            // missionManager.renameMission(mission,name);
            // CHANGED / only check if path is available, this is already saving!
            // is it possible to check if rename would be possible if file/path is locked via other program?
            // when savining later with error, changed data is saved and toast is shown for failed renaming,
            //    mission stays open
        }

        return false;
    }

    public ObjectProperty<DialogTypes> dialogTypeProperty() {
        return dialogType;
    }

    public boolean needsToSaveItem(ISaveable saveable) {
        for (ChangedItemViewModel model : changedItems) {
            if (model.getChangedItem().equals(saveable) && model.needsToSaveProperty().get()) {
                return true;
            }
        }

        return false;
    }

}
