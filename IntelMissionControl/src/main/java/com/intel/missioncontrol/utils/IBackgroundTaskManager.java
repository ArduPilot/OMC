/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import eu.mavinci.core.obfuscation.IKeepClassname;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface IBackgroundTaskManager {

    abstract class BackgroundTask extends Task<Void> implements IKeepClassname {
        private final Logger logger = LogManager.getLogger(BackgroundTask.class);
        public String name;
        private EventHandler<WorkerStateEvent> onFinishedHandler;

        protected BackgroundTask(String name) {
            this.name = name;

            addEventHandler(
                WorkerStateEvent.ANY,
                event -> {
                    if (getState() == State.FAILED) {
                        try (StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw)) {
                            getException().printStackTrace(pw);
                            logger.warn(sw.toString());
                        } catch (Exception e) {
                            // do nothing
                        }
                    }

                    if (onFinishedHandler != null && hasFinished()) {
                        onFinishedHandler.handle(event);
                    }
                });
        }

        public String getName() {
            return name;
        }

        public @Nullable Toast getRunningToast(ILanguageHelper languageHelper) {
            return Toast.of(ToastType.INFO)
                .setText(languageHelper.getString(BackgroundTask.class.getName() + ".defaultRunningMessage", getName()))
                .create();
        }

        public @Nullable Toast getSucceededToast(ILanguageHelper languageHelper) {
            return Toast.of(ToastType.INFO)
                .setText(
                    languageHelper.getString(BackgroundTask.class.getName() + ".defaultSucceededMessage", getName()))
                .create();
        }

        public @Nullable Toast getCancelledToast(ILanguageHelper languageHelper) {
            return Toast.of(ToastType.INFO)
                .setText(
                    languageHelper.getString(BackgroundTask.class.getName() + ".defaultCancelledMessage", getName()))
                .create();
        }

        public @Nullable Toast getFailedToast(ILanguageHelper languageHelper) {
            return Toast.of(ToastType.ALERT)
                .setText(languageHelper.getString(BackgroundTask.class.getName() + ".defaultFailedMessage", getName()))
                .setShowIcon(true)
                .create();
        }

        public boolean hasFinished() {
            switch (getState()) {
            case READY:
            case SCHEDULED:
            case RUNNING:
                return false;

            default:
                return true;
            }
        }

        public void setOnFinished(EventHandler<WorkerStateEvent> handler) {
            if (!Platform.isFxApplicationThread()) {
                throw new IllegalStateException("Task must only be used from the FX Application Thread");
            }

            this.onFinishedHandler = handler;
        }

        @Override
        public void updateProgress(double workDone, double max) {
            super.updateProgress(workDone, max);
        }

        @Override
        public void updateMessage(String message) {
            super.updateMessage(message);
        }
    }

    class ProgressStageFirer {

        public ProgressStageFirer(long stagesCount) {
            this.stagesCount = stagesCount;
        }

        private long stagesCount;
        private long currentStage;

        public ProgressStageFirer(long stagesCount, long currentStage) {
            this.stagesCount = stagesCount;
            this.currentStage = currentStage;
        }

        public long getStagesCount() {
            return stagesCount;
        }

        public long getCurrentStage() {
            return currentStage;
        }

        public void setCurrentStage(long currentStage) {
            this.currentStage = currentStage;
        }
    }

    void submitTask(BackgroundTask task);

    @Nullable
    BackgroundTask getTaskByName(String name);

    List<BackgroundTask> getTasks();

    int getTaskCount();

    IntegerProperty taskCountProperty();

}
