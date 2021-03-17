/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.rtk;

import com.intel.missioncontrol.linkbox.LinkBoxGnssState;
import gov.nasa.worldwind.geom.Position;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface IRTKStation {

    ReadOnlyAsyncObjectProperty<LinkBoxGnssState> getGnssState();

    ReadOnlyAsyncIntegerProperty getNumberOfSatellites();

    ReadOnlyAsyncObjectProperty<Position> getRTKStationPosition();
}
