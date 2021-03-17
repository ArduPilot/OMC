/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.commands;

import com.intel.missioncontrol.helper.Expect;

public class ParameterizedCommandInvocation<T> implements Runnable {

    private final IParameterizedCommand<T> command;
    private final T payload;

    public ParameterizedCommandInvocation(IParameterizedCommand<T> command, T payload) {
        Expect.notNull(command, "command", payload, "payload");
        this.command = command;
        this.payload = payload;
    }

    @Override
    public void run() {
        command.execute(payload);
    }

}
