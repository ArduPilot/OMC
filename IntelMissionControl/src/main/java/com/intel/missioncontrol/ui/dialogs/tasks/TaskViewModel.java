/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.tasks;

import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;

public class TaskViewModel implements ViewModel {

    private final ReadOnlyStringProperty name;
    private final IBackgroundTaskManager.BackgroundTask task;
    private final Command cancelCommand;
    private final BooleanProperty notCancelled = new SimpleBooleanProperty();

    public TaskViewModel(IBackgroundTaskManager.BackgroundTask task) {
        this.task = task;
        this.name = task.messageProperty();
        this.task.setOnCancelled(event -> notCancelled.set(false));
        this.notCancelled.set(!task.isCancelled());
        this.cancelCommand = new DelegateCommand(task::cancel, notCancelled);
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public ReadOnlyDoubleProperty progressProperty() {
        return task.progressProperty();
    }

    public ReadOnlyDoubleProperty totalWorkProperty() {
        return task.totalWorkProperty();
    }

    public ReadOnlyDoubleProperty workDoneProperty() {
        return task.workDoneProperty();
    }

    public Command getCancelCommand() {
        return cancelCommand;
    }
}
