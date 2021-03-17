/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.rasterizer;

public class RasterizerWindmill {
    //                    boolean circleLeftTrueRightFalse = this.circleLeftTrueRightFalse;
    //                    if (startCaptureVertically.equals(StartCaptureVerticallyTypes.up)) {
    //                        circleLeftTrueRightFalse = !circleLeftTrueRightFalse;
    //                    }
    //                    // WINDMILL parameters, fixed for now, need ui/etc to change
    //                    //                if (windmill == null) {
    //                    //                    windmill = new WindmillData();
    //                    //                } else {
    //                    //                    System.out.println("reusing existing WindmillData");
    //                    //                }
    //
    //                    // TODO: these should come from UI for windmill eventually
    //                    //                windmill.setTowerHeight(cropHeightMax);
    //                    //                windmill.setTowerRadius(corridorWidthInMeter);
    //                    //                windmill.setDistanceFromBlade(alt);
    //
    //                    // make waypoints and lines (flightplan) for WINDMILL blades
    //                    // treat blades as tilted cylinders
    //
    //                    // next few variables are for flightlines
    //                    int lines = 4; // number of lines around a blade
    //                    double step = 2 * Math.PI / lines; // angular steps around blades
    //                    double radius = // radius of flight lines around blades
    //                            windmill.distanceFromBlade
    //                                    + windmill.bladeRadius
    //                                    +
    // cameraDescription.getOffsetToTail().convertTo(Unit.METER).getValue().doubleValue();
    //                    double thinRadius = radius - windmill.bladeRadius + windmill.bladeThinRadius;
    //                    double startAlt = windmill.hubRadius + windmill.bladeStartLength;
    //                    double stopAlt = windmill.hubRadius + windmill.bladeLength;
    //
    //                    // build flight path for all blades
    //                    double bladeRotationStep = 360 / windmill.numberOfBlades;
    //                    double bladeRotationDegs = windmill.bladeStartRotation;
    //
    //                    for (int i = 0; i < windmill.numberOfBlades; i++) {
    //                        AddBladeWaypoints(
    //                                startAlt,
    //                                stopAlt,
    //                                step,
    //                                radius,
    //                                thinRadius,
    //                                lines,
    //                                windmill.hubRadius,
    //                                windmill.hubHalfLength,
    //                                windmill.hubYaw,
    //                                windmill.towerHeight,
    //                                bladeRotationDegs,
    //                                windmill.bladeRadius,
    //                                windmill.bladePitch,
    //                                windmill.numberOfBlades);
    //
    //                        bladeRotationDegs += bladeRotationStep;
    //                    }
    //
    //                    break;
    //                }
}
