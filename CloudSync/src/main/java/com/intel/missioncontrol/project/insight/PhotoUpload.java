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


public final class PhotoUpload {
   @SerializedName("project")
   @Expose
   @Nullable
   private String project;
   @SerializedName("mission")
   @Expose
   @Nullable
   private String mission;
   @SerializedName("flight")
   @Expose
   @Nullable
   private String flight;
   @SerializedName("photos")
   @Expose
   @NotNull
   private List photos = new ArrayList();

   @Nullable
   public final String getProject() {
      return this.project;
   }

   public final void setProject(@Nullable String var1) {
      this.project = var1;
   }

   @Nullable
   public final String getMission() {
      return this.mission;
   }

   public final void setMission(@Nullable String var1) {
      this.mission = var1;
   }

   @Nullable
   public final String getFlight() {
      return this.flight;
   }

   public final void setFlight(@Nullable String var1) {
      this.flight = var1;
   }

   @NotNull
   public final List getPhotos() {
      return this.photos;
   }

   public final void setPhotos(@NotNull List var1) {
            this.photos = var1;
   }

   @NotNull
   public final PhotoUpload withProject(@NotNull String project) {
            this.project = project;
      return this;
   }

   @NotNull
   public final PhotoUpload withMission(@NotNull String mission) {
            this.mission = mission;
      return this;
   }

   @NotNull
   public final PhotoUpload withFlight(@NotNull String flight) {
            this.flight = flight;
      return this;
   }

   @NotNull
   public final PhotoUpload withPhotos(@NotNull List photos) {
            this.photos = photos;
      return this;
   }
}
