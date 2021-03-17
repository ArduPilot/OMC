/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.commands;

import com.intel.missioncontrol.TestBase;
import com.intel.missioncontrol.Waiter;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import java.time.Duration;
import javafx.application.Platform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DelegateCommandTest extends TestBase {

    @Test
    void Command_Cannot_Be_Invoked_On_Background_Thread() {
        var waiter = new Waiter();
        var command = new DelegateCommand(waiter::signal);
        Assertions.assertThrows(IllegalStateException.class, () -> command.execute());
    }

    @Test
    void Command_Can_Be_Invoked_On_UI_Thread() {
        var waiter = new Waiter();

        Platform.runLater(
            () -> {
                var command =
                    new DelegateCommand(
                        () -> {
                            waiter.assertTrue(Platform.isFxApplicationThread());
                            waiter.signal();
                        });

                command.execute();
            });

        waiter.await(1, Duration.ofSeconds(5));
    }

}
