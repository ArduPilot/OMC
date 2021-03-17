/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;

public class ElevationModelShiftWrapperEGM extends AElevationModelWrapper {

    private final IEgmModel egm;

    public ElevationModelShiftWrapperEGM(ElevationModel slave, IEgmModel egm) {
        super(slave);
        this.egm = egm;
    }

    @Override
    public double shift(double slaveAlt, Angle latitude, Angle longitude) {
        if (latitude == null) {
            return slaveAlt;
        }

        return egm.getEGM96Offset(new LatLon(latitude, longitude)) + slaveAlt;
    }

    @Override
    double getExtremeMinShift() {
        return -106;
    }

    @Override
    double getExtremeMaxShift() {
        return 85;
    }

    @Override
    public String toString() {
        return "EGsubstracter-Wrapper around:" + slave;
    }

    @Override
    public double getBestResolution(Sector sector) {
        if (sector == null) {
            return 1e-4; // fake crap accuracy. this is an bug fix workaround: if we are asking the SRTM model without
            // specifying a sector,
        }
        // it pretend to be very precisely
        return super.getBestResolution(sector);
    }

}
