/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic.dto;

import java.util.List;

/**
 * Documentation for the AirAvionicsResponse can be found at https://endpointsportal.traffic-monitor-254907.cloud.goog/ or
 * by contacting Stephan Besser <stephan.besser@air-avionics.com> or Tobias Fetzer <tobias.fetzer@air-avionics.com>.
 * <p>
 * In summary, it contains a list of airtraffic objects that is returned by, e.g. a REST GET from the AirAvionicsClient that is intended
 * to be parsed from json by something like GSON.
 */
public class AirAvionicsResponse {
    List<AirtrafficObject> features;
    String type;

    public AirAvionicsResponse(List<AirtrafficObject> features, String type) {
        this.features = features;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public List<AirtrafficObject> getFeatures() {
        return features;
    }
}
