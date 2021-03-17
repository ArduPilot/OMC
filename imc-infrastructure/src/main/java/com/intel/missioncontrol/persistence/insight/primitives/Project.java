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


public final class Project {
   @SerializedName("_id")
   @Expose
   @Nullable
   private String id = "-1";

   @SerializedName("my_version")
   @Expose (serialize = true, deserialize = false)
   private Integer my_version = 0;

   @SerializedName("__v")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private Integer v = 0;
   @SerializedName("companyId")
   @Expose
   @Nullable
   private String companyId;
   @SerializedName("created")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private String created;
   @SerializedName("deliverables")
   @Expose (serialize = false, deserialize = true)
   @NotNull
   private List deliverables = new ArrayList();
   @SerializedName("dxobjects")
   @Expose (serialize = false, deserialize = true)
   @NotNull
   private List dxobjects = new ArrayList();
   @SerializedName("geometry")
   @Expose
   @Nullable
   private Geometries geometry;
   @SerializedName("industry")
   @Expose
   @Nullable
   private String industry;
   @SerializedName("missions")
   @Expose (serialize = false, deserialize = true)
   @NotNull
   //READ ONLY
   private List<Object> missionsIds = new ArrayList();
   @SerializedName("missionList")
   @NotNull
   //only IMC usage
   private List<Mission> missionList = new ArrayList();
   @SerializedName("modification_date")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private String modificationDate;
   @SerializedName("modification_user")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private ModificationUser modificationUser;
   @SerializedName("name")
   @Expose
   @Nullable
   private String name;
   @SerializedName("place_name")
   @Expose
   @Nullable
   private String placeName;
   @SerializedName("preview")
   @Expose
   @Nullable
   private String preview;
   @SerializedName("real_bbox")
   @Expose
   @Nullable
   private RealBbox realBbox;
   @SerializedName("units")
   @Expose
   @Nullable
   private Units units;
   @SerializedName("user")
   @Expose
   @Nullable
   private User user;
   @SerializedName("horizontal_srs_wkt")
   @Expose
   @Nullable
   private String horizontalSrsWkt;
   @SerializedName("vertical_srs_wkt")
   @Expose
   @Nullable
   private String verticalSrsWkt;
   @SerializedName("addProjectToUsers")
   @Expose
   @Nullable
   private Boolean addProjectToUsers = true;
   @SerializedName("flightIds")
   @Expose (serialize = false, deserialize = true)
   @NotNull
   //READ ONLY
   private List<Object> flightIds = new ArrayList();
   @SerializedName("flightList")
   @NotNull
   //Imc usage only
   private List<Flight> flightList = new ArrayList();
   @SerializedName("epsg")
   @Expose
   @Nullable
   private String epsg;

   @Nullable
   public final String getId() {
      return this.id;
   }

   public final void setId(@Nullable String var1) {
      this.id = var1;
   }

   @Nullable
   public final Integer getV() {
      return this.v;
   }

   @Nullable
   public final String getCompanyId() {
      return this.companyId;
   }

   public final void setCompanyId(@Nullable String var1) {
      this.companyId = var1;
   }

   @Nullable
   public final String getCreated() {
      return this.created;
   }

   public final void setCreated(@Nullable String var1) {
      this.created = var1;
   }

   @NotNull
   public final List getDeliverables() {
      return this.deliverables;
   }

   public final void setDeliverables(@NotNull List var1) {
            this.deliverables = var1;
   }

   @NotNull
   public final List getDxobjects() {
      return this.dxobjects;
   }

   public final void setDxobjects(@NotNull List var1) {
            this.dxobjects = var1;
   }

   @Nullable
   public final Geometries getGeometry() {
      return this.geometry;
   }

   public final void setGeometry(@Nullable Geometries var1) {
      this.geometry = var1;
   }

   @Nullable
   public final String getIndustry() {
      return this.industry;
   }

   public final void setIndustry(@Nullable String var1) {
      this.industry = var1;
   }

   @NotNull
   public final List<Object> getMissionsIds() {
      return this.missionsIds;
   }

   @NotNull
   public final List<Mission> getMissionList() {
      return this.missionList;
   }

   public final void setMissionList(@NotNull List var1) {
            this.missionList = var1;
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

   @Nullable
   public final String getName() {
      return this.name;
   }

   public final void setName(@Nullable String var1) {
      this.name = var1;
   }

   @Nullable
   public final String getPlaceName() {
      return this.placeName;
   }

   public final void setPlaceName(@Nullable String var1) {
      this.placeName = var1;
   }

   @Nullable
   public final String getPreview() {
      return this.preview;
   }

   public final void setPreview(@Nullable String var1) {
      this.preview = var1;
   }

   @Nullable
   public final RealBbox getRealBbox() {
      return this.realBbox;
   }

   public final void setRealBbox(@Nullable RealBbox var1) {
      this.realBbox = var1;
   }

   @Nullable
   public final Units getUnits() {
      return this.units;
   }

   public final void setUnits(@Nullable Units var1) {
      this.units = var1;
   }

   @Nullable
   public final User getUser() {
      return this.user;
   }

   public final void setUser(@Nullable User var1) {
      this.user = var1;
   }

   @Nullable
   public final String getHorizontalSrsWkt() {
      return this.horizontalSrsWkt;
   }

   public final void setHorizontalSrsWkt(@Nullable String var1) {
      this.horizontalSrsWkt = var1;
   }

   @Nullable
   public final String getVerticalSrsWkt() {
      return this.verticalSrsWkt;
   }

   public final void setVerticalSrsWkt(@Nullable String var1) {
      this.verticalSrsWkt = var1;
   }

   @Nullable
   public final Boolean getAddProjectToUsers() {
      return this.addProjectToUsers;
   }

   public final void setAddProjectToUsers(@Nullable Boolean var1) {
      this.addProjectToUsers = var1;
   }

   @NotNull
   public final List getFlightIds() {
      return this.flightIds;
   }

   public final void setFlightIds(@NotNull List var1) {
            this.flightIds = var1;
   }

   @NotNull
   public final List<Flight> getFlightList() {
      return this.flightList;
   }

   public final void setFlightList(@NotNull List var1) {
            this.flightList = var1;
   }

   @Nullable
   public final String getEpsg() {
      return this.epsg;
   }

   public final void setEpsg(@Nullable String var1) {
      this.epsg = var1;
   }

   @NotNull
   public String toString() {
      return "project: id:" + this.id + " name:" + this.name;
   }
   @Nullable
   public final Integer getMy_version() {
      return this.my_version;
   }

   public final void setMy_version(@Nullable Integer var1) {
      this.my_version = var1;
   }
}
