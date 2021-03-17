/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

public class CMathHelper {

    public static double intoRange(double val, double min, double max) {
        if (val < min) {
            return min;
        }

        if (val > max) {
            return max;
        }

        if (Double.isNaN(val)) {
            val = (max - min) / 2;
        }
        // normalize -0.0==+0.0 for primitives but NOT for Double or Float objects...
        if (val == 0) {
            return 0;
        }

        return val;
    }

    public static int intoRange(int val, int min, int max) {
        if (val < min) {
            return min;
        }

        if (val > max) {
            return max;
        }

        return val;
    }

    /**
     * this function is giving the position of the first relevant digit
     *
     * @param number
     * @return
     */
    public static int orderOfNumber(double number) {
        return (int)Math.floor(Math.log(number) / Math.log(10d));
    }

    public static int firstDigitAfterDot(double number) {
        return Math.max(0, -orderOfNumber(number));
    }

    public static double roundLike(double num, double precisionSample) {
        return round(num, firstDigitAfterDot(precisionSample));
    }

    public static boolean isValid(double d) {
        return !(Double.isInfinite(d) || Double.isNaN(d));
    }

    /**
     * @param num
     * @param digits the number of digits where to round, if negative, its the number of significat digits
     * @return
     */
    public static double round(double num, int digits) {
        if (digits < 0) {
            // get count of digits of number
            if (num != 0) {
                int length = (int)Math.floor(Math.log10(Math.abs(num)) + 1);
                digits = -digits - length;
            } else {
                digits = -digits - 1;
            }
        }

        if (digits == 0) {
            return Math.round(num);
        } else {
            double shift = Math.pow(10, digits);
            num *= shift;
            num = Math.round(num);
            return num / shift;
        }
    }

    public static void main(String[] args) {
        System.out.println(round(10.01, -1));
    }

}
