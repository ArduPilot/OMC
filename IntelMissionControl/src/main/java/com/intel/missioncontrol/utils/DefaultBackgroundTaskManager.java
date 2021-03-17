/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFileTransfer;
import eu.mavinci.core.plane.sendableobjects.MVector;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.WorkerStateEvent;
import org.asyncfx.concurrent.Dispatcher;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public class DefaultBackgroundTaskManager implements IBackgroundTaskManager {

    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final IDialogService dialogService;
    private final IntegerProperty taskCount = new SimpleIntegerProperty();
    private final List<BackgroundTask> tasks = new ArrayList<>();

    @Inject
    public DefaultBackgroundTaskManager(
            IApplicationContext applicationContext, IDialogService dialogService, ILanguageHelper languageHelper) {
        this.applicationContext = applicationContext;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;

        applicationContext.addCloseRequestListener(this::canCloseApplication);
    }

    public boolean canCloseApplication(boolean cancelTasks) {
        if (getTasks().isEmpty()) {
            return true;
        }

        boolean confirmed =
            dialogService.requestConfirmation(
                languageHelper.getString("com.intel.missioncontrol.ui.dialogs.uncompleted.background.tasks.title"),
                languageHelper.getString("com.intel.missioncontrol.ui.dialogs.uncompleted.background.tasks.message"));
        if (confirmed && cancelTasks) {
            Dispatcher dispatcher = Dispatcher.platform();
            dispatcher.runLater(
                () -> {
                    try {
                        for (BackgroundTask task : tasks) {
                            applicationContext.addToast(task.getCancelledToast(languageHelper));
                        }

                        tasks.clear();
                        taskCount.set(tasks.size());
                    } catch (Exception e) {
                        applicationContext.addToast(
                            Toast.of(ToastType.INFO)
                                .setText(
                                    languageHelper.getString(
                                        "com.intel.missioncontrol.ui.dialogs.uncompleted.background.tasks.canceled"))
                                .create());
                    }
                });
        }

        return confirmed;
    }

    public boolean canCloseApplication() {
        return canCloseApplication(false);
    }

    @Override
    public void submitTask(BackgroundTask task) {
        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                if (tasks.contains(task)) {
                    throw new IllegalArgumentException("Task was already submitted.");
                }

                task.addEventHandler(
                    WorkerStateEvent.WORKER_STATE_RUNNING,
                    event -> {
                        Toast toast = task.getRunningToast(languageHelper);
                        if (toast != null) {
                            applicationContext.addToast(toast);
                        }
                    });

                task.addEventHandler(
                    WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                    event -> handleFinished(task, task.getSucceededToast(languageHelper)));

                task.addEventHandler(
                    WorkerStateEvent.WORKER_STATE_CANCELLED,
                    event -> handleFinished(task, task.getCancelledToast(languageHelper)));

                task.addEventHandler(
                    WorkerStateEvent.WORKER_STATE_FAILED,
                    event -> handleFinished(task, task.getFailedToast(languageHelper)));

                tasks.add(task);
                taskCount.set(tasks.size());

                Dispatcher.background().run(task);
            });
    }

    private void handleFinished(BackgroundTask task, Toast toast) {
        if (toast != null) {
            applicationContext.addToast(toast);
        }

        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                tasks.remove(task);
                taskCount.set(tasks.size());
            });
    }

    @Override
    public @Nullable BackgroundTask getTaskByName(String name) {
        for (BackgroundTask task : tasks) {
            String taskName = task.getName();
            if (taskName != null && taskName.equals(name)) {
                return task;
            }
        }

        return null;
    }

    @Override
    public List<BackgroundTask> getTasks() {
        return tasks;
    }

    @Override
    public int getTaskCount() {
        return taskCount.get();
    }

    @Override
    public IntegerProperty taskCountProperty() {
        return taskCount;
    }

    private abstract class ProgressTaskInternal extends BackgroundTask implements IAirplaneListenerFileTransfer {

        private final long size;

        ProgressTaskInternal(String name, long size) {
            super(name);
            this.size = size;
        }

        @Override
        public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {}

        @Override
        public void recv_fileSendingProgress(String path, Integer progress) {
            updateMessage(path);
            updateProgress(size, progress);
        }

        @Override
        public void recv_fileReceivingProgress(String path, Integer progress) {
            updateMessage(path);
            updateProgress(size, progress);
        }

        @Override
        public void recv_fileSendingSucceeded(String path) {}

        @Override
        public void recv_fileReceivingSucceeded(String path) {}

        @Override
        public void recv_fileSendingCancelled(String path) {
            cancel();
        }

        @Override
        public void recv_fileReceivingCancelled(String path) {
            cancel();
        }
    }

}
