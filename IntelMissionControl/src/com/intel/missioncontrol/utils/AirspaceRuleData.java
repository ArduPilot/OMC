/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import javafx.beans.property.BooleanProperty;


public class AirspaceRuleData {

    // Constructor with parameters
    public AirspaceRuleData(String name, BooleanProperty booleanStatus){
        this.name = name;
        this.status = booleanStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStatus() {
        return status.get();
    }

    public BooleanProperty statusProperty() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status.set(status);
    }

    String name;
    BooleanProperty status;
}
