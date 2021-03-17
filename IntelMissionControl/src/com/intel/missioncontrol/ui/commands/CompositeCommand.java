/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.commands;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Arrays;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeCommand implements ICommand {

    private static Logger LOGGER = LoggerFactory.getLogger(CompositeCommand.class);

    private final BooleanProperty executing =
        new SimpleBooleanProperty(false) {
            @Override
            protected void invalidated() {
                if (notExecuting != null) {
                    notExecuting.set(!get());
                }

                super.invalidated();
            }
        };

    private final BooleanProperty executable =
        new SimpleBooleanProperty(true) {
            @Override
            protected void invalidated() {
                if (notExecutable != null) {
                    notExecutable.set(!get());
                }

                super.invalidated();
            }
        };

    private final ICommand[] commands;

    private BooleanProperty notExecutable;
    private BooleanProperty notExecuting;

    public CompositeCommand(ICommand... commands) {
        this.commands = commands;
        var dependencies = Arrays.stream(commands).map(ICommand::executableProperty).toArray(Observable[]::new);

        this.executable.bind(
            Bindings.createBooleanBinding(
                () -> {
                    for (var command : this.commands) {
                        if (!command.isExecutable()) {
                            return false;
                        }
                    }

                    return true;
                },
                dependencies));

        this.executing.bind(
            Bindings.createBooleanBinding(
                () -> {
                    for (var command : this.commands) {
                        if (!command.isExecuting()) {
                            return false;
                        }
                    }

                    return true;
                },
                dependencies));
    }

    /** Executes the commands synchronously in the order they were added to this CompositeCommand instance. */
    @Override
    public void execute() {
        CommandHelper.verifyAccess();

        if (!isExecutable()) {
            throw new RuntimeException("The command is not executable.");
        }

        for (var command : commands) {
            command.execute();
        }
    }

    /**
     * Executes the commands asynchronously and concurrently. The order of command execution is undefined, and the
     * commands may be executed on different threads.
     */
    @Override
    public ListenableFuture<Void> executeAsync() {
        CommandHelper.verifyAccess();

        var completionFuture =
            new AbstractFuture<Void>() {
                private final int targetCount = commands.length;
                private int completedCount;

                synchronized void signalComplete() {
                    ++completedCount;
                    if (completedCount == targetCount) {
                        set(null);
                    }
                }
            };

        for (var command : commands) {
            var future = command.executeAsync();
            Futures.addCallback(
                future,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(Void arg) {
                        completionFuture.signalComplete();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        LOGGER.error("delegated command failed", throwable);
                        completionFuture.signalComplete();
                    }
                });
        }

        return completionFuture;
    }

    @Override
    public boolean isExecutable() {
        return executable.get();
    }

    @Override
    public ReadOnlyBooleanProperty executableProperty() {
        return executable;
    }

    @Override
    public boolean isNotExecutable() {
        return !executable.get();
    }

    @Override
    public ReadOnlyBooleanProperty notExecutableProperty() {
        if (notExecutable == null) {
            notExecutable = new SimpleBooleanProperty(!executable.get());
        }

        return notExecutable;
    }

    @Override
    public boolean isExecuting() {
        return executing.get();
    }

    @Override
    public ReadOnlyBooleanProperty executingProperty() {
        return executing;
    }

    public boolean isNotExecuting() {
        return notExecuting.get();
    }

    @Override
    public ReadOnlyBooleanProperty notExecutingProperty() {
        if (notExecuting == null) {
            notExecuting = new SimpleBooleanProperty(!executing.get());
        }

        return notExecuting;
    }

}
