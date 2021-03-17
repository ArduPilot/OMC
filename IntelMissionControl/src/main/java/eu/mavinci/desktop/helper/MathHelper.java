/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import eu.mavinci.core.helper.CMathHelper;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.flightplan.computation.LocalTransformationProvider;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import java.util.Arrays;
import java.util.Vector;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;

public class MathHelper extends CMathHelper {

    public static boolean isValid(LatLon latLon) {
        return isValid(latLon.latitude.degrees) && isValid(latLon.longitude.degrees);
    }

    public static boolean isValid(Position p) {
        LatLon latLon = p;
        return isValid(latLon) && isValid(p.elevation);
    }

    public static int roundInt(double d) {
        return (int)Math.round(d);
    }

    public static int roundIntMin1(double d) {
        return (int)Math.round(Math.max(1, d));
    }

    public static String coordToUTMzone(LatLon latLon) {
        double lat = latLon.getLatitude().degrees;
        double lon = latLon.getLongitude().degrees;
        lon += 180;
        lon /= 6;
        lon = (int)lon;
        lon += 1;
        return lon + (lat >= 0 ? "N" : "S");
    }

    public static String coordToUTMzoneEPSG(LatLon latLon) {
        double lat = latLon.getLatitude().degrees;
        double lon = latLon.getLongitude().degrees;
        lon += 186;
        lon /= 6;
        int no = 32600 + (int)lon;
        if (lat < 0) no += 100;
        return "EPSG::" + no;
    }

    /**
     * keeping mavinci angles, where roll has a different sign than the norm
     *
     * @param roll in deg
     * @param pitch in deg
     * @param yaw in deg
     * @return
     */
    public static Matrix getRollPitchYawTransformationMAVinicAngles(double roll, double pitch, double yaw) {
        return getRollPitchYawTransformation(-roll, pitch, yaw);
    }

    /**
     * according to the wikipedia angle convention, but applied in the WW reference frame
     *
     * @param roll in deg
     * @param pitch in deg
     * @param yaw in deg
     * @return
     */
    public static Matrix getRollPitchYawTransformation(double roll, double pitch, double yaw) {
        Matrix transform =
            Matrix.fromRotationY(Angle.fromDegrees(-roll)); // fromRotation was different sign excepted than the NORM
        transform =
            transform.multiply(
                Matrix.fromRotationX(
                    Angle.fromDegrees(-pitch))); // fromRotation was different sign excepted than the NORM
        transform =
            transform.multiply(
                Matrix.fromRotationZ(
                    Angle.fromDegrees(
                        yaw))); // -1*-1=+1 -> Z is pointing in the wrong direction AND rotation has different
        // orientation
        return transform;
    }

    /**
     * @param transform a transformation in the WWJ reference system
     * @return double vector of size 3, with {roll,pitch,yaw} in official system (without mavinci sign bug) in degree
     */
    public static double[] transformationToRollPitchYaw(Matrix transform) {
        double[] ret = new double[3];

        ret[0] = transform.getKMLRotationY().degrees;
        ret[1] = transform.getKMLRotationX().degrees;
        ret[2] = -transform.getKMLRotationZ().degrees;

        return ret;
    }

    /**
     * @param transform a transformation in the WWJ reference system
     * @return double vector of size 3, with {omega,phi,kappa} in official system (without mavinci sign bug) in degree
     */
    public static double[] transformationToOmegaPhiKappa(Matrix transform) {
        double[] ret = new double[3];

        ret[0] = -transform.getRotationY().degrees;
        ret[1] = -transform.getRotationX().degrees;
        ret[2] = transform.getRotationZ().degrees;

        return ret;
    }

    public static double[] transformationToRollPitchYawMavinciAngles(Matrix transfrom) {
        double[] ret = transformationToRollPitchYaw(transfrom);
        ret[0] = -ret[0];
        return ret;
    }

    public static final Matrix tranformNED_WW =
        Matrix.fromAxes(new Vec4[] {new Vec4(0, 1, 0), new Vec4(1, 0, 0), new Vec4(0, 0, -1)});

    public static final Matrix tranformWW_NED = tranformNED_WW.getTranspose();

    public static boolean isDifferenceTiny(double a, double b) {
        return Math.abs(a - b) < 1e-10 * (Math.abs(a) + Math.abs(b));
    }

    public static class LineSegment {
        public final Vec4 first;
        public final Vec4 second;

        public LineSegment(Vec4 first, Vec4 second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public String toString() {
            return "[" + first + " - " + second + "]";
        }
    }

    public static LineSegment shortestLineBetween(LineSegment a, LineSegment b, boolean clamp) {
        Vec4 A = a.second.subtract3(a.first);
        Vec4 B = b.second.subtract3(b.first);
        double magA = A.getLength3();
        double magB = B.getLength3();

        if (magA == 0) {
            if (magB == 0) {
                return new LineSegment(a.first, b.first);
            } else {
                return new LineSegment(a.first, Line.nearestPointOnSegment(b.first, b.second, a.first));
            }
        } else if (magB == 0) {
            return new LineSegment(Line.nearestPointOnSegment(a.first, a.second, b.first), b.first);
        }

        Vec4 _A = A.divide3(magA);
        Vec4 _B = B.divide3(magB);

        Vec4 cross = _A.cross3(_B);
        double denom = cross.getLengthSquared3();

        if (isDifferenceTiny(denom, 0)) {
            double d0 = _A.dot3(b.first.subtract3(a.first));

            if (clamp) {
                double d1 = _A.dot3(b.second.subtract3(a.first));

                if (d0 <= 0 && 0 >= d1) {
                    if (Math.abs(d0) < Math.abs(d1)) {
                        return new LineSegment(a.first, b.first);
                    }

                    return new LineSegment(a.first, b.second);
                } else if (d0 >= magA && magA <= d1) {
                    if (Math.abs(d0) < Math.abs(d1)) {
                        return new LineSegment(a.second, b.first);
                    }

                    return new LineSegment(a.second, b.second);
                }
            }

            return new LineSegment(a.first, b.first);
        }

        Vec4 t = b.first.subtract3(a.first);
        double detA =
            new Matrix(t.x, t.y, t.z, t.w, _B.x, _B.y, _B.z, _B.w, cross.x, cross.y, cross.z, cross.w, 0, 0, 0, 1)
                .getDeterminant();
        double detB =
            new Matrix(t.x, t.y, t.z, t.w, _A.x, _A.y, _A.z, _A.w, cross.x, cross.y, cross.z, cross.w, 0, 0, 0, 1)
                .getDeterminant();
        double t0 = detA / denom;
        double t1 = detB / denom;

        Vec4 pA = a.first.add3(_A.multiply3(t0));
        Vec4 pB = b.first.add3(_B.multiply3(t1));

        if (clamp) {
            if (t0 < 0) {
                pA = a.first;
            } else if (t0 > magA) {
                pA = a.second;
            }

            if (t1 < 0) {
                pB = b.first;
            } else if (t1 > magB) {
                pB = b.second;
            }

            if (t0 < 0 || t0 > magA) {
                double dot = _B.dot3(pA.subtract3(b.first));
                if (dot < 0) {
                    dot = 0;
                } else if (dot > magB) {
                    dot = magB;
                }

                pB = b.first.add3(_B.multiply3(dot));
            }

            if (t1 < 0 || t1 > magB) {
                double dot = _A.dot3(pB.subtract3(a.first));
                if (dot < 0) {
                    dot = 0;
                } else if (dot > magA) {
                    dot = magA;
                }

                pA = a.first.add3(_A.multiply3(dot));
            }
        }

        return new LineSegment(pA, pB);
    }

    public static double acosFast(double x) {
        return (-0.69813170079773212 * x * x - 0.87266462599716477) * x + 1.5707963267948966;
        //	   return 1.57079-1.57079*x;
    }

    private static interface IGetNextStep {
        public double getNextStep(MinMaxPair range, double leftVal, double rightVal, double startStep);

    }

    private static class NestedStepper implements IGetNextStep {

        @Override
        public double getNextStep(MinMaxPair range, double leftVal, double rightVal, double startStep) {
            if (rightVal * leftVal < 0) {
                return range.mean();
            } else if (startStep < 1) {
                return range.min * startStep;
            } else {
                return range.max * startStep;
            }
        }

    }

    static NestedStepper nestedStepper = new NestedStepper();

    private static class LinearApproxStepper implements IGetNextStep {

        @Override
        public double getNextStep(MinMaxPair range, double leftVal, double rightVal, double startStep) {
            if (rightVal * leftVal < 0) {
                return range.mean();
            } else if (startStep < 1) {
                return range.min * startStep;
            } else {
                return range.max * startStep;
            }
        }

    }

    static LinearApproxStepper linearApproxStepper = new LinearApproxStepper();

    public static MinMaxPair findRoot1D(
            IObjectiveFuntion func,
            double startX,
            Double stopX,
            double startStep,
            double requestedAcc,
            int maxIter,
            IGetNextStep stepper)
            throws Exception {
        MinMaxPair range = new MinMaxPair(startX, startX * startStep);
        double leftVal = func.getValue(range.min);
        if (leftVal == 0) return new MinMaxPair(range.min);
        double rightVal = func.getValue(range.max);
        if (rightVal == 0) return new MinMaxPair(range.max);

        if (leftVal * rightVal > 0) {
            if (startX == range.min && Math.abs(leftVal) < Math.abs(rightVal)) {
                startStep = 1 / startStep;
            } else if (startX == range.max && Math.abs(leftVal) > Math.abs(rightVal)) {
                startStep = 1 / startStep;
            }
        }

        int i = 1;
        while (true) {
            //			System.out.println(i+" curRange:" +range);
            // linear extrapolation to find root
            double xNew = nestedStepper.getNextStep(range, leftVal, rightVal, startStep);
            if (stopX != null && xNew > stopX) {
                xNew = stopX;
            }
            //			System.out.println(" leftVal:"+leftVal +" rightVal:"+rightVal+" startStep:"+startStep + " xNew:"+xNew);
            double valNew = func.getValue(xNew);
            //			System.out.println("new Value:"+valNew);
            if (valNew == 0) return new MinMaxPair(xNew);

            //			System.out.println("leftVal:"+ leftVal + "  rightVal:"+rightVal);
            //			System.out.println("xNEw:"+xNew + " ->  "+ valNew);

            if (xNew < range.min || (valNew * leftVal < 0 && xNew < range.max)) {
                range = new MinMaxPair(xNew, range.min);
                if (xNew == range.min) {
                    rightVal = leftVal;
                    leftVal = valNew;
                } else {
                    rightVal = valNew;
                }

                //				System.out.println("left");
            } else { // if (xNew > range.max || valNew*rightVal < 0 ) {
                range = new MinMaxPair(xNew, range.max);
                if (xNew == range.max) {
                    leftVal = rightVal;
                    rightVal = valNew;
                } else {
                    leftVal = valNew;
                }
                //				System.out.println("right");
            }

            if (range.size() <= requestedAcc || i >= maxIter) {
                //				System.out.println("took iter:" + i + " range:"+range);
                return range;
            }

            i++;
        }
    }

    public static MinMaxPair findRootNestedIntervals(
            IObjectiveFuntion func, double startX, double startStep, double requestedAcc, int maxIter)
            throws Exception {
        return findRoot1D(func, startX, null, startStep, requestedAcc, maxIter, nestedStepper);
    }

    public static MinMaxPair findRootNestedIntervals(
            IObjectiveFuntion func, double startX, double stopX, double startStep, double requestedAcc, int maxIter)
            throws Exception {
        return findRoot1D(func, startX, stopX, startStep, requestedAcc, maxIter, nestedStepper);
    }

    public static MinMaxPair findRootLinearApprox(
            IObjectiveFuntion func, double startX, double startStep, double requestedAcc, int maxIter)
            throws Exception {
        return findRoot1D(func, startX, null, startStep, requestedAcc, maxIter, linearApproxStepper);
    }

    public static double powerOfTenCeiling(double number) {
        int power = (int)Math.ceil(Math.log(number) / Math.log(10d));
        return Math.pow(10d, power);
    }

    public static double powerOfTenFloor(double number) {
        int power = (int)Math.floor(Math.log(number) / Math.log(10d));
        return Math.pow(10d, power);
    }

    public static class PCAresult {

        public double largestDiameter;
        public double smallestDiameter;

    }

    /**
     * computes the average minimal and maximal diameter of an ellipse fitted into a 2d LatLon cloud
     *
     * @param posList
     * @return
     */
    public static PCAresult pcaPositions(Iterable<? extends LatLon> posList) {
        LatLon pCenter = Position.getCenter(posList);
        LocalTransformationProvider trafo =
            new LocalTransformationProvider(new Position(pCenter, 0), Angle.ZERO, 0, 0, false);

        Vector<Vec4> vecList = new Vector<Vec4>();
        double xSum = 0;
        double ySum = 0;
        for (LatLon p : posList) {
            Vec4 v = trafo.transformToLocal(p);
            xSum += v.x;
            ySum += v.y;
            vecList.addElement(v);
        }

        xSum /= -vecList.size();
        ySum /= -vecList.size();
        for (int i = 0; i != vecList.size(); i++) {
            Vec4 v = vecList.get(i);
            v = v.add3(xSum, ySum, 0);
            vecList.set(i, v);
            System.out.println(v);
        }

        double xx = 0;
        double xy = 0;
        double yy = 0;
        for (int i = 0; i != vecList.size(); i++) {
            Vec4 v = vecList.get(i);
            xx += v.x * v.x;
            xy += v.x * v.y;
            yy += v.y * v.y;
        }

        xx /= vecList.size();
        xy /= vecList.size();
        yy /= vecList.size();
        //    	System.out.println();
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(new double[][] {{xx, xy}, {xy, yy}});
        //    	System.out.println(m);

        EigenDecomposition ed = new EigenDecomposition(m);
        double[] ev = ed.getRealEigenvalues();
        Arrays.sort(ev);
        //    	System.out.println("ev:"+Arrays.toString(ev));

        //    	double []im = ed.getImagEigenvalues();
        //    	System.out.println("im:" + Arrays.toString(im));
        //    	System.out.println("det:" +ed.getDeterminant());

        PCAresult res = new PCAresult();
        res.largestDiameter = Math.sqrt(ev[1]);
        res.smallestDiameter = Math.sqrt(ev[0]);
        return res;
    }



    public static Sector extendSector(Sector bbox, double bufferMeters) {
        double minLat = bbox.getMinLatitude().getDegrees();
        double maxLat = bbox.getMaxLatitude().getDegrees();
        double minLon = bbox.getMinLongitude().getDegrees();
        double maxLon = bbox.getMaxLongitude().getDegrees();

        return Sector.fromDegrees(
                minLat - getLatitudeDifferenceFor(bufferMeters),
                maxLat + getLatitudeDifferenceFor(bufferMeters),
                minLon - getLongitudeDifferenceFor(bufferMeters, minLat),
                maxLon + getLongitudeDifferenceFor(bufferMeters, maxLat));
    }

    private static double getLatitudeDifferenceFor(double bufferMeters) {
        return Math.toDegrees(bufferMeters / Earth.WGS84_POLAR_RADIUS);
    }

    private static double getLongitudeDifferenceFor(double bufferMeters, double latitude) {
        return Math.toDegrees(bufferMeters / Earth.WGS84_POLAR_RADIUS) / Math.cos(Math.toRadians(latitude));
    }


    public static double getTrueCourseRadians(LatLon pos1, LatLon pos2) {
        if (pos1 == null || pos2 == null)
            throw new IllegalArgumentException();
        double lat1 = pos1.getLatitude().getRadians();
        double lon1 = pos1.getLongitude().getRadians();
        double lat2 = pos2.getLatitude().getRadians();
        double lon2 = pos2.getLongitude().getRadians();
//		System.out.println("center " + pos1 + " to:" + pos2);

        if (lat1 == lat2 && lon1 == lon2)
            throw new IllegalArgumentException();
//		double tc = Math.atan2(Math.sin(lon1 - lon2) * Math.cos(lat2), Math
//				.cos(lat1)
//				* Math.sin(lat2)
//				- Math.sin(lat1)
//				* Math.cos(lat2)
//				* Math.cos(lon1 - lon2));
//		double old = tc % (2 * Math.PI);
//		System.out.println("old" + old);



        if (lat1 == lat2 && lon1 == lon2)
            return 0;

        // Taken from http://www.movable-type.co.uk/scripts/latlong.html
        double dLon = lon2 - lon1;
        double dPhi = Math.log(Math.tan(lat2 / 2.0 + Math.PI / 4.0) / Math.tan(lat1 / 2.0 + Math.PI / 4.0));
        // If lonChange over 180 take shorter rhumb across 180 meridian.
        if (Math.abs(dLon) > Math.PI)
        {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }
        double azimuthRadians = Math.atan2(dLon, dPhi);

//        System.out.println("new"+(azimuthRadians));

        return Double.isNaN(azimuthRadians) ? 0 : azimuthRadians;
    }
}
