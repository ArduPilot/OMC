/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.data;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.UUID;

// TODO: wtf, make AirSpaceObject an interface or something...
public class AirSpaceBase extends AirSpaceObject {




    public static class Related {
        UUID id;
        GeoJson.Geometry geometry;
    }

    @SerializedName("related_geometry")
    public Map<String, Related> relatedGeometry;


    static final String KEY_PROPERTY_BOUNDARY = "property_boundary";

    public GeoJson.Geometry getPropertyBoundary() {
        if (relatedGeometry != null) {
            Related related = relatedGeometry.get(KEY_PROPERTY_BOUNDARY);
            return related == null ? null : related.geometry;
        } else {
            return null;
        }
    }

}
