/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.tools;

import eu.mavinci.desktop.helper.gdal.MGDAL;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class ScanGDAL {
    public static void main(String[] args) {
        MGDAL.assureInit();
        int found = 0;
        try (PrintWriter out = new PrintWriter("resource/eu/mavinci/other/gdalEPSGknown.dat")) {
            System.out.println("=======Scanning GDAL========");
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Date date = new Date();
            out.println("#Open Mission Control");
            out.println("#Generated at: " + dateFormat.format(date) + "   by: " + System.getProperty("user.name"));
            out.println(
                "#EPSG-id</|/|<name</|/|<wkt</|/|<angularUnits</|/|<isLocal</|/|<isGeocentric</|/|<isGeocentric</|/|<linearUnits</|/|<linearUnitsName</|/|<isPrivate");

            //			for (int i = 4300; i != 4400; i++){
            for (int i = 2000; i != 32769; i++) {
                String id = MSpatialReference.PREFIX_EPSG + i;
                // System.out.println("checking "+id);

                try {
                    MSpatialReference srs = MSpatialReference.createUsingGdal(id);
                    System.out.println("name: " + srs.name);
                    out.println(srs.createPredefinedString());
                    found++;
                } catch (Exception e) {
                    //					System.out.println("skip");
                }
            }

            System.out.println("======= Scanning GDAL DONE ========");
            System.out.println("======= Found " + found + " valid definitions ========");
        } catch (IOException e) {
            Debug.getLog().log(Level.SEVERE, "could not transform country mapping", e);
        }

        System.exit(0);
    }
}
