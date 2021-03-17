/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class FlightRequest {
   @SerializedName("_id")
   @Expose
   @Nullable
   private String id;
   @SerializedName("annotation_id")
   @Expose
   @Nullable
   private String annotationId;
   @SerializedName("capture_data")
   @Expose
   @Nullable
   private String captureData;
   @SerializedName("notes")
   @Expose
   @Nullable
   private String notes;
   @SerializedName("ratio")
   @Expose
   @Nullable
   private String ratio;
   @SerializedName("type")
   @Expose
   @Nullable
   private String type;
   @SerializedName("resolution")
   @Expose
   @Nullable
   private String resolution;
   @SerializedName("project_id")
   @Expose
   @Nullable
   private String projectId;
   @SerializedName("mission_id")
   @Expose
   @Nullable
   private String missionId;
   @SerializedName("company")
   @Expose
   @Nullable
   private String company;
   @SerializedName("user_id")
   @Expose
   @Nullable
   private String userId;
   @SerializedName("created")
   @Expose
   @Nullable
   private String created;

   @Nullable
   public final String getId() {
      return this.id;
   }

   public final void setId(@Nullable String var1) {
      this.id = var1;
   }

   @Nullable
   public final String getAnnotationId() {
      return this.annotationId;
   }

   public final void setAnnotationId(@Nullable String var1) {
      this.annotationId = var1;
   }

   @Nullable
   public final String getCaptureData() {
      return this.captureData;
   }

   public final void setCaptureData(@Nullable String var1) {
      this.captureData = var1;
   }

   @Nullable
   public final String getNotes() {
      return this.notes;
   }

   public final void setNotes(@Nullable String var1) {
      this.notes = var1;
   }

   @Nullable
   public final String getRatio() {
      return this.ratio;
   }

   public final void setRatio(@Nullable String var1) {
      this.ratio = var1;
   }

   @Nullable
   public final String getType() {
      return this.type;
   }

   public final void setType(@Nullable String var1) {
      this.type = var1;
   }

   @Nullable
   public final String getResolution() {
      return this.resolution;
   }

   public final void setResolution(@Nullable String var1) {
      this.resolution = var1;
   }

   @Nullable
   public final String getProjectId() {
      return this.projectId;
   }

   public final void setProjectId(@Nullable String var1) {
      this.projectId = var1;
   }

   @Nullable
   public final String getMissionId() {
      return this.missionId;
   }

   public final void setMissionId(@Nullable String var1) {
      this.missionId = var1;
   }

   @Nullable
   public final String getCompany() {
      return this.company;
   }

   public final void setCompany(@Nullable String var1) {
      this.company = var1;
   }

   @Nullable
   public final String getUserId() {
      return this.userId;
   }

   public final void setUserId(@Nullable String var1) {
      this.userId = var1;
   }

   @Nullable
   public final String getCreated() {
      return this.created;
   }

   public final void setCreated(@Nullable String var1) {
      this.created = var1;
   }

   @NotNull
   public final FlightRequest withId(@NotNull String id) {
            this.id = id;
      return this;
   }

   @NotNull
   public final FlightRequest withAnnotationId(@NotNull String annotationId) {
            this.annotationId = annotationId;
      return this;
   }

   @NotNull
   public final FlightRequest withCaptureData(@NotNull String captureData) {
            this.captureData = captureData;
      return this;
   }

   @NotNull
   public final FlightRequest withNotes(@NotNull String notes) {
            this.notes = notes;
      return this;
   }

   @NotNull
   public final FlightRequest withRatio(@NotNull String ratio) {
            this.ratio = ratio;
      return this;
   }

   @NotNull
   public final FlightRequest withType(@NotNull String type) {
            this.type = type;
      return this;
   }

   @NotNull
   public final FlightRequest withResolution(@NotNull String resolution) {
            this.resolution = resolution;
      return this;
   }

   @NotNull
   public final FlightRequest withProjectId(@NotNull String projectId) {
            this.projectId = projectId;
      return this;
   }

   @NotNull
   public final FlightRequest withCompany(@NotNull String company) {
            this.company = company;
      return this;
   }

   @NotNull
   public final FlightRequest withUserId(@NotNull String userId) {
            this.userId = userId;
      return this;
   }

   @NotNull
   public final FlightRequest withCreated(@NotNull String created) {
            this.created = created;
      return this;
   }

   @NotNull
   public String toString() {
      return "flightReqiest: id:" + this.id;
   }
}
