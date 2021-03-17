/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.commands;

import com.intel.missioncontrol.TestBase;
import com.intel.missioncontrol.Waiter;
import de.saxsys.mvvmfx.utils.commands.DelegateAsyncCommand;
import javafx.application.Platform;
import org.asyncfx.concurrent.Futures;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DelegateAsyncCommandTest extends TestBase {

    @Test
    void Command_Cannot_Be_Invoked_On_Background_Thread() {
        var waiter = new Waiter();
        var command = new DelegateAsyncCommand(waiter::signal);
        Assertions.assertThrows(IllegalStateException.class, () -> command.executeAsync());
    }

    @Test
    void Command_Can_Be_Invoked_On_UI_Thread() {
        var waiter = new Waiter();

        Platform.runLater(
            () -> {
                var command =
                    new DelegateAsyncCommand(
                        () -> {
                            waiter.assertTrue(!Platform.isFxApplicationThread());
                            waiter.signal();
                        });

                Futures.fromListenableFuture(command.executeAsync()).whenSucceeded(f -> waiter.signal());
            });

        waiter.await(2);
    }

}
