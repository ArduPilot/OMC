/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PhotoGeometry {

    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("coordinates")
    @Expose
    var coordinates: List<Double> = ArrayList()

    fun withType(type: String): PhotoGeometry {
        this.type = type
        return this
    }

    fun withCoordinates(coordinates: List<Double>): PhotoGeometry {
        this.coordinates = coordinates
        return this
    }

}
