/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings.rtk;

import com.intel.missioncontrol.settings.Serializable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** @author Vladimir Iordanov */
@Serializable
public class RtkHttpConnection {
    private final StringProperty url = new SimpleStringProperty();
    private final IntegerProperty port = new SimpleIntegerProperty(80);
    private final StringProperty user = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public int getPort() {
        return port.get();
    }

    public IntegerProperty portProperty() {
        return port;
    }

    public String getUser() {
        return user.get();
    }

    public StringProperty userProperty() {
        return user;
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }
}
