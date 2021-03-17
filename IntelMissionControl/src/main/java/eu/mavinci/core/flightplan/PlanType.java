/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.Localizable;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import java.util.LinkedList;
import org.asyncfx.beans.property.AsyncObjectProperty;

public enum PlanType implements Localizable {
    POLYGON,
    CORRIDOR,
    CITY,
    SPIRAL,
    COPTER3D,
    STAR,
    TOWER,
    BUILDING,
    FACADE,
    MANUAL,
    GEOFENCE_POLY,
    NO_FLY_ZONE_POLY,
    GEOFENCE_CIRC,
    NO_FLY_ZONE_CIRC,
    POINT_OF_INTEREST,
    PANORAMA,
    WINDMILL,
    SEARCH,
    INSPECTION_POINTS;

    public static PlanType DEFAULT = POLYGON;
    static AsyncObjectProperty<OperationLevel> operationLevel =
        DependencyInjector.getInstance()
            .getInstanceOf(ISettingsManager.class)
            .getSection(GeneralSettings.class)
            .operationLevelProperty();

    public boolean isSelectable(Boolean camSupportsCopter, boolean allowIdenticalPoints) {
        switch (this) {
        case TOWER:
        case BUILDING:
        case FACADE:
        case POINT_OF_INTEREST:
        case SEARCH:
            return camSupportsCopter == null || camSupportsCopter.booleanValue();

        case PANORAMA:
            return allowIdenticalPoints && (camSupportsCopter == null || camSupportsCopter.booleanValue());

        case SPIRAL:
        case STAR:
        case MANUAL:
            return operationLevel.get() == OperationLevel.DEBUG;

        case COPTER3D:
        case WINDMILL:
        case INSPECTION_POINTS:
        case GEOFENCE_POLY:
        case NO_FLY_ZONE_POLY:
        case GEOFENCE_CIRC:
        case NO_FLY_ZONE_CIRC:
            return (camSupportsCopter == null || camSupportsCopter.booleanValue())
                && operationLevel.get() == OperationLevel.DEBUG;
        default:
            return true;
        }
    }

    public boolean isClosedPolygone() {
        switch (this) {
        case CORRIDOR:
        case FACADE:
            return false;
        default:
            return true;
        }
    }

    public AltitudeAdjustModes getNeededAltMode() {
        if (this == COPTER3D || this == INSPECTION_POINTS) {
            return AltitudeAdjustModes.FOLLOW_TERRAIN;
        }

        return null;
    }

    public boolean canOptimizeYawForTerrain() {
        return getNeededAltMode() != AltitudeAdjustModes.CONSTANT_OVER_R;
    }

    public boolean canOptimizeYawForTime() {
        return getMinCorners() > 2;
    }

    public boolean canDoMultiPlans() {
        return this == POLYGON || this == CITY || this == CORRIDOR;
    }

    public boolean needsWidth() {
        return this == SPIRAL
            || this == CORRIDOR
            || this == STAR
            || this == TOWER
            || this == POINT_OF_INTEREST
            || this == WINDMILL
            || this == SEARCH;
    }

    public boolean needsHeights() {
        return this == TOWER
            || this == FACADE
            || this == BUILDING
            || this == PANORAMA
            || this == POINT_OF_INTEREST
            || this == WINDMILL;
    }

    public boolean needsCeiling() {
        return this == TOWER || this == BUILDING || this == FACADE;
    }

    public boolean shouldPermuteLines() {
        return this == POLYGON || this == CITY || this == CORRIDOR;
    }

    public boolean canOptimizeCorners() {
        return this == POLYGON || this == CITY || this == SPIRAL;
    }

    public int getMinCorners() {
        switch (this) {
        case MANUAL:
        case INSPECTION_POINTS:
            return 0;
        case SPIRAL:
        case STAR:
        case TOWER:
        case PANORAMA:
        case POINT_OF_INTEREST:
        case NO_FLY_ZONE_CIRC:
        case GEOFENCE_CIRC:
        case WINDMILL:
        case SEARCH:
            return 1;
        case CORRIDOR:
        case FACADE:
            return 2;
        default:
            return 3;
        }
    }

    public boolean onlySingleCorner() {
        return getMinCorners() == 1;
    }

    public static LinkedList<PlanType> getSelectableValues(boolean camIsInCopterMode, boolean allowIdenticalPoints) {
        LinkedList<PlanType> types = new LinkedList<PlanType>();
        for (PlanType type : values()) {
            if (type.isSelectable(camIsInCopterMode, allowIdenticalPoints)) {
                types.add(type);
            }
        }

        return types;
    }

    public boolean isCircular() {
        return (this == SPIRAL)
            || (this == TOWER)
            || (this == POINT_OF_INTEREST)
            || (this == PANORAMA)
            || this == NO_FLY_ZONE_CIRC
            || this == GEOFENCE_CIRC
            || this == WINDMILL
            || this == SEARCH;
    }

    public boolean hasCircleDirection() {
        return isCircular() || (this == BUILDING);
    }

    public boolean supportsScanDirection() {
        return (this == POLYGON)
            || (this == CITY)
            || (this == SPIRAL)
            || (this == CORRIDOR)
            || (this == BUILDING)
            || (this == TOWER)
            || (this == FACADE);
    }

    public boolean supportsSingleDirection() {
        return (this == POLYGON) || (this == CITY) || (this == SPIRAL) || (this == CORRIDOR);
    }

    public boolean supportsFlightDirection() {
        return (this == POLYGON)
            || (this == CITY)
            || (this == SPIRAL)
            || (this == BUILDING)
            || (this == TOWER)
            || (this == POINT_OF_INTEREST)
            || (this == PANORAMA)
            || this == WINDMILL
            || this == SEARCH;
    }

    public boolean supportsVerticalScanPatterns() {
        return (this == BUILDING) || (this == FACADE) || this == TOWER;
    }

    public boolean supportsSegmentScan() {
        return (this == BUILDING) || (this == FACADE);
    }

    public boolean supportsCrop() {
        return (this == TOWER || this == FACADE || this == BUILDING || this == COPTER3D) || this == WINDMILL;
    }

    public boolean supportsCoverageComputation() {
        return (this == POLYGON) || (this == CITY) || (this == SPIRAL) || (this == CORRIDOR) || (this == SEARCH);
    }

    public boolean hasDimensions() {
        return ((this != POLYGON) && (this != CITY));
    }

    public boolean useStartCaptureVertically() {
        return this == TOWER || this == BUILDING || this == FACADE;
    }

    public boolean supportMaxTiltChange() {
        return this == FACADE || this == BUILDING || this == TOWER;
    }

    public boolean supportMaxRollYawChange() {
        return this == FACADE || this == BUILDING || this == TOWER;
    }

    public boolean needsImagesPerCircle() {
        return isCircular() && this != SEARCH && this != PANORAMA && this != POINT_OF_INTEREST;
    }

    public boolean needsCircles() {
        return isCircular() && this != PANORAMA && this != POINT_OF_INTEREST;
    }

    public boolean doAutoComputation() {
        return hasWaypoints() && this != MANUAL;
    }

    public boolean hasWaypoints() {
        return !(this == GEOFENCE_CIRC
            || this == GEOFENCE_POLY
            || this == NO_FLY_ZONE_CIRC
            || this == NO_FLY_ZONE_POLY);
    }

    public boolean isNoFlyZone() {
        return this == NO_FLY_ZONE_CIRC || this == NO_FLY_ZONE_POLY;
    }

    public boolean isGeofence() {
        return this == GEOFENCE_CIRC || this == GEOFENCE_POLY;
    }

    public boolean needsTranformation() {
        return this == COPTER3D;
    }

    public boolean keepPointOnTargetContantOnRotations() {
        return this != PANORAMA;
    }
}
