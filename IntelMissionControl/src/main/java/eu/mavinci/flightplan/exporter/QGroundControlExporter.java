/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.project.FlightPlan;
import eu.mavinci.core.flightplan.visitors.ExtractTypeVisitor;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.Waypoint;
import gov.nasa.worldwind.geom.LatLon;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QGroundControlExporter implements IFlightplanExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(QGroundControlExporter.class);

    public QGroundControlExporter() {}

    public void exportLegacy(Flightplan flightplan, File target, IMProgressMonitor progressMonitor) {
        try (FileWriter fs = new FileWriter(target);
            BufferedWriter bw = new BufferedWriter(fs)) {
            ExtractTypeVisitor<Waypoint> vis = new ExtractTypeVisitor<>(Waypoint.class);
            vis.startVisit(flightplan);
            List<Waypoint> waypoints = vis.filterResults;

            bw.write(
                "{\n"
                    + "    \"fileType\": \"Plan\",\n"
                    + "    \"geoFence\": {\n"
                    + "        \"circles\": [\n"
                    + "        ],\n"
                    + "        \"polygons\": [\n"
                    + "        ],\n"
                    + "        \"version\": 2\n"
                    + "    },\n"
                    + "    \"groundStation\": \"QGroundControl\",\n"
                    + "    \"mission\": {\n"
                    + "        \"cruiseSpeed\": "
                    + flightplan.getPhotoSettings().getMaxGroundSpeedMPSec()
                    + ",\n"
                    + "        \"firmwareType\": 12,\n"
                    + "        \"hoverSpeed\": 5,\n"
                    + "        \"items\": [\n");

            int i = 0;
            for (Waypoint wp : waypoints) {
                if (i > 0) {
                    bw.write(",\n");
                }

                i++;
                bw.write(
                    "            {\n"
                        + "                \"autoContinue\": true,\n"
                        + "                \"command\": 178,\n"
                        + "                \"doJumpId\": "
                        + i
                        + ",\n"
                        + "                \"frame\": 2,\n"
                        + "                \"params\": [\n"
                        + "                    1,\n"
                        + "                    2,\n"
                        + "                    -1,\n"
                        + "                    0,\n"
                        + "                    0,\n"
                        + "                    0,\n"
                        + "                    0\n"
                        + "                ],\n"
                        + "                \"type\": \"SimpleItem\"\n"
                        + "            },\n");
                i++;
                bw.write(
                    "            {\n"
                        + "                \"AMSLAltAboveTerrain\": null,\n"
                        + "                \"Altitude\": "
                        + (wp.getAltWithinCM() / 100.)
                        + ",\n"
                        + "                \"AltitudeMode\": 0,\n"
                        + "                \"autoContinue\": true,\n"
                        + "                \"command\": "
                        + (i <= 2 ? 22 : 16)
                        + ",\n"
                        + "                \"doJumpId\": "
                        + i
                        + ",\n"
                        + "                \"frame\": 3,\n"
                        + "                \"params\": [\n"
                        + "                    15,\n"
                        + "                    0,\n"
                        + "                    0,\n"
                        + "                    null,\n"
                        + "                    "
                        + wp.getLat()
                        + ",\n"
                        + "                    "
                        + wp.getLon()
                        + ",\n"
                        + "                    "
                        + (wp.getAltWithinCM() / 100.)
                        + "\n"
                        + "                ],\n"
                        + "                \"type\": \"SimpleItem\"\n"
                        + "            }");

                i++;
            }

            LatLon homePoint = flightplan.getRefPoint().getLatLon();

            bw.write(
                "\n],\n"
                    + "        \"plannedHomePosition\": [\n"
                    + "            "
                    + homePoint.latitude.degrees
                    + ",\n"
                    + "            "
                    + homePoint.longitude.degrees
                    + ",\n"
                    + "            0\n"
                    + "        ],\n"
                    + "        \"vehicleType\": 2,\n"
                    + "        \"version\": 2\n"
                    + "    },\n"
                    + "    \"rallyPoints\": {\n"
                    + "        \"points\": [\n"
                    + "        ],\n"
                    + "        \"version\": 2\n"
                    + "    },\n"
                    + "    \"version\": 1\n"
                    + "}");
        } catch (Exception e) {
            LOGGER.error("cant export to " + target, e);
        }
    }

    @Override
    public void export(FlightPlan flightplan, File target, IMProgressMonitor progressMonitor) {
        throw new NotImplementedException();
    }
}
