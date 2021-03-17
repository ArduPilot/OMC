/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("bbox")
    @Expose
    var bbox: List<Double> = ArrayList()
    @SerializedName("geometry")
    @Expose
    var geometry: Coordinates? = null

}
