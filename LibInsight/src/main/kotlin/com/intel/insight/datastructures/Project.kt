/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class Project {

    @SerializedName("_id")
    @Expose
    var id: String? = "-1"
    @SerializedName("companyId")
    @Expose
    var companyId: String? = null
    @SerializedName("created")
    @Expose
    var created: String? = null
    @SerializedName("deliverables")
    @Expose
    var deliverables: List<Any> = ArrayList()
    @SerializedName("dxobjects")
    @Expose
    var dxobjects: List<Any> = ArrayList()
    @SerializedName("geometry")
    @Expose
    var geometry: Geometries? = null
    @SerializedName("industry")
    @Expose
    var industry: String? = null
    @SerializedName("missions")
    @Expose
    var missions: List<String> = ArrayList()
    @SerializedName("modification_date")
    @Expose
    var modificationDate: String? = null
    @SerializedName("modification_user")
    @Expose
    var modificationUser: ModificationUser? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("place_name")
    @Expose
    var placeName: String? = null
    @SerializedName("preview")
    @Expose
    var preview: String? = null
    @SerializedName("real_bbox")
    @Expose
    var realBbox: RealBbox? = null
    @SerializedName("units")
    @Expose
    var units: Units? = null
    @SerializedName("user")
    @Expose
    var user: User? = null
    @SerializedName("horizontal_srs_wkt")
    @Expose
    var horizontalSrsWkt: String? = null
    @SerializedName("vertical_srs_wkt")
    @Expose
    var verticalSrsWkt: String? = null

    override fun toString(): String {
        return "project: id:" + id + " name:" + name
    }
}
