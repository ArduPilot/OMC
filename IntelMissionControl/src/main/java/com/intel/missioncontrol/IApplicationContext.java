/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.intel.missioncontrol.project.Dataset;
import com.intel.missioncontrol.project.FlightPlan;
import com.intel.missioncontrol.project.Mission;
import com.intel.missioncontrol.project.Project;
import com.intel.missioncontrol.ui.notifications.Toast;
import java.lang.ref.WeakReference;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.concurrent.Future;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Encapsulates application-wide state. */
public interface IApplicationContext {

    ReadOnlyAsyncObjectProperty<Project> currentProjectProperty();

    AsyncObjectProperty<Mission> currentMissionProperty();

    AsyncObjectProperty<FlightPlan> currentFlightPlanProperty();

    AsyncObjectProperty<Dataset> currentDatasetProperty();

    default @Nullable Project getCurrentProject() {
        return currentProjectProperty().get();
    }

    default @Nullable Mission getCurrentMission() {
        return currentMissionProperty().get();
    }

    default @Nullable FlightPlan getCurrentFlightPlan() {
        return currentFlightPlanProperty().get();
    }

    default @Nullable Dataset getCurrentDataset() {
        return currentDatasetProperty().get();
    }

    boolean checkDroneConnected(boolean execute);

    interface ICloseRequestListener {
        boolean canClose();
    }

    interface IClosingListener {
        void close();
    }

    final class WeakCloseRequestListener implements ICloseRequestListener {
        static class Accessor {
            static void setApplicationContext(
                    WeakCloseRequestListener listener, IApplicationContext applicationContext) {
                listener.applicationContext = applicationContext;
            }
        }

        private final WeakReference<ICloseRequestListener> listener;
        private IApplicationContext applicationContext;

        public WeakCloseRequestListener(ICloseRequestListener listener) {
            this.listener = new WeakReference<>(listener);
        }

        @Override
        public boolean canClose() {
            ICloseRequestListener listener = this.listener.get();
            if (listener != null) {
                return listener.canClose();
            } else if (applicationContext != null) {
                applicationContext.removeCloseRequestListener(this);
            }

            return true;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            return obj == listener.get();
        }
    }

    final class WeakClosingListener implements IClosingListener {
        static class Accessor {
            static void setApplicationContext(WeakClosingListener listener, IApplicationContext applicationContext) {
                listener.applicationContext = applicationContext;
            }
        }

        private final WeakReference<IClosingListener> listener;
        private IApplicationContext applicationContext;

        public WeakClosingListener(IClosingListener listener) {
            this.listener = new WeakReference<>(listener);
        }

        @Override
        public void close() {
            IClosingListener listener = this.listener.get();
            if (listener != null) {
                listener.close();
            } else if (applicationContext != null) {
                applicationContext.removeClosingListener(this);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            return obj == listener.get();
        }
    }

    void addToast(Toast toast);

    void addCloseRequestListener(ICloseRequestListener listener);

    void removeCloseRequestListener(ICloseRequestListener listener);

    void addClosingListener(IClosingListener listener);

    void removeClosingListener(IClosingListener listener);

    ReadOnlyListProperty<Toast> toastsProperty();

    @Deprecated
    ReadOnlyObjectProperty<com.intel.missioncontrol.mission.Mission> currentLegacyMissionProperty();

    @Deprecated
    com.intel.missioncontrol.mission.Mission getCurrentLegacyMission();

    @Deprecated
    boolean unloadCurrentMission();

    @Deprecated
    boolean askUserForMissionSave();

    @Deprecated
    boolean renameCurrentMission();

    @Deprecated
    BooleanExpression currentMissionIsNoDemo();

    @Deprecated
    Future<Void> ensureMissionAsync();

    @Deprecated
    Future<Void> loadMissionAsync(com.intel.missioncontrol.mission.Mission mission);

    @Deprecated
    Future<Void> loadNewMissionAsync();

    @Deprecated
    Future<Void> loadClonedMissionAsync(com.intel.missioncontrol.mission.Mission mission);

    void revertProjectChange();

    /** Miscellaneous variables/properties that are used from different (mostly independent) parts of the application */
    // non-persistent setting for live video widget visibility
    SimpleBooleanProperty liveVideoActive = new SimpleBooleanProperty(false);

}
