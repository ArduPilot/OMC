/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

public enum LayerGroupType {
    BASE_MAPS_GROUP("%" + LayerGroupType.class.getName() + ".baseMaps", true),
    AIRSPACES_GROUP("%" + LayerGroupType.class.getName() + ".airspaces", false),
    KML_SHP_GROUP("%" + LayerGroupType.class.getName() + ".kmlShp", true),
    GEOTIFF_GROUP("%" + LayerGroupType.class.getName() + ".geoTiff", true),
    WMS_SERVER_GROUP("%" + LayerGroupType.class.getName() + ".wms", true),
    LINES_AND_GRIDS_GROUP("%" + LayerGroupType.class.getName() + ".linesAndGrids", false),
    AIRCRAFT_GROUP("%" + LayerGroupType.class.getName() + ".aircraft", false),
    FLIGHT_PLAN_GROUP("%" + LayerGroupType.class.getName() + ".flightPlan", false),
    DATASET_GROUP("%" + LayerGroupType.class.getName() + ".dataset", false);

    private final String name;
    private final boolean isBackground;

    LayerGroupType(String airspaceGroupName, boolean isBackground) {
        this.name = airspaceGroupName;
        this.isBackground = isBackground;
    }

    public String getName() {
        return name;
    }

    public boolean isBackground() {
        return isBackground;
    }
}
