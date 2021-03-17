/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.notifications;

import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.helper.Expect;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.AsyncCommand;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.FutureCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedAsyncCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.asyncfx.concurrent.Futures;

@SuppressLinter(
    value = "IllegalViewModelMethod",
    reviewer = "mstrauss",
    justification = "Non-private methods used for view-model to view-model communication"
)
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
    private final Command closeCommand;
    private final Command actionCommand;
    private final Toast toast;
    private final BooleanProperty showIcon;
    private final BooleanProperty isCheckableAction;
    private final StringProperty text = new SimpleStringProperty();
    private final StringProperty actionText;
    private final BooleanProperty isCloseable;
    private final BooleanProperty canExecute;

    @SuppressWarnings("unchecked")
    ToastViewModel(Toast toast, CloseHandler closeHandler) {
        Expect.notNull(
            toast, "toast",
            closeHandler, "closeHandler");

        this.toast = toast;
        this.showIcon = new ReadOnlyBooleanWrapper(toast.showIcon());
        this.isCheckableAction = new ReadOnlyBooleanWrapper(toast.isCheckableAction());
        this.text.bind(toast.getTextProperty());
        this.actionText = new ReadOnlyStringWrapper(toast.getActionText());
        this.isCloseable = new ReadOnlyBooleanWrapper(toast.isCloseable());
        this.canExecute = new SimpleBooleanProperty(true);

        this.closeCommand =
            new DelegateCommand(() -> closeHandler.closed(ToastViewModel.this), status.isEqualTo(Status.VISIBLE));

        final Command toastCommand = toast.getCommand();
        if (toastCommand instanceof ParameterizedAsyncCommand) {
            this.actionCommand =
                new FutureCommand(
                    () ->
                        Futures.fromListenableFuture(
                                ((ParameterizedAsyncCommand)toastCommand).executeAsync(toast.getCommandParameter()))
                            .whenDone(
                                f -> {
                                    if (toast.isAutoClose()) {
                                        closeHandler.closed(ToastViewModel.this);
                                    }
                                }));
        } else if (toastCommand instanceof AsyncCommand) {
            this.actionCommand =
                new FutureCommand(
                    () ->
                        Futures.fromListenableFuture(((AsyncCommand)toastCommand).executeAsync())
                            .whenDone(
                                f -> {
                                    if (toast.isAutoClose()) {
                                        closeHandler.closed(ToastViewModel.this);
                                    }
                                }));
        } else if (toastCommand instanceof ParameterizedCommand) {
            this.actionCommand =
                new ParameterizedDelegateCommand(
                    unused -> {
                        ((ParameterizedCommand)toastCommand).execute(toast.getCommandParameter());

                        if (toast.isAutoClose()) {
                            closeHandler.closed(ToastViewModel.this);
                        }
                    });
        } else if (toastCommand != null) {
            this.actionCommand =
                new DelegateCommand(
                    () -> {
                        toastCommand.execute();

                        if (toast.isAutoClose()) {
                            closeHandler.closed(ToastViewModel.this);
                        }
                    });
        } else {
            this.actionCommand =
                new DelegateCommand(
                    () -> {
                        canExecute.set(false);
                        toast.executeAction();

                        if (toast.isAutoClose()) {
                            closeHandler.closed(ToastViewModel.this);
                        }
                    },
                    canExecute);
        }
    }

    public Command getCloseCommand() {
        return closeCommand;
    }

    public Command getActionCommand() {
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
