/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class Flight {
   @SerializedName("my_version")
   @Expose (serialize = true, deserialize = false)
   private Integer my_version = 0;

   @SerializedName("__v")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private Integer v = 0;

   @SerializedName("data")
   @Expose
   @Nullable
   private Data data;
   @SerializedName("cameras")
   @Expose
   @NotNull
   private List cameras = new ArrayList();
   @SerializedName("logs")
   @Expose
   @NotNull
   private List logs = new ArrayList();
   @SerializedName("number_of_photos")
   @Expose
   @Nullable
   private Integer numberOfPhotos;
   @SerializedName("status")
   @Expose
   @Nullable
   private Status status;
   @SerializedName("_id")
   @Expose
   @Nullable
   private String id = "-1";
   @SerializedName("project")
   @Expose
   @Nullable
   private String project;
   @SerializedName("mission")
   @Expose
   @Nullable
   private String mission;
   @SerializedName("flightPlan")
   @Expose
   @Nullable
   private String flightPlan;
   @SerializedName("name")
   @Expose
   @Nullable
   private String name;
   @SerializedName("survey_date")
   @Expose
   @Nullable
   private String surveyDate;
   @SerializedName("created")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private String created;
   @SerializedName("user")
   @Expose
   @Nullable
   private User user;
   @SerializedName("hwConfig")
   @Expose
   @Nullable
   private String hwConfig;

   @Nullable
   public final Integer getV() {
      return this.v;
   }

   @Nullable
   public final Data getData() {
      return this.data;
   }

   public final void setData(@Nullable Data var1) {
      this.data = var1;
   }

   @NotNull
   public final List getCameras() {
      return this.cameras;
   }

   public final void setCameras(@NotNull List var1) {
            this.cameras = var1;
   }

   @NotNull
   public final List getLogs() {
      return this.logs;
   }

   public final void setLogs(@NotNull List var1) {
            this.logs = var1;
   }

   @Nullable
   public final Integer getNumberOfPhotos() {
      return this.numberOfPhotos;
   }

   public final void setNumberOfPhotos(@Nullable Integer var1) {
      this.numberOfPhotos = var1;
   }

   @Nullable
   public final Status getStatus() {
      return this.status;
   }

   public final void setStatus(@Nullable Status var1) {
      this.status = var1;
   }

   @Nullable
   public final String getId() {
      return this.id;
   }

   public final void setId(@Nullable String var1) {
      this.id = var1;
   }

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
   public final String getFlightPlan() {
      return this.flightPlan;
   }

   public final void setFlightPlan(@Nullable String var1) {
      this.flightPlan = var1;
   }

   @Nullable
   public final String getName() {
      return this.name;
   }

   public final void setName(@Nullable String var1) {
      this.name = var1;
   }

   @Nullable
   public final String getSurveyDate() {
      return this.surveyDate;
   }

   public final void setSurveyDate(@Nullable String var1) {
      this.surveyDate = var1;
   }

   @Nullable
   public final String getCreated() {
      return this.created;
   }

   public final void setCreated(@Nullable String var1) {
      this.created = var1;
   }

   @Nullable
   public final User getUser() {
      return this.user;
   }

   public final void setUser(@Nullable User var1) {
      this.user = var1;
   }

   @Nullable
   public final String getHwConfig() {
      return this.hwConfig;
   }

   public final void setHwConfig(@Nullable String var1) {
      this.hwConfig = var1;
   }
}
