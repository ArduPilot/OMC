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


public final class Mission {
   @SerializedName("my_version")
   @Expose (serialize = true, deserialize = false)
   private Integer my_version = 0;

   @SerializedName("__v")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private Integer v = 0;
   @SerializedName("application")
   @Expose
   @Nullable
   private Application application;
   @SerializedName("delivery")
   @Expose
   @Nullable
   private Delivery delivery;
   @SerializedName("precision")
   @Expose
   @Nullable
   private Precision precision;
   @SerializedName("area")
   @Expose
   @Nullable
   private Double area;
   @SerializedName("length")
   @Expose
   @Nullable
   private Integer length;
   @SerializedName("geometry")
   @Expose
   @Nullable
   private Geometries geometry;
   @SerializedName("real_bbox")
   @Expose
   @Nullable
   private RealBbox realBbox;
   @SerializedName("epsg")
   @Expose
   @Nullable
   private String epsg;
   @SerializedName("flightplanlist")
   @NotNull
   private List<FlightPlan> flightPlanList = new ArrayList();
   @SerializedName("annotationslist")
   @NotNull
   private List<Annotation> annotationList = new ArrayList();
   @SerializedName("dxobjects")
   @Expose (serialize = false, deserialize = true)
   @NotNull
   private List dxobjects = new ArrayList();
   @SerializedName("status")
   @Expose
   @Nullable
   private Status status;
   @SerializedName("deliverables")
   @Expose
   @NotNull
   private List deliverables = new ArrayList();
   @SerializedName("_id")
   @Expose
   @Nullable
   private String id = "-1";
   @SerializedName("project")
   @Expose
   @Nullable
   private String project;
   @SerializedName("name")
   @Expose
   @Nullable
   private String name;
   @SerializedName("survey_date")
   @Expose
   @Nullable
   private String surveyDate;
   @SerializedName("processSettings")
   @Expose
   @Nullable
   private ProcessSettings processSettings;
   @SerializedName("created")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private String created;
   @SerializedName("user")
   @Expose
   @Nullable
   private User user;
   @SerializedName("modification_date")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private String modificationDate;
   @SerializedName("modification_user")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private ModificationUser modificationUser;
   @SerializedName("cameras")
   @Expose
   @NotNull
   private List cameras = new ArrayList();
   @SerializedName("hw_config")
   @Expose
   @Nullable
   private String hwConfig;
   @SerializedName("reference_point")
   @Expose
   @NotNull
   private List<Double> referencePoint = new ArrayList();
   @SerializedName("terrain_adjustment_mode")
   @Expose
   @Nullable
   private String terrainAdjustmentMode;

   @Nullable
   public final Integer getV() {
      return this.v;
   }

   @Nullable
   public final Application getApplication() {
      return this.application;
   }

   public final void setApplication(@Nullable Application var1) {
      this.application = var1;
   }

   @Nullable
   public final Delivery getDelivery() {
      return this.delivery;
   }

   public final void setDelivery(@Nullable Delivery var1) {
      this.delivery = var1;
   }

   @Nullable
   public final Precision getPrecision() {
      return this.precision;
   }

   public final void setPrecision(@Nullable Precision var1) {
      this.precision = var1;
   }

   @Nullable
   public final Double getArea() {
      return this.area;
   }

   public final void setArea(@Nullable Double var1) {
      this.area = var1;
   }

   @Nullable
   public final Integer getLength() {
      return this.length;
   }

   public final void setLength(@Nullable Integer var1) {
      this.length = var1;
   }

   @Nullable
   public final Geometries getGeometry() {
      return this.geometry;
   }

   public final void setGeometry(@Nullable Geometries var1) {
      this.geometry = var1;
   }

   @Nullable
   public final RealBbox getRealBbox() {
      return this.realBbox;
   }

   public final void setRealBbox(@Nullable RealBbox var1) {
      this.realBbox = var1;
   }

   @Nullable
   public final String getEpsg() {
      return this.epsg;
   }

   public final void setEpsg(@Nullable String var1) {
      this.epsg = var1;
   }

   @NotNull
   public final List<FlightPlan> getFlightPlanList() {
      return this.flightPlanList;
   }

   public final void setFlightPlanList(@NotNull List var1) {
            this.flightPlanList = var1;
   }

   @NotNull
   public final List<Annotation> getAnnotationList() {
      return this.annotationList;
   }

   public final void setAnnotationList(@NotNull List var1) {
            this.annotationList = var1;
   }

   @NotNull
   public final List getDxobjects() {
      return this.dxobjects;
   }

   public final void setDxobjects(@NotNull List var1) {
            this.dxobjects = var1;
   }

   @Nullable
   public final Status getStatus() {
      return this.status;
   }

   public final void setStatus(@Nullable Status var1) {
      this.status = var1;
   }

   @NotNull
   public final List getDeliverables() {
      return this.deliverables;
   }

   public final void setDeliverables(@NotNull List var1) {
            this.deliverables = var1;
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
   public final ProcessSettings getProcessSettings() {
      return this.processSettings;
   }

   public final void setProcessSettings(@Nullable ProcessSettings var1) {
      this.processSettings = var1;
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
   public final String getModificationDate() {
      return this.modificationDate;
   }

   public final void setModificationDate(@Nullable String var1) {
      this.modificationDate = var1;
   }

   @Nullable
   public final ModificationUser getModificationUser() {
      return this.modificationUser;
   }

   public final void setModificationUser(@Nullable ModificationUser var1) {
      this.modificationUser = var1;
   }

   @NotNull
   public final List getCameras() {
      return this.cameras;
   }

   public final void setCameras(@NotNull List var1) {
            this.cameras = var1;
   }

   @Nullable
   public final String getHwConfig() {
      return this.hwConfig;
   }

   public final void setHwConfig(@Nullable String var1) {
      this.hwConfig = var1;
   }

   @NotNull
   public final List<Double> getReferencePoint() {
      return this.referencePoint;
   }

   public final void setReferencePoint(@NotNull List var1) {
            this.referencePoint = var1;
   }

   @Nullable
   public final String getTerrainAdjustmentMode() {
      return this.terrainAdjustmentMode;
   }

   public final void setTerrainAdjustmentMode(@Nullable String var1) {
      this.terrainAdjustmentMode = var1;
   }

   @Nullable
   public final Integer getMy_version() {
      return this.my_version;
   }

   public final void setMy_version(@Nullable Integer var1) {
      this.my_version = var1;
   }
}
