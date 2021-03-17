/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Image {

    @SerializedName("vertical_srs_wkt")
    @Expose
    var verticalSrsWkt: String? = null
    @SerializedName("horizontal_srs_wkt")
    @Expose
    var horizontalSrsWkt: String? = null

    fun withVerticalSrsWkt(verticalSrsWkt: String): Image {
        this.verticalSrsWkt = verticalSrsWkt
        return this
    }

    fun withHorizontalSrsWkt(horizontalSrsWkt: String): Image {
        this.horizontalSrsWkt = horizontalSrsWkt
        return this
    }

}
