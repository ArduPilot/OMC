/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncDoubleProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface IGnssInfo {;
    default GnssState getGnssState() {
        return gnssStateProperty().get();
    }

    ReadOnlyAsyncObjectProperty<GnssState> gnssStateProperty();

    ReadOnlyAsyncDoubleProperty qualityPercentageProperty();

    default double getQuality() {
        return qualityPercentageProperty().get();
    }

    /**
     * Number of currently visible GNSS satellites. -1 if unknown.
     */
    ReadOnlyAsyncIntegerProperty numberOfSatellitesProperty();

    /**
     * Number of currently visible GNSS satellites. -1 if unknown.
     */
    default double getNumberOfSatellites() {
        return numberOfSatellitesProperty().get();
    }

    ReadOnlyAsyncBooleanProperty telemetryOldProperty();
}
