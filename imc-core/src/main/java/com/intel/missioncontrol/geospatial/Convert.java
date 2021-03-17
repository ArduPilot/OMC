/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

import com.intel.missioncontrol.geometry.Vec3;

public class Convert {

    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double RAD_TO_DEG = 180.0 / Math.PI;

    private static final double WGS84_EQUATORIAL_RADIUS = 6378137.0; // ellipsoid equatorial getRadius, in meters
    private static final double WGS84_ES = 0.00669437999013; // eccentricity squared, semi-major axis

    public static double degreesToRadians(double degrees) {
        return degrees * DEG_TO_RAD;
    }

    public static double radiansToDegrees(double radians) {
        return radians * RAD_TO_DEG;
    }

    /**
     * Maps a position to ellipsoidal coordinates. The Y axis points to the north pole. The Z axis points to the
     * intersection of the prime meridian and the equator, in the equatorial plane. The X axis completes a right-handed
     * coordinate system, and is 90 degrees east of the Z axis and also in the equatorial plane. Sea level is at z =
     * zero.
     *
     * @param latitude the latitude of the position.
     * @param longitude the longitude of the position.
     * @param elevation the number of meters above or below mean sea level.
     * @return The ellipsoidal point corresponding to the input position.
     * @see #ecefToGeodetic(Vec3)
     */
    public static Vec3 geodeticToEcef(double latitude, double longitude, double elevation) {
        double cosLat = Math.cos(latitude);
        double sinLat = Math.sin(latitude);
        double cosLon = Math.cos(longitude);
        double sinLon = Math.sin(longitude);

        double rpm = // getRadius (in meters) of vertical in prime meridian
            WGS84_EQUATORIAL_RADIUS / Math.sqrt(1.0 - WGS84_ES * sinLat * sinLat);

        double x = (rpm + elevation) * cosLat * sinLon;
        double y = (rpm * (1.0 - WGS84_ES) + elevation) * sinLat;
        double z = (rpm + elevation) * cosLat * cosLon;

        return new Vec3(x, y, z);
    }
    /**
     * Compute the geographic position to corresponds to an ellipsoidal point.
     *
     * @param point Ellipsoidal point to convert to geographic.
     * @return The geographic position of {@code point}.
     * @see #geodeticToEcef(double, double, double)
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static Position ecefToGeodetic(Vec3 point) {
        // Contributed by Nathan Kronenfeld. Integrated 1/24/2011. Brings this calculation in line with Vermeille's
        // most recent update.
        if (point == null) {
            throw new IllegalArgumentException("point");
        }

        // According to H. Vermeille,
        // "An analytical method to transform geocentric into geodetic coordinates"
        // http://www.springerlink.com/content/3t6837t27t351227/fulltext.pdf
        // Journal of Geodesy, accepted 10/2010, not yet published
        double X = point.z;
        double Y = point.x;
        double Z = point.y;
        double XXpYY = X * X + Y * Y;
        double sqrtXXpYY = Math.sqrt(XXpYY);

        double a = WGS84_EQUATORIAL_RADIUS;
        double ra2 = 1 / (a * a);
        double e2 = WGS84_ES;
        double e4 = e2 * e2;

        // Step 1
        double p = XXpYY * ra2;
        double q = Z * Z * (1 - e2) * ra2;
        double r = (p + q - e4) / 6;

        double h;
        double phi;

        double evoluteBorderTest = 8 * r * r * r + e4 * p * q;
        if (evoluteBorderTest > 0 || q != 0) {
            double u;

            if (evoluteBorderTest > 0) {
                // Step 2: general case
                double rad1 = Math.sqrt(evoluteBorderTest);
                double rad2 = Math.sqrt(e4 * p * q);

                // 10*e2 is my arbitrary decision of what Vermeille means by "near... the cusps of the evolute".
                if (evoluteBorderTest > 10 * e2) {
                    double rad3 = Math.cbrt((rad1 + rad2) * (rad1 + rad2));
                    u = r + 0.5 * rad3 + 2 * r * r / rad3;
                } else {
                    double r0 = 0.5 * Math.cbrt((rad1 + rad2) * (rad1 + rad2));
                    double r1 = 0.5 * Math.cbrt((rad1 - rad2) * (rad1 - rad2));
                    u = r + r0 + r1;
                }
            } else {
                // Step 3: near evolute
                double rad1 = Math.sqrt(-evoluteBorderTest);
                double rad2 = Math.sqrt(-8 * r * r * r);
                double rad3 = Math.sqrt(e4 * p * q);
                double atan = 2 * Math.atan2(rad3, rad1 + rad2) / 3;
                u = -4 * r * Math.sin(atan) * Math.cos(Math.PI / 6 + atan);
            }

            double v = Math.sqrt(u * u + e4 * q);
            double w = e2 * (u + v - q) / (2 * v);
            double k = (u + v) / (Math.sqrt(w * w + u + v) + w);
            double D = k * sqrtXXpYY / (k + e2);
            double sqrtDDpZZ = Math.sqrt(D * D + Z * Z);

            h = (k + e2 - 1) * sqrtDDpZZ / k;
            phi = 2 * Math.atan2(Z, sqrtDDpZZ + D);
        } else {
            // Step 4: singular disk
            double rad1 = Math.sqrt(1 - e2);
            double rad2 = Math.sqrt(e2 - p);
            double e = Math.sqrt(e2);

            h = -a * rad1 * rad2 / e;
            phi = rad2 / (e * rad2 + rad1 * Math.sqrt(p));
        }

        // Compute lambda
        double lambda;
        double s2 = Math.sqrt(2);
        if ((s2 - 1) * Y < sqrtXXpYY + X) {
            // case 1 - -135deg < lambda < 135deg
            lambda = 2 * Math.atan2(Y, sqrtXXpYY + X);
        } else if (sqrtXXpYY + Y < (s2 + 1) * X) {
            // case 2 - -225deg < lambda < 45deg
            lambda = -Math.PI * 0.5 + 2 * Math.atan2(X, sqrtXXpYY - Y);
        } else {
            // if (sqrtXXpYY-Y<(s2=1)*X) {  // is the test, if needed, but it's not
            // case 3: - -45deg < lambda < 225deg
            lambda = Math.PI * 0.5 - 2 * Math.atan2(X, sqrtXXpYY + Y);
        }

        return Position.fromRadians(phi, lambda, h);
    }

}
