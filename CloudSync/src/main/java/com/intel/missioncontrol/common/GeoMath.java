/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.common;

import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.Logging;

public class GeoMath {
    public static final double WGS84_EQUATORIAL_RADIUS = 6378137.0; // ellipsoid equatorial getRadius, in meters
    public static final double WGS84_POLAR_RADIUS = 6356752.3; // ellipsoid polar getRadius, in meters
    public static final double WGS84_ES = 0.00669437999013; // eccentricity squared, semi-major axis

    private enum ExcessOption {
        SATURATE,
        WRAP_AROUND
    }

    private static final Quantity<Angle> MAX_LAT = Quantity.of(Math.PI / 2, Unit.RADIAN);
    private static final Quantity<Angle> MIN_LAT = Quantity.of(-Math.PI / 2, Unit.RADIAN);
    private static final Quantity<Angle> MAX_LON = Quantity.of(Math.PI, Unit.RADIAN);
    private static final Quantity<Angle> MIN_LON = Quantity.of(-Math.PI, Unit.RADIAN);

    public static VariantQuantity addLat(VariantQuantity a, VariantQuantity b) {
        return addAngles(a, b, MIN_LAT, MAX_LAT, ExcessOption.SATURATE);
    }

    public static VariantQuantity addLon(VariantQuantity a, VariantQuantity b) {
        return addAngles(a, b, MIN_LON, MAX_LON, ExcessOption.WRAP_AROUND);
    }

    public static VariantQuantity subtractLat(VariantQuantity a, VariantQuantity b) {
        return addAngles(a, b.negate(), MIN_LAT, MAX_LAT, ExcessOption.SATURATE);
    }

    public static VariantQuantity subtractLon(VariantQuantity a, VariantQuantity b) {
        return addAngles(a, b.negate(), MIN_LON, MAX_LON, ExcessOption.WRAP_AROUND);
    }

    @SuppressWarnings("unchecked")
    private static VariantQuantity addAngles(
            VariantQuantity a, VariantQuantity b, Quantity<Angle> min, Quantity<Angle> max, ExcessOption excessOption) {
        Dimension dimA = a.getDimension();
        Dimension dimB = b.getDimension();

        if (dimA == Dimension.ANGLE && dimB == Dimension.ANGLE) {
            return addAnglesConstrained(a.convertTo(Unit.RADIAN), b.convertTo(Unit.RADIAN), min, max, excessOption)
                .convertTo((Unit<Angle>)a.getUnit())
                .toVariant();
        } else if (dimA == Dimension.ANGLE && dimB == Dimension.LENGTH) {
            return addAnglesConstrained(a.convertTo(Unit.RADIAN), getApproxAngleFromLength(b), min, max, excessOption)
                .convertTo((Unit<Angle>)a.getUnit())
                .toVariant();
        } else if (dimA == Dimension.LENGTH && dimB == Dimension.ANGLE) {
            return addAnglesConstrained(getApproxAngleFromLength(a), b.convertTo(Unit.RADIAN), min, max, excessOption)
                .convertTo((Unit<Angle>)b.getUnit())
                .toVariant();
        } else if (dimA != Dimension.ANGLE && dimA != Dimension.LENGTH) {
            throw new IllegalArgumentException("Unsupported dimension: a = " + dimA);
        } else if (dimB != Dimension.LENGTH) {
            throw new IllegalArgumentException("Unsupported dimension: b = " + dimB);
        }

        throw new IllegalArgumentException("Unsupported dimensions: a = " + dimA + ", b = " + dimB);
    }

    private static Quantity<Angle> addAnglesConstrained(
            Quantity<Angle> a, Quantity<Angle> b, Quantity<Angle> min, Quantity<Angle> max, ExcessOption excessOption) {
        Quantity<Angle> res = a.add(b);
        double overflow = res.subtract(min).getValue().doubleValue();
        if (overflow < 0) {
            if (excessOption == ExcessOption.SATURATE) {
                return min;
            }

            return max.add(overflow);
        }

        overflow = res.subtract(max).getValue().doubleValue();
        if (overflow > 0) {
            if (excessOption == ExcessOption.SATURATE) {
                return max;
            }

            return min.add(overflow);
        }

        return res;
    }

    private static Quantity<Angle> getApproxAngleFromLength(VariantQuantity length) {
        return Quantity.of(length.convertTo(Unit.METER).getValue().doubleValue() * (1. / 111_111.), Unit.DEGREE)
            .convertTo(Unit.RADIAN);
    }

    public static Quantity<Angle> metersOfLatitudeToDegrees(VariantQuantity length) {
        return Quantity.of(
            length.convertTo(Unit.METER).getValue().doubleValue() / WGS84_EQUATORIAL_RADIUS, Unit.RADIAN);
    }

    public static Quantity<Angle> metersOfLongitudeToDegrees(VariantQuantity length, VariantQuantity lat) {
        return Quantity.of(
            length.convertTo(Unit.METER).getValue().doubleValue()
                / (WGS84_EQUATORIAL_RADIUS
                    * Math.cos(Math.PI * lat.convertTo(Unit.DEGREE).getValue().doubleValue() / 180.0)),
            Unit.RADIAN);
    }

    public static Quantity<Dimension.Length> degreesOfLatitudeToMeters(VariantQuantity angle) {
        return Quantity.of(angle.convertTo(Unit.DEGREE).getValue().doubleValue() * (1. / 111_111.), Unit.METER)
            .convertTo(Unit.METER);
    }

    public static Quantity<Dimension.Length> degreesOfLongitudeToMeters(VariantQuantity angle) {
        return Quantity.of(angle.convertTo(Unit.DEGREE).getValue().doubleValue() * (1. / 111_111.), Unit.METER)
            .convertTo(Unit.METER);
    }

    // copied from the WW Globe

    /**
     * Maps a position to ellipsoidal coordinates. The Y axis points to the north pole. The Z axis points to the
     * intersection of the prime meridian and the equator, in the equatorial plane. The X axis completes a right-handed
     * coordinate system, and is 90 degrees east of the Z axis and also in the equatorial plane. Sea level is at z =
     * zero.
     *
     * @param latitude the latitude of the position.
     * @param longitude the longitude of the position.
     * @param metersElevation the number of meters above or below mean sea level.
     * @return The ellipsoidal point corresponding to the input position.
     * @see #EcefToGeodetic(gov.nasa.worldwind.geom.Vec4)
     */
    public static Vec4 geodeticToEcef(
            gov.nasa.worldwind.geom.Angle latitude, gov.nasa.worldwind.geom.Angle longitude, double metersElevation) {
        if (latitude == null || longitude == null) {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double cosLat = Math.cos(latitude.radians);
        double sinLat = Math.sin(latitude.radians);
        double cosLon = Math.cos(longitude.radians);
        double sinLon = Math.sin(longitude.radians);

        double rpm = // getRadius (in meters) of vertical in prime meridian
            WGS84_EQUATORIAL_RADIUS / Math.sqrt(1.0 - WGS84_ES * sinLat * sinLat);

        double x = (rpm + metersElevation) * cosLat * sinLon;
        double y = (rpm * (1.0 - WGS84_ES) + metersElevation) * sinLat;
        double z = (rpm + metersElevation) * cosLat * cosLon;

        return new Vec4(x, y, z);
    }
    /**
     * Compute the geographic position to corresponds to an ellipsoidal point.
     *
     * @param cart Ellipsoidal point to convert to geographic.
     * @return The geographic position of {@code cart}.
     * @see #geodeticToEcef(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle, double)
     */
    @SuppressWarnings({"SuspiciousNameCombination"})
    public static Position EcefToGeodetic(Vec4 cart) {
        // Contributed by Nathan Kronenfeld. Integrated 1/24/2011. Brings this calculation in line with Vermeille's
        // most recent update.
        if (null == cart) {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // According to
        // H. Vermeille,
        // "An analytical method to transform geocentric into geodetic coordinates"
        // http://www.springerlink.com/content/3t6837t27t351227/fulltext.pdf
        // Journal of Geodesy, accepted 10/2010, not yet published
        double X = cart.z;
        double Y = cart.x;
        double Z = cart.y;
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
                    u =
                        r
                            + 0.5 * Math.cbrt((rad1 + rad2) * (rad1 + rad2))
                            + 0.5 * Math.cbrt((rad1 - rad2) * (rad1 - rad2));
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

    public static Matrix computeEllipsoidalOrientationAtPosition(
            gov.nasa.worldwind.geom.Angle latitude, gov.nasa.worldwind.geom.Angle longitude, double metersElevation) {
        if (latitude == null || longitude == null) {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 point = geodeticToEcef(latitude, longitude, metersElevation);
        // Transform to the cartesian coordinates of (latitude, longitude, metersElevation).
        Matrix transform = Matrix.fromTranslation(point);
        // Rotate the coordinate system to match the longitude.
        // Longitude is treated as counter-clockwise rotation about the Y-axis.
        transform = transform.multiply(Matrix.fromRotationY(longitude));
        // Rotate the coordinate system to match the latitude.
        // Latitude is treated clockwise as rotation about the X-axis. We flip the latitude value so that a positive
        // rotation produces a clockwise rotation (when facing the axis).
        transform = transform.multiply(Matrix.fromRotationX(latitude.multiply(-1.0)));
        return transform;
    }

    private Matrix getTransformMatrix(Vec4 oldRefPoint, Vec4 newRefPoint, double oldYaw, double newYaw) {

        var deltaLat = newRefPoint.x - oldRefPoint.x;
        var deltaLon = newRefPoint.y - oldRefPoint.y;
        var deltaAlt = newRefPoint.z - oldRefPoint.z;
        double deltaYaw = newYaw - oldYaw;

        Matrix translation =
                Matrix.fromTranslation(
                        deltaLat,
                        deltaLon,
                        deltaAlt);
        Matrix rotation = Matrix.fromRotationZ(gov.nasa.worldwind.geom.Angle.fromDegrees(deltaYaw));

        return translation.multiply(rotation);
    }


}
