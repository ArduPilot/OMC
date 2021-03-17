/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Geometry {

    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("coordinates")
    @Expose
    var coordinates: List<List<List<Double>>> = ArrayList()

    fun withType(type: String): Geometry {
        this.type = type
        return this
    }

    fun withCoordinates(coordinates: List<List<List<Double>>>): Geometry {
        this.coordinates = coordinates
        return this
    }

}
