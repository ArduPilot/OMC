/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Flight {

    @SerializedName("data")
    @Expose
    var data: Data? = null
    @SerializedName("cameras")
    @Expose
    var cameras: List<String> = ArrayList()
    @SerializedName("number_of_photos")
    @Expose
    var numberOfPhotos: Int? = null
    @SerializedName("status")
    @Expose
    var status: Status? = null
    @SerializedName("_id")
    @Expose
    var id: String? = "-1"
    @SerializedName("project")
    @Expose
    var project: String? = null
    @SerializedName("mission")
    @Expose
    var mission: String? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("survey_date")
    @Expose
    var surveyDate: String? = null
    @SerializedName("created")
    @Expose
    var created: String? = null
    @SerializedName("user")
    @Expose
    var user: User? = null

}
