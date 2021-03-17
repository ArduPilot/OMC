/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.notifications;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;

public class ToastViewModel implements ViewModel {

    enum Status {
        NOT_YET_ARRANGED,
        APPEARING,
        VISIBLE,
        DISAPPEARING,
        HIDDEN
    }

    interface CloseHandler {
        void closed(ToastViewModel toastViewModel);
    }

    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.NOT_YET_ARRANGED);
    private final ICommand closeCommand;
    private final ICommand actionCommand;
    private final Toast toast;
    private final BooleanProperty showIcon;
    private final BooleanProperty isCheckableAction;
    private final StringProperty text;
    private final StringProperty actionText;
    private final BooleanProperty isCloseable;
    private final BooleanProperty canExecute;

    ToastViewModel(Toast toast, CloseHandler closeHandler) {
        this.toast = toast;
        this.showIcon = new ReadOnlyBooleanWrapper(toast.showIcon());
        this.isCheckableAction = new ReadOnlyBooleanWrapper(toast.isCheckableAction());
        this.text = new ReadOnlyStringWrapper(toast.getText());
        this.actionText = new ReadOnlyStringWrapper(toast.getActionText());
        this.isCloseable = new ReadOnlyBooleanWrapper(toast.isCloseable());
        this.canExecute = new SimpleBooleanProperty(true);

        this.closeCommand =
            new DelegateCommand(
                () -> {
                    Expect.notNull(closeHandler, "closeHandler");
                    closeHandler.closed(ToastViewModel.this);
                },
                status.isEqualTo(Status.VISIBLE));

        this.actionCommand =
            new DelegateCommand(
                () -> {
                    Expect.notNull(
                        toast, "toast",
                        closeHandler, "closeHandler");

                    canExecute.set(false);
                    toast.executeAction();

                    if (toast.isAutoClose()) {
                        closeHandler.closed(ToastViewModel.this);
                    }
                },
                canExecute);
    }

    public ICommand getCloseCommand() {
        return closeCommand;
    }

    public ICommand getActionCommand() {
        return actionCommand;
    }

    public ReadOnlyObjectProperty<Status> statusProperty() {
        return status;
    }

    public ReadOnlyBooleanProperty showIconProperty() {
        return showIcon;
    }

    public ReadOnlyBooleanProperty isCheckableActionProperty() {
        return isCheckableAction;
    }

    public ReadOnlyStringProperty textProperty() {
        return text;
    }

    public ReadOnlyStringProperty actionTextProperty() {
        return actionText;
    }

    public ReadOnlyBooleanProperty isCloseableProperty() {
        return isCloseable;
    }

    void dismiss() {
        if (status.get() != Status.APPEARING && status.get() != Status.VISIBLE) {
            return;
        }

        status.set(Status.DISAPPEARING);
    }

    void notifyStatusTransitionFinished() {
        if (status.get() == Status.APPEARING) {
            status.set(Status.VISIBLE);
        } else if (status.get() == Status.DISAPPEARING) {
            status.set(Status.HIDDEN);
        }
    }

    void notifyArranged() {
        if (status.get() != Status.NOT_YET_ARRANGED) {
            throw new IllegalStateException();
        }

        status.set(Status.APPEARING);
    }

    Toast getToast() {
        return toast;
    }

    ToastType getToastType() {
        return toast.getType();
    }

}
