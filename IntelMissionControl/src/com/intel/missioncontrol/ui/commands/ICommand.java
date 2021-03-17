/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.commands;

import com.google.common.util.concurrent.ListenableFuture;
import javafx.beans.property.ReadOnlyBooleanProperty;

public interface ICommand {

    void execute();

    ListenableFuture<Void> executeAsync();

    boolean isExecutable();

    ReadOnlyBooleanProperty executableProperty();

    boolean isNotExecutable();

    ReadOnlyBooleanProperty notExecutableProperty();

    boolean isExecuting();

    ReadOnlyBooleanProperty executingProperty();

    boolean isNotExecuting();

    ReadOnlyBooleanProperty notExecutingProperty();

}
