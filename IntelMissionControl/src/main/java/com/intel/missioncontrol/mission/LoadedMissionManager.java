/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.api.IFlightPlanService;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.map.worldwind.impl.IScreenshotManager;
import com.intel.missioncontrol.ui.common.components.RenameDialog;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.savechanges.ChangedItemViewModel;
import com.intel.missioncontrol.ui.dialogs.savechanges.SaveChangesDialogViewModel;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.flight.fly.FlyDroneMenuModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.inject.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Strand;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LoadedMissionManager {

    private static final Logger logger = LogManager.getLogger(LoadedMissionManager.class);
    public static final String PNG = "png";

    private final ObjectProperty<Mission> mission = new SimpleObjectProperty<>();

    private final IApplicationContext applicationContext;
    private final IMissionManager missionManager;
    private final ILanguageHelper languageHelper;
    private final IDialogService dialogService;
    private final Strand strand = new Strand();
    private final IFlightPlanService flightPlanService;
    private final Provider<IScreenshotManager> screenshotManager;

    public LoadedMissionManager(
            IApplicationContext applicationContext,
            IMissionManager missionManager,
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            IFlightPlanService flightPlanService,
            Provider<IScreenshotManager> screenshotManager) {
        this.applicationContext = applicationContext;
        this.missionManager = missionManager;
        this.languageHelper = languageHelper;
        this.dialogService = dialogService;
        this.flightPlanService = flightPlanService;
        this.screenshotManager = screenshotManager;

        applicationContext.addCloseRequestListener(
            () -> {
                if (saveCurrentMission()) {
                    if (checkDroneConnected(true)) {
                        return false;
                    }

                    return true;
                }

                if (checkDroneConnected(true)) {
                    return false;
                }

                return false;
            });
    }

    public ReadOnlyObjectProperty<Mission> currentMissionProperty() {
        return mission;
    }

    public @Nullable Mission getCurrentMission() {
        return mission.get();
    }

    public boolean unloadCurrentMission() {
        if (mission.get() != null) {
            if (!Platform.isFxApplicationThread()) {
                Dispatcher.platform().runLater(this::unloadCurrentMission);
            } else {
                if (checkUnsavedCurrentMission()) {
                    // show the dialog
                    boolean shouldProceed = checkUnsavedChangesAndExecute(SaveChangesDialogViewModel.DialogTypes.close);
                    if (!shouldProceed) {
                        return false;
                    }
                }

                makeScreenshot();
                this.mission.setValue(null);
                missionManager.refreshRecentMissionInfos();
            }
        }

        return true;
    }

    private boolean saveCurrentMission() {
        if (mission.get() != null) {
            if (!Platform.isFxApplicationThread()) {
                Dispatcher.platform().runLater(this::saveCurrentMission);
            } else {
                if (checkUnsavedCurrentMission()) {
                    // show the dialog
                    boolean shouldProceed = checkUnsavedChangesAndExecute(SaveChangesDialogViewModel.DialogTypes.close);
                    if (!shouldProceed) {
                        return false;
                    }

                    makeScreenshot();
                    this.mission.setValue(null);
                    missionManager.refreshRecentMissionInfos();
                } else {
                    makeScreenshot();
                }
            }
        }

        return true;
    }

    private void makeScreenshot() {
        // make sure the legacy infrastructure is ingormed first abotu the mission change.
        // if she would just listen to this change, we could not guarantee that she
        // would be initialized first
        boolean demo = missionManager.deleteDemoMission(mission.get());
        boolean empty = missionManager.deleteEmptyMission(mission.get());
        if (!empty && !demo) {
            missionManager.makeScreenshot(mission.get());
        }
    }

    public boolean renameCurrentMission() {
        String key = "";
        final Mission currentMission = mission.get();
        String oldName = currentMission.getName();
        if (oldName.equals(Mission.DEMO_MISSION_NAME)) {
            key = ".demo";
        }

        Optional<String> newName =
            RenameDialog.requestNewMissionName(
                languageHelper.getString("renameDialog" + key + ".title"),
                languageHelper.getString("renameDialog" + key + ".name"),
                oldName,
                languageHelper,
                missionManager::isValidMissionName);

        if (newName.isPresent()) {
            missionManager.renameMission(currentMission, newName.get());
            missionManager.makeDefaultScreenshot(mission.get());
        } else if (oldName.equals(Mission.DEMO_MISSION_NAME)) {
            return false;
        }

        return true;
    }

    public Future<Void> ensureMissionAsync() {
        return strand.runLater(
            () -> {
                Mission currentMission = Dispatcher.platform().getLaterAsync(mission::get).getUnchecked();
                if (currentMission == null) {
                    loadNewMissionInternal();
                }
            });
    }

    public Future<Void> loadNewMissionAsync() {
        return strand.runLater(this::loadNewMissionInternal);
    }

    public Future<Void> loadMissionAsync(Mission mission) {
        return strand.runLater(() -> loadMissionInternal(mission));
    }

    public Future<Void> loadClonedMissionAsync(Mission mission) {
        return strand.runLater(
            () -> {
                try {
                    loadMissionInternal(missionManager.cloneMission(mission));
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                    applicationContext.addToast(
                        Toast.of(ToastType.ALERT)
                            .setText(
                                languageHelper.getString(
                                    "com.intel.missioncontrol.mission.LoadedMissionManager.ioError"))
                            .setShowIcon(true)
                            .create());
                    throw new RuntimeException(ex);
                }
            });
    }

    private void loadNewMissionInternal() {
        try {
            loadMissionInternal(missionManager.createNewMission());
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            applicationContext.addToast(
                Toast.of(ToastType.ALERT)
                    .setText(
                        languageHelper.getString("com.intel.missioncontrol.mission.LoadedMissionManager.ioError")
                            + "\n"
                            + ex.getMessage())
                    .setShowIcon(true)
                    .create());
            throw new RuntimeException(ex);
        }
    }

    private void loadMissionInternal(Mission mission) {
        mission.load();
        Dispatcher.platform().run(() -> this.mission.set(mission));
    }

    public boolean checkDroneConnected(boolean execute) {
        var flyDroneMenuModel = missionManager.getMainViewModel().getMainScope().flyDroneMenuModelProperty().get();

        if (flyDroneMenuModel == null) {
            return false;
        }

        Command disconnectDroneCommand = flyDroneMenuModel.find(FlyDroneMenuModel.MenuIds.DISCONNECT).getCommand();
        if (disconnectDroneCommand.executableProperty().get()) {
            if (execute) {
                disconnectDroneCommand.execute();
                return true; // true as long as not waiting
            }

            return true;
        }

        return false;
    }

    public boolean checkUnsavedCurrentMission() {
        if (mission.get() != null) {
            return mission.get().hasUnsavedItems();
        }

        return false;
    }

    public boolean checkUnsavedChangesAndExecute(SaveChangesDialogViewModel.DialogTypes dialogType) {
        if (applicationContext.getCurrentLegacyMission() != null) {
            Mission currentMission = applicationContext.getCurrentLegacyMission();

            Collection<ISaveable> unsavedItems = currentMission.getAllUnsavedItems();

            if (!unsavedItems.isEmpty()) {
                SaveChangesDialogViewModel viewModel =
                    dialogService.requestDialogAndWait(
                        WindowHelper.getPrimaryViewModel(),
                        SaveChangesDialogViewModel.class,
                        () ->
                            new SaveChangesDialogViewModel.Payload(
                                unsavedItems,
                                mission.get(),
                                currentMission.getName().equals(Mission.DEMO_MISSION_NAME)
                                    ? SaveChangesDialogViewModel.DialogTypes.demo
                                    : dialogType));

                // check if the dialog was just closed
                // do not save nothing and return
                boolean shouldProceed = viewModel.shouldProceedProperty().get();
                if (!shouldProceed) {
                    return false;
                }

                boolean shouldSaveChanges = viewModel.shouldSaveChangesProperty().get();
                saveChangedElementsAndDiscardUnwanted(viewModel, shouldSaveChanges);
                if (shouldSaveChanges) {
                    missionManager.saveMission(mission.get());
                    if (!viewModel.nameProperty().get().equals(mission.get().getName())) {
                        missionManager.renameMission(mission.get(), viewModel.nameProperty().get());
                    }
                }
            }
        }

        if (checkDroneConnected(true)) {
            return false; // as long as not waiting
        } else {
            return true;
        }
    }

    private void saveChangedElementsAndDiscardUnwanted(
            SaveChangesDialogViewModel viewModel, boolean shouldSaveChanges) {
        for (ChangedItemViewModel item : viewModel.changedItemsProperty()) {
            if (item.needsToSaveProperty().get() && shouldSaveChanges) {
                ISaveable saveable = item.getChangedItem();
                if (saveable instanceof FlightPlan) {
                    flightPlanService.saveFlightPlan(mission.getValue(), ((FlightPlan)saveable));
                } else {
                    saveable.save();
                }
            } else { // discard a change ref #IMC-1373
                // or do not store the mission object - and the change will be discarded on reload
            }
        }
    }

    public void makeDefaultScreenshotAndCloseMission() {
        missionManager.makeDefaultScreenshot(mission.get());
        mission.set(null);
    }
}
