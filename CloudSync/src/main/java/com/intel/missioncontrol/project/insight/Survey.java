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


public final class Survey {
   @SerializedName("name")
   @Expose
   @Nullable
   private String name;
   @SerializedName("addProjectToUsers")
   @Expose
   @Nullable
   private Boolean addProjectToUsers;
   @SerializedName("industry")
   @Expose
   @Nullable
   private String industry;
   @SerializedName("geometry")
   @Expose
   @Nullable
   private Geometries geometry;
   @SerializedName("area")
   @Expose
   @Nullable
   private Double area;
   @SerializedName("processSettings")
   @Expose
   @Nullable
   private ProcessSettings processSettings;
   @SerializedName("survey_date")
   @Expose
   @Nullable
   private String surveyDate;
   @SerializedName("number_of_photos")
   @Expose
   @Nullable
   private Integer numberOfPhotos;
   @SerializedName("cameras")
   @Expose
   @NotNull
   private List cameras = new ArrayList();
   @SerializedName("horizontal_srs_wkt")
   @Expose
   private String horizontalSrsWkt;
   @SerializedName("vertical_srs_wkt")
   @Expose
   private String verticalSrsWkt;

   @Nullable
   public final String getName() {
      return this.name;
   }

   public final void setName(@Nullable String var1) {
      this.name = var1;
   }

   @Nullable
   public final Boolean getAddProjectToUsers() {
      return this.addProjectToUsers;
   }

   public final void setAddProjectToUsers(@Nullable Boolean var1) {
      this.addProjectToUsers = var1;
   }

   @Nullable
   public final String getIndustry() {
      return this.industry;
   }

   public final void setIndustry(@Nullable String var1) {
      this.industry = var1;
   }

   @Nullable
   public final Geometries getGeometry() {
      return this.geometry;
   }

   public final void setGeometry(@Nullable Geometries var1) {
      this.geometry = var1;
   }

   @Nullable
   public final Double getArea() {
      return this.area;
   }

   public final void setArea(@Nullable Double var1) {
      this.area = var1;
   }

   @Nullable
   public final ProcessSettings getProcessSettings() {
      return this.processSettings;
   }

   public final void setProcessSettings(@Nullable ProcessSettings var1) {
      this.processSettings = var1;
   }

   @Nullable
   public final String getSurveyDate() {
      return this.surveyDate;
   }

   public final void setSurveyDate(@Nullable String var1) {
      this.surveyDate = var1;
   }

   @Nullable
   public final Integer getNumberOfPhotos() {
      return this.numberOfPhotos;
   }

   public final void setNumberOfPhotos(@Nullable Integer var1) {
      this.numberOfPhotos = var1;
   }

   @NotNull
   public final List getCameras() {
      return this.cameras;
   }

   public final void setCameras(@NotNull List var1) {
            this.cameras = var1;
   }
}
