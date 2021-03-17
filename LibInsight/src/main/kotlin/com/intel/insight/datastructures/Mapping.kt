/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.intel.insight.RawJsonAdapter

class Mapping {

    @SerializedName("preset")
    @Expose
    var preset: String? = null
    @SerializedName("mesh")
    @Expose
    var mesh: Boolean? = null
    @SerializedName("processingAreaSetting")
    @Expose
    var processingAreaSetting: String? = null
    @SerializedName("crs")
    @Expose
    var crs: Crs? = null

    @SerializedName("photogrammetry_params")
    @Expose
    @JsonAdapter(RawJsonAdapter::class)
    var photogrammetry_params: String? = null

    fun withPreset(preset: String): Mapping {
        this.preset = preset
        return this
    }

    fun withMesh(mesh: Boolean?): Mapping {
        this.mesh = mesh
        return this
    }

    fun withProcessingAreaSetting(processingAreaSetting: String): Mapping {
        this.processingAreaSetting = processingAreaSetting
        return this
    }

    fun withCrs(crs: Crs): Mapping {
        this.crs = crs
        return this
    }

}
