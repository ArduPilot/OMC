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


public final class CreatedProject {
   @SerializedName("project")
   @Expose
   @Nullable
   private Project project = new Project();
   @SerializedName("mission")
   @Expose
   @Nullable
   private Mission mission = new Mission();
   @SerializedName("flight")
   @Expose
   @Nullable
   private Flight flight = new Flight();
   @SerializedName("cameras")
   @Expose
   @NotNull
   private List cameras = new ArrayList();

   @Nullable
   public final Project getProject() {
      return this.project;
   }

   public final void setProject(@Nullable Project var1) {
      this.project = var1;
   }

   @Nullable
   public final Mission getMission() {
      return this.mission;
   }

   public final void setMission(@Nullable Mission var1) {
      this.mission = var1;
   }

   @Nullable
   public final Flight getFlight() {
      return this.flight;
   }

   public final void setFlight(@Nullable Flight var1) {
      this.flight = var1;
   }

   @NotNull
   public final List getCameras() {
      return this.cameras;
   }

   public final void setCameras(@NotNull List var1) {
            this.cameras = var1;
   }
}
