/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import java.util.function.Consumer;

public interface IInterProcessHandler {

    void setMessageHandler(Consumer<String> messageHandler);

    void sendMessage(String message);

    void close();

    boolean isAlreadyRunning();

}
