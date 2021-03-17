/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.custom;

@FunctionalInterface
public interface ActionDelegate {
    ActionDelegate DO_NOTHING = () -> {};

    void execute();
}
