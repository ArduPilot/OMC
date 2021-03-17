/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProcessSettings {
    @SerializedName("tools_set")
    @Expose
    var toolsSet: String? = null
    @SerializedName("mapType")
    @Expose
    var mapType: String? = null
    @SerializedName("analytics")
    @Expose
    var analytics: List<Any> = ArrayList()
    @SerializedName("inspection")
    @Expose
    var inspection: Inspection? = null
    @SerializedName("mapping")
    @Expose
    var mapping: Mapping? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProcessSettings

        if (mapType != other.mapType) return false
        if (analytics != other.analytics) return false
        if (inspection != other.inspection) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mapType?.hashCode() ?: 0
        result = 31 * result + analytics.hashCode()
        result = 31 * result + (inspection?.hashCode() ?: 0)
        return result
    }

}
