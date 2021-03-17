/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.rasterizer;

public class RasterizerSearchPattern {

    //                { // scoped
    //                    numberLines = 1;
    //
    //                    Vector<Vec4> corners = new Vector<>();
    //                    int ySign = circleLeftTrueRightFalse ? 1 : -1;
    //                    lineNo = 1;
    //
    //                    corners.add(new Vec4(0, 0));
    //
    //                    int circleCountMax =
    //                            Math.max(
    //                                    1,
    //                                    (int)Math.ceil((corridorWidthInMeter - 0.5 * sizeParallelFlightEff) /
    // sizeParallelFlightEff));
    //
    //                    for (int circleCount = 1; circleCount <= circleCountMax; circleCount++) {
    //                        double radius = circleCount * sizeParallelFlightEff;
    //                        corners.add(new Vec4(radius, -ySign * (radius - sizeParallelFlightEff)));
    //                        corners.add(new Vec4(radius, ySign * radius));
    //                        corners.add(new Vec4(-radius, ySign * radius));
    //                        corners.add(new Vec4(-radius, -ySign * radius));
    //                        lengthEstimation += 8 * radius - (2 * sizeParallelFlightEff);
    //                    }
    //
    //                    double radius = circleCountMax * sizeParallelFlightEff;
    //                    corners.add(new Vec4(radius, -ySign * radius));
    //                    lengthEstimation += 2 * radius;
    //
    //                    FlightLine fl = new FlightLine(corners, lineNo, 0, this, false);
    //                    flightLines.add(fl);
    //                }
    //

}
