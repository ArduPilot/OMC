/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.rasterizer;

public class RasterizerStar {

    //                { // scoped
    //                    minYenlarged = -getCorridorWidthInMeter();
    //                    maxYenlarged = +getCorridorWidthInMeter();
    //
    //                    minXenlarged = -getCorridorWidthInMeter();
    //                    maxXenlarged = +getCorridorWidthInMeter();
    //
    //                    numberLines = 1;
    //                    Vector<Vec4> points = new Vector<>();
    //                    for (double yaw = 0; yaw < 180; yaw += Math.max(getYaw(), 1)) {
    //                        double yawRad = Math.toRadians(yaw);
    //                        Vec4 v =
    //                                new Vec4(
    //                                        getCorridorWidthInMeter() * Math.sin(yawRad),
    // getCorridorWidthInMeter() * Math.cos(yawRad));
    //                        points.add(v);
    //                        points.add(v.getNegative3());
    //                        points.add(v);
    //                    }
    //
    //                    if (points.size() > 0) {
    //                        lineNo = 1;
    //                        FlightLine fl = new FlightLine(points, lineNo, sizeParallelFlightEff, this, true);
    //                        lineNo++;
    //                        flightLines.add(fl);
    //                        lengthEstimation += fl.getLength();
    //                    }
    //                }
    //                // return false;
    //                break;

}
