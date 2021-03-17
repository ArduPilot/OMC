/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class FlightPlan {
   @SerializedName("my_version")
   @Expose (serialize = true, deserialize = false)
   private Integer my_version = 0;

   @SerializedName("__v")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private Integer v = 0;

   @SerializedName("_id")
   @Expose
   @Nullable
   private String id;
   @SerializedName("name")
   @Expose
   @Nullable
   private String name = "name";
   @SerializedName("type")
   @Expose
   @Nullable
   private String type = "SURVEY";
   @SerializedName("project_id")
   @Expose
   @Nullable
   private String projectId = "";
   @SerializedName("mission_id")
   @Expose
   @Nullable
   private String missionId;
   @SerializedName("camera")
   @Expose
   @Nullable
   private Camera camera;
   @SerializedName("hw_config")
   @Expose
   @Nullable
   private String hwConfig;
   @SerializedName("created")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private String created;
   @SerializedName("waypoints")
   @Expose
   @NotNull
   private List<Waypoint> waypoints = new ArrayList();
   @SerializedName("safety_procedures")
   @Expose
   @NotNull
   private Map safetyProcedures = (Map)(new HashMap());
   @SerializedName("takeoff")
   @Expose
   @NotNull
   private List takeoff = new ArrayList();
   @SerializedName("landing")
   @Expose
   @NotNull
   private List landing = new ArrayList();
   @SerializedName("company_id")
   @Expose
   @Nullable
   private String companyId;
   @SerializedName("modified")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private String modified;

   @SerializedName("safety_altitude")
   @Expose
   @Nullable
   private Double safetyAltitude = 0.0D;
   @SerializedName("max_ground_speed")
   @Expose
   @Nullable
   private Double maxGroundSpeed = 0.0D;

   @Nullable
   public final String getId() {
      return this.id;
   }

   public final void setId(@Nullable String var1) {
      this.id = var1;
   }

   @Nullable
   public final String getName() {
      return this.name;
   }

   public final void setName(@Nullable String var1) {
      this.name = var1;
   }

   @Nullable
   public final String getType() {
      return this.type;
   }

   public final void setType(@Nullable String var1) {
      this.type = var1;
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
   public final Camera getCamera() {
      return this.camera;
   }

   public final void setCamera(@Nullable Camera var1) {
      this.camera = var1;
   }

   @Nullable
   public final String getHwConfig() {
      return this.hwConfig;
   }

   public final void setHwConfig(@Nullable String var1) {
      this.hwConfig = var1;
   }

   @Nullable
   public final String getCreated() {
      return this.created;
   }

   public final void setCreated(@Nullable String var1) {
      this.created = var1;
   }

   @NotNull
   public final List<Waypoint> getWaypoints() {
      return this.waypoints;
   }

   public final void setWaypoints(@NotNull List var1) {
            this.waypoints = var1;
   }

   @NotNull
   public final Map getSafetyProcedures() {
      return this.safetyProcedures;
   }

   public final void setSafetyProcedures(@NotNull Map var1) {
            this.safetyProcedures = var1;
   }

   @NotNull
   public final List getTakeoff() {
      return this.takeoff;
   }

   public final void setTakeoff(@NotNull List var1) {
            this.takeoff = var1;
   }

   @NotNull
   public final List getLanding() {
      return this.landing;
   }

   public final void setLanding(@NotNull List var1) {
            this.landing = var1;
   }

   @Nullable
   public final String getCompanyId() {
      return this.companyId;
   }

   public final void setCompanyId(@Nullable String var1) {
      this.companyId = var1;
   }

   @Nullable
   public final String getModified() {
      return this.modified;
   }

   public final void setModified(@Nullable String var1) {
      this.modified = var1;
   }

   @Nullable
   public final Integer getV() {
      return this.v;
   }

   public final void setMy_version(@Nullable Integer var1) {
      this.my_version = var1;
   }

   @Nullable
   public final Double getSafetyAltitude() {
      return this.safetyAltitude;
   }

   public final void setSafetyAltitude(@Nullable Double var1) {
      this.safetyAltitude = var1;
   }

   @Nullable
   public final Double getMaxGroundSpeed() {
      return this.maxGroundSpeed;
   }

   public final void setMaxGroundSpeed(@Nullable Double var1) {
      this.maxGroundSpeed = var1;
   }
}
