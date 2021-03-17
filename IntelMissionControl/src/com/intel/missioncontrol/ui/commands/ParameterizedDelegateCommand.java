/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.commands;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.SynchronizationContext;
import com.intel.missioncontrol.helper.Expect;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

public class ParameterizedDelegateCommand<T> extends DelegateCommandBase implements IParameterizedCommand<T> {

    private final ObjectProperty<T> parameter =
        new SimpleObjectProperty<>() {
            @Override
            protected void invalidated() {
                if (isExecuting()) {
                    throw new RuntimeException("Parameter cannot be changed while the command is executing.");
                }

                super.invalidated();
            }
        };

    private final Action<T> action;

    public ParameterizedDelegateCommand(Action<T> action) {
        Expect.notNull(action, "action");
        this.action = action;
    }

    public ParameterizedDelegateCommand(Action<T> action, ObservableValue<Boolean> executableObservable) {
        super(executableObservable);
        Expect.notNull(action, "action");
        this.action = action;
    }

    @Override
    public void execute() {
        CommandHelper.verifyAccess();

        if (!isExecutable()) {
            throw new RuntimeException("The command is not executable.");
        }

        action.execute(parameter.get());
    }

    @Override
    public void execute(T parameter) {
        CommandHelper.verifyAccess();

        if (!isExecutable()) {
            throw new RuntimeException("The command is not executable.");
        }

        action.execute(parameter);
    }

    @Override
    public ListenableFuture<Void> executeAsync() {
        return executeAsync(parameter.get());
    }

    @Override
    public ListenableFuture<Void> executeAsync(T parameter) {
        CommandHelper.verifyAccess();

        if (!isExecutable()) {
            throw new RuntimeException("The command is not executable.");
        }

        setExecuting(true);

        var synchronizationContext = SynchronizationContext.getCurrent();
        var future = Dispatcher.post(() -> action.execute(parameter));

        Futures.addCallback(
            future,
            new FutureCallback<>() {
                @Override
                public void onSuccess(Void arg) {
                    setExecuting(false);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    setExecuting(false);
                }
            },
            synchronizationContext);

        return future;
    }

    @Override
    public ObjectProperty<T> parameterProperty() {
        return parameter;
    }

}
