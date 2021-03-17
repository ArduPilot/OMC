/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Inspection {

    @SerializedName("video")
    @Expose
    var video: Boolean? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Inspection

        if (video != other.video) return false

        return true
    }

    override fun hashCode(): Int {
        return video?.hashCode() ?: 0
    }


}
/*
"{\n" +
"  \"name\": \"cli_test_2\",\n" +
"  \"addProjectToUsers\": true,\n" +
"  \"industry\": \"Geospatial\",\n" +
"  \"geometry\": {\n" +
"    \"type\": \"GeometryCollection\",\n" +
"    \"geometries\": [\n" +
"      {\n" +
"        \"type\": \"Polygon\",\n" +
"        \"coordinates\": [\n" +
"          [\n" +
"            [\n" +
"              23.864491681523386,\n" +
"              61.452707086048974\n" +
"            ],\n" +
"            [\n" +
"              23.865460152914082,\n" +
"              61.452707086048974\n" +
"            ],\n" +
"            [\n" +
"              23.865460152914082,\n" +
"              61.453012636318704\n" +
"            ],\n" +
"            [\n" +
"              23.864491681523386,\n" +
"              61.453012636318704\n" +
"            ],\n" +
"            [\n" +
"              23.864491681523386,\n" +
"              61.452707086048974\n" +
"            ]\n" +
"          ]\n" +
"        ]\n" +
"      }\n" +
"    ]\n" +
"  },\n" +
"  \"area\": 1752.3959482859652,\n" +
"  \"processSettings\": {\n" +
"    \"mapType\": \"\",\n" +
"    \"analytics\": [],\n" +
"    \"inspection\": {\n" +
"      \"video\": false\n" +
"    }\n" +
"  },\n" +
"  \"survey_date\": \"2018-04-12T00:00:00.000Z\",\n" +
"  \"number_of_photos\": 5,\n" +
"  \"cameras\": [\n" +
"    {\n" +
"      \"model\": \"SONY_UMC-R10C_24\",\n" +
"      \"width\": 5456,\n" +
"      \"height\": 3632,\n" +
"      \"fnumber\": 8,\n" +
"      \"focal_length\": 24,\n" +
"      \"aspect_ratio\": 1.502202643171806\n" +
"    }\n" +
"  ]\n" +
"}"*/