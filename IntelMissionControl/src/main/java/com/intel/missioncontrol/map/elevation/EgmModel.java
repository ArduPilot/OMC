/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.EGM96;

public class EgmModel implements IEgmModel {

    private final EGM96 egm96;

    public EgmModel() {
        try {
            egm96 = new EGM96("config/EGM96.dat");
        } catch (Exception e1) {
            throw new RuntimeException("could not load EGM96 geoID", e1);
        }
    }

    @Override
    public double getEGM96Offset(LatLon latLon) {
        return egm96 == null ? 0 : egm96.getOffset(latLon.latitude, latLon.longitude);
    }
}
