/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.rasterizer;

import com.intel.flightplanning.core.FlightLine;
import com.intel.flightplanning.core.FlightPlan;
import com.intel.flightplanning.core.Goal;
import com.intel.flightplanning.core.terrain.IElevationModel;
import com.intel.flightplanning.core.vehicle.UAV;
import com.intel.flightplanning.geometry2d.Geometry2D;
import com.intel.flightplanning.geometry2d.LineString;
import com.intel.flightplanning.math.CorridorHelper;
import com.intel.flightplanning.sensor.Camera;
import com.intel.flightplanning.sensor.PhotoProperties;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RasterizerCorridor {

    public FlightPlan rasterize(LineString aoi, Camera camera, UAV uav, IElevationModel elev, Goal goal) {
        return null;
    }

//should probably be split up in two steps: generating the flight lines and then generating waypoints for each flight line

    public FlightPlan rasterize(
            Geometry2D aoi,
            float overshootParallelFlight,
            float corridorMinLines,
            float corridorWidthInMeter,
            Camera camera,
            UAV uav,
            IElevationModel elev,
            Goal goal,
            PhotoProperties photoProps) {

        int numberLines =
            (int)Math.max(corridorMinLines, 1 + Math.ceil((2 * overshootParallelFlight + corridorWidthInMeter)));

        var cornerPoints = aoi.getCornerPoints();
        List<Vector3f> cp =
            aoi.getCornerPoints().stream().map(v -> new Vector3f(v.x, v.y, 0f)).collect(Collectors.toList());
        var corridorHelper = new CorridorHelper(cp, true);
        float minYenlarged = 0;
        float maxYenlarged = corridorHelper.getCenterLength();

        float maxXenlarged = (float)(photoProps.getSizeParallelFlightEff() * (numberLines - 1) / 2.);
        float minXenlarged = -maxXenlarged;
        int lineNo = 0;

        List<FlightLine> flightLines = new ArrayList<>();
        for (lineNo = 1; lineNo != numberLines; lineNo++) {
            // minus sign to reverse the line order, as sortIntoRows would expect this
            float shift = (float)-(photoProps.getSizeParallelFlightEff() * (lineNo - (numberLines - 1) / 2.));
            List<Vector3f> line = corridorHelper.getShifted(shift);

            // Todo: this whole thing needs rework
            FlightLine fl = new FlightLine(line.get(0), line.get(0), line.get(0));
            // FlightLine fl = new FlightLine(line, lineNo, shift - minXenlarged, this, false);
            // fl.enlarge(overshootTotalLinesEnd + centrencyInFlight, overshootTotalLinesEnd + centrencyInFlight);
            flightLines.add(fl);
            // firstFreeID+=4;

        }
        var fp = FlightPlan.fromFlightLines(flightLines);


        return fp;
    }


}
