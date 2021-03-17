/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.geo;

import eu.mavinci.geo.CountryDetector.RadioRegulation;

public abstract class GeoRestriction {

    public boolean isRestricted; // this is false by default
    public boolean isEU; // this is false by default
    public CountryDetector.RadioRegulation radioRegulation;

    public GeoRestriction() {
        radioRegulation = CountryDetector.RadioRegulation.other;
    }

    public boolean isSimilarRegulated(GeoRestriction c) {
        // System.out.println(c.isRestricted +" "+ isRestricted +" "+
        // c.radioRegulation+" "+radioRegulation +" "+
        // dataQuality +" "+ c.dataQuality + " "+(c.isRestricted == isRestricted &&
        // c.radioRegulation.equals(radioRegulation) &&
        // dataQuality == c.dataQuality));
        return c.isRestricted == isRestricted && c.isEU == isEU && c.radioRegulation.equals(radioRegulation);
    }

    public void updateRestrictions(GeoRestriction c) {
        if (c.radioRegulation.ordinal() > radioRegulation.ordinal()) {
            radioRegulation = c.radioRegulation;
        }

        if (c.isRestricted) {
            isRestricted = true;
        }

        if (c.isEU) {
            isEU = true;
        }
    }

    public void overwriteRestrictions(GeoRestriction c) {
        radioRegulation = c.radioRegulation;
        isRestricted = c.isRestricted;
        isEU = c.isEU;
    }
}
