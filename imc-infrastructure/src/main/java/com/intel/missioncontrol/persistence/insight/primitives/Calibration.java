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

public final class Calibration {
    @SerializedName("area")
    @Expose
    @NotNull
    private List area = new ArrayList();

    @NotNull
    public final List getArea() {
        return this.area;
    }

    public final void setArea(@NotNull List var1) {
        this.area = var1;
    }

    @NotNull
    public final Calibration withArea(@NotNull List area) {
        this.area = area;
        return this;
    }
}
