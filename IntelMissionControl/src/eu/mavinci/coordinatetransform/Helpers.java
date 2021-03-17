/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.coordinatetransform;

import com.intel.missioncontrol.helper.Ensure;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

import java.util.Vector;

public class Helpers {

    public static Vector<Vec4> convertPositionsToVec4(Vector<Position> posVec) {
        Vector<Vec4> vec = new Vector<Vec4>();
        for (int i = 0; i != posVec.size(); i++) {
            vec.addElement(
                new Vec4(posVec.get(i).longitude.degrees, posVec.get(i).latitude.degrees, posVec.get(i).elevation));
        }

        return vec;
    }

    public static Vector<Position> convertVec4ToPositions(Vector<Vec4> vec) {
        Vector<Position> posVec = new Vector<Position>();
        for (int i = 0; i != vec.size(); i++) {
            posVec.addElement(Position.fromDegrees(vec.get(i).y, vec.get(i).x, vec.get(i).z));
        }

        return posVec;
    }

    public static Vector<Vec4> convertArrayToVec4(double[][] arr) {
        Vector<Vec4> vec = new Vector<Vec4>();
        for (int i = 0; i != arr.length; i++) {
            vec.addElement(new Vec4(arr[i][0], arr[i][1], arr[i][2]));
        }

        return vec;
    }

    public static double[][] convertVec4ToArray(Vector<Vec4> vec) {
        Ensure.notNull(vec, "vec");
        double[][] arr = new double[vec.size()][3];
        for (int i = 0; i != vec.size(); i++) {
            arr[i][0] = vec.get(i).x;
            arr[i][1] = vec.get(i).y;
            arr[i][2] = vec.get(i).z;
        }

        return arr;
    }

    public static Vector<Vec4> transform(Vector<Vec4> orgVec, SpatialReference srsOrg, SpatialReference srsTarget) {
        CoordinateTransformation trafo = new CoordinateTransformation(srsOrg, srsTarget);

        // long start = System.currentTimeMillis();
        double[][] arr = convertVec4ToArray(orgVec);
        trafo.TransformPoints(arr);
        // System.out.println("function evaluation took in milliseconds:" +(System.currentTimeMillis()-start));

        return convertArrayToVec4(arr);
    }

    private static SpatialReference srsWGS84;

    static {
        srsWGS84 = new SpatialReference();
        srsWGS84.SetWellKnownGeogCS("WGS84");
    }

    public static Vector<Position> transformToWgs84Positions(Vector<Vec4> vec, SpatialReference srs) {
        return convertVec4ToPositions(transform(vec, srs, srsWGS84));
    }

    /*
     * Finds the RMS deviation (in meters) between two data sets of Position (Latitude/Longitude/elevation) coordinates with given
     * Ellipsoids
     */
    public static double getDeviation(
            Vector<Position> posVec1, Ellipsoid ellipsoid1, Vector<Position> posVec2, Ellipsoid ellipsoid2) {
        return getResidualError(
            EcefCoordinate.fromPositionVector(posVec1, ellipsoid1),
            EcefCoordinate.fromPositionVector(posVec2, ellipsoid2));
    }

    public static double getDeviationWithSrs(
            Vector<Position> orgPositionsWgs84, Vector<Vec4> targetList, SpatialReference targetSrs) {
        // Test transformation
        Vector<Position> targetPositionsOptimizedWgs84 = Helpers.transformToWgs84Positions(targetList, targetSrs);
        double err =
            Helpers.getDeviation(
                orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid, targetPositionsOptimizedWgs84, Ellipsoid.wgs84Ellipsoid);

        if (Double.isNaN(err)) {
            return Double.NaN;
        }

        // Test back transformation as well to ensure accuracy

        // System.out.println("WGS84->local->WGS84 calculation:");
        SpatialReference srsWgs84 = new SpatialReference();
        srsWgs84.SetWellKnownGeogCS("WGS84");

        Vector<Vec4> targetList2 =
            Helpers.transform(Helpers.convertPositionsToVec4(orgPositionsWgs84), srsWgs84, targetSrs);
        Vector<Position> targetPositions2 = Helpers.transformToWgs84Positions(targetList2, targetSrs);
        double err2 =
            Helpers.getDeviation(
                orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid, targetPositions2, Ellipsoid.wgs84Ellipsoid);

        if (Double.isNaN(err2)) {
            return Double.NaN;
        }

        return Double.max(err, err2);
    }

    /*
     * Finds the RMS deviation (in meters) between two data sets of ECEF (XYZ) coordinates
     */
    public static double getResidualError(Vector<EcefCoordinate> list1, Vector<EcefCoordinate> list2) {
        if (list1.size() != list2.size()) {
            throw new IllegalArgumentException("Input vectors need to have the same length");
        }

        double errsum = 0;
        for (int i = 0; i != list1.size(); i++) {
            errsum += list1.get(i).distanceToSquared3(list2.get(i));
        }

        return Math.sqrt(errsum / list1.size());
    }

    /*
     * Finds the RMS deviation (in meters) between two data sets of Position (Latitude/Longitude/elevation) coordinates with given
     * Ellipsoids
     */
    public static double[] getDeviationArray(
            Vector<Position> posVec1, Ellipsoid ellipsoid1, Vector<Position> posVec2, Ellipsoid ellipsoid2) {
        return getResidualErrorArray(
            EcefCoordinate.fromPositionVector(posVec1, ellipsoid1),
            EcefCoordinate.fromPositionVector(posVec2, ellipsoid2));
    }

    /*
     * Finds the RMS deviation (in meters) between two data sets of ECEF (XYZ) coordinates
     */
    public static double[] getResidualErrorArray(Vector<EcefCoordinate> list1, Vector<EcefCoordinate> list2) {
        if (list1.size() != list2.size()) {
            throw new IllegalArgumentException("Input vectors need to have the same length");
        }

        double[] err = new double[list1.size()];
        // double errsum=0;
        for (int i = 0; i != list1.size(); i++) {
            err[i] = Math.sqrt(list1.get(i).distanceToSquared3(list2.get(i)));
            // errsum += list1.get(i).distanceToSquared3(list2.get(i));
        }

        return err; // Math.sqrt(errsum / list1.size());
    }

}
