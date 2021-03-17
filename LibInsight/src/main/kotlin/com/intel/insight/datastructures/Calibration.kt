/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures
import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Calibration {

    @SerializedName("area")
    @Expose
    var area: List<Any> = ArrayList()

    fun withArea(area: List<Any>): Calibration {
        this.area = area
        return this
    }

}
