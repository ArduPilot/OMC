/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.tasks;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;

public class BackgroundTasksViewModel extends DialogViewModel {

    private final ListProperty<TaskViewModel> runningTasks =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    @Inject
    public BackgroundTasksViewModel(IBackgroundTaskManager backgroundTaskManager) {
        backgroundTaskManager
            .taskCountProperty()
            .addListener(
                observable -> {
                    List<TaskViewModel> list =
                        backgroundTaskManager
                            .getTasks()
                            .stream()
                            .filter(Task::isRunning)
                            .map(TaskViewModel::new)
                            .collect(Collectors.toList());

                    Platform.runLater(() -> runningTasks.setAll(list));
                });

        List<TaskViewModel> list =
            backgroundTaskManager
                .getTasks()
                .stream()
                .filter(Task::isRunning)
                .map(TaskViewModel::new)
                .collect(Collectors.toList());

        runningTasks.addAll(list);
    }

    public ReadOnlyListProperty<TaskViewModel> runningTasksProperty() {
        return runningTasks;
    }

}
