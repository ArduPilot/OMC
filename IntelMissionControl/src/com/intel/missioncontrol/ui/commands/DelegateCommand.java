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
import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegateCommand extends DelegateCommandBase {

    private static Logger LOGGER = LoggerFactory.getLogger(DelegateCommand.class);

    private final Runnable runnable;

    public DelegateCommand(Runnable runnable) {
        Expect.notNull(runnable, "runnable");
        this.runnable = runnable;
    }

    public DelegateCommand(Runnable runnable, ObservableValue<Boolean> executableObservable) {
        super(executableObservable);
        Expect.notNull(runnable, "runnable", executableObservable, "executableObservable");
        this.runnable = runnable;
    }

    @Override
    public void execute() {
        CommandHelper.verifyAccess();

        if (!isExecutable()) {
            throw new RuntimeException("The command is not executable.");
        }

        runnable.run();
    }

    @Override
    public ListenableFuture<Void> executeAsync() {
        CommandHelper.verifyAccess();

        if (!isExecutable()) {
            throw new RuntimeException("The command is not executable.");
        }

        setExecuting(true);

        var synchronizationContext = SynchronizationContext.getCurrent();
        var future = Dispatcher.post(runnable);

        Futures.addCallback(
            future,
            new FutureCallback<>() {
                @Override
                public void onSuccess(Void arg) {
                    setExecuting(false);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    LOGGER.error("delegated command failed", throwable);
                    setExecuting(false);
                }
            },
            synchronizationContext);

        return future;
    }

}
