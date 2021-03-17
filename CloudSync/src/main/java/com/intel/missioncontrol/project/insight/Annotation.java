/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class Annotation {
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
   @SerializedName("target")
   @Expose
   @Nullable
   private Target target;
   @SerializedName("feature")
   @Expose
   @Nullable
   private Feature feature;
   @SerializedName("project_id")
   @Expose
   @Nullable
   private String projectId;
   @SerializedName("mission_id")
   @Expose
   @Nullable
   private String missionId;
   @SerializedName("created_by")
   @Expose
   @Nullable
   private CreatedBy createdBy;
   @SerializedName("created_date")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private String createdDate;
   @SerializedName("modified_date")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private String modifiedDate;

   @SerializedName("modified_by")
   @Expose (serialize = false, deserialize = true)
   @Nullable
   private ModifiedBy modifiedBy;
   @SerializedName("parameters")
   @Expose
   @Nullable
   private AnnotationParameters parameters;

   @Nullable
   public final String getId() {
      return this.id;
   }

   public final void setId(@Nullable String var1) {
      this.id = var1;
   }

   @Nullable
   public final Target getTarget() {
      return this.target;
   }

   public final void setTarget(@Nullable Target var1) {
      this.target = var1;
   }

   @Nullable
   public final Feature getFeature() {
      return this.feature;
   }

   public final void setFeature(@Nullable Feature var1) {
      this.feature = var1;
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
   public final CreatedBy getCreatedBy() {
      return this.createdBy;
   }

   public final void setCreatedBy(@Nullable CreatedBy var1) {
      this.createdBy = var1;
   }

   @Nullable
   public final String getCreatedDate() {
      return this.createdDate;
   }

   public final void setCreatedDate(@Nullable String var1) {
      this.createdDate = var1;
   }

   @Nullable
   public final String getModifiedDate() {
      return this.modifiedDate;
   }

   public final void setModifiedDate(@Nullable String var1) {
      this.modifiedDate = var1;
   }

   @Nullable
   public final Integer getV() {
      return this.v;
   }

   public final void setV(@Nullable Integer var1) {
      this.v = var1;
   }

   @Nullable
   public final ModifiedBy getModifiedBy() {
      return this.modifiedBy;
   }

   public final void setModifiedBy(@Nullable ModifiedBy var1) {
      this.modifiedBy = var1;
   }

   @Nullable
   public final AnnotationParameters getParameters() {
      return this.parameters;
   }

   public final void setParameters(@Nullable AnnotationParameters var1) {
      this.parameters = var1;
   }

   @NotNull
   public final Annotation withId(@NotNull String id) {
            this.id = id;
      return this;
   }

   @NotNull
   public final Annotation withTarget(@NotNull Target target) {
            this.target = target;
      return this;
   }

   @NotNull
   public final Annotation withFeature(@NotNull Feature feature) {
            this.feature = feature;
      return this;
   }

   @NotNull
   public final Annotation withProjectId(@NotNull String projectId) {
            this.projectId = projectId;
      return this;
   }

   @NotNull
   public final Annotation withCreatedBy(@NotNull CreatedBy createdBy) {
            this.createdBy = createdBy;
      return this;
   }

   @NotNull
   public final Annotation withCreatedDate(@NotNull String createdDate) {
            this.createdDate = createdDate;
      return this;
   }

   @NotNull
   public final Annotation withModifiedDate(@NotNull String modifiedDate) {
            this.modifiedDate = modifiedDate;
      return this;
   }

   @NotNull
   public final Annotation withV(@Nullable Integer v) {
      this.v = v;
      return this;
   }

   @NotNull
   public final Annotation withModifiedBy(@NotNull ModifiedBy modifiedBy) {
            this.modifiedBy = modifiedBy;
      return this;
   }

   @NotNull
   public String toString() {
      return "annotation: id:" + this.id;
   }
}
