/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CreatedProject {

    @SerializedName("project")
    @Expose
    var project: Project? = Project()
    @SerializedName("mission")
    @Expose
    var mission: Mission? = Mission()
    @SerializedName("flight")
    @Expose
    var flight: Flight? = Flight()
    @SerializedName("cameras")
    @Expose
    var cameras: List<Camera> = ArrayList()

}
