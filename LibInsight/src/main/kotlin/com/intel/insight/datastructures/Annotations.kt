/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Annotations {

    @SerializedName("annotations")
    @Expose
    var annotations: List<Annotation> = ArrayList()

    fun withAnnotations(annotations: List<Annotation>): Annotations {
        this.annotations = annotations
        return this
    }

}
