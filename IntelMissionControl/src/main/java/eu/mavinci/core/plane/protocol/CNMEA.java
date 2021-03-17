/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.protocol;

import eu.mavinci.desktop.main.debug.Debug;

import java.util.Calendar;
import java.util.logging.Level;

public class CNMEA {
    public static String getNmeaSum(String in) {
        int checksum = 0;
        if (in.startsWith("$")) {
            in = in.substring(1, in.length());
        }

        int end = in.indexOf('*');
        if (end == -1) {
            end = in.length();
        }

        for (int i = 0; i < end; i++) {
            checksum = checksum ^ in.charAt(i);
        }

        String hex = Integer.toHexString(checksum);
        if (hex.length() == 1) {
            hex = "0" + hex;
        }

        return hex.toUpperCase();
    }

    public static boolean checkNmeaSum(String in) {
        String sum = getNmeaSum(in);
        return in.endsWith("*" + sum);
    }

    public static void main(String[] args) {
        String nmeaOK = "$GPRMC,225922.0,V,,,,,,,050714,,,N*46";
        System.out.println(getNmeaSum(nmeaOK));
    }

    /*
     * NMEA doesn't include century in date format
     */
    private static final int CURRENT_CENTURY = 2000;

    public static Long parseUtcTimestamp(String nmea_time, String nmea_date) {
        try {
            String str_hh = nmea_time.substring(0, 2);
            String str_mm = nmea_time.substring(2, 4);
            String str_ss = nmea_time.substring(4, 6);
            int hh = Integer.parseInt(str_hh);
            int mm = Integer.parseInt(str_mm);
            int ss = Integer.parseInt(str_ss);
            int ms = 0;
            // ms not always present at NMEA messages
            if (nmea_time.length() > 7) {
                String str_ms = nmea_time.substring(7);
                ms = Integer.parseInt(str_ms);
            }

            String str_dd = nmea_date.substring(0, 2);
            String str_month = nmea_date.substring(2, 4);
            String str_yy = nmea_date.substring(4, 6);
            int dd = Integer.parseInt(str_dd);
            int month = Integer.parseInt(str_month) - 1;
            int yy = Integer.parseInt(str_yy) + CURRENT_CENTURY;

            // Get current date and time from Calendar class
            Calendar today = Calendar.getInstance();

            // Set new values
            today.set(Calendar.HOUR_OF_DAY, hh);
            today.set(Calendar.MINUTE, mm);
            today.set(Calendar.SECOND, ss);
            today.set(Calendar.MILLISECOND, ms);

            // Set new values
            today.set(Calendar.DAY_OF_MONTH, dd);
            today.set(Calendar.MONTH, month);
            today.set(Calendar.YEAR, yy);

            // Return timestamp in unix format
            return new Long(today.getTimeInMillis());

        } catch (Throwable e) {
            Debug.getLog().log(Level.WARNING, "could not parse NMEA time+data", e);
            return null;
        }
    }

}
