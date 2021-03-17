/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.flightplan.Waypoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/** Unit test program to test LitchiCsvExporter. Creates three waypoints, generates exported file, checks. */
public class LitchiCsvExporterTest {

    @Test
    public void csvTest() {

        // waypoint definitions
        Waypoint wp1 = new Waypoint(8.708903447, 49.346867333, 3000, AltAssertModes.linear, 0, "2isfun", 0);
        Waypoint wp2 = new Waypoint(8.708903440, 49.346773728, 3100, AltAssertModes.linear, 0, "2isfun", 1);
        Waypoint wp3 = new Waypoint(8.708903434, 49.346688470, 3200, AltAssertModes.linear, 0, "2isfun", 2);
        // defining speeds and orientations for waypoints. yaw will be used for heading, pitch for gimbal pitch.
        wp1.setSpeedMpSec(5);
        wp2.setSpeedMpSec(6);
        wp3.setSpeedMpSec(7);
        wp1.setOrientation(new Orientation(0, 120, 180));
        wp2.setOrientation(new Orientation(0, 90, 359));
        wp3.setOrientation(new Orientation(0, 0, 0));

        // putting waypoints into a list
        List<Waypoint> wpList = List.of(wp1, wp2, wp3);
        // constructing LitchiCsvExporter in order to use export function
        LitchiCsvExporter mpce = new LitchiCsvExporter();

        // create file, export header and waypoints into file
        try {
            File temp = File.createTempFile("litchiCsvExportTest", ".csv");
            temp.deleteOnExit();
            mpce.exportWaypoints(wpList, temp);
            String fileName = temp.getPath();

            // read newly created file
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                // check header line is correct
                String line1 = br.readLine();
                Assert.assertEquals(
                    "latitude,longitude,altitude(m),heading(deg),curvesize(m),rotationdir,gimbalmode,"
                        + "gimbalpitchangle,actiontype1,actionparam1,actiontype2,actionparam2,actiontype3,actionparam3,"
                        + "actiontype4,actionparam4,actiontype5,actionparam5,actiontype6,actionparam6,actiontype7,"
                        + "actionparam7,actiontype8,actionparam8,actiontype9,actionparam9,actiontype10,actionparam10,"
                        + "actiontype11,actionparam11,actiontype12,actionparam12,actiontype13,actionparam13,actiontype14,"
                        + "actionparam14,actiontype15,actionparam15,altitudemode,speed(m/s),poi_latitude,poi_altitude(m),"
                        + "poi_altitudemode",
                    line1);
                // check first waypoint is correct
                String line2 = br.readLine();
                Assert.assertEquals(
                    "49.346867333,8.708903447,30.0,180,0.2,0,2,30.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,"
                        + "0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,0,5.0,0.0,0.0,"
                        + "0",
                    line2);
                // check second waypoint is correct
                String line3 = br.readLine();
                Assert.assertEquals(
                    "49.346773728,8.70890344,31.0,359,0.2,0,2,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,"
                        + "-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,0,6.0,0.0,0.0,0",
                    line3);
                // check third waypoint is correct
                String line4 = br.readLine();
                Assert.assertEquals(
                    "49.34668847,8.708903434,32.0,0,0.2,0,2,-90.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,"
                        + "-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,-1,0.0,0,7.0,0.0,0.0,0",
                    line4);
            }
            // if an IO exception at any time, fail test
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
