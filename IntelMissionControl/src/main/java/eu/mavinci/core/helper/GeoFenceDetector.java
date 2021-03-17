/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

public class GeoFenceDetector implements IGeoFenceDetector {
    boolean isGeoFencingRestrictionOn;

    @Override
    public boolean isGeoFencingRestrictionOn() {
        return isGeoFencingRestrictionOn;
    }

    @Override
    public void setGeoFencingRestrictionOn() {
        isGeoFencingRestrictionOn = true;
    }
}
