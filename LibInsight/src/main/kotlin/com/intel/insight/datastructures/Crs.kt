/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Crs {

    @SerializedName("output")
    @Expose
    var output: Output? = null
    @SerializedName("gcp")
    @Expose
    var gcp: Gcp? = null
    @SerializedName("image")
    @Expose
    var image: Image? = null

    fun withOutput(output: Output): Crs {
        this.output = output
        return this
    }

    fun withGcp(gcp: Gcp): Crs {
        this.gcp = gcp
        return this
    }

    fun withImage(image: Image): Crs {
        this.image = image
        return this
    }

}
