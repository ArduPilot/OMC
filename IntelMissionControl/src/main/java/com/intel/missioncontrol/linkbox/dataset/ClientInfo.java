/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.linkbox.dataset;

import com.google.gson.annotations.SerializedName;

public class ClientInfo {

    @SerializedName("name")
    public String name;

    @SerializedName("authorized")
    public Boolean authorized;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAuthorized() {
        return authorized;
    }

    public void setAuthorized(Boolean authorized) {
        this.authorized = authorized;
    }

    @Override
    public String toString() {
        return "{" + "\"name\" :" + "\"" + name + "\"" + ", " + "\"authorized\" :" + "\"" + authorized + "\"" + " }";
    }
}
