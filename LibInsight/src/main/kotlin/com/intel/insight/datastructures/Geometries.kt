/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Geometries {

    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("geometries")
    @Expose
    var geometries: MutableList<Coordinates> = ArrayList()

}
