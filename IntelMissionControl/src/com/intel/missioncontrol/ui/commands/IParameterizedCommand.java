/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.commands;

import com.google.common.util.concurrent.ListenableFuture;
import javafx.beans.property.ObjectProperty;

public interface IParameterizedCommand<T> extends ICommand {

    /**
     * Synchronously executes the command with the given parameter. This is equivalent to setting the
     * parameterProperty() value and calling the parameterless execute() method.
     */
    void execute(T parameter);

    /**
     * Asynchronously executes the command with the given parameter. This is equivalent to setting the
     * parameterProperty() value and calling the parameterless executeAsync() method.
     */
    ListenableFuture<Void> executeAsync(T parameter);

    ObjectProperty<T> parameterProperty();

}
