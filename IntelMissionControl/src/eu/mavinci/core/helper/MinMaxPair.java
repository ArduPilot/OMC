/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

import eu.mavinci.core.flightplan.CPicArea;
import java.util.OptionalDouble;

public class MinMaxPair {
    public double min;
    public double max;

    public MinMaxPair() {
        reset();
    }

    public MinMaxPair(double... val) {
        reset();
        for (double d : val) update(d);
    }

    public static MinMaxPair fromMinMax(double min, double max) {
        if (max < min) {
            return new MinMaxPair();
        } else {
            return new MinMaxPair(min, max);
        }
    }

    public void reset() {
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
    }

    public double mapToInterval(double x) {
        x -= min;
        x /= (max - min);
        return x;
    }

    public void overwrite(double val) {
        min = val;
        max = val;
    }

    public void overwrite(double val1, double val2) {
        if (val1 < val2) {
            min = val1;
            max = val2;
        } else {
            min = val2;
            max = val1;
        }
    }

    public void overwrite(MinMaxPair val) {
        min = val.min;
        max = val.max;
    }

    public void update(double val) {
        if (val < min) {
            if (!isValid()) max = val;
            min = val;
        } else if (val > max) {
            max = val;
        }
        //		min = Math.min(min, val);
        //		max = Math.max(max, val);
    }

    public boolean updateMinChanged(double val) {
        if (val < min) {
            if (!isValid()) max = val;
            min = val;
            return true;
        } else if (val > max) {
            max = val;
        }

        return false;
        //		min = Math.min(min, val);
        //		max = Math.max(max, val);
    }

    @Override
    public String toString() {
        return "(" + min + " <= " + max + ")";
    }

    public boolean isValid() {
        return min != Double.POSITIVE_INFINITY;
    }

    public void enlarge(double val) {
        min -= val;
        max += val;
    }

    public void lowerMin(double val) {
        min -= val;
    }

    public void largerMax(double val) {
        max += val;
    }

    public void shift(double val) {
        min += val;
        max += val;
    }

    /**
     * modify the interval to one which contains the distances to the val parameter.
     *
     * <p>if val > min,max, the new interval will have postive values
     *
     * @param val
     */
    public void distanceTo(double val) {
        double t = val - min;
        min = val - max;
        max = t;
    }

    public double size() {
        return max - min;
    }

    public double sizeValid() {
        if (isValid()) return max - min;
        else return 0;
    }

    public double size(double unappliedUpdate) {
        double minT = Math.min(min, unappliedUpdate);
        double maxT = Math.max(max, unappliedUpdate);
        return maxT - minT;
    }

    public double mean() {
        return (max + min) / 2;
    }

    public void enlarge(MinMaxPair p) {
        if (p == null || !p.isValid()) return;
        max = Math.max(max, p.max);
        min = Math.min(min, p.min);
    }

    public void shrink(MinMaxPair p) {
        if (p == null || !p.isValid()) return;
        max = Math.min(max, p.max);
        min = Math.max(min, p.min);
    }

    public void shrink(double c) {
        if (!isValid()) return;
        max -= c;
        min += c;
        if (min > max) reset();
    }

    public MinMaxPair clone() {
        MinMaxPair n = new MinMaxPair();
        n.min = min;
        n.max = max;
        return n;
    }

    public double absMax() {
        return Math.max(Math.abs(min), Math.abs(max));
    }

    public void mult(double factor) {
        if (factor < 0) factor = -factor;
        min *= factor;
        max *= factor;
    }

    public boolean contains(double num) {
        return min <= num && num <= max;
    }

    public double getDistanceTo(double num) {
        if (num < min) return min - num;
        if (num > max) return num - max;
        return 0;
    }

    public double getDistanceTo(MinMaxPair num) {
        if (!num.isValid()) return Double.POSITIVE_INFINITY;
        if (num.max < min) return min - num.max;
        if (num.min > max) return num.min - max;
        return 0;
    }

    public MinMaxPair intersect(MinMaxPair other) {
        double max = Math.min(this.max, other.max);
        double min = Math.max(this.min, other.min);
        if (max < min) return null;
        return new MinMaxPair(min, max);
    }

    public static void main(String[] args) {
        MinMaxPair a = new MinMaxPair();
        System.out.println(a.isValid());

        MinMaxPair m = new MinMaxPair(1);
        System.out.println(m);

        MinMaxPair p = new MinMaxPair(10, 20);
        p.distanceTo(0);
        System.out.println(p);
        p = new MinMaxPair(10, 20);
        p.distanceTo(30);
        System.out.println(p);
    }

    public double getOffset(CPicArea.ModelAxisAlignment reference) {
        switch (reference) {
        case MAX:
            return max;
        case MIN:
            return min;
        case UNCHANGED:
            return 0;
        case CENTER:
            return mean();
        }

        return 0;
    }

    /**
     * interpolation inside this interval. x==0 -> returns min, x==1 -> return max
     *
     * @param x
     * @return
     */
    public double interpolate(double x) {
        if (!isValid()) return 0;
        return min + x * size();
    }

    public void update(OptionalDouble val) {
        val.ifPresent(this::update);
    }

    public final double restricByInterval(double val) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        }

        return val;
    }
}
