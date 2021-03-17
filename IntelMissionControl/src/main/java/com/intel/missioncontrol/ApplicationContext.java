/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.project.Dataset;
import com.intel.missioncontrol.project.FlightPlan;
import com.intel.missioncontrol.project.IProjectManager;
import com.intel.missioncontrol.project.Mission;
import com.intel.missioncontrol.project.Project;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastManager;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyObject;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;

public class ApplicationContext implements IApplicationContext, EventHandler<WindowEvent> {

    private final AsyncObjectProperty<Project> currentProject = new UIAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Mission> currentMission = new UIAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<FlightPlan> currentFlightPlan = new UIAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Dataset> currentDataset = new UIAsyncObjectProperty<>(this);

    private final ToastManager toastManager = new ToastManager();
    private final List<ICloseRequestListener> closeRequestListeners = new ArrayList<>();
    private final List<IClosingListener> closingListeners = new ArrayList<>();

    private final BooleanBinding currentMissionIsNoDemoBinding;
    private BooleanProperty currentMissionIsDemo = new SimpleBooleanProperty(true);

    @Inject
    public ApplicationContext(
            IProjectManager projectManager) {
        currentProject.addListener(this::projectChanged);
        currentProject.bind(projectManager.currentProjectProperty());

        currentMission.addListener(this::attachToPlatformDispatcher);
        currentFlightPlan.addListener(this::attachToPlatformDispatcher);
        currentDataset.addListener(this::attachToPlatformDispatcher);

        currentMissionIsNoDemoBinding =
            Bindings.createBooleanBinding(
                () -> {
                    return true;
                    /*if (currentLegacyMissionProperty().get() == null) {
                        currentMissionIsDemo.set(false);
                    } else {
                        if (currentLegacyMissionProperty()
                                .get()
                                .nameProperty()
                                .get()
                                .equals(com.intel.missioncontrol.mission.Mission.DEMO_MISSION_NAME)) {
                            currentMissionIsDemo.set(true);
                        } else {
                            currentMissionIsDemo.set(false);
                        }
                    }

                    return !currentMissionIsDemo.get();*/
                });
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Project> currentProjectProperty() {
        return currentProject;
    }

    @Override
    public AsyncObjectProperty<Mission> currentMissionProperty() {
        return currentMission;
    }

    @Override
    public AsyncObjectProperty<FlightPlan> currentFlightPlanProperty() {
        return currentFlightPlan;
    }

    @Override
    public AsyncObjectProperty<Dataset> currentDatasetProperty() {
        return currentDataset;
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
    public ReadOnlyObjectProperty<com.intel.missioncontrol.mission.Mission> currentLegacyMissionProperty() {
        // return loadedMissionManager.currentMissionProperty();
        return null;
    }

    private BooleanProperty currentMissionIsDemoProperty() {
        return currentMissionIsDemo;
    }

    @Override
    public com.intel.missioncontrol.mission.Mission getCurrentLegacyMission() {
        // return loadedMissionManager.getCurrentMission();
        return null;
    }

    @Override
    public boolean unloadCurrentMission() {
        // return loadedMissionManager.unloadCurrentMission();
        return false;
    }

    @Override
    public boolean checkDroneConnected(boolean execute) {
        // return loadedMissionManager.checkDroneConnected(execute);
        return false;
    }

    @Override
    public boolean askUserForMissionSave() {
        /*  if (loadedMissionManager.checkUnsavedCurrentMission()) {
            // show the dialog
            boolean shouldProceed =
                loadedMissionManager.checkUnsavedChangesAndExecute(SaveChangesDialogViewModel.DialogTypes.sendSupport);
            if (!shouldProceed) {
                return false;
            }

            loadedMissionManager.makeDefaultScreenshotAndCloseMission();
        }*/

        return false;
    }

    @Override
    public boolean renameCurrentMission() {
        /*  boolean renamed = loadedMissionManager.renameCurrentMission();
        if (renamed) {
            currentMissionIsDemo.set(false);
        }

        return renamed;*/
        return false;
    }

    @Override
    public Future<Void> ensureMissionAsync() {
        // return loadedMissionManager.ensureMissionAsync();
        return null;
    }

    @Override
    public Future<Void> loadMissionAsync(com.intel.missioncontrol.mission.Mission mission) {
        // return loadedMissionManager.loadMissionAsync(mission);
        return null;
    }

    @Override
    public Future<Void> loadNewMissionAsync() {
        // return loadedMissionManager.loadNewMissionAsync();
        return null;
    }

    @Override
    public Future<Void> loadClonedMissionAsync(com.intel.missioncontrol.mission.Mission mission) {
        // return loadedMissionManager.loadClonedMissionAsync(mission);
        return null;
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

    @Override
    public void revertProjectChange() {
        throw new NotImplementedException();
    }

    // If a project, mission, flight plan or dataset is made current, we attach it to the platform dispatcher. This
    // means that as long as any of these objects is current, modifications are only allowed on the UI thread.
    //
    private void attachToPlatformDispatcher(
            ObservableValue<? extends PropertyObject> observable, PropertyObject oldValue, PropertyObject newValue) {
        if (oldValue != null) {
            oldValue.detachFromDispatcher();
        }

        if (newValue != null) {
            newValue.attachToDispatcher(Dispatcher.platform());
        }
    }

    // If the current project is changed, we automatically set the current mission, current flight plan and current
    // dataset to the first items of that project.
    //
    private void projectChanged(ObservableValue<? extends Project> observable, Project oldProject, Project newProject) {
        if (newProject != null) {
            try (LockedList<Mission> missions = newProject.getMissions().lock()) {
                currentMission.set(missions.isEmpty() ? null : missions.get(0));
            }

            try (LockedList<Dataset> datasets = newProject.getDatasets().lock()) {
                currentDataset.set(datasets.isEmpty() ? null : datasets.get(0));
            }

            Mission currentMission = this.currentMission.get();
            if (currentMission != null) {
                try (LockedList<FlightPlan> flightPlans = currentMission.getFlightPlans().lock()) {
                    currentFlightPlan.set(flightPlans.isEmpty() ? null : flightPlans.get(0));
                }
            } else {
                currentFlightPlan.set(null);
            }
        } else {
            currentMission.set(null);
            currentFlightPlan.set(null);
            currentDataset.set(null);
        }
    }

}
