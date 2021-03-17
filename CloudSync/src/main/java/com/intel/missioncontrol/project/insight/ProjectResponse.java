/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;


public final class ProjectResponse {
   @SerializedName("project")
   @Expose
   @Nullable
   private Project project;

   @Nullable
   public final Project getProject() {
      return this.project;
   }

   public final void setProject(@Nullable Project var1) {
      this.project = var1;
   }
}
