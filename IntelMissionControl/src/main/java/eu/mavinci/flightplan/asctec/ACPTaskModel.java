/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.asctec;

import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import eu.mavinci.desktop.helper.gdal.SRStransformCacheEntry;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * ACPTaskModel is an abstraction for the AscTec mission. It contains ACPTasks (analogy of way points). It can be
 * transformed to a list of ATOS_WAYPOINT to be send via the ACI or it can be exported to the xml in AscTec project
 * format
 *
 * @author elena
 */
public class ACPTaskModel {

    public ACPTaskModel(IHardwareConfiguration hardwareConfiguration) {
        this.hardwareConfiguration = hardwareConfiguration;
    }

    public ACPTask getTask(int i) {
        return wps.get(i);
    }

    public boolean isLastWP(int currentPointIndex) {
        return this.wps.size() == currentPointIndex + 1;
    }

    IHardwareConfiguration hardwareConfiguration;

    public class ACPTask {
        double roll;
        double pitch;
        double yaw;
        double speed;
        double height;
        double lat;
        double lon;
        boolean triggerImage;
        int reentryId;
        double waitTimeEvent1; // ms to wait on a waypoint before taking image
        double waitTimeEvent2; // ms to wait on a waypoint after taking image
        double desiredAcceleration;

        public ACPTask(
                double roll,
                double pitch,
                double yaw,
                double speed,
                double height,
                double lat,
                double lon,
                // WaypointType waypointType,
                boolean triggerImage,
                int reentryId,
                double waitTimeEvent1,
                double waitTimeEvent2) {
            super();
            this.roll = roll;
            this.pitch = pitch;
            this.yaw = yaw;

            if (speed < 0) {
                speed =
                    hardwareConfiguration
                        .getPlatformDescription()
                        .getPlaneSpeed()
                        .convertTo(Unit.METER_PER_SECOND)
                        .getValue()
                        .doubleValue();
            }

            this.speed = speed;
            this.height = height;
            this.lat = lat;
            this.lon = lon;
            // this.waypointType = waypointType;
            this.triggerImage = triggerImage;
            this.reentryId = reentryId;
            this.waitTimeEvent1 = waitTimeEvent1;
            this.waitTimeEvent2 = waitTimeEvent2;
            this.desiredAcceleration = triggerImage ? 1.5 : 3.0;
        }

        @Override
        public String toString() {
            return "lat:"
                + lat
                + " lon:"
                + lon
                + " height:"
                + height
                + " yaw:"
                + yaw
                + " pitch:"
                + pitch
                + " speed:"
                + speed;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }
    }

    private String generalFlightData; // TODO
    private static final int S_CONST = 100;
    private static final int B_CONST = 10000000;
    private List<ACPTask> wps = new ArrayList<>();

    public void addPoint(
            double roll,
            double pitch,
            double yaw,
            double speed,
            double height,
            double lat,
            double lon,
            boolean triggerImage,
            int reentreId,
            double waitTimeEvent1,
            double waitTimeEvent2) {

        // toing the transformation mavinci -> asctec pitch
        wps.add(
            new ACPTask(
                roll,
                pitch - 90,
                yaw,
                speed,
                height,
                lat,
                lon,
                triggerImage,
                reentreId,
                waitTimeEvent1,
                waitTimeEvent2));
    }

    private ATOS_WAYPOINT acpTask2ATOS_wp(ACPTask t, int idx, MSpatialReference srs, boolean isLocal) {
        ATOS_WAYPOINT wp = new ATOS_WAYPOINT();

        // only 32767 points possible ! guess its enough
        wp.id = (short)(idx);
        wp.camAnglePitch = (short)(t.pitch * S_CONST);
        wp.camAngleRoll = (short)(t.roll * S_CONST);

        // double yawPositive = (double) (t.yaw < 0 ? t.yaw + 360 : t.yaw);
        // double yaw180Rotated = yawPositive + (srs == null ? 0 : srs.getOrigin().getYaw());
        // double yawLess360 = yaw180Rotated > 360 ? (yaw180Rotated - 360) : yaw180Rotated;
        // wp.camAngleYaw = (short)(yawLess360 * 10);
        double yaw = t.yaw;
        if (srs != null) {
            yaw += srs.getOrigin().getYaw();
        }

        while (yaw < 0) {
            yaw += 360;
        }

        while (yaw >= 360) {
            yaw -= 360;
        }

        wp.camAngleYaw = (short)(yaw * 10);

        if (isLocal) {
            SRStransformCacheEntry local;
            try {
                Ensure.notNull(srs, "srs");
                local = srs.fromWgs84(new Position(Angle.fromDegrees(t.lat), Angle.fromDegrees(t.lon), (t.height)));
                wp.lat = (int)(local.y * 1000);
                wp.lon = (int)(local.x * 1000);
                wp.height = (float)t.height; // test if its in meters! and above the start!!

            } catch (Exception e) {
                Debug.printStackTrace(Level.SEVERE, "cant transform to local coordinates", e);
            }
            // Angle.fromDegrees(t.lon), (height))).transformBy4(srs);
        } else {
            wp.lat = (int)(t.lat * B_CONST);
            wp.lon = (int)(t.lon * B_CONST);
            wp.height = (float)(t.height /*- startAltWgs84*/); // test if its in meters! and above the start!!
        }

        if (isLocal) {
            wp.flags =
                ATOS_DEFINES.NAV_FLAG_MATRIX_SPLINE
                    | ATOS_DEFINES.NAV_FLAG_SINGLE_WP_SPLINE
                    | ATOS_DEFINES.WP_FLAG_CAM_PITCH_ACTIVE
                    | ATOS_DEFINES.WP_FLAG_HEIGHT_ACTIVE
                    | ATOS_DEFINES.WP_FLAG_CAM_YAW_ACTIVE
                    | ATOS_DEFINES.NAV_FLAG_RING_BUFFER
                    | ((!isLocal) ? 0x0000000 : ATOS_DEFINES.WP_FLAG_RELATIVE_COORDS)
                    | ATOS_DEFINES.NAV_FLAG_ABSOLUTE_ORIGIN;
        } else {
            wp.flags =
                ATOS_DEFINES.NAV_FLAG_SINGLE_WAYPOINTS
                    | ATOS_DEFINES.WP_FLAG_CAM_PITCH_ACTIVE
                    | ATOS_DEFINES.WP_FLAG_HEIGHT_ACTIVE
                    | ATOS_DEFINES.WP_FLAG_CAM_YAW_ACTIVE
                    | ATOS_DEFINES.NAV_FLAG_RING_BUFFER;
        }

        wp.speed =
            (float)
                Math.min(
                    t.speed,
                    hardwareConfiguration
                        .getPlatformDescription()
                        .getMaxPlaneSpeed()
                        .convertTo(Unit.METER_PER_SECOND)
                        .getValue()
                        .doubleValue());
        // System.out.println("WP speed: " + t.speed + " cropped to: " + wp.speed);
        wp.desiredAcceleration = (float)t.desiredAcceleration;

        // see also;  https://jira.drones.intel.com/browse/AT-57
        if (t.triggerImage) {
            wp.event1 = 0x01; // NAV_EVENT_TRIGGER
        } else {
            wp.event1 = 0x00; // NAV_EVENT_NONE
        }

        wp.event2 = 0;
        wp.parameterEvent1 = 0;
        wp.parameterEvent2 = 0;
        wp.waitTimeEvent1 = (short)Math.round(t.waitTimeEvent1);
        wp.waitTimeEvent2 = (short)Math.round(t.waitTimeEvent2);

        return wp;
    }

    int[] starts; // idx start to, (count from zero).. -1 is last point of the list == LANDING POINT!!)
    int[] stops; // idx to stop INLUDING (count from zero)

    public List<ATOS_WAYPOINT> getATOS_WPList(MSpatialReference srs, boolean isLocal) {
        List<ATOS_WAYPOINT> atos_wps = new ArrayList<>();

        atos_wps.clear();
        for (int i = 0; i < wps.size(); i++) {
            atos_wps.add(acpTask2ATOS_wp(wps.get(i), i + 1, srs, isLocal));
        }

        return atos_wps;
    }

    public double getCameraRoll(ATOS_WAYPOINT wp) {
        return wp.camAngleRoll / S_CONST;
    }

    public double getCameraPitch(ATOS_WAYPOINT wp) {
        return wp.camAnglePitch / S_CONST;
    }

    public double getCameraYaw(ATOS_WAYPOINT wp) {
        return wp.camAngleYaw / S_CONST;
    }

    public double getSpeed(ATOS_WAYPOINT wp) {
        return wp.speed;
    }

    public double getHeight(ATOS_WAYPOINT wp) {
        return wp.height;
    }

    public double getLat(ATOS_WAYPOINT wp) {
        return wp.lat / B_CONST;
    }

    public double getLon(ATOS_WAYPOINT wp) {
        return wp.lon / B_CONST;
    }

    public boolean isMatrix(ATOS_WAYPOINT wp) {
        return (wp.flags | ATOS_DEFINES.NAV_FLAG_MATRIX_SPLINE) != 0;
    }

    public boolean isEndOfMatrix(ATOS_WAYPOINT wp) {
        return wp.id == wps.size() - 1;
    }

    public List<ACPTask> getTasks() {
        return wps;
    }

    public int getReentryIdByItemId(int i) {
        if (wps.size() > i) {
            return wps.get(i).reentryId;
        }

        return -1;
    }

}
