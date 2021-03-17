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


public final class Annotation3D {
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

    @SerializedName("created_by")
    @Expose
    @Nullable
    private CreatedBy createdBy;

    @SerializedName("created_date")
    @Expose
    @Nullable
    private String createdDate;

    @SerializedName("modified_date")
    @Expose
    @Nullable
    private String modifiedDate;

    @SerializedName("__v")
    @Expose
    @Nullable
    private Integer v;

    @SerializedName("modified_by")
    @Expose
    @Nullable
    private ModifiedBy modifiedBy;

    @SerializedName("annotation_parameters_id")
    @Expose
    @Nullable
    private String annotationParametersId;

    @Nullable
    private AnnotationParameters annotationParameters;

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
    public final String getAnnotationParametersId() {
        return this.annotationParametersId;
    }

    public final void setAnnotationParametersId(@Nullable String var1) {
        this.annotationParametersId = var1;
    }

    @Nullable
    public final AnnotationParameters getAnnotationParameters() {
        return this.annotationParameters;
    }

    public final void setAnnotationParameters(@Nullable AnnotationParameters var1) {
        this.annotationParameters = var1;
    }

    @NotNull
    public final Annotation3D withId(@NotNull String id) {
                this.id = id;
        return this;
    }

    @NotNull
    public final Annotation3D withTarget(@NotNull Target target) {
                this.target = target;
        return this;
    }

    @NotNull
    public final Annotation3D withFeature(@NotNull Feature feature) {
                this.feature = feature;
        return this;
    }

    @NotNull
    public final Annotation3D withProjectId(@NotNull String projectId) {
                this.projectId = projectId;
        return this;
    }

    @NotNull
    public final Annotation3D withCreatedBy(@NotNull CreatedBy createdBy) {
                this.createdBy = createdBy;
        return this;
    }

    @NotNull
    public final Annotation3D withCreatedDate(@NotNull String createdDate) {
                this.createdDate = createdDate;
        return this;
    }

    @NotNull
    public final Annotation3D withModifiedDate(@NotNull String modifiedDate) {
                this.modifiedDate = modifiedDate;
        return this;
    }

    @NotNull
    public final Annotation3D withV(@Nullable Integer v) {
        this.v = v;
        return this;
    }

    @NotNull
    public final Annotation3D withModifiedBy(@NotNull ModifiedBy modifiedBy) {
                this.modifiedBy = modifiedBy;
        return this;
    }

    @NotNull
    public String toString() {
        return "Annotation3D: id:" + this.id;
    }
}
