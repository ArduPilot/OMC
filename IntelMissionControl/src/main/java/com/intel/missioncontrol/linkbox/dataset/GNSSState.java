/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.linkbox.dataset;

import com.google.gson.annotations.SerializedName;
import com.intel.missioncontrol.linkbox.LinkBoxGnssState;

public class GNSSState {

    @SerializedName("status")
    public String Status;

    @SerializedName("longitude")
    public float Longitude;

    @SerializedName("latitude")
    public float Latitude;

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public float getLongitude() {
        return Longitude;
    }

    public void setLongitude(float longitude) {
        Longitude = longitude;
    }

    public float getLatitude() {
        return Latitude;
    }

    public void setLatitude(float latitude) {
        Latitude = latitude;
    }

    @Override
    public String toString() {
        return "{ "
            + "\"status\" :"
                + "\""+ Status+ "\""
            + ", "
            + "\"longitude\" :"
                + "\""+ Longitude+ "\""
            + ", "
            + "\"latitude\" :"
                + "\""+ Latitude+ "\""
            + " }";
    }

    public LinkBoxGnssState getLinkBoxGnssState(){
        return LinkBoxGnssState.valueOf(Status.trim().toUpperCase());
    }
}
