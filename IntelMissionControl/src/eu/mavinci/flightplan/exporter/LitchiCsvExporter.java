/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import com.google.common.io.Files;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.FluentFuture;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import eu.mavinci.core.flightplan.visitors.ExtractTypeVisitor;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.Waypoint;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

// import com.intel.missioncontrol.hardware.IPlatformDescription;
// import com.intel.missioncontrol.measure.Unit;

/** Exports waypoints for use with Litchi for DJI Drones mobile app. */
public class LitchiCsvExporter implements IFlightplanExporter {

    public LitchiCsvExporter() {}

    /**
     * Exports list of waypoints to .csv file in Litchi format.
     *
     * @param waypoints List of waypoints to be exported
     * @param target Target .csv file to be written
     * @throws IOException Exception thrown if csv file cannot be written
     */
    public void exportWaypoints(List<Waypoint> waypoints, File target) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(target);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
            LitchiCsv csvFile = new LitchiCsv(",");
            bw.write(csvFile.getHeader());
            bw.newLine();

            for (Waypoint wp : waypoints) {
                double gimbalPitchAngle = wp.getOrientation().getPitch() - 90;

                csvFile.setLatitude(wp.getLat());
                csvFile.setLongitude(wp.getLon());
                csvFile.setAltitude(wp.getAltInMAboveFPRefPoint());
                csvFile.setCurveSize(0.2); // DJI minimum, but will be ignored
                csvFile.setHeading((int)Math.round(wp.getOrientation().getYaw()));
                csvFile.setRotationDir(0);
                csvFile.setGimbalPitchAngle(gimbalPitchAngle);
                csvFile.setActionType(
                    wp.isTriggerImageHereCopterMode()
                        ? LitchiCsv.ActionType.TAKE_PHOTO
                        : LitchiCsv.ActionType.NO_ACTION,
                    0);
                csvFile.setActionParameter(0, 0);
                csvFile.setAltitudeMode(LitchiCsv.AltitudeMode.ABOVE_TAKEOFF);
                csvFile.setSpeed(wp.getSpeedMpSec());

                bw.write(csvFile.getLine());
                bw.newLine();
            }
        }
    }

    @Override
    public void export(Flightplan flightplan, File target, IMProgressMonitor progressMonitor) {
        ExtractTypeVisitor<Waypoint> vis = new ExtractTypeVisitor<>(Waypoint.class);
        vis.startVisit(flightplan);
        try {
            List<Waypoint> waypoints = vis.filterResults;

            // Splits flightplans to several export files taken from hardware description files
            IPlatformDescription platformDesc = flightplan.getHardwareConfiguration().getPlatformDescription();
            int maxWaypoints = platformDesc.getMaxNumberOfWaypoints();

            int sizeWaypointsList = waypoints.size();
            int remainder = sizeWaypointsList % maxWaypoints;
            int numExportFiles = sizeWaypointsList / maxWaypoints;
            if (remainder != 0) {
                numExportFiles++;
            }

            String filePath = target.getPath();

            for (int i = 0; i < numExportFiles; i++) {
                int startIndex = i * maxWaypoints;
                int endIndex;
                if (i == numExportFiles - 1) {
                    endIndex = startIndex + remainder;
                } else {
                    endIndex = startIndex + maxWaypoints;
                }

                List<Waypoint> iWaypoints = waypoints.subList(startIndex, endIndex);

                String newFileName;
                if (numExportFiles > 1) {
                    newFileName =
                        Files.getNameWithoutExtension(target.getName())
                            + " - Split"
                            + (i + 1)
                            + "."
                            + Files.getFileExtension(target.getName());
                } else {
                    newFileName = target.getName();
                }

                File iTarget = new File(target.getParent(), newFileName);
                exportWaypoints(iWaypoints, iTarget);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
