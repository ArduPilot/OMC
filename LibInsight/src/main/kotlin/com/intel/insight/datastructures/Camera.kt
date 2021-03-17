/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.ArrayList

class Camera {

    @SerializedName("model")
    @Expose
    var model: String? = null
    @SerializedName("width")
    @Expose
    var width: Int? = null
    @SerializedName("height")
    @Expose
    var height: Int? = null
    @SerializedName("fnumber")
    @Expose
    var fnumber: Int? = null
    @SerializedName("focal_length")
    @Expose
    var focalLength: Int? = null
    @SerializedName("aspect_ratio")
    @Expose
    var aspectRatio: Double? = null


    @SerializedName("calibration")
    @Expose
    var calibration: List<Any> = ArrayList()
    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("created")
    @Expose
    var created: String? = null


}
