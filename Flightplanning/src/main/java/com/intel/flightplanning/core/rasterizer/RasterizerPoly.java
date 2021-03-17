/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.rasterizer;

import com.intel.flightplanning.core.FlightLine;
import com.intel.flightplanning.core.FlightPlan;
import com.intel.flightplanning.core.Goal;
import com.intel.flightplanning.core.MinMaxPair;
import com.intel.flightplanning.core.annotations.NeedsRework;
import com.intel.flightplanning.core.terrain.IElevationModel;
import com.intel.flightplanning.core.vehicle.UAV;
import com.intel.flightplanning.geometry2d.Geometry2D;
import com.intel.flightplanning.sensor.Camera;
import com.intel.flightplanning.sensor.GsdUtils;
import com.intel.flightplanning.sensor.PhotoProperties;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class RasterizerPoly {

    @NeedsRework
    public static BoundingBox calculateBoundingBox(Geometry2D aoi) {
        return new BoundingBox(new Vector3f(0, 0, 0), new Vector3f(1000, 1000, 0));
    }

    public static boolean isValidAOI() {
        return true;
    }

    @NeedsRework
    public static FlightPlan rasterize(
            Geometry2D aoi, Camera camera, UAV uav, IElevationModel elev, Goal goal, PhotoProperties photoProps) {
        var aoiList = new ArrayList<Geometry2D>();
        aoiList.add(aoi);
        return rasterize(aoiList, camera, uav, elev, goal, photoProps);
    }

    @NeedsRework
    public static FlightPlan rasterize(
            List<Geometry2D> aoiList,
            Camera camera,
            UAV uav,
            IElevationModel elev,
            Goal goal,
            PhotoProperties photoProps) {
        if (!isValidAOI()) {
            // Todo: handle error
        }

        var fp = new FlightPlan();

        double gsd = goal.getTargetGSD();
        double alt = goal.getTargetAlt();

        double centrencyParallelFlight = photoProps.getCentrencyInFlight(); // unknown, what is centrency?
        double centrencyInFlight = photoProps.getCentrencyInFlight(); // unknown, what is centrency?
        double sizeParallelFlight = photoProps.getSizeParallelFlight(); // unknown, size of what?
        double sizeInFlight = photoProps.getSizeInFlight(); // unknown, size of what?
        double footprintShapeOvershoot = Math.abs(photoProps.getLeftOvershoot()); // unknown

        float offsetToRightWing = uav.getOffsetToRightWing();
        float offsetToTail = uav.getOffsetToTail();

        centrencyParallelFlight -= offsetToRightWing;
        centrencyInFlight += offsetToTail;

        double motionBlurrEst =
            GsdUtils.getFuzzyTotal(
                GsdUtils.calculateFuzzySpeed(uav.getMaxSpeedInMS(), 0, camera.getExposureTime()),
                GsdUtils.calculateFuzzyAngle(alt, uav.getAngularSpeedNoise(), camera.getExposureTime()));

        double overlapInFlight = goal.getOverlapInFlight();

        double sizeInFlightEff =
            (1. - overlapInFlight / 100.)
                * sizeInFlight; // recompute,since overlap might have changed // todo: recompute mentioned but there is

        double sizeInFlightEffMax = (1. - goal.getOverlapInFlightMin() / 100.) * sizeInFlight;
        double sizeParallelFlightEff = (1. - goal.getOverlapParallel() / 100.) * sizeParallelFlight;

        // double overlapInFlight = sizeInFlight - sizeInFlightEff;
        // double overlapInFlightMin = sizeInFlight - sizeInFlightEffMax; //overlapInFlightMin < overlapInFlight

        // where does the -50 come from? with a 50 per-cent overlap, there is no overshoot?
        double overshootParallelFlight = (goal.getOverlapParallel() - 50) / 100. * sizeParallelFlight;

        // unknown intent
        double overshoot = uav.getOvershootInM();
        double overshootInnerLinesEnd = 0;
        double overshootTotalLinesEnd = 0;

        var flightLines = new ArrayList<FlightLine>();

        double lengthEstimation = 0;

        int numberLines;
        int lineNo;

        // var aoiBounds = calculateBoundingBox(aoiList);
        var aoiBounds = calculateBoundingBox(aoiList.get(0));
        var min = new Vector3f();
        aoiBounds.getMin(min);
        var max = new Vector3f();
        aoiBounds.getMax(max);
        double minY = min.y;
        double maxY = max.y;
        // todo: this looks weird, why is either minY or overshootPArallelFlight negative? coordinates given in
        // drone ref frame?
        minY = minY - overshootParallelFlight;
        maxY = maxY + overshootParallelFlight;

        numberLines =
            (int)
                Math.max(
                    Math.ceil((maxY - minY) / sizeParallelFlightEff), // might be that max/minY is min/max of aoi?
                    Math.round(sizeParallelFlight / sizeParallelFlightEff));

        double offset = ((numberLines - 1) * sizeParallelFlightEff - (maxY - minY)) / 2;
        maxY = maxY + offset;
        double currentY = maxY;

        Vector2f ori = new Vector2f(0, 0);
        Vector2f dir = new Vector2f(1, 0);

        var polyList = new ArrayList<List<Vector2f>>();
        for (var aoi : aoiList) {
            polyList.add(aoi.getCornerPoints());
        }

        for (lineNo = 0; lineNo != numberLines; lineNo++) {
            List<MinMaxPair> minMax = FlightLine.getFlightlineIntersectionWithPolygonsHorizontal(polyList, currentY, uav.getWidth());

            System.out.println(minMax);
            // FlightLine.calculateFlightlineIntersections()
            //            FlightLine fl = new FlightLine(currentY, minMaxX, lineNo, this, false);
            //            flightLines.add(fl);
            //            lengthEstimation += fl.getLength();
            //            //                }
            //
            //                currentY -= sizeParallelFlightEff;
            //            }
            //

            // estimate length
        }

        return fp;
    }

    public boolean isValidSizeAOI() {
        return true;
    }
}
