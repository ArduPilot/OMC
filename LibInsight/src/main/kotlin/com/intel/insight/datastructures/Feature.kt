/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Feature {

    @SerializedName("geometry")
    @Expose
    var geometry: Geometry? = null
    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("properties")
    @Expose
    var properties: Properties? = null

    fun withGeometry(geometry: Geometry): Feature {
        this.geometry = geometry
        return this
    }

    fun withType(type: String): Feature {
        this.type = type
        return this
    }

    fun withProperties(properties: Properties): Feature {
        this.properties = properties
        return this
    }

}
