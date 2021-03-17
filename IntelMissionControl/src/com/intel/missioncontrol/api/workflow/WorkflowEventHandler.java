/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api.workflow;

@FunctionalInterface
public interface WorkflowEventHandler<T extends WorkflowEvent> {
    void handle(T event);
}
