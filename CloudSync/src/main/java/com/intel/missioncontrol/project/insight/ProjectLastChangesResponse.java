/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.insight;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProjectLastChangesResponse {
    @SerializedName("latestTimestamp")
    @Expose
    private String latestTimestamp;

    public String getLatestTimestamp() {
        return latestTimestamp;
    }

    public void setLatestTimestamp(String latestTimestamp) {
        this.latestTimestamp = latestTimestamp;
    }
}
