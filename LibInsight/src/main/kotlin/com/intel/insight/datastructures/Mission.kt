/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Mission {

    @SerializedName("application")
    @Expose
    var application: Application? = null
    @SerializedName("delivery")
    @Expose
    var delivery: Delivery? = null
    @SerializedName("precision")
    @Expose
    var precision: Precision? = null
    @SerializedName("area")
    @Expose
    var area: Double? = null
    @SerializedName("length")
    @Expose
    var length: Int? = null
    @SerializedName("geometry")
    @Expose
    var geometry: Geometries? = null
    @SerializedName("real_bbox")
    @Expose
    var realBbox: RealBbox? = null
    @SerializedName("flights")
    @Expose
    var flights: List<String> = ArrayList()
    @SerializedName("dxobjects")
    @Expose
    var dxobjects: List<Any> = ArrayList()
    @SerializedName("status")
    @Expose
    var status: Status? = null
    @SerializedName("deliverables")
    @Expose
    var deliverables: List<Any> = ArrayList()
    @SerializedName("_id")
    @Expose
    var id: String? = "-1"
    @SerializedName("project")
    @Expose
    var project: String? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("survey_date")
    @Expose
    var surveyDate: String? = null
    @SerializedName("processSettings")
    @Expose
    var processSettings: ProcessSettings? = null
    @SerializedName("created")
    @Expose
    var created: String? = null
    @SerializedName("user")
    @Expose
    var user: User? = null
    @SerializedName("modification_date")
    @Expose
    var modificationDate: String? = null
    @SerializedName("modification_user")
    @Expose
    var modificationUser: ModificationUser? = null

}
