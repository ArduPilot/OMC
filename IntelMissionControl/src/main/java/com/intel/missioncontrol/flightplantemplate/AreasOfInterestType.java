/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.flightplantemplate;

import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Expect;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.obfuscation.IKeepAll;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum AreasOfInterestType implements IKeepAll {
    AOI_2D(PlanType.POLYGON, PlanType.CITY, PlanType.CORRIDOR, PlanType.SPIRAL, PlanType.SEARCH),
    AOI_3D(
        PlanType.TOWER,
        PlanType.BUILDING,
        PlanType.FACADE,
        PlanType.POINT_OF_INTEREST,
        PlanType.PANORAMA,
        PlanType.COPTER3D,
        PlanType.WINDMILL),
    AOI_OTHER(PlanType.NO_FLY_ZONE_POLY, PlanType.GEOFENCE_CIRC, PlanType.GEOFENCE_POLY, PlanType.MANUAL, PlanType.INSPECTION_POINTS),
    AOI_2D_COPTER(PlanType.POLYGON, PlanType.CITY, PlanType.CORRIDOR, PlanType.SPIRAL, PlanType.SEARCH);

    private final List<PlanType> areasOfInterests;

    AreasOfInterestType(PlanType... areasOfInterests) {
        this.areasOfInterests = new ArrayList<>(areasOfInterests.length);

        for (var areaOfInterest : areasOfInterests) {
            if (areaOfInterest.isSelectable(null, true)) {
                this.areasOfInterests.add(areaOfInterest);
            }
        }
    }

    public List<PlanType> getAreasOfInterests() {
        return areasOfInterests;
    }

    public boolean contains(PlanType planType) {
        Expect.notNull(planType, "planType");
        return areasOfInterests.contains(planType);
    }

    private static List<PlanType> filter(List<PlanType> list, IPlatformDescription platformDescription) {
        ArrayList<PlanType> out = new ArrayList<>(list.size());
        for (PlanType planType : list) {
            if (planType.isSelectable(
                    platformDescription.isInCopterMode(),
                    platformDescription.getMinWaypointSeparation().getValue().doubleValue() == 0)) {
                out.add(planType);
            }
        }

        return out;
    }

    public static Map<AreasOfInterestType, List<PlanType>> forPlatform(IPlatformDescription platformDescription) {
        Map<AreasOfInterestType, List<PlanType>> result = new LinkedHashMap<>();

        if (platformDescription.isInCopterMode()) {
            result.put(AOI_2D_COPTER, filter(AOI_2D_COPTER.getAreasOfInterests(), platformDescription));
            result.put(AOI_3D, filter(AOI_3D.getAreasOfInterests(), platformDescription));
        } else {
            result.put(AOI_2D, filter(AOI_2D.getAreasOfInterests(), platformDescription));
        }

        result.put(AOI_OTHER, filter(AOI_OTHER.getAreasOfInterests(), platformDescription));
        return result;
    }

}
