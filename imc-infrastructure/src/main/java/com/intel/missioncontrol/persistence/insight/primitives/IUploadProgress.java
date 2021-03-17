/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;


import org.jetbrains.annotations.NotNull;


public interface IUploadProgress {
   void progressMessage(@NotNull String var1, double var2);
}
