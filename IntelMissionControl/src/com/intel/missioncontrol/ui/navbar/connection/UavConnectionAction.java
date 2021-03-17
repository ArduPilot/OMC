/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import eu.mavinci.plane.IAirplane;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public interface UavConnectionAction {

    UavConnection connectTo(UnmannedAerialVehicle uav, IAirplane plane) throws Exception;
    ObjectProperty<ConnectionState> connectionStateObjectProperty();
    StringProperty connectionErrorProperty();
    void disconnect();
}
