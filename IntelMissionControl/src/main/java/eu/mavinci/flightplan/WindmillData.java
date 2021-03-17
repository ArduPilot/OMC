/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import gov.nasa.worldwind.geom.Matrix;
import java.util.ArrayList;

public class WindmillData {

    //    +
    //    |       windmillHubHalfLength?
    //    |       +----->
    //    |
    //    |
    //   +--------------+       ^
    //   |--------------|       + windmillHubRadius
    //   +--------------+
    //    |    |  |  |        ^
    //    |    |  |  |        |
    //    |    |  |  |        |
    //    |    |  |  |        |
    //    +    |  |  |        |
    //         |  |  |        |
    //         |  |  |        |
    //         |  |  |        |
    //         |  |  |        |
    //         |  |  |        |
    //         |  |  |        |
    //         |  |  |        |
    //         |  |  |        |
    //         +-----+        + windmillTowerHeight
    //
    //            +-->
    //            windmillTowerRadius
    //

    // hub is centered on tower
    // blades are mounted to hub, attached to hub one blade radius from end of hub

    //         +windmillBladeStartRotation
    //         |---->X
    // XXX     |    X
    //  XX     |   X
    //   XXX   |  X
    //    XXX  | X
    //      XX+-+
    //       X+-+
    //     XXX   XX
    //   XX       XX
    // XX          XXX
    // XX            X
    //

    // all the pieces: tower, hub, blades, are modeled as cylinders
    // the blades are modeled as elliptical cylinders, with
    // two radii (radius and thin radius) and a pitch (assumed locked)

    public double towerHeight; // meters
    public double towerRadius; // meters

    // hub/nacelle parameters
    public double hubYaw; // degs CCW from N (x)
    public double hubRadius; // meters
    public double hubHalfLength; // meters

    // blade parameters
    public int numberOfBlades; // count
    public double bladeLength; // meters
    public double bladeRadius; // meters
    public double bladeThinRadius; // meters
    public double bladePitch; // deg from parallel to hub yaw, into wind (+-90 degs)
    public double bladeStartRotation; // deg CCW from UP
    public double bladeStartLength; // meters
    public double distanceFromBlade; // meters, from gsd

    // WINDMILL parameters when UNLOCKED
    public boolean locked; // locked or not

    // windmill derived parameters
    public double totalHeight; // meters
    public double unlockedRadius; // meters

    // saved transforms for collision testing
    public Matrix centerTransform = null; // transform center point to origin
    public Matrix hubTransform = null; // transform points to centered, vertical hub
    public ArrayList<Matrix> bladeTransforms = new ArrayList<Matrix>(numberOfBlades); // transforms for individual blades

    public WindmillData() {
        // setup default WindmillData values
        towerHeight = 60; // meters
        towerRadius = 5; // meters
        hubYaw = 10; // degs CCW from N (x)
        hubRadius = 5; // meters
        hubHalfLength = 15; // meters
        numberOfBlades = 3; // count
        bladeLength = 50; // meters
        bladeRadius = 5; // meters
        bladeThinRadius = 1; // meters
        bladePitch = 0; // "feathered" position, typical when locked for inspection
        bladeStartRotation = 0; // deg CCW from UP
        bladeStartLength = 20; // meters
        distanceFromBlade = 10; // meters, from gsd
        locked = true; // boolean

        updateDerivedValues();
    }

    public WindmillData(
            double TowerHeight,
            double TowerRadius,
            double HubYaw,
            double HubRadius,
            double HubHalfLength,
            int NumberOfBlades,
            double BladeLength,
            double BladeRadius,
            double BladeThinRadius,
            double BladePitch,
            double BladeStartRotation,
            double BladeStartLength,
            double DistanceFromBlade,
            boolean Locked) {

        towerHeight = TowerHeight;
        towerRadius = TowerRadius;
        hubYaw = HubYaw;
        hubRadius = HubRadius;
        hubHalfLength = HubHalfLength;
        numberOfBlades = NumberOfBlades;
        bladeLength = BladeLength;
        bladeRadius = BladeRadius;
        bladeThinRadius = BladeThinRadius;
        bladePitch = BladePitch;
        bladeStartRotation = BladeStartRotation;
        bladeStartLength = BladeStartLength;
        distanceFromBlade = DistanceFromBlade;
        locked = Locked;

        updateDerivedValues();
    }

    public WindmillData(WindmillData source) {

        towerHeight = source.towerHeight;
        towerRadius = source.towerRadius;
        hubYaw = source.hubYaw;
        hubRadius = source.hubRadius;
        hubHalfLength = source.hubHalfLength;
        numberOfBlades = source.numberOfBlades;
        bladeLength = source.bladeLength;
        bladeRadius = source.bladeRadius;
        bladeThinRadius = source.bladeThinRadius;
        bladePitch = source.bladePitch;
        bladeStartRotation = source.bladeStartRotation;
        bladeStartLength = source.bladeStartLength;
        distanceFromBlade = source.distanceFromBlade;
        locked = source.locked;

        centerTransform = source.centerTransform;
        hubTransform = source.hubTransform;
        bladeTransforms = source.bladeTransforms;

        updateDerivedValues();
    }

    public void updateDerivedValues() {
        totalHeight = towerHeight + 2 * hubRadius + bladeLength;
        unlockedRadius =
                Math.sqrt(
                        Math.pow(hubHalfLength - bladeRadius, 2)
                                + Math.pow(hubRadius + bladeLength, 2));
    }

    public void invalidateTransforms() {
        centerTransform = null;
        hubTransform = null;
        bladeTransforms.clear();
    }

    public double getTowerHeight() { return towerHeight; }

    public boolean setTowerHeight(double towerHeight) {
        if (this.towerHeight == towerHeight) {
            return false;
        }
        this.towerHeight = towerHeight;
        invalidateTransforms();
        updateDerivedValues();
        return true;
    }

    public double getTowerRadius() { return towerRadius; }

    public boolean setTowerRadius(double towerRadius) {
        if (this.towerRadius == towerRadius) {
            return false;
        }
        this.towerRadius = towerRadius;
        invalidateTransforms();
        return true;
    }

    public double getHubYaw() { return hubYaw; }

    public boolean setHubYaw(double hubYaw) {
        if (this.hubYaw == hubYaw) {
            return false;
        }
        this.hubYaw = hubYaw;
        invalidateTransforms();
        return true;
    }

    public double getHubRadius() { return hubRadius; }

    public boolean setHubRadius(double hubRadius) {
        if (this.hubRadius == hubRadius) {
            return false;
        }
        this.hubRadius = hubRadius;
        invalidateTransforms();
        updateDerivedValues();
        return true;
    }

    public double getHubHalfLength() { return hubHalfLength; }

    public boolean setHubHalfLength(double hubHalfLength) {
        if (this.hubHalfLength == hubHalfLength) {
            return false;
        }
        this.hubHalfLength = hubHalfLength;
        invalidateTransforms();
        updateDerivedValues();
        return true;
    }

    public int getNumberOfBlades() { return numberOfBlades; }

    public boolean setNumberOfBlades(int numberOfBlades) {
        if (this.numberOfBlades == numberOfBlades) {
            return false;
        }
        this.numberOfBlades = numberOfBlades;
        invalidateTransforms();
        return true;
    }

    public double getBladeLength() { return bladeLength; }

    public boolean setBladeLength(double bladeLength) {
        if (this.bladeLength == bladeLength) {
            return false;
        }
        this.bladeLength = bladeLength;
        invalidateTransforms();
        updateDerivedValues();
        return true;
    }

    public double getBladeRadius() { return bladeRadius; }

    public boolean setBladeRadius(double bladeRadius) {
        if (this.bladeRadius == bladeRadius) {
            return false;
        }
        this.bladeRadius = bladeRadius;
        invalidateTransforms();
        updateDerivedValues();
        return true;
    }

    public double getBladeThinRadius() { return bladeThinRadius; }

    public boolean setBladeThinRadius(double bladeThinRadius) {
        if (this.bladeThinRadius == bladeThinRadius) {
            return false;
        }
        this.bladeThinRadius = bladeThinRadius;
        invalidateTransforms();
        return true;
    }

    public double getBladePitch() { return bladePitch; }

    public boolean setBladePitch(double bladePitch) {
        if (this.bladePitch == bladePitch) {
            return false;
        }
        this.bladePitch = bladePitch;
        invalidateTransforms();
        return true;
    }

    public double getBladeStartRotation() { return bladeStartRotation; }

    public boolean setBladeStartRotation(double bladeStartRotation) {
        if (this.bladeStartRotation == bladeStartRotation) {
            return false;
        }
        this.bladeStartRotation = bladeStartRotation;
        invalidateTransforms();
        return true;
    }

    public double getBladeStartLength() { return bladeStartLength; }

    public boolean setBladeStartLength(double bladeStartLength) {
        if (this.bladeStartLength == bladeStartLength) {
            return false;
        }
        this.bladeStartLength = bladeStartLength;
        invalidateTransforms();
        return true;
    }

    public double getDistanceFromBlade() { return distanceFromBlade; }

    public boolean setDistanceFromBlade(double distanceFromBlade) {
        if (this.distanceFromBlade == distanceFromBlade) {
            return false;
        }
        this.distanceFromBlade = distanceFromBlade;
        invalidateTransforms();
        return true;
    }

    public boolean getLocked() { return locked; }

    public boolean setLocked(boolean locked) {
        if (this.locked == locked) {
            return false;
        }
        this.locked = locked;
        invalidateTransforms();
        return true;
    }

    public boolean equals(WindmillData wd) {
        if (wd == this) {
            return true;
        }

        if (wd == null) {
            return false;
        }

        return wd.towerHeight == towerHeight
                && wd.towerRadius == towerRadius
                && wd.hubYaw == hubYaw
                && wd.hubRadius == hubRadius
                && wd.hubHalfLength == hubHalfLength
                && wd.numberOfBlades == numberOfBlades
                && wd.bladeLength == bladeLength
                && wd.bladeRadius == bladeRadius
                && wd.bladeThinRadius == bladeThinRadius
                && wd.bladePitch == bladePitch
                && wd.bladeStartRotation == bladeStartRotation
                && wd.bladeStartLength == bladeStartLength
                && wd.distanceFromBlade == distanceFromBlade
                && wd.locked == locked;
    }

}
