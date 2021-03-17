/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class SynchronizeProjectsResponse {
   @SerializedName("date")
   @Expose
   @Nullable
   private String date;
   @SerializedName("projects")
   @Expose
   @NotNull
   private List projects = new ArrayList();

   @Nullable
   public final String getDate() {
      return this.date;
   }

   public final void setDate(@Nullable String var1) {
      this.date = var1;
   }

   @NotNull
   public final List getProjects() {
      return this.projects;
   }

   public final void setProjects(@NotNull List var1) {
            this.projects = var1;
   }
}
