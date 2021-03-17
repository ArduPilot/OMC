/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Photogrammetry {

    @SerializedName("analytics")
    @Expose
    var analytics: String? = null
    @SerializedName("_id")
    @Expose
    var id: String? = null
    @SerializedName("parameters")
    @Expose
    var parameters: Parameters? = null

    fun withAnalytics(analytics: String): Photogrammetry {
        this.analytics = analytics
        return this
    }

    fun withId(id: String): Photogrammetry {
        this.id = id
        return this
    }

    fun withParameters(parameters: Parameters): Photogrammetry {
        this.parameters = parameters
        return this
    }

}
