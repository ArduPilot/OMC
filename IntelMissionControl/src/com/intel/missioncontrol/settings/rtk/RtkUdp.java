/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings.rtk;

import com.intel.missioncontrol.settings.Serializable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/** @author Vladimir Iordanov */
@Serializable
public class RtkUdp {

    public static final int UDP_DEFAULT_VALUE = 3784;
    public static final int UDP_MIN_VALUE = 0;
    public static final int UDP_MAX_VALUE = 65535;

    private final ObjectProperty<Integer> port = new SimpleObjectProperty<>(UDP_DEFAULT_VALUE);

    public int getPort() {
        return port.get();
    }

    public ObjectProperty<Integer> portProperty() {
        return port;
    }
}
