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

public final class ProcessSettings {
    @SerializedName("tools_set")
    @Expose
    @Nullable
    private String toolsSet;

    @SerializedName("mapType")
    @Expose
    @Nullable
    private String mapType;

    @SerializedName("analytics")
    @Expose
    @NotNull
    private List analytics = new ArrayList();

    @SerializedName("inspection")
    @Expose
    @Nullable
    private Inspection inspection;

    @SerializedName("mapping")
    @Expose
    @Nullable
    private Mapping mapping;

    @Nullable
    public final String getToolsSet() {
        return this.toolsSet;
    }

    public final void setToolsSet(@Nullable String var1) {
        this.toolsSet = var1;
    }

    @Nullable
    public final String getMapType() {
        return this.mapType;
    }

    public final void setMapType(@Nullable String var1) {
        this.mapType = var1;
    }

    @NotNull
    public final List getAnalytics() {
        return this.analytics;
    }

    public final void setAnalytics(@NotNull List var1) {
        this.analytics = var1;
    }

    @Nullable
    public final Inspection getInspection() {
        return this.inspection;
    }

    public final void setInspection(@Nullable Inspection var1) {
        this.inspection = var1;
    }

    @Nullable
    public final Mapping getMapping() {
        return this.mapping;
    }

    public final void setMapping(@Nullable Mapping var1) {
        this.mapping = var1;
    }

}
