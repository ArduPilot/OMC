/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.airmap.airmapsdk.networking.services;

import java.io.Serializable;

public enum AirMapGeometryFormat implements Serializable {
        WKT("wkt"),
        GeoJSON("geojson");

        private final String text;

        private AirMapGeometryFormat(String text) {
            this.text = text;
        }

        public String toString() {
            return this.text;
        }
}

