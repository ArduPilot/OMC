/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import eu.mavinci.plane.FTPManager;

public interface MBackgroundTaskManager {

    void submitTask(String name, FTPManager.Task mTask, long size, Runnable task);

    void hintJobStatus(String status);
}
