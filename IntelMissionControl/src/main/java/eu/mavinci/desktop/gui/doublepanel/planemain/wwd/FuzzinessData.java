/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Globe;
import java.util.ArrayList;

public class FuzzinessData {

    private static final IElevationModel elevationModel = StaticInjector.getInstance(IElevationModel.class);
    private static final Globe globe = StaticInjector.getInstance(IWWGlobes.class).getDefaultGlobe();

    private double inMeter = -1;
    private double maxLenseDistance = -1;
    private double inPixel = -1;
    private double exposureTime;

    public double getExposureTime() {
        return exposureTime;
    }

    public double getInMeter() {
        return inMeter;
    }

    public double getInPixel() {
        return inPixel;
    }

    public double getMaxLenseDistance() {
        return maxLenseDistance;
    }

    @Override
    public String toString() {
        return "Fuz:"
            + StringHelper.lengthToIngName(inMeter, -3, false)
            + "/"
            + inPixel
            + "pix  dist:"
            + StringHelper.lengthToIngName(maxLenseDistance, -3, false);
    }

    public static FuzzinessData getFuzziness(
            ComputeCornerData.IAerialPinholeImageContext context,
            CPhotoLogLine line,
            double exposureTime,
            ComputeCornerData cornersNonDelayed) {
        FuzzinessData dat = new FuzzinessData();
        dat.exposureTime = exposureTime;

        IHardwareConfiguration hardwareConfiguration = context.getHardwareConfiguration();

        if (exposureTime < 0 && hardwareConfiguration != null) {
            IGenericCameraConfiguration cameraConfiguration =
                hardwareConfiguration.getPayload(IGenericCameraConfiguration.class);
            exposureTime =
                1.0
                    / cameraConfiguration
                        .getDescription()
                        .getOneOverExposureTime()
                        .convertTo(Unit.MILLISECOND)
                        .getValue()
                        .doubleValue();
        }

        ArrayList<LatLon> corn0 = cornersNonDelayed.getGroundProjectedCorners();

        if (cornersNonDelayed.getShiftedPosOnLevel() != null) {
            dat.inPixel =
                -2; // notify that elevation model is not loaded, so even no fuzzyness is avaliable, recompute will not
            // help
        }

        if (corn0 == null || corn0.size() != 4) {
            return dat;
        }

        ComputeCornerData computeCornerData = ComputeCornerData.computeCorners(context, line, exposureTime);
        if (computeCornerData == null) {
            return dat;
        }

        ArrayList<LatLon> corn1 = computeCornerData.getGroundProjectedCorners();

        if (corn1 == null || corn1.size() != 4) {
            return dat;
        }

        double radiusEq = globe.getEquatorialRadius();
        double radiusPol = globe.getPolarRadius();
        double fuzzynessMeter = 0;
        double distance = 0;

        for (int i = 0; i != 4; ++i) {
            double fuz;
            if (corn0.get(i).equals(corn1.get(i))) {
                fuz = 0;
            } else {
                fuz = LatLon.ellipsoidalDistance(corn0.get(i), corn1.get(i), radiusEq, radiusPol);
            }

            fuzzynessMeter = Math.max(fuzzynessMeter, fuz);
            double elev = elevationModel.getElevationAsGoodAsPossible(corn0.get(i));
            double d =
                LatLon.ellipsoidalDistance(corn0.get(i), cornersNonDelayed.getShiftedPosOnLevel(), radiusEq, radiusPol);
            elev -= cornersNonDelayed.getShiftedPosOnLevel().elevation;
            distance = Math.max(distance, d * d + elev * elev);
        }

        dat.inMeter = fuzzynessMeter;
        distance = Math.sqrt(distance);
        dat.maxLenseDistance = distance;

        if (hardwareConfiguration != null) {
            dat.inPixel = fuzzynessMeter / CameraHelper.getPixSizeInM(distance, hardwareConfiguration);
        } else {
            dat.inPixel = -1; // ok, in this case recompute actually will help!
        }

        return dat;
    }

}
