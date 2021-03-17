/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.data;

public class ControlledAirspaceObject extends AirSpaceBase {

    public Properties properties;

    static class Properties {
        public String url;
//        public boolean laanc;
        public String airport_id;
        public String airport_name;
        public String airspace_class;
        public String airspace_classification;
        public boolean authorization;
        public double ceiling;
    }

    public String getAirspaceClass() {
        if (properties == null) return null;

        if (properties.airspace_class != null) {
            return properties.airspace_class.toUpperCase();
        } else if (properties.airspace_classification != null){
            return properties.airspace_classification.toUpperCase();
        } else {
            return null;
        }
    }


}
