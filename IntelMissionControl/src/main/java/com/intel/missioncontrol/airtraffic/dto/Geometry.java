/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic.dto;


import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Geometry {

    public Geometry(List<Double> coordinates, String type) {
        this.coordinates = coordinates;
        this.type = type;
    }

    @SerializedName("coordinates")
    @Expose
    public List<Double> coordinates = new ArrayList<Double>();
    @SerializedName("type")
    @Expose
    public String type;

}
