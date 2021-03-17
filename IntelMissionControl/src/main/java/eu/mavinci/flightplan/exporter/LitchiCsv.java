/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import java.util.stream.IntStream;

/**
 * Represents the header and one waypoint line for csv export to Litchi for DJI Drones app. Used in LitchiCsvExporter.
 */
public class LitchiCsv {

    /** Possible actions to do at waypoints. */
    public enum ActionType {
        NO_ACTION(-1),
        WAIT_FOR(0),
        TAKE_PHOTO(1),
        START_RECORDING(2),
        STOP_RECORDING(3),
        ROTATE_AIRCRAFT(4),
        TILT_CAMERA(5);

        private final int actionType;

        ActionType(int actionType) {
            this.actionType = actionType;
        }

        public int getValue() {
            return actionType;
        }
    }

    /** used to define z-coordinates of waypoints. ABOVE_GROUND uses Litchi terrain model. */
    public enum AltitudeMode {
        ABOVE_TAKEOFF(0),
        ABOVE_GROUND(1);

        private final int altitudeMode;

        AltitudeMode(int altitudeMode) {
            this.altitudeMode = altitudeMode;
        }

        public int getValue() {
            return altitudeMode;
        }
    }

    // separator used when creating export file. For csv, separator is comma.
    // Litchi limited to 15 actions. Exported file requires 15 fields regardless of whether there are 15 actions or not.
    private String separator;
    private static final int maxActions = 15;

    // standard parameters required for export
    private double latitude;
    private double longitude;
    private double altitude;
    private int heading;
    private double curveSize;
    private int rotationDir;
    private int gimbalMode = 2;
    private double gimbalPitchAngle;
    private ActionType actionTypes[];
    private double actionParameters[];
    private AltitudeMode altitudeMode = AltitudeMode.ABOVE_TAKEOFF;
    private double speed;
    private double poiLatitude;
    private double poiAltitude;
    private AltitudeMode poiAltitudeMode = AltitudeMode.ABOVE_TAKEOFF;

    // getters and setters for all parameters (some unused)
    public int getMaxActions() {
        return maxActions;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public int getHeading() {
        return heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public double getCurveSize() {
        return curveSize;
    }

    public void setCurveSize(double curveSize) {
        this.curveSize = curveSize;
    }

    public int getRotationDir() {
        return rotationDir;
    }

    public void setRotationDir(int rotationDir) {
        this.rotationDir = rotationDir;
    }

    public int getGimbalMode() {
        return gimbalMode;
    }

    public void setGimbalMode(int gimbalMode) {
        this.gimbalMode = gimbalMode;
    }

    public double getGimbalPitchAngle() {
        return gimbalPitchAngle;
    }

    public void setGimbalPitchAngle(double gimbalPitchAngle) {
        this.gimbalPitchAngle = gimbalPitchAngle;
    }

    public ActionType getActionType(int index) {
        return actionTypes[index];
    }

    public void setActionType(ActionType val, int index) {
        this.actionTypes[index] = val;
    }

    public double getActionParameter(int index) {
        return actionParameters[index];
    }

    public void setActionParameter(double val, int index) {
        this.actionParameters[index] = val;
    }

    public AltitudeMode getAltitudeMode() {
        return altitudeMode;
    }

    public void setAltitudeMode(AltitudeMode altitudeMode) {
        this.altitudeMode = altitudeMode;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getPoiLatitude() {
        return poiLatitude;
    }

    public void setPoiLatitude(double poiLatitude) {
        this.poiLatitude = poiLatitude;
    }

    public double getPoiAltitude() {
        return poiAltitude;
    }

    public void setPoiAltitude(double poiAltitude) {
        this.poiAltitude = poiAltitude;
    }

    public AltitudeMode getPoiAltitudeMode() {
        return poiAltitudeMode;
    }

    public void setPoiAltitudeMode(AltitudeMode poiAltitudeMode) {
        this.poiAltitudeMode = poiAltitudeMode;
    }

    /** @param separator Character(s) used to separate columns in output file (eg. CSV = comma) */
    public LitchiCsv(String separator) {
        this.separator = separator;
        actionParameters = new double[maxActions];
        actionTypes = new ActionType[maxActions];

        for (int i = 0; i < maxActions; i++) {
            actionTypes[i] = ActionType.NO_ACTION;
            actionParameters[i] = 0;
        }
    }

    // builds header line for export file
    public final String getHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("latitude");
        sb.append(separator);
        sb.append("longitude");
        sb.append(separator);
        sb.append("altitude(m)");
        sb.append(separator);
        sb.append("heading(deg)");
        sb.append(separator);
        sb.append("curvesize(m)");
        sb.append(separator);
        sb.append("rotationdir");
        sb.append(separator);
        sb.append("gimbalmode");
        sb.append(separator);
        sb.append("gimbalpitchangle");
        sb.append(separator);

        IntStream.range(0, maxActions)
            .forEach(
                i -> {
                    sb.append("actiontype");
                    sb.append(i + 1);
                    sb.append(separator);
                    sb.append("actionparam");
                    sb.append(i + 1);
                    sb.append(separator);
                });

        sb.append("altitudemode");
        sb.append(separator);
        sb.append("speed(m/s)");
        sb.append(separator);
        sb.append("poi_latitude");
        sb.append(separator);
        sb.append("poi_altitude(m)");
        sb.append(separator);
        sb.append("poi_altitudemode");

        return sb.toString();
    }

    // builds line for one waypoint
    public String getLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(latitude);
        sb.append(separator);
        sb.append(longitude);
        sb.append(separator);
        sb.append(altitude);
        sb.append(separator);
        sb.append(heading);
        sb.append(separator);
        sb.append(curveSize);
        sb.append(separator);
        sb.append(rotationDir);
        sb.append(separator);
        sb.append(gimbalMode);
        sb.append(separator);
        sb.append(gimbalPitchAngle);
        sb.append(separator);

        IntStream.range(0, maxActions)
            .forEach(
                i -> {
                    sb.append(actionTypes[i].getValue());
                    sb.append(separator);
                    sb.append(actionParameters[i]);
                    sb.append(separator);
                });

        sb.append(altitudeMode.getValue());
        sb.append(separator);
        sb.append(speed);
        sb.append(separator);
        sb.append(poiLatitude);
        sb.append(separator);
        sb.append(poiAltitude);
        sb.append(separator);
        sb.append(poiAltitudeMode.getValue());

        return sb.toString();
    }

}
