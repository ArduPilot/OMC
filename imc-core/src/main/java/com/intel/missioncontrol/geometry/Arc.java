/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import java.util.Locale;

/* An arc segment defined by a start and end angle, sweeping clockwise from start to end */
public class Arc {
    private final double startAngleRad;
    private final double endAngleRad;

    /**
     * Create an arc sweeping clockwise from startAngleRad to endAngleRad. Equal angles correspond to an arc constrained
     * to one angle, with arc length 0. The arc length is always shorter than 2*pi.
     *
     * @param startAngleRad The start angle, in radians, defining the counterclockwise limit of the arc. The arc spans
     *     from startAngleRad to endAngleRad in a clockwise sweep.
     * @param endAngleRad The end angle, in radians, defining the clockwise limit of the arc. The arc spans from
     *     startAngleRad to * endAngleRad in a clockwise sweep.
     */
    public Arc(double startAngleRad, double endAngleRad) {
        this.startAngleRad = normalizeRad(startAngleRad);
        this.endAngleRad = normalizeRad(endAngleRad);
    }

    /**
     * Create an arc sweeping clockwise from startAngleDeg to endAngleDeg. Equal values correspond to an arc constrained
     * to one angle, with arc length 0.
     *
     * @param startAngleDeg The start angle, in degrees, defining the counterclockwise limit of the arc. The arc spans
     *     from startAngleDeg to endAngleDeg in a clockwise sweep.
     * @param endAngleDeg The end angle, in degrees, defining the clockwise limit of the arc. The arc spans from
     *     startAngleDeg to * endAngleDeg in a clockwise sweep.
     */
    public static Arc fromAnglesDeg(double startAngleDeg, double endAngleDeg) {
        return new Arc(startAngleDeg * Math.PI / 180.0, endAngleDeg * Math.PI / 180.0);
    }

    /**
     * If angleDeg is within the arc, it is normalized to the -180° <.. 180° range and returned. Otherwise, the closest
     * angle at the edge of the arc is returned, also normalized to the -180° <.. 180° range.
     */
    public double limitToArcDeg(double angleDeg) {
        return toNormalizedDegrees(limitToArc(angleDeg * Math.PI / 180.0));
    }

    /**
     * If angleRad is within the arc, it is normalized to the 0..<2pi range and returned. Otherwise, the closest angle
     * at the edge of the arc is returned, also normalized to the 0..<2pi range.
     */
    public double limitToArc(double angleRad) {
        double a = normalizeRad(angleRad);

        if (contains(a)) {
            return a;
        }

        // measure distance (arc length) to start or end:
        double dStart = undirectedDistanceRad(a, startAngleRad);
        double dEnd = undirectedDistanceRad(a, endAngleRad);

        // return the angle with the smaller distance
        return (dStart <= dEnd) ? startAngleRad : endAngleRad;
    }

    public boolean contains(double angleRad) {
        double a = normalizeRad(angleRad);

        // current arc does not cross 0:
        if (endAngleRad >= startAngleRad) {
            return a >= startAngleRad && a <= endAngleRad;
        } else // current arc crosses 0:
        {
            return a >= startAngleRad || a <= endAngleRad;
        }
    }

    /**
     * Returns an arc that spans the current arc extended to include the given angle. If angleRad is within the current
     * arc, the current arc is returned. Otherwise, the arc is extended in a way that minimizes its arc length.
     */
    public Arc extendToInclude(double angleRad) {
        if (contains(angleRad)) {
            return this;
        }

        Arc arc1 = new Arc(startAngleRad, angleRad);
        Arc arc2 = new Arc(angleRad, endAngleRad);
        return (arc1.getArcLengthRad() <= arc2.getArcLengthRad()) ? arc1 : arc2;
    }

    /* The length of this arc, in radians, 0 ..< 2pi*/
    public double getArcLengthRad() {
        return normalizeRad(endAngleRad - startAngleRad);
    }

    /* The length of this arc, in degrees, 0° ..< 360° */
    public double getArcLengthDeg() {
        return getArcLengthRad() * 180.0 / Math.PI;
    }

    /** Return an array of linearly spaced angles covering the arc, with a given minimum angular resolution. */
    public double[] toLinearlySpacedAnglesDeg(double minResolutionDeg) {
        double arcLength = getArcLengthDeg();
        int n = (int)(arcLength / (minResolutionDeg * 1.01)) + 2;
        double actualResolutionDeg = arcLength / (double)(n - 1);
        double[] res = new double[n];

        double r = getMinAngleDeg();
        res[0] = r;
        for (int i = 1; i < n - 1; i++) {
            r += actualResolutionDeg;
            res[i] = r;
        }

        res[n - 1] = getMaxAngleDeg();
        return res;
    }

    /** Normalize angle in radians to 0 ..< 2pi */
    private static double normalizeRad(double angleRad) {
        double res = angleRad % (2 * Math.PI);
        return res >= 0 ? res : res + 2 * Math.PI;
    }

    /**
     * Measure the arc length distance between two angles, in radians, either clockwise or counterclockwise, whichever
     * is shorter. 0 ..<= pi
     */
    private static double undirectedDistanceRad(double angleRad1, double angleRad2) {
        double d1 = normalizeRad(angleRad1 - angleRad2);
        double d2 = normalizeRad(angleRad2 - angleRad1);
        return Math.min(d1, d2);
    }

    /**
     * Measure the arc length distance between two angles, both within the arc, in radians, 0 ..< 2pi. The order of the
     * arguments is not significant.
     */
    public double distanceWithinArcRad(double angleRad1, double angleRad2) {
        if (!contains(angleRad1) || !contains(angleRad2)) {
            throw new IllegalArgumentException("angleRad1 and angleRad2 must be contained in arc");
        }

        double a1 = normalizeRad(angleRad1);
        double a2 = normalizeRad(angleRad2);

        // make a1 the smaller normalized angle
        if (a2 < a1) {
            double tmp = a1;
            a1 = a2;
            a2 = tmp;
        }

        // current arc does not cross 0:
        if (endAngleRad >= startAngleRad) {
            return new Arc(a1, a2).getArcLengthRad();
        } else // current arc crosses 0:
        {
            return new Arc(a2, a1).getArcLengthRad();
        }
    }

    /**
     * Measure the arc length distance between two angles, both within the arc, in degrees, 0° ..< 360°. The order of
     * the arguments is not significant.
     */
    public double distanceWithinArcDeg(double angleDeg1, double angleDeg2) {
        return distanceWithinArcRad(angleDeg1 * Math.PI / 180.0, angleDeg2 * Math.PI / 180.0) * 180.0 / Math.PI;
    }

    /**
     * Measure the arc length distance between two angles, in degrees, either clockwise or counterclockwise, whichever
     * is shorter. 0° .. 180°
     */
    public static double undirectedDistanceDeg(double angleDeg1, double angleDeg2) {
        return undirectedDistanceRad(angleDeg1 * Math.PI / 180.0, angleDeg2 * Math.PI / 180.0) * 180.0 / Math.PI;
    }

    /**
     * The start angle, in radians, defining the counterclockwise limit of the arc. The arc spans from the start angle
     * to the end angle in a clockwise sweep. Normalized to the 0 ..< 2*pi range.
     */
    public double getNormalizedStartAngleRad() {
        return startAngleRad;
    }

    /**
     * The end angle, in radians, defining the clockwise limit of the arc. The arc spans from the start angle to the end
     * angle in a clockwise sweep. Normalized to the 0 ..< 2*pi range.
     */
    public double getNormalizedEndAngleRad() {
        return endAngleRad;
    }

    @Override
    public String toString() {
        return String.format(
            Locale.ROOT,
            "Arc{%.2f..%.2f (%.2f°..%.2f°)}",
            startAngleRad,
            endAngleRad,
            toNormalizedDegrees(startAngleRad),
            toNormalizedDegrees(endAngleRad));
    }

    /**
     * The start angle, in degrees, defining the counterclockwise limit of the arc. The arc spans from the start angle
     * to the end angle in a clockwise sweep. Normalized to the -180° <.. 180° range.
     */
    public double getNormalizedStartAngleDeg() {
        return toNormalizedDegrees(startAngleRad);
    }

    /**
     * The end angle, in degrees, defining the clockwise limit of the arc. The arc spans from the start angle to the end
     * angle in a clockwise sweep. Normalized to the -180° <.. 180° range.
     */
    public double getNormalizedEndAngleDeg() {
        return toNormalizedDegrees(endAngleRad);
    }

    /** Convert radians to degrees and normalize to -180° <.. 180° range */
    private static double toNormalizedDegrees(double angleRad) {
        double a = normalizeRad(angleRad);
        double res = a * 180.0 / Math.PI;
        return (res <= 180.0) ? res : res - 360.0;
    }

    /**
     * The start angle, in degrees, defining the counterclockwise limit of the arc. The arc spans from the start angle
     * to the end angle in a clockwise sweep. Normalized to guarantee minAngleDeg <= maxAngleDeg, resulting in a
     * possible value in the range -360° <..< 360°.
     */
    public double getMinAngleDeg() {
        double startAngleDeg = getNormalizedStartAngleDeg();
        double endAngleDeg = getNormalizedEndAngleDeg();

        if (startAngleDeg <= endAngleDeg || (endAngleDeg + 360.0) < 360.0) {
            return startAngleDeg;
        }

        return startAngleDeg - 360.0;
    }

    /**
     * TThe end angle, in degrees, defining the clockwise limit of the arc. The arc spans from the start angle to the
     * end angle in a clockwise sweep. Normalized to guarantee minAngleDeg <= maxAngleDeg, resulting in a possible value
     * in the range -360° <..< 360°.
     */
    public double getMaxAngleDeg() {
        double minAngleDeg = getMinAngleDeg();
        double maxAngleDeg = getNormalizedEndAngleDeg();

        while (maxAngleDeg < minAngleDeg) {
            maxAngleDeg += 360.0;
        }

        return maxAngleDeg;
    }
}
