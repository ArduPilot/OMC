/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import java.util.function.Consumer;
import org.apache.commons.math3.util.FastMath;

public class LatLongInterpolationUtils {
    // TODO: probably want to use mean readius not equitorial radius
    static final double EARTH_RADIUS = Earth.WGS84_EQUATORIAL_RADIUS;
    static final double EARTH_RADIUS_INVERSE = 1.0D / Earth.WGS84_EQUATORIAL_RADIUS;

    static final double DISTANCE_THRESHOLD_RAD = 5000D /* meters */ / EARTH_RADIUS;
    static final double LATITUDE_THRESHOLD_DEG = 75.0D;

    private LatLongInterpolationUtils() {}

    /** Interpolates between a pair of LatLon points */
    public abstract static class LatLongPairInterpolator {
        public final LatLon p1;
        public final LatLon p2;
        public final boolean is3D;
        public final double elev1;
        public final double deltaElev;

        LatLongPairInterpolator(LatLon p1, LatLon p2) {
            this.p1 = p1;
            this.p2 = p2;
            is3D = p1 instanceof Position && p2 instanceof Position;
            if (is3D) {
                elev1 = ((Position)p1).elevation;
                deltaElev = ((Position)p2).elevation - elev1;
            } else {
                elev1 = 0;
                deltaElev = 0;
            }
        }

        protected LatLon maybeMake3d(LatLon latLon, double step) {
            if (is3D) {
                return new Position(latLon, elev1 + step * deltaElev);
            } else {
                return latLon;
            }
        }

        /**
         * Calculates interpolation between p1 and p2
         *
         * @param t amount - in range [0, 1.0] (so 0.1 would be p1 and 1.0 would be p2)
         * @return point
         */
        public abstract LatLon interpolate(double t);

        /**
         * Generate samples along at some samplings, will include edges p1 and p2. I guess this should probably return
         * an Iterable or something, but whatever...
         *
         * @param sampleDistance in meters
         * @param consumer to receive each intermediate LatLong sample
         */
        public abstract void sampleAtDistance(double sampleDistance, Consumer<LatLon> consumer);
    }

    /**
     * Interpolate between two lat longs using the arc of great circle path. Accurate, but slow.
     *
     * <p>Better than repeatedly calling {@link LatLon#interpolateGreatCircle} because it caches expensive azimuth and
     * distance calculations.
     */
    public static final class GreatCircleLatLonInterpolator extends LatLongPairInterpolator {
        Angle azimuth;
        Angle distance;

        GreatCircleLatLonInterpolator(LatLon p1, LatLon p2) {
            super(p1, p2);
        }

        private void lazyInit() {
            if (azimuth == null || distance == null) {
                // things that are expensive
                azimuth = LatLon.greatCircleAzimuth(p1, p2);
                distance = LatLon.greatCircleDistance(p1, p2);
            }
        }

        @Override
        public LatLon interpolate(double t) {
            lazyInit();

            Angle pathLength = Angle.fromDegrees(t * distance.degrees);
            return LatLon.greatCircleEndPosition(p1, azimuth, pathLength);
        }

        @Override
        public void sampleAtDistance(double sampleDistance, Consumer<LatLon> consumer) {
            lazyInit();

            final int steps = (int)Math.ceil(distance.radians / (sampleDistance * EARTH_RADIUS_INVERSE));
            double stepsize = steps > 1 ? 1.0 / steps : 0.0;

            consumer.accept(p1);
            stepsize *= distance.degrees;
            for (int step = 1; step < steps; step++) {
                double percent = step * stepsize;
                Angle pathLength = Angle.fromDegrees(percent);
                LatLon p = LatLon.greatCircleEndPosition(p1, azimuth, pathLength);
                p = maybeMake3d(p, percent);
                consumer.accept(p);
            }

            consumer.accept(p2);
        }
    }

    /**
     * Linearly interpolates between two LatLons. Fast but not as accurate as using spheroid earth model. <b>will break
     * if crossing date lines!</b>
     */
    public static final class LinearLatLonInterpolator extends LatLongPairInterpolator {
        final Line line;
        final double dist;

        LinearLatLonInterpolator(LatLon p1, LatLon p2) {
            super(p1, p2);
            line =
                Line.fromSegment(
                    new Vec4(p1.getLongitude().radians, p1.getLatitude().radians, 0.0D),
                    new Vec4(p2.getLongitude().radians, p2.getLatitude().radians, 0.0D));
            dist = linearDistanceRadians(p1, p2);
        }

        @Override
        public LatLon interpolate(double t) {
            Vec4 v = line.getPointAt(t);
            return LatLon.fromRadians(v.y, v.x);
        }

        @Override
        public void sampleAtDistance(double sampleDistance, Consumer<LatLon> consumer) {
            final int steps = (int)Math.ceil(dist / (sampleDistance * EARTH_RADIUS_INVERSE));
            final double stepsize = steps > 1 ? 1.0 / steps : 0.0;

            consumer.accept(p1);
            for (int step = 1; step < steps; step++) {
                double percent = step * stepsize;
                Vec4 v = line.getPointAt(percent);
                LatLon p = LatLon.fromRadians(v.y, v.x);
                p = maybeMake3d(p, percent);
                consumer.accept(p);
            }

            consumer.accept(p2);
        }
    }

    public static final class SamePointInterpolator extends LatLongPairInterpolator {

        SamePointInterpolator(LatLon p1, LatLon p2) {
            super(p1, p2);
            assert (p1.equals(p2));
        }

        @Override
        public LatLon interpolate(double t) {
            return p1;
        }

        @Override
        public void sampleAtDistance(double sampleDistance, Consumer<LatLon> consumer) {
            consumer.accept(p1);
            consumer.accept(p1);
        }
    }
    /**
     * a faster version of {@link LatLon#linearDistance} using {@link FastMath#hypot}
     *
     * @param p1 LatLon of the first location
     * @param p2 LatLon of the second location
     * @return the arc length of the line between the two locations in radians
     */
    public static double linearDistanceRadians(final LatLon p1, final LatLon p2) {
        double lat1 = p1.getLatitude().radians;
        double lon1 = p1.getLongitude().radians;
        double lat2 = p2.getLatitude().radians;
        double lon2 = p2.getLongitude().radians;
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        } else {
            double dLat = lat2 - lat1;
            double dLon = lon2 - lon1;
            if (Math.abs(dLon) > 3.141592653589793D) {
                dLon = dLon > 0.0D ? -(6.283185307179586D - dLon) : 6.283185307179586D + dLon;
            }

            double distanceRadians = FastMath.hypot(dLat, dLon); // almost 20x faster than Math.hypot
            return Double.isNaN(distanceRadians) ? 0.0 : distanceRadians;
        }
    }

    /**
     * Creates a LatLongPairInterpolator, preferring the fast linear interpolation if when it's reasonable to do so
     * because distance is small and not near the poles
     *
     * @param p1
     * @param p2
     * @return
     */
    public static LatLongPairInterpolator makeFastInterpolatorIfSafe(final LatLon p1, final LatLon p2) {
        double dist = linearDistanceRadians(p1, p2);
        double p1_lat = p1.getLatitude().getDegrees();
        double p2_lat = p2.getLatitude().getDegrees();

        if (p1.latitude.equals(p2.latitude) && p1.longitude.equals(p2.longitude)) {
            // dont compare entire latLon object, since this coudld actually be a Position object with different heights
            // and those would still not work in the other interpolators
            return new SamePointInterpolator(p1, p2);
        } else if (dist > DISTANCE_THRESHOLD_RAD
                || LatLon.locationsCrossDateline(p1, p2) // todo: faster check?
                || p1_lat > LATITUDE_THRESHOLD_DEG
                || p1_lat < -LATITUDE_THRESHOLD_DEG
                || p2_lat > LATITUDE_THRESHOLD_DEG
                || p2_lat < -LATITUDE_THRESHOLD_DEG) {
            // use slow path because we are somewhere annoying
            return new GreatCircleLatLonInterpolator(p1, p2);
        } else {
            return new LinearLatLonInterpolator(p1, p2);
        }
    }

}
