/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.intel.missioncontrol.api.IFlightPlanService;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.impl.IScreenshotManager;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.LoadedMissionManager;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.savechanges.SaveChangesDialogViewModel;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastManager;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import org.asyncfx.concurrent.Future;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationContext implements IApplicationContext, EventHandler<WindowEvent> {

    private static Logger LOGGER = LoggerFactory.getLogger(ApplicationContext.class);

    private final ToastManager toastManager = new ToastManager();
    private final List<ICloseRequestListener> closeRequestListeners = new ArrayList<>();
    private final List<IClosingListener> closingListeners = new ArrayList<>();
    private final LoadedMissionManager loadedMissionManager;
    private final ISettingsManager settingsManager;
    private final ILanguageHelper languageHelper;
    private final BooleanBinding currentMissionIsNoDemoBinding;

    @Inject
    private IBackgroundTaskManager backgroundTaskManager;

    private BooleanProperty currentMissionIsDemo = new SimpleBooleanProperty(true);

    @Inject
    public ApplicationContext(
            IMissionManager missionManager,
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            IFlightPlanService flightPlanService,
            ISettingsManager settingsManager,
            Provider<IScreenshotManager> screenshotManager) {
        this.languageHelper = languageHelper;
        this.settingsManager = settingsManager;
        this.loadedMissionManager =
            new LoadedMissionManager(
                this, missionManager, languageHelper, dialogService, flightPlanService, screenshotManager);

        currentMissionIsNoDemoBinding =
            Bindings.createBooleanBinding(
                () -> {
                    if (currentMissionProperty().get() == null) {
                        currentMissionIsDemo.set(false);
                    } else {
                        if (currentMissionProperty().get().nameProperty().get().equals(Mission.DEMO_MISSION_NAME)) {
                            currentMissionIsDemo.set(true);
                        } else {
                            currentMissionIsDemo.set(false);
                        }
                    }

                    return !currentMissionIsDemo.get();
                },
                currentMissionProperty(),
                currentMissionIsDemoProperty());
    }

    @Override
    public BooleanExpression currentMissionIsNoDemo() {
        return currentMissionIsNoDemoBinding;
    }

    @Override
    public void addToast(Toast toast) {
        Expect.notNull(toast, "toast");
        toastManager.addToast(toast);
    }

    @Override
    public void addCloseRequestListener(ICloseRequestListener listener) {
        Expect.notNull(listener, "listener");

        synchronized (closeRequestListeners) {
            if (listener instanceof WeakCloseRequestListener) {
                WeakCloseRequestListener.Accessor.setApplicationContext((WeakCloseRequestListener)listener, this);
            }

            closeRequestListeners.add(0, listener);
        }
    }

    @Override
    public void removeCloseRequestListener(ICloseRequestListener listener) {
        Expect.notNull(listener, "listener");

        synchronized (closeRequestListeners) {
            closeRequestListeners.remove(listener);
        }
    }

    @Override
    public void addClosingListener(IClosingListener listener) {
        Expect.notNull(listener, "listener");

        synchronized (closingListeners) {
            if (listener instanceof WeakClosingListener) {
                WeakClosingListener.Accessor.setApplicationContext((WeakClosingListener)listener, this);
            }

            closingListeners.add(listener);
        }
    }

    @Override
    public void removeClosingListener(IClosingListener listener) {
        Expect.notNull(listener, "listener");

        synchronized (closingListeners) {
            closingListeners.remove(listener);
        }
    }

    @Override
    public ReadOnlyListProperty<Toast> toastsProperty() {
        return toastManager.toastsProperty();
    }

    @Override
    public ReadOnlyObjectProperty<Mission> currentMissionProperty() {
        return loadedMissionManager.currentMissionProperty();
    }

    private BooleanProperty currentMissionIsDemoProperty() {
        return currentMissionIsDemo;
    }

    @Override
    public @Nullable Mission getCurrentMission() {
        return loadedMissionManager.getCurrentMission();
    }

    @Override
    public boolean unloadCurrentMission() {
        return loadedMissionManager.unloadCurrentMission();
    }

    @Override
    public boolean checkDroneConnected(boolean execute) {
        return loadedMissionManager.checkDroneConnected(execute);
    }

    @Override
    public boolean askUserForMissionSave() {
        if (loadedMissionManager.checkUnsavedCurrentMission()) {
            // show the dialog
            boolean shouldProceed =
                loadedMissionManager.checkUnsavedChangesAndExecute(SaveChangesDialogViewModel.DialogTypes.sendSupport);
            if (!shouldProceed) {
                return false;
            }

            loadedMissionManager.makeDefaultScreenshotAndCloseMission();
        }

        return true;
    }

    @Override
    public boolean renameCurrentMission() {
        boolean renamed = loadedMissionManager.renameCurrentMission();
        if (renamed) {
            currentMissionIsDemo.set(false);
        }

        return renamed;
    }

    @Override
    public Future<Void> ensureMissionAsync() {
        return loadedMissionManager.ensureMissionAsync();
    }

    @Override
    public Future<Void> loadMissionAsync(Mission mission) {
        return loadedMissionManager.loadMissionAsync(mission);
    }

    @Override
    public Future<Void> loadNewMissionAsync() {
        return loadedMissionManager.loadNewMissionAsync();
    }

    @Override
    public Future<Void> loadClonedMissionAsync(Mission mission) {
        return loadedMissionManager.loadClonedMissionAsync(mission);
    }

    @Override
    public void handle(WindowEvent event) {
        boolean closeRequest =
            event.getEventType() == WindowEvent.WINDOW_HIDING
                || event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST;

        boolean closing =
            event.getEventType() == WindowEvent.WINDOW_HIDDEN
                || event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST;

        if (closeRequest) {
            synchronized (closeRequestListeners) {
                for (ICloseRequestListener listener : closeRequestListeners) {
                    if (!listener.canClose()) {
                        event.consume();
                        return;
                    }
                }
            }
        }

        if (closing) {
            synchronized (closingListeners) {
                for (IClosingListener listener : closingListeners) {
                    listener.close();
                }
            }
        }

        // TODO: Investigate why some threads don't want to stop.
        System.exit(0);
    }

}
