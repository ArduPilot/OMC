/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight.primitives;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public final class Inspection {
    @SerializedName("video")
    @Expose
    @Nullable
    private Boolean video;

    @Nullable
    public final Boolean getVideo() {
        return this.video;
    }

    public final void setVideo(@Nullable Boolean var1) {
        this.video = var1;
    }

}
