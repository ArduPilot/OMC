/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ExifInfos;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonGeotags.Geotag;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CPhotoLogLine implements Comparable<CPhotoLogLine> {

    private static Logger LOGGER = LoggerFactory.getLogger(CPhotoLogLine.class);

    public static final String PREFIX_MATCHED_PLG = "matched_";

    public double alt;
    public int alt_ultrasonic = Integer.MIN_VALUE;
    public double gps_altitude_cm;
    public boolean dji_altitude;

    public double gps_ellipsoid_cm;

    public int groundSpeed_cms; // in cm/s

    public double heading;

    public int imageNumber;

    public int lineNumber;

    public double lat;

    public double lon;

    public double latTakeoff;

    public double lonTakeoff;

    public double gps_altitude_takeoff_cm;
    public String rawString;

    /** Camera roll pitch yaw */
    public double cameraRoll;

    public double cameraRollRate;
    public double cameraPitch;
    public double cameraPitchRate;
    public double cameraYaw;
    public double cameraYawRate;

    /** Plane roll pitch yaw */
    public double planeRoll;

    public double planePitch;
    public double planeYaw;

    public int time_since_last_fix = 0; // in ms
    public int interpolationErrorCode = -1;
    /** -1 means undefined, 255 means error in this line */
    private double timestamp; // in seconds

    public GPSFixType fixType = null;

    public PhotoLogLineType type = PhotoLogLineType.TIGGER;

    public CPhotoLogLine(ExifInfos exifInfos) throws Exception {
        timestamp = exifInfos.timestamp;

        Position p = exifInfos.getGPSPosition();
        try {
            // Use XMP metadata if available (more accurate for DJI drones)
            LatLon latLon = exifInfos.getGPSLatLonFromXmp();
            lat = latLon.latitude.degrees;
            lon = latLon.longitude.degrees;
        } catch (Exception e) {
            lat = p.getLatitude().degrees;
            lon = p.getLongitude().degrees;
        }

        try {
            // Use XMP metadata if available (more accurate for DJI drones)
            double relAltitude = exifInfos.getRelativeAltitudeFromXmp();
            alt = relAltitude * 100;
            double elevation = p.getElevation(); // elevation in meters above sea level according to EXIF specification.
            // gps_altitude_cm = elevation * 100;
            gps_altitude_cm = 0; // DJI gps wrong
            dji_altitude = true;
        } catch (Exception e) {
            double elevation = p.getElevation(); // elevation in meters above sea level according to EXIF specification.
            gps_altitude_cm = alt = elevation * 100; // GH in m
            dji_altitude = false;
        }

        try {
            Orientation orientation;
            try {
                // GH
                orientation = exifInfos.getOrientationFromGH(); // TODO check this
            } catch (Exception e) {
                try {
                    // Use XMP metadata if available (necessary for DJI drones)
                    orientation = exifInfos.getOrientationFromXmp(); // TODO check this
                } catch (Exception e1) {
                    orientation = exifInfos.getOrientation();
                }
            }

            planeYaw = cameraYaw = orientation.getYaw();
            planeRoll = cameraRoll = orientation.getRoll();
            planePitch = cameraPitch = orientation.getPitch();
        } catch (Exception e) {
            LOGGER.warn("cant extract orientation from metadata", e);
        }
    }

    public CPhotoLogLine(double lat, double lon, double elev, Orientation or) {
        cameraRoll = or.getRoll();
        cameraPitch = or.getPitch();
        cameraYaw = or.getYaw();
        this.lat = lat;
        this.lon = lon;
        this.alt = elev * 100;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof CPhotoLogLine) {
            CPhotoLogLine plg = (CPhotoLogLine)obj;
            return alt == plg.alt
                && alt_ultrasonic == plg.alt_ultrasonic
                && gps_altitude_cm == plg.gps_altitude_cm
                && gps_ellipsoid_cm == plg.gps_ellipsoid_cm
                && groundSpeed_cms == plg.groundSpeed_cms
                && heading == plg.heading
                && imageNumber == plg.imageNumber
                && lat == plg.lat
                && lineNumber == plg.lineNumber
                && lon == plg.lon
                && cameraPitch == plg.cameraPitch
                && cameraPitchRate == plg.cameraPitchRate
                && cameraRoll == plg.cameraRoll
                && cameraRollRate == plg.cameraRollRate
                && time_since_last_fix == plg.time_since_last_fix
                && interpolationErrorCode == plg.interpolationErrorCode
                && timestamp == plg.timestamp
                && cameraYaw == plg.cameraYaw
                && cameraYawRate == plg.cameraYawRate
                && planePitch == plg.planePitch
                && planeRoll == plg.planeRoll
                && planeYaw == plg.planeYaw
                && type == plg.type
                && fixType == plg.fixType
                && latTakeoff == plg.latTakeoff
                && lonTakeoff == plg.lonTakeoff
                && gps_altitude_takeoff_cm == plg.gps_altitude_takeoff_cm;
        }

        return false;
    }

    protected CPhotoLogLine() {}

    protected CPhotoLogLine(double timestamp) {
        this.setTimestampRaw(timestamp);
    }

    public CPhotoLogLine(PhotoData pd) {
        refreshFromPhotoData(pd);
    }

    public CPhotoLogLine(Geotag geotag) {
        imageNumber = geotag.geotag.trigger_count;
        lat = geotag.geotag.latitude_deg;
        lon = geotag.geotag.longitude_deg;
        gps_altitude_cm = geotag.geotag.gps_height_m * 100; // TODO FIXME... it this ellipsodial or geoidial height?
        gps_ellipsoid_cm = 0; // TODO FIXME, get this from JSON
        type = PhotoLogLineType.TIGGER;
        fixType = GPSFixType.dgps; // TODO FIXME, get this from JSON
        alt = geotag.geotag.pressure_height_m * 100;

        groundSpeed_cms = (int)Math.round(geotag.geotag.speed_m_per_s.getLength() * 100);
        heading =
            Math.toDegrees(
                Math.atan2(
                    -geotag.geotag.speed_m_per_s.y,
                    -geotag.geotag.speed_m_per_s.x)); // TODO FIXME .. is this correct signs/axis swap?..

        cameraRoll = Math.toDegrees(geotag.geotag.cam_angle_roll_rad);
        cameraPitch = Math.toDegrees(geotag.geotag.cam_angle_pitch_rad) + 90;
        cameraYaw = Math.abs(Math.toDegrees(geotag.geotag.cam_angle_yaw_rad)); // TODO FIXME... this abs feels weared.
        timestamp = geotag.geotag.getTimetampMs() / 1000.;

        // TODO FIXME, read out the orientation of the platform itself for future advanced level arm compensation

    }

    public void refreshFromPhotoData(PhotoData pd) {
        imageNumber = pd.number;
        setTimestampRaw(pd.getTimestamp());
        cameraRoll = pd.camera_roll;
        cameraPitch = pd.camera_pitch;
        cameraYaw = pd.camera_yaw;
        planeRoll = pd.plane_roll;
        planePitch = pd.plane_pitch;
        planeYaw = pd.plane_yaw;

        alt = pd.alt;
        lat = pd.lat;
        lon = pd.lon;

        type = PhotoLogLineType.values()[pd.type - 1];

        cameraRollRate = pd.gyroroll;
        cameraPitchRate = pd.gyropitch;
        cameraYawRate = pd.gyroyaw;

        lineNumber = pd.reentrypoint;

        time_since_last_fix = pd.time_since_last_fix;

        groundSpeed_cms = pd.groundspeed;
        heading = pd.heading;
        gps_ellipsoid_cm = pd.gps_ellipsoid;
        fixType = pd.gps_mode < 0 ? null : GPSFixType.values()[pd.gps_mode];
        gps_altitude_cm = pd.gps_alt;
    }

    public static boolean isMaybeParseableLine(String line) {
        if (!line.startsWith(LOG_PREFIX_ANY_PHOTO)) {
            return false;
        }

        return line.startsWith(CPhotoLogLine.LOG_PREFIX_TRIGGER)
            || line.startsWith(CPhotoLogLine.LOG_PREFIX_DELAYED)
            || line.startsWith(CPhotoLogLine.LOG_PREFIX_FLASH);
    }

    public String toRawString() {
        String tmp = "";
        switch (type) {
        case TIGGER:
            tmp = LOG_PREFIX_TRIGGER;
            break;
        case DELAY_30MS:
            tmp = LOG_PREFIX_DELAYED;
            break;
        case FLASH:
            tmp = LOG_PREFIX_FLASH;
            break;
        }

        tmp += ";";
        tmp += imageNumber + ";";
        tmp += String.format(Locale.ENGLISH, "%.6f", timestamp) + ";";
        tmp += alt + ";";
        tmp += alt_ultrasonic + ";";
        tmp += String.format(Locale.ENGLISH, "%.16f", lat) + ";";
        tmp += String.format(Locale.ENGLISH, "%.16f", lon) + ";";
        tmp += groundSpeed_cms + ";";
        tmp += 0 + ";"; // mode
        tmp += 0 + ";"; // phase
        tmp += cameraRoll + ";";
        tmp += cameraPitch + ";";
        tmp += cameraYaw + ";";
        tmp += cameraRollRate + ";";
        tmp += cameraPitchRate + ";";
        tmp += cameraYawRate + ";";
        tmp += planeRoll + ";";
        tmp += planePitch + ";";
        tmp += planeYaw + ";";
        tmp += gps_altitude_cm + ";";
        tmp += gps_ellipsoid_cm + ";";
        tmp += lineNumber + ";";
        tmp += heading + ";";
        tmp += time_since_last_fix + ";";
        tmp += interpolationErrorCode + ";";
        return tmp;
    }

    public CPhotoLogLine(String line) throws Exception {
        line = line.replaceAll("[ ;]+", Matcher.quoteReplacement(";"));

        String[] parts = line.split(";");
        // System.out.println("parts-len:"+parts.length);

        if (parts[0].equals(CPhotoLogLine.LOG_PREFIX_TRIGGER)) {
            type = PhotoLogLineType.TIGGER;
        } else if (parts[0].equals(CPhotoLogLine.LOG_PREFIX_DELAYED)) {
            type = PhotoLogLineType.DELAY_30MS;
        } else if (parts[0].equals(CPhotoLogLine.LOG_PREFIX_FLASH)) {
            type = PhotoLogLineType.FLASH;
        } else {
            throw new Exception("Prefix missmatch");
        }

        imageNumber = Integer.parseInt(parts[1]);
        lon = Double.parseDouble(parts[6]);
        lat = Double.parseDouble(parts[5]);
        alt = Double.parseDouble(parts[3]);
        alt_ultrasonic = Integer.parseInt(parts[4]);
        groundSpeed_cms = Integer.parseInt(parts[7]);

        cameraRollRate = Double.parseDouble(parts[13]);
        cameraPitchRate = Double.parseDouble(parts[14]);
        cameraYawRate = Double.parseDouble(parts[15]);

        cameraRoll = Double.parseDouble(parts[10]);
        cameraPitch = Double.parseDouble(parts[11]);
        cameraYaw = Double.parseDouble(parts[12]);
        String time = parts[2];
        if (time.toLowerCase().contains("e")) {
            setTimestampRaw(Double.parseDouble(time));
        } else {
            String[] timePart = time.split(Pattern.quote("."));
            if (timePart.length == 1) {
                setTimestampRaw(Double.parseDouble(timePart[0]));
            } else {
                setTimestampRaw(Double.parseDouble(timePart[0]) + Double.parseDouble(timePart[1]) / 1000000.);
            }
        }

        try {
            gps_ellipsoid_cm = Double.parseDouble(parts[17]);
            gps_altitude_cm = Double.parseDouble(parts[16]);
            lineNumber = (int)Double.parseDouble(parts[18]);
        } catch (Throwable t) {
        }

        try {
            heading = Double.parseDouble(parts[19]);
        } catch (Throwable t) {
            heading = cameraYaw;
            // dont care if they are not avaliable
        }
        //
        try {
            time_since_last_fix = (int)Double.parseDouble(parts[20]);
        } catch (Throwable t) {
            time_since_last_fix = 20; // magical typical value
        }

        try {
            interpolationErrorCode = (int)Double.parseDouble(parts[21]);
        } catch (Throwable t) {
        }

        try {
            fixType = GPSFixType.values()[(int)Double.parseDouble(parts[26])];
        } catch (Throwable t) {
        }

        // System.out.println("time:" + time + " -> "+timestamp);
        // System.out.println(timestamp);
        this.rawString = line;
    }

    public boolean isLineValid() {
        return interpolationErrorCode != 255;
    }

    public boolean isBetterThan(CPhotoLogLine otherPlg) {
        return type.isBetterThan(otherPlg.type) && (!otherPlg.isLineValid() || isLineValid());
    }

    public CPhotoLogLine clone() {
        CPhotoLogLine line = new CPhotoLogLine();
        line.alt = alt;
        line.alt_ultrasonic = alt_ultrasonic;
        line.gps_altitude_cm = gps_altitude_cm;
        line.gps_ellipsoid_cm = gps_ellipsoid_cm;
        line.groundSpeed_cms = groundSpeed_cms;
        line.heading = heading;
        line.imageNumber = imageNumber;
        line.lat = lat;
        line.lineNumber = lineNumber;
        line.lon = lon;
        line.cameraPitch = cameraPitch;
        line.cameraPitchRate = cameraPitchRate;
        line.rawString = rawString;
        line.cameraRoll = cameraRoll;
        line.cameraRollRate = cameraRollRate;
        line.planeRoll = planeRoll;
        line.planePitch = planePitch;
        line.planeYaw = planeYaw;
        line.time_since_last_fix = time_since_last_fix;
        line.timestamp = timestamp;
        // line.timestamp_exif=timestamp_exif;
        // line.focalLengthMM_exif = focalLengthMM_exif;
        // line.exposureSec_exif = exposureSec_exif;
        line.cameraYaw = cameraYaw;
        line.cameraYawRate = cameraYawRate;
        line.type = type;
        line.fixType = fixType;
        line.latTakeoff = latTakeoff;
        line.lonTakeoff = lonTakeoff;
        line.gps_altitude_takeoff_cm = gps_altitude_takeoff_cm;
        // line.aperture_exif = aperture_exif;
        // line.iso_exif = iso_exif;
        return line;
    }

    public int compareTo(CPhotoLogLine o) {
        int diff = Double.compare(timestamp, o.timestamp);
        if (diff != 0) {
            return diff;
        }

        return Integer.compare(hashCode(), o.hashCode());
    }

    public double getAltInM() {
        return alt / 100.;
    }

    public int getCellNumber() {
        return ReentryPointID.getCellNumber(lineNumber);
    }

    public int getRefinementID() {
        return ReentryPointID.getRefinementID(lineNumber);
    }

    public int getIDPureWithoutCell() {
        return ReentryPointID.getIDPureWithoutCell(lineNumber);
    }

    public int getLineNumberPure() {
        return ReentryPointID.getLineNumberPure(lineNumber);
    }

    public boolean isOrthogonalLine() {
        return ReentryPointID.isOrthogonalLine(lineNumber);
    }

    public OrientationData getPackageOrientationData() {
        OrientationData o = new OrientationData();
        o.altitude = alt;
        o.roll = (float)cameraRoll;
        o.pitch = (float)cameraPitch;
        o.yaw = (float)cameraYaw;
        return o;
    }

    public PhotoData getPackagePhotoData() {
        PhotoData pd = new PhotoData();
        pd.number = imageNumber;
        pd.time_sec = (int)getTimestampRaw();
        pd.time_usec = (int)((getTimestampRaw() - pd.time_sec) * 1.e6);
        pd.camera_roll = (float)cameraRoll;
        pd.camera_pitch = (float)cameraPitch;
        pd.camera_yaw = (float)cameraYaw;
        pd.plane_roll = (float)planeRoll;
        pd.plane_pitch = (float)planePitch;
        pd.plane_yaw = (float)planeYaw;
        pd.alt = alt;
        pd.lat = lat;
        pd.lon = lon;
        pd.gyroroll = (float)cameraRollRate;
        pd.gyropitch = (float)cameraPitchRate;
        pd.gyroyaw = (float)cameraYawRate;
        pd.heading = (float)heading;
        pd.groundspeed = groundSpeed_cms;
        pd.gps_ellipsoid = (float)gps_ellipsoid_cm;
        pd.gps_alt = (float)gps_altitude_cm;
        pd.gps_mode = fixType == null ? -1 : fixType.ordinal();
        pd.type = type.ordinal() + 1;

        pd.reentrypoint = lineNumber;
        pd.time_since_last_fix = time_since_last_fix;
        return pd;
    }

    public PositionData getPackagePositionData() {
        PositionData p = new PositionData();
        p.reentrypoint = lineNumber;
        p.time_sec = (int)getTimestamp();
        p.time_usec = (int)((getTimestamp() - p.time_sec) * 1.e6);
        p.altitude = alt;
        p.lat = lat;
        p.lon = lon;
        p.flightmode = AirplaneFlightmode.AutomaticFlight.ordinal();
        p.flightphase = AirplaneFlightphase.airborne.ordinal();
        p.cross_track_error = 0;
        p.gpsAltitude = (int)gps_altitude_cm;
        p.groundspeed = groundSpeed_cms;
        return p;
    }

    public PositionOrientationData getPackagePositionOrientationData() {
        PositionOrientationData p = new PositionOrientationData();
        p.reentrypoint = lineNumber;
        p.time_sec = (int)getTimestamp();
        p.time_usec = (int)((getTimestamp() - p.time_sec) * 1.e6);
        p.altitude = alt;
        p.lat = lat;
        p.lon = lon;
        p.flightmode = AirplaneFlightmode.AutomaticFlight.ordinal();
        p.flightphase = AirplaneFlightphase.airborne.ordinal();
        p.roll = (float)cameraRoll;
        p.pitch = (float)cameraPitch;
        p.yaw = (float)cameraYaw;
        return p;
    }

    public DebugData getPackageDebugData() {
        DebugData p = new DebugData();
        p.time_sec = (int)getTimestamp();
        p.time_usec = (int)((getTimestamp() - p.time_sec) * 1.e6);
        p.gyroroll = (float)cameraRollRate;
        p.gyropitch = (float)cameraPitchRate;
        p.gyroyaw = (float)cameraYawRate;
        p.heading = (float)heading;
        p.groundspeed = groundSpeed_cms;
        p.gpsAltitude = (float)gps_altitude_cm;
        p.gps_ellipsoid = (float)gps_ellipsoid_cm;
        return p;
    }

    public boolean isAutoPlanned() {
        return ReentryPointID.isAutoPlanned(lineNumber);
    }

    /**
     * ist dies eine "vorwärts" fluglinie ? ==3 is "rückwärts" bei onlyOneLine, sonst halt die normale gegenrichtung
     *
     * @return
     */
    public boolean isForwardLine() {
        return ReentryPointID.isForwardLine(lineNumber);
    }

    public boolean isMultiFP() {
        return ReentryPointID.isMultiFPid(lineNumber);
    }

    /** ist dies eine wichtige (True) linie, oder nur eine unwichtige querlinie (false)? */
    public boolean isOnMainLine() {
        return ReentryPointID.isOnMainLine(lineNumber);
    }

    public String numToString() {
        return ReentryPointID.toString(lineNumber);
    }

    public String numToStringWithoutCell() {
        return ReentryPointID.toStringWithoutCell(lineNumber);
    }

    @Override
    public String toString() {
        return "timeStamp:"
            + getTimestamp()
            + " lineNo:"
            + lineNumber
            + " imgNo:"
            + imageNumber
            + " LineNumberPure:"
            + getLineNumberPure()
            + " cellNumber:"
            + getCellNumber()
            + " IDPureWithoutCell:"
            + getIDPureWithoutCell()
            + " isForwardLine:"
            + isForwardLine()
            + " isMultiFP:"
            + isMultiFP()
            + " alt(m):"
            + (alt / 100.)
            + " gpsAlt(m):"
            + (gps_altitude_cm / 100.)
            + " gps_ellips(m):"
            + (gps_ellipsoid_cm / 100.)
            + " lat:"
            + lat
            + " lon:"
            + lon
            + " raw:"
            + rawString;
    }

    public static final double DATA_DELAY_INSTANT = 0; // 30 ; //ms
    public static final double DATA_DELAY_PHOTO2 = 30; // ms

    public static final String LINE_LOG_PREFIX = "$";
    public static final String LOG_PREFIX_ANY_PHOTO = LINE_LOG_PREFIX + "PHOTO";
    public static final String LOG_PREFIX_TRIGGER = LOG_PREFIX_ANY_PHOTO + "1";
    public static final String LOG_PREFIX_DELAYED = LOG_PREFIX_ANY_PHOTO + "2";
    public static final String LOG_PREFIX_FLASH = LOG_PREFIX_ANY_PHOTO + "3";

    public static void main(String[] args) throws Exception {
        // int i =3;
        // System.out.println("i="+i);
        // System.out.println("i%4="+(i%4));
        // System.out.println("-(i+5*threashold)="+((-(i+5*thresholdMultiFPids))));
        // System.out.println("-(i+5*threashold)%4="+((-(i+5*thresholdMultiFPids))%4));

        String str =
            "$PHOTO3;1;1383824976.060200;-182;0;49.3710408999999970;8.6409297783333336;1;0;1;15.72;-5.48;112.43;-0.48;-0.95;-0.23;10687;4795;4;233.85;0;0;1383824976.070000;1383824976.060000;1383824976.060200;1383782400;4;0.840000";
        CPhotoLogLine line = new CPhotoLogLine(str);
        System.out.println("line:" + line);
        System.out.println("fixType:" + line.fixType);

        File f =
            new File(
                "/home/marco/data/Open Mission Control/sessions/Demo_Session/matchings/2012_10_12_Corn_Hail_Damage_at_Nienburg/matched_photo-Fri-12-Oct-2012_09-26-46.plg");

        System.out.println("Reading and fixing camera_yaw interpolation bug in: " + f);

        try (FileInputStream in = new FileInputStream(f)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            File fOut = new File(f.getParentFile(), "fixed_" + f.getName());
            PrintWriter out = new PrintWriter(fOut);

            String strLine;
            // Read File Line By Line
            CPhotoLogLine plgLast = null;
            while ((strLine = br.readLine()) != null) {
                try {
                    CPhotoLogLine plg = new CPhotoLogLine(strLine);
                    if (plg.type == PhotoLogLineType.FLASH && plgLast != null) {
                        plg.cameraYaw = plgLast.cameraYaw;
                        plg.heading = plgLast.heading;
                    }

                    out.println(plg.toRawString());
                    plgLast = plg;
                } catch (Exception e) {
                    System.out.println(strLine);
                    out.println(strLine);
                    e.printStackTrace();
                }
            }
        }
    }

    public double getTimestamp() {
        return timestamp;
    }

    public double getTimestampRaw() {
        return timestamp;
    }

    public void setTimestampRaw(double timestamp) {
        this.timestamp = timestamp;
    }

    public boolean hasSeenAnotherFlashPlg = false;

}
