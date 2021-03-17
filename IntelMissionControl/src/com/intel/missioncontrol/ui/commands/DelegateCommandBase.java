/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.commands;

import com.intel.missioncontrol.helper.Expect;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

public abstract class DelegateCommandBase implements ICommand {

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

    private final BooleanBinding executableBinding;

    private BooleanProperty notExecutable;
    private BooleanProperty notExecuting;

    protected DelegateCommandBase() {
        executableBinding = executing.not();
        executable.bind(executableBinding);
    }

    protected DelegateCommandBase(ObservableValue<Boolean> executableObservable) {
        Expect.notNull(executableObservable, "executableObservable");

        executableBinding =
            Bindings.createBooleanBinding(
                () -> executableObservable.getValue() && !executing.get(), executableObservable, executing);

        executable.bind(executableBinding);
    }

    @Override
    public boolean isExecutable() {
        return executable.get();
    }

    @Override
    public boolean isNotExecutable() {
        return !executable.get();
    }

    @Override
    public ReadOnlyBooleanProperty executableProperty() {
        return executable;
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

    @Override
    public boolean isNotExecuting() {
        return !executing.get();
    }

    @Override
    public ReadOnlyBooleanProperty notExecutingProperty() {
        if (notExecuting == null) {
            notExecuting = new SimpleBooleanProperty(!executing.get());
        }

        return notExecuting;
    }

    void setExecuting(boolean executing) {
        this.executing.set(executing);
    }

}
