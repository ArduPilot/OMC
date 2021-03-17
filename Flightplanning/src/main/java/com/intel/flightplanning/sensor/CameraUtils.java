/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.sensor;

import com.intel.flightplanning.core.annotations.NeedsRework;
import com.intel.flightplanning.math.MathHelper;
import com.jme3.math.Matrix3f;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;

// only static members, think Math
public class CameraUtils {
    //
    //    public static void compensateForMotionBlur(UAV uav, double sizeInFlight) {
    //        // compensate for motion blur
    //        double maxPossiblePlaneSpeedMpSBlurr = altitudeGsdCalculator.computeMaxGroundSpeedMpS();
    //
    //        double maxPossiblePlaneSpeedMpS = uav.getMaxSpeedInMS();
    //        // compensate for forward overlap isues
    //
    //        // compute max possible plane speed
    //        sizeInFlightEff = (1. - getOverlapInFlight() / 100.) * sizeInFlight;
    //        double maxPossiblePlaneSpeedMpSOverlap = sizeInFlightEff / photoSettings.getMinTimeInterval();
    //
    //        if (photoSettings.getMaxGroundSpeedAutomatic() == FlightplanSpeedModes.AUTOMATIC_DYNAMIC) {
    //            this.optimalSpeedThisAoi = Math.min(maxPossiblePlaneSpeedMpSBlurr, maxPossiblePlaneSpeedMpS);
    //            if (photoSettings.getMaxGroundSpeedMPSec() == maxPossiblePlaneSpeedMpS) {
    //                photoSettings.setMaxGroundSpeedMPSec(this.optimalSpeedThisAoi);
    //            } else {
    //                if (photoSettings.getMaxGroundSpeedMPSec() < this.optimalSpeedThisAoi) {
    //                    photoSettings.setMaxGroundSpeedMPSec(this.optimalSpeedThisAoi);
    //                }
    //            }
    //        } else {
    //            this.optimalSpeedThisAoi =
    //                Math.min(
    //                    Math.min(maxPossiblePlaneSpeedMpSBlurr, maxPossiblePlaneSpeedMpSOverlap),
    // maxPossiblePlaneSpeedMpS);
    //            if (photoSettings.getMaxGroundSpeedMPSec() == maxPossiblePlaneSpeedMpS) {
    //                photoSettings.setMaxGroundSpeedMPSec(this.optimalSpeedThisAoi);
    //            } else {
    //                if (photoSettings.getMaxGroundSpeedMPSec() < this.optimalSpeedThisAoi) {
    //                    photoSettings.setMaxGroundSpeedMPSec(this.optimalSpeedThisAoi);
    //                }
    //            }
    //        }
    //    }

    @NeedsRework
    public static Vector3f lineIntersection(
            Vector3f planeNormal, Vector3f planePoint, Vector3f linePoint, Vector3f lineDirection) {
        if (planeNormal.dot(lineDirection.normalize()) == 0) {
            return null;
        }

        float t =
            (planeNormal.dot(planePoint) - planeNormal.dot(linePoint)) / planeNormal.dot(lineDirection.normalize());
        return linePoint.add(lineDirection.normalize().mult(t));
    }

    public static PhotoProperties calculatePhotoProperties(Camera cam, float alt) {
        {
            return calculatePhotoProperties(cam, alt, new Quaternion());
        }
    }
    /**
    *
    * TODO: right now, camera poitns downwards, this should not happen! It's in the corner direction part
    *
     * @param cam
     * @param alt
     * @param roll roll around flight direction, clockwise - 0 is horizontal
     * @param pitch pitch around axis horizontal, orthogonal to flight direction - 0 is horizontal, -90 is nadir
     *     downwards
     * @param yaw yaw is uav attitude, clockwise, 0 is "north"
     * @return PhotoProperties
     */
    public static PhotoProperties calculatePhotoProperties(Camera cam, float alt, float roll, float pitch, float yaw) {
        float[] angles = {pitch, yaw, roll};
        var rot = new Quaternion(angles);
        return calculatePhotoProperties(cam, alt, rot);
    }

    public static PhotoProperties calculatePhotoProperties(Camera cam, float alt, float roll, float pitch) {
        return calculatePhotoProperties(cam, alt, roll, pitch, 0f);
    }

    public static PhotoProperties calculatePhotoProperties(Camera cam, float alt, float pitch) {
        return calculatePhotoProperties(cam, alt, 0f, pitch, 0f);
    }

    public static PhotoProperties calculatePhotoProperties(Camera cam, float alt, Quaternion camTrafo) {
        var photoProperties = new PhotoProperties();

        Vector3f[] cs = getCornerDirections(cam);
        Plane p = new Plane(new Vector3f(0, 0, 1), 0);
        Matrix3f just = getCameraJustageTransform(cam);
        // System.out.println("just="+just);
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double[] footprintXd = new double[4];
        double[] footprintYd = new double[4];

        Vector3f origin = new Vector3f(0, 0, -alt);

        for (int i = 0; i != 4; ++i) {
            just.multLocal(cs[i]);
            cs[i].normalizeLocal();
            camTrafo.toRotationMatrix().multLocal(cs[i]);
            Ray ray = new Ray(origin, cs[i]);
            var intersec = lineIntersection(p.getNormal(), new Vector3f(), ray.origin, ray.direction);

            if (intersec == null) {
                return photoProperties;
            }

            footprintXd[i] = intersec.x;
            footprintYd[i] = intersec.y;
            maxX = Math.max(maxX, intersec.x);
            maxY = Math.max(maxY, intersec.y);
            minX = Math.min(minX, intersec.x);
            minY = Math.min(minY, intersec.y);
            // System.out.println(i+" x="+intersec.x + " y="+intersec.y);
        }

        // bestimme dreieckstumpf, untere kante liegt auf der x-achse
        int idCornerLeft = -1;
        int idCornerRight = -1;

        for (int i = 0; i != 4; ++i) {
            if (footprintXd[i] == minX) {
                idCornerLeft = i;
            }

            if (footprintXd[i] == maxX) {
                idCornerRight = i;
            }
        }

        // System.out.println("idCornerLeft:"+idCornerLeft);
        // System.out.println("idCornerRight:"+idCornerRight);

        int idTop1 = -1;
        int idTop2 = -1;
        for (int i = 0; i != 4; ++i) {
            if (i != idCornerLeft && i != idCornerRight) {
                if (idTop1 == -1) {
                    idTop1 = i;
                } else if (idTop2 == -1) {
                    idTop2 = i;
                }
            }
        }
        // System.out.println("idTop1:"+idTop1);
        // System.out.println("idTop2:"+idTop2);

        double hightTop1 = 0;
        double hightTop2 = 0;
        for (int i = 0; i != 4; i++) {
            int j = (i + 1) % 4;
            double x1 = footprintXd[i];
            double x2 = footprintXd[j];
            if (x1 == x2) {
                continue;
            }

            double y1 = footprintYd[i];
            double y2 = footprintYd[j];
            // System.out.println("i="+i);
            // System.out.println("x1="+x1);
            // System.out.println("x2="+x2);

            // find crossing edge for top1
            double x = footprintXd[idTop1];
            // System.out.println("x id1:"+x);
            if ((x1 >= x && x2 <= x) || (x1 <= x && x2 >= x)) {
                // System.out.println("cross for id1 at i="+i);
                double y = y1 + (y2 - y1) / (x2 - x1) * (x - x1);
                hightTop1 = Math.max(hightTop1, Math.abs(y - footprintYd[idTop1]));
            }

            // find crossing edge for top2
            x = footprintXd[idTop2];
            // System.out.println("x id2:"+x);
            if ((x1 >= x && x2 <= x) || (x1 <= x && x2 >= x)) {
                // System.out.println("cross for id2 at i="+i);
                double y = y1 + (y2 - y1) / (x2 - x1) * (x - x1);
                hightTop2 = Math.max(hightTop2, Math.abs(y - footprintYd[idTop2]));
            }
        }
        // System.out.println("hightTop1:"+hightTop1);
        // System.out.println("hightTop2:"+hightTop2);

        int idTopHigher = -1;
        double hightTopHigher = 0;
        double hightTopLower = 0;

        if (hightTop1 > hightTop2) {
            idTopHigher = idTop1;
            hightTopHigher = hightTop1;
            hightTopLower = hightTop2;
        } else {
            idTopHigher = idTop2;
            hightTopHigher = hightTop2;
            hightTopLower = hightTop1;
        }

        // System.out.println("idTopHigher:"+idTopHigher);
        // System.out.println("hightTopHigher:"+hightTopHigher);
        // System.out.println("hightTopLower:"+hightTopLower);

        double hightTopLeft = 0;
        double hightTopRight = 0;
        int idTopLeft = -1;
        int idTopRight = -1;
        if (footprintXd[idTop1] > footprintXd[idTop2]) {
            idTopLeft = idTop2;
            idTopRight = idTop1;
            hightTopLeft = hightTop2;
            hightTopRight = hightTop1;
        } else {
            idTopLeft = idTop1;
            idTopRight = idTop2;
            hightTopLeft = hightTop1;
            hightTopRight = hightTop2;
        }

        // System.out.println("idTopLeft:"+idTopLeft);
        // System.out.println("idTopRight:"+idTopRight);
        // System.out.println("hightTopLeft:"+hightTopLeft);
        // System.out.println("hightTopRight:"+hightTopRight);

        // berechne höhe und x position von virtueller spitze
        @SuppressWarnings("checkstyle:localvariablename")
        double dXleft = footprintXd[idTopLeft] - footprintXd[idCornerLeft];
        @SuppressWarnings("checkstyle:localvariablename")
        double dXright = footprintXd[idCornerRight] - footprintXd[idTopRight];

        double cutLeftDx;
        double cutRightDx;
        double high;
        double det = hightTopLeft * dXright + hightTopRight * dXleft;
        double g = footprintXd[idCornerRight] - footprintXd[idCornerLeft];
        if (det != 0) {
            det = 1 / det;
            double alpha = det * hightTopRight * g;

            double virtTopDx = alpha * dXleft;
            double highVirt = alpha * hightTopLeft;

            // System.out.println("dYleft:"+dXleft);
            // System.out.println("dYright:"+dXright);
            // System.out.println("det:"+det);
            // System.out.println("g:"+g);
            // System.out.println("alpha:"+alpha);
            // System.out.println("virtTopDx:"+virtTopDx);
            // System.out.println("highVirt:"+highVirt);

            // berechne linken und rechten X-pos des cuts
            // fallunterscheidung
            if (hightTop1 > highVirt * 0.5 && hightTop2 > highVirt * 0.5) {
                cutLeftDx = virtTopDx * 0.5;
                cutRightDx = (g + virtTopDx) * 0.5;
                high = highVirt * 0.5;
                // System.out.println("case1");
            } else if (hightTopLower * 2 < hightTopHigher) {
                double topHigherDx = footprintXd[idTopHigher] - footprintXd[idCornerLeft];
                cutLeftDx = topHigherDx * 0.5;
                cutRightDx = (g + topHigherDx) * 0.5;
                high = hightTopHigher * 0.5;
                // System.out.println("case2");
            } else {
                high = hightTopLower;
                cutLeftDx = high / highVirt * virtTopDx;
                cutRightDx = g - high / highVirt * (g - virtTopDx);
                // System.out.println("case3");
            }
        } else {
            cutLeftDx = 0;
            cutRightDx = g;
            high = hightTopLower;
        }

        // System.out.println("cutLeftDx:"+cutLeftDx);
        // System.out.println("cutRightDx:"+cutRightDx);
        // System.out.println("high:"+high);

        double cutLeftX = cutLeftDx + footprintXd[idCornerLeft];
        double cutRightX = cutRightDx + footprintXd[idCornerLeft];

        // System.out.println("cutLeftX:"+cutLeftX);
        // System.out.println("cutRightX:"+cutRightX);

        double cutLeftYmax = Double.NEGATIVE_INFINITY;
        double cutRightYmax = Double.NEGATIVE_INFINITY;
        double cutLeftYmin = Double.POSITIVE_INFINITY;
        double cutRightYmin = Double.POSITIVE_INFINITY;

        // sometimes due numerically inaccuarcy, the left and right x value is out of range,
        // so no cut will be found. to prevent this, set the X values to the actually extream values

        cutLeftX = Math.max(cutLeftX, footprintXd[idCornerLeft]);
        cutRightX = Math.min(cutRightX, footprintXd[idCornerRight]);

        // back to original polygon, calculate their the REAL
        for (int i = 0; i != 4; i++) {
            int j = (i + 1) % 4;
            double x1 = footprintXd[i];
            double x2 = footprintXd[j];
            // System.out.println("x1="+x1+ "\tx2="+x2);
            if (x1 == x2) {
                continue;
            }

            double y1 = footprintYd[i];
            double y2 = footprintYd[j];
            // System.out.println("y1="+y1+ "\ty2="+y2);

            // find crossing edge for left cut
            double x = cutLeftX;
            if ((x1 >= x && x2 <= x) || (x1 <= x && x2 >= x)) {
                if (MathHelper.isDifferenceTiny(x1, x2)) {
                    cutLeftYmax = Math.max(cutLeftYmax, y1);
                    cutLeftYmin = Math.min(cutLeftYmin, y1);
                    cutLeftYmax = Math.max(cutLeftYmax, y2);
                    cutLeftYmin = Math.min(cutLeftYmin, y2);
                } else {
                    double y = y1 + (y2 - y1) / (x2 - x1) * (x - x1);
                    // System.out.println("curLeft i=" + i + " y="+y);
                    cutLeftYmax = Math.max(cutLeftYmax, y);
                    cutLeftYmin = Math.min(cutLeftYmin, y);
                }
            }

            // find crossing edge for right cut
            x = cutRightX;
            if ((x1 >= x && x2 <= x) || (x1 <= x && x2 >= x)) {
                if (MathHelper.isDifferenceTiny(x1, x2)) {
                    cutRightYmax = Math.max(cutRightYmax, y1);
                    cutRightYmin = Math.min(cutRightYmin, y1);
                    cutRightYmax = Math.max(cutRightYmax, y2);
                    cutRightYmin = Math.min(cutRightYmin, y2);
                } else {
                    double y = y1 + (y2 - y1) / (x2 - x1) * (x - x1);
                    // System.out.println("curRight i=" + i + " y="+y);
                    cutRightYmax = Math.max(cutRightYmax, y);
                    cutRightYmin = Math.min(cutRightYmin, y);
                }
            }
        }

        double cutLeftYmean = (cutLeftYmax + cutLeftYmin) * 0.5;
        double cutRightYmean = (cutRightYmax + cutRightYmin) * 0.5;

        double centrencyParallelFlight = (cutRightX + cutLeftX) * 0.5;
        double centrencyInFlight = (cutLeftYmean + cutRightYmean) * 0.5;
        double sizeParallelFlight = cutRightDx - cutLeftDx;
        double sizeInFlight = high;
        double leftOvershoot = cutLeftYmean - cutRightYmean;

        // calculate area efficiency
        double areaTotal = 0;
        for (int i = 0; i != 4; i++) {
            int j = (i + 1) % 4;
            double x1 = footprintXd[i];
            double x2 = footprintXd[j];
            double y1 = footprintYd[i];
            double y2 = footprintYd[j];
            areaTotal += (x1 + x2) * (y1 - y2);
        }

        areaTotal = Math.abs(0.5 * areaTotal);

        double areaInner = high * sizeParallelFlight;
        double efficiency = areaInner / areaTotal;

        double pixelEnlargingCenter =
            Math.sqrt(
                    alt * alt
                        + centrencyParallelFlight * centrencyParallelFlight
                        + centrencyInFlight * centrencyInFlight)
                / alt;

        photoProperties.setCentrencyInFlight(centrencyInFlight);
        photoProperties.setCentrencyParallelFlight((float) centrencyParallelFlight);
        photoProperties.setEfficiency(efficiency);
        photoProperties.setLeftOvershoot(leftOvershoot);
        photoProperties.setPixelEnlargingCenter(pixelEnlargingCenter);
        photoProperties.setSizeInFlight(sizeInFlight);
        photoProperties.setSizeParallelFlight(sizeParallelFlight);
        return photoProperties;
    }

    public static Vector3f[] getCornerDirections(Camera cam) {

        // all 4 corners, of an image where the center of the left border is
        // pointing north, and the center of the upper border is pointing West
        // results are the same frame as in @see
        // gov.nasa.worldwind.globes.EllipsoidalGlobe.computeSurfaceOrientationAtPosition(gov.nasa.worldwind.geom.Position)
        //
        // They X axis is mapped to the vector tangent to the globe and pointing East. The Y
        // axis is mapped to the vector tangent to the Globe and pointing to the North Pole. The Z axis is mapped to the
        // Globe normal at (latitude, longitude, metersElevation).
        // The coordinates must be specified in counter-clockwise order with the first coordinate
        // corresponding to the lower-left corner of the overlayed image.

        // System.out.println();
        // System.out.println("camName" + getName());
        // System.out.println("witdth" + ccdWidth+ " shiftY:" + ccdshiftYTransl);
        // System.out.println("height" + ccdHeight + " shiftX:" + ccdXTransl);

        float length = (float)cam.getFocalLength();
        float y = (float)(cam.getCcdHeight() / 2);
        float x = (float)(cam.getCcdWidth() / 2);
        float yTransl = (float)cam.getCcdYTransl();
        float xTransl = (float)cam.getCcdXTransl();
        return new Vector3f[] {
            new Vector3f(y + yTransl, -x + xTransl, -length), new Vector3f(y + yTransl, x + xTransl, -length),
            new Vector3f(-y + yTransl, x + xTransl, -length), new Vector3f(-y + yTransl, -x + xTransl, -length),
        };
    }

    private static Matrix3f getCameraJustageTransform(Camera cam) {
        return new Matrix3f();
    }

}
