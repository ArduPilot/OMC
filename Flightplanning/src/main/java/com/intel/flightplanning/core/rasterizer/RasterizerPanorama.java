/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.rasterizer;

public class RasterizerPanorama {
    //{
        //                    double radius =
        // cameraDescription.getOffsetToTail().convertTo(Unit.METER).getValue().doubleValue();
        //                    int lines;
        //                    double yawOffset;
        //                    if (planType == PlanType.POINT_OF_INTEREST) {
        //                        radius += alt + corridorWidthInMeter;
        //                        lines =
        //                                (int)
        //                                        Math.max(
        //                                                Math.ceil(2 * Math.PI / (sizeParallelFlightEff /
        // corridorWidthInMeter)),
        //                                                getCorridorMinLines());
        //                        yawOffset = Math.PI / 2.0;
        //                    } else { // PANORAMA
        //                        double openingAngleRad =
        //                                2
        //                                        * Math.tan(
        //                                        hardwareConfiguration
        //                                                .getPrimaryPayload(IGenericCameraConfiguration.class)
        //                                                .getDescription()
        //                                                .getCcdWidth()
        //                                                .convertTo(Unit.MILLIMETER)
        //                                                .getValue()
        //                                                .doubleValue()
        //                                                / 2
        //                                                / hardwareConfiguration
        //                                                .getPrimaryPayload(IGenericCameraConfiguration.class)
        //                                                .getLens()
        //                                                .getDescription()
        //                                                .getFocalLength()
        //                                                .convertTo(Unit.MILLIMETER)
        //                                                .getValue()
        //                                                .doubleValue());
        //                        openingAngleRad *= 1 - (getOverlapParallel() / 100.);
        //                        lines = (int)Math.ceil(2 * Math.PI / openingAngleRad);
        //                        yawOffset = -Math.PI / 2.0;
        //                    }
        //
        //                    double step = 2 * Math.PI / lines;
        //
        //                    Vector<Vec4> pointsBase = new Vector<>();
        //                    Vector<Orientation> directionsBase = new Vector<>();
        //                    double curAlt =
        //                            Math.max(
        //                                    cropHeightMax,
        //
        // platformDescription.getMinGroundDistance().convertTo(Unit.METER).getValue().doubleValue());
        //                    for (int k = 0; k < lines; k++) {
        //                        double yawRad = yawOffset + step * k * (circleLeftTrueRightFalse ? -1 : 1);
        //                        Vec4 v = new Vec4(radius * Math.sin(yawRad), radius * Math.cos(yawRad), curAlt);
        //                        Orientation o = new Orientation(0, 90, Math.toDegrees(yawRad) + getYaw() + 90);
        //                        // System.out.println("V:" + v + " o:" + o);
        //                        pointsBase.addElement(v);
        //                        directionsBase.add(o);
        //                    }
        //
        //                    FlightLine fl = new FlightLine(pointsBase, directionsBase, radius, 0, this);
        //
        //                    flightLines.add(fl);
        //                    // System.out.println("flightlines:" + flightLines);
        //                    break;
        //                }
}
