/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.geo;

import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.geo.CountryDetector.RadioRegulation;
import gov.nasa.worldwind.geom.LatLon;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.logging.Level;

public class GeoFencing {

    public static int[] fenceArr;

    public static void readFile(InputStream is) {
        // fenceArr = new int[(int) fileDat.length() / 4];
        LinkedList<Integer> lst = new LinkedList<Integer>();
        try (DataInputStream in = new DataInputStream(is)) {
            int i = 0;
            while (in.available() > 0) {
                lst.add(in.readInt());
                i++;
            }

            fenceArr = new int[i];
            i = 0;
            for (int n : lst) {
                fenceArr[i] = n;
                // System.out.print(n+",");
                i++;
            }
            // for (i = 0; i != 10; i++){
            // System.out.print(fenceArr[i]+",");
            // }
            // System.out.println();
            // System.out.println("fenceLen:" + fenceArr.length);
            Debug.getLog().log(Level.FINE, "GeoFence successuflly loaded with " + fenceArr.length + " entries.");
        } catch (IOException e1) {
            Debug.getLog().log(Level.SEVERE, "could not read restrictions database", e1);
        }
    }

    public static int getID(LatLon latLon) {
        return getID(latLon.latitude.degrees, latLon.longitude.degrees);
    }

    public static int getID(double lat, double lon) {
        double minLat = -90;
        double maxLat = 90;
        double minLon = -180;
        double maxLon = 180;
        while (lon > 180) {
            lon -= 360;
        }

        return getID(lat, lon, minLat, maxLat, minLon, maxLon, 0);
    }

    private static int getID(
            double lat, double lon, double minLat, double maxLat, double minLon, double maxLon, int offset) {
        double centerLat = (minLat + maxLat) / 2;
        double centerLon = (minLon + maxLon) / 2;
        int cell;
        if (lat < centerLat) {
            maxLat = centerLat;
            if (lon < centerLon) {
                maxLon = centerLon;
                cell = 0;
            } else {
                minLon = centerLon;
                cell = 1;
            }
        } else {
            minLat = centerLat;
            if (lon < centerLon) {
                maxLon = centerLon;
                cell = 2;
            } else {
                minLon = centerLon;
                cell = 3;
            }
        }

        int idx = offset + cell;
        if (idx >= fenceArr.length || idx < 0) {
            return WORST_CASE;
        }

        int i = fenceArr[idx];
        if (i >= 0) {
            return i;
        } else if (-i <= idx) {
            return WORST_CASE;
        } else {
            return getID(lat, lon, minLat, maxLat, minLon, maxLon, -i);
        }
    }

    public static int WORST_CASE =
        128 + 64 + CountryDetector.RadioRegulation.ce.ordinal(); // ==not in EU + Restricted + CE == Worst Case => fallback value
    // in case of bad data

    public static boolean isRestricted(double lat, double lon) {
        return getID(lat, lon) >= 128;
    }

    public static boolean isEU(double lat, double lon) {
        return getID(lat, lon) % 128 >= 64;
    }

    public static boolean isEU(LatLon latLon) {
        return isEU(latLon.latitude.degrees, latLon.longitude.degrees);
    }

    public static boolean isRestricted(LatLon latLon) {
        return isRestricted(latLon.latitude.degrees, latLon.longitude.degrees);
    }

    /** 0 == unknown 1 == CE/EU 2 == CEother 3== FCC/IC */
    public static int getRadioCode(double lat, double lon) {
        return getID(lat, lon) % 64;
    }

    public static int getRadioCode(LatLon latLon) {
        return getRadioCode(latLon.latitude.degrees, latLon.longitude.degrees);
    }

}
