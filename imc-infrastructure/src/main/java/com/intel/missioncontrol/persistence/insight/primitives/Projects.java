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

public final class Projects {

    @SerializedName("projects")
    @Expose
    @NotNull
    private List<Project> projects = new ArrayList();

    @NotNull
    public final List<Project> getProjects() {
        return this.projects;
    }

    public final void setProjects(@NotNull List var1) {
        this.projects = var1;
    }
}
