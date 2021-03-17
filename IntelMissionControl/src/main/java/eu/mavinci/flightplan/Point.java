/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanLatLonReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IMuteable;
import eu.mavinci.geo.ILatLonReferenced;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import org.apache.commons.lang3.StringUtils;

public class Point extends FlightplanStatement implements ILatLonReferenced, IFlightplanLatLonReferenced, IMuteable {

    public static final String KEY = "eu.mavinci.flightplan.Point";
    public static final String KEY_TO_STRING = KEY + ".toString";

    // public static final int DIGITS_LAT_LON_SHOWN = 5; //means a precision of at least 1.1m
    // public static final int DIGITS_LAT_LON_SHOWN = 9; //means a precision of at least 0.11mm
    public static final int DIGITS_LAT_LON_SHOWN = 8; // means a precision of at least 1.1mm

    protected double lat;
    protected double lon;

    public static enum DistanceSource {
        NO_UPDATE,
        BY_RESOLUTION,
        BY_DISTANCE,
        BY_DIAGONALE,
    }

    protected String note = "";
    protected double pitch;
    protected double yaw;
    protected boolean triggerImage;
    protected boolean target;

    protected double altitude = 0.0;

    public String getNote() {
        return note;
    }

    public void setAltitude(double altitude) {
        if (this.altitude == altitude) return;
        this.altitude = altitude;
        notifyStatementChanged();
    }

    public double getAltitude() {
        return altitude;
    }

    public boolean isTarget() {
        return target;
    }

    public void setTarget(boolean target) {
        if (this.target == target) return;
        this.target = target;
        notifyStatementChanged();
    }

    public void setNote(String note) {
        if (StringUtils.equals(this.note, note)) return;
        this.note = note;
        notifyStatementChanged();
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        if (this.pitch == pitch) return;
        this.pitch = pitch;
        notifyStatementChanged();
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        if (this.yaw == yaw) return;
        this.yaw = yaw;
        notifyStatementChanged();
    }

    public boolean isTriggerImage() {
        return triggerImage;
    }

    public void setTriggerImage(boolean triggerImage) {
        if (this.triggerImage == triggerImage) return;
        this.triggerImage = triggerImage;
        notifyStatementChanged();
    }

    public DistanceSource getDistanceSource() {
        return distanceSource;
    }

    public void setDistanceSource(DistanceSource distanceSource) {
        if (this.distanceSource == distanceSource) return;
        this.distanceSource = distanceSource;
        notifyStatementChanged();
    }

    public double getGsdMeter() {
        return gsdMeter;
    }

    public void setGsdMeter(double gsdMeter) {
        if (this.gsdMeter == gsdMeter) return;
        this.gsdMeter = gsdMeter;
        distanceSource = DistanceSource.BY_RESOLUTION;
        update();
        notifyStatementChanged();
    }

    public double getDistanceMeter() {
        return distanceMeter;
    }

    public void setDistanceMeter(double distanceMeter) {
        if (this.distanceMeter == distanceMeter) return;
        this.distanceMeter = distanceMeter;
        distanceSource = DistanceSource.BY_DISTANCE;
        update();
        notifyStatementChanged();
    }

    public double getFrameDiagonaleMeter() {
        return frameDiagonaleMeter;
    }

    public void setFrameDiagonaleMeter(double frameDiagonaleMeter) {
        if (this.frameDiagonaleMeter == frameDiagonaleMeter) return;
        this.frameDiagonaleMeter = frameDiagonaleMeter;
        distanceSource = DistanceSource.BY_DIAGONALE;
        update();
        notifyStatementChanged();
    }

    void update() {
        var hwConfiguration = getFlightplan().getHardwareConfiguration();
        IGenericCameraConfiguration cameraConfiguration =
            hwConfiguration.getPrimaryPayload(IGenericCameraConfiguration.class);
        ILensDescription lensDescription = cameraConfiguration.getLens().getDescription();
        IGenericCameraDescription cameraDescription = cameraConfiguration.getDescription();

        double focalLength = lensDescription.getFocalLength().convertTo(Unit.METER).getValue().doubleValue();
        double ccdWidth = cameraDescription.getCcdWidth().convertTo(Unit.METER).getValue().doubleValue();
        double ccdHeight = cameraDescription.getCcdHeight().convertTo(Unit.METER).getValue().doubleValue();
        double sensorDiag = Math.sqrt(ccdWidth * ccdWidth + ccdHeight * ccdHeight);
        int ccdResX = cameraDescription.getCcdResX();
        int ccdResY = cameraDescription.getCcdResY();

        switch (distanceSource) {
        case BY_DISTANCE:
            {
                frameDiagonaleMeter = sensorDiag / focalLength * distanceMeter;
                gsdMeter = distanceMeter / focalLength * (ccdWidth / ccdResX + ccdHeight / ccdResY) / 2;
            }
        case BY_RESOLUTION:
            {
                distanceMeter = gsdMeter * focalLength / (ccdWidth / ccdResX + ccdHeight / ccdResY) * 2;
                frameDiagonaleMeter = sensorDiag / focalLength * distanceMeter;
            }

            break;
        case BY_DIAGONALE:
            {
                distanceMeter = frameDiagonaleMeter * focalLength / sensorDiag;
                gsdMeter = distanceMeter / focalLength * (ccdWidth / ccdResX + ccdHeight / ccdResY) / 2;
            }

            break;
        case NO_UPDATE:
            break;
        }
    }

    protected DistanceSource distanceSource = DistanceSource.NO_UPDATE;
    double gsdMeter;
    protected double distanceMeter;
    protected double frameDiagonaleMeter;

    public Point(Point source) {
        this.gsdMeter = source.gsdMeter;
        this.distanceMeter = source.distanceMeter;
        this.frameDiagonaleMeter = source.frameDiagonaleMeter;
        this.distanceSource = source.distanceSource;
        this.pitch = source.pitch;
        this.yaw = source.yaw;
        this.mute = source.mute;
        this.note = source.note;
        this.triggerImage = source.triggerImage;
        this.lat = source.lat;
        this.lon = source.lon;
    }

    public Point(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public Point(IFlightplanContainer container, double lat, double lon) {
        super(container);
        this.lat = lat;
        this.lon = lon;
    }

    public Point(IFlightplanContainer container) {
        super(container);
    }

    @Override
    public String toString() {
        return "Point:" + this.lat + " " + this.lon;
    }

    @Override
    public void setLat(double lat) {
        if (this.lat != lat) {
            this.lat = lat;

            notifyStatementChanged();
        }
    }

    @Override
    public void setLon(double lon) {
        if (this.lon != lon) {
            this.lon = lon;

            notifyStatementChanged();
        }
    }

    @Override
    public void setLatLon(double lat, double lon) {
        if (this.lat != lat || this.lon != lon) {
            this.lat = lat;
            this.lon = lon;

            notifyStatementChanged();
        }
    }

    @Override
    protected void notifyStatementChanged() {
        if (mute) {
            return;
        }

        super.notifyStatementChanged();
    }

    @Override
    public boolean isStickingToGround() {
        return true;
    }

    @Override
    public double getLon() {
        return this.lon;
    }

    @Override
    public double getLat() {
        return this.lat;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof Point) {
            Point landingPoint = (Point)o;
            return this.lat == landingPoint.lat
                && this.lon == landingPoint.lon
                && this.pitch == landingPoint.pitch
                && this.target == landingPoint.target
                && this.altitude == landingPoint.altitude
                && this.triggerImage == landingPoint.triggerImage
                && this.note == landingPoint.note
                && this.mute == landingPoint.mute
                && this.yaw == landingPoint.yaw
                && this.distanceSource == landingPoint.distanceSource
                && this.frameDiagonaleMeter == landingPoint.frameDiagonaleMeter
                && this.distanceMeter == landingPoint.distanceMeter
                && this.gsdMeter == landingPoint.gsdMeter;
        } else {
            return false;
        }
    }

    @Override
    public LatLon getLatLon() {
        return new LatLon(Angle.fromDegreesLatitude(this.lat), Angle.fromDegreesLongitude(this.lon));
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new Point(this);
    }

    boolean mute;

    @Override
    public void setMute(boolean mute) {
        if (mute == this.mute) {
            return;
        }

        this.mute = mute;
        if (!mute) {
            notifyStatementChanged();
        }
    }

    @Override
    public void setSilentUnmute() {
        this.mute = false;
    }

    @Override
    public boolean isMute() {
        return mute;
    }
}
