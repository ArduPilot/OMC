/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.linkbox.dataset;

import com.google.gson.annotations.SerializedName;

public class GpTelemetryMessage {

    @SerializedName("gpTelemetry")
    GpTelemetryData gpTelemetryData;

    @SerializedName("clientInfo")
    ClientInfo clientInfo;

    public GpTelemetryData getGpTelemetryData() {
        return gpTelemetryData;
    }

    public void setGpTelemetryData(GpTelemetryData gpTelemetryData) {
        this.gpTelemetryData = gpTelemetryData;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    @Override
    public String toString() {
        return "{ "
            + "\"gpTelemetry\" :"
            + gpTelemetryData.toString()
            + ", "
            + "\"clientInfo\" : "
            + clientInfo.toString()
            + " }";
    }

}
