/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.tasks;

public interface IUpdateProgressMessage {
    void update(CreateDatasetSubTasks step, double subProgress, double subTotal, Object... msParams);
}
