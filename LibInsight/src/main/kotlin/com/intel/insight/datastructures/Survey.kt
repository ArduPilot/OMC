/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.insight.datastructures

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

/* Example
{
  "name": "cli_test",
  "addProjectToUsers": true,
  "industry": "Geospatial",
  "geometry": {
    "type": "GeometryCollection",
    "geometries": [
      {
        "type": "Polygon",
        "coordinates": [
          [
            [
              23.864491681523386,
              61.452707086048974
            ],
            [
              23.865460152914082,
              61.452707086048974
            ],
            [
              23.865460152914082,
              61.453012636318704
            ],
            [
              23.864491681523386,
              61.453012636318704
            ],
            [
              23.864491681523386,
              61.452707086048974
            ]
          ]
        ]
      }
    ]
  },
  "area": 1752.3959482859652,
  "processSettings": {
    "mapType": "",
    "analytics": [],
    "inspection": {
      "video": false
    }
  },
  "survey_date": "2018-04-12T00:00:00.000Z",
  "number_of_photos": 5,
  "cameras": [
    {
      "model": "SONY_UMC-R10C_24",
      "width": 5456,
      "height": 3632,
      "fnumber": 8,
      "focal_length": 24,
      "aspect_ratio": 1.502202643171806
    }
  ]
}
 */
class Survey {

    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("addProjectToUsers")
    @Expose
    var addProjectToUsers: Boolean? = null
    @SerializedName("industry")
    @Expose
    var industry: String? = null
    @SerializedName("geometry")
    @Expose
    var geometry: Geometries? = null
    @SerializedName("area")
    @Expose
    var area: Double? = null
    @SerializedName("processSettings")
    @Expose
    var processSettings: ProcessSettings? = null
    @SerializedName("survey_date")
    @Expose
    var surveyDate: String? = null
    @SerializedName("number_of_photos")
    @Expose
    var numberOfPhotos: Int? = null
    @SerializedName("cameras")
    @Expose
    var cameras: MutableList<Camera> = ArrayList()
    @SerializedName("horizontal_srs_wkt")
    @Expose
    private var horizontalSrsWkt: String? = null
    @SerializedName("vertical_srs_wkt")
    @Expose
    private var verticalSrsWkt: String? = null

}
