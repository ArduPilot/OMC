/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic.dto;



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Feature {

    public Feature(Geometry geometry, Integer id, AittrafficObjectProperties properties) {
        this.geometry = geometry;
        this.id = id;
        this.properties = properties;
    }

    @SerializedName("geometry")
    @Expose
    public Geometry geometry;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("properties")
    @Expose
    public AittrafficObjectProperties properties;

}