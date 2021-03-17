/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.logfile;

import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.core.plane.sendableobjects.SingleHealthDescription;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.core.plane.sendableobjects.SingleHealthDescription;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class BBX_logline {
    String id;
    int timestamp = -1;

    // position
    int altitude; // aircraft.position.altitude,
    int shortRangeAltitude; // aircraft.position.shortRangeAltitude,
    double lat; // gps_position.lat,
    double lon; // gps_position.lon,
    int gps_altitude; // gps_position.alt,
    int gps_bearing; // aircraft.speed.groundspeedDirectionGPS,
    int groundspeed; // aircraft.speed.groundspeed,
    int altitudeASL; // from GPS, ... in cm

    // misc
    int flightmode;
    int flightphase;
    int bearing_next_wp; // bearing(&gps_position,&gps_next_waypoint),
    int aerroravg; // converted to short:
    // (aerrorcnt>0?aerroravg/aerrorcnt:0),
    int[] orientation_euler; // r2d(imud.euler[0]),
    int[] setpoint_orientation_euler; // r2d(imud.euler[0]),

    // 8 servo channels
    int[] servo_values;

    int vario; // ! vario in cm/sec

    int declination; // estimation from model
    int cte; // ! in cm

    int temp_pressureSensor; // int sm_tempertature;
    int temp_powerSupply;
    int temp_processingUnit;
    int temp_imu_x;
    int temp_imu_y;
    int temp_imu_z;
    int mainloopRate;
    int imuRateFrequency;
    int motor1_rpm; // ! Speed of motor 1 [rpm]
    int motor2_rpm; // ! Speed of motor 2 [rpm]

    int mainBattery;
    int supply_5v;
    int supply_5va;
    int supply_5v_servos;
    int supply_5v_imu;
    int supply_3v3;
    int supply_3v3_analog;
    int[] external;

    int servosOverLoad; // 8 bit bit field

    int[] numberOfSatellites;

    double[] compassRaw;
    double[] gyroRaw;
    double[] acclRaw;

    double[] compass_calibration_offset;
    double[] compass_calibration_rotation;
    double[] compass_calibration_diameter;
    double compass_calibration_absoluteStrength;
    double magnAbsoluteStrength;

    int[] timing_MAIN;
    // int[] timing_IMU_MEASURE;
    // int[] timing_IMU_FILTER;
    int[] timing_LOG;
    int[] timing_STABILIZE;
    // int[] timing_PRESSURE;
    // int[] timing_COMPASS;
    // int[] timing_MANUAL;
    int[] timing_TINY;
    int[] timing_NAV;
    int[] timing_VM;
    // int[] timing_ROAD;
    // int[] timing_ALT;
    int[] timing_GPS;
    // int[] timing_MOTOR;
    int[] timing_HEALTH;
    int[] timing_DEBUG;
    int[] timing_PHOTO;
    // int[] timing_PHASE;
    int[] timing_CAN;
    int[] timing_SERVO;
    int[] timing_LO_RXTX;
    int[] timing_BB;
    int[] timing_DISP;

    String[] splits;

    public BBX_logline(String line) {
        try {
            splits = line.split(Pattern.quote("\t"));
            id = splits[0];
            timestamp = Integer.parseInt(splits[1]);
            altitude = Integer.parseInt(splits[2]);
            shortRangeAltitude = Integer.parseInt(splits[3]);
            lat = parseDouble(splits[4]);
            lon = parseDouble(splits[5]);
            gps_altitude = Integer.parseInt(splits[6]);
            gps_bearing = Integer.parseInt(splits[7]);
            groundspeed = Integer.parseInt(splits[8]);
            altitudeASL = Integer.parseInt(splits[9]);
            flightmode = Integer.parseInt(splits[10]);
            flightphase = Integer.parseInt(splits[11]);
            bearing_next_wp = Integer.parseInt(splits[12]);
            aerroravg = Integer.parseInt(splits[13]);
            orientation_euler = new int[3];
            orientation_euler[0] = Integer.parseInt(splits[14]);
            orientation_euler[1] = Integer.parseInt(splits[15]);
            orientation_euler[2] = Integer.parseInt(splits[16]);
            setpoint_orientation_euler = new int[3];
            for (int i = 0; i != 3; i++) {
                setpoint_orientation_euler[i] = Integer.parseInt(splits[17 + i]);
            }

            servo_values = new int[8];
            for (int i = 0; i != 8; i++) {
                servo_values[i] = Integer.parseInt(splits[20 + i]);
            }

            vario = Integer.parseInt(splits[28]);
            declination = Integer.parseInt(splits[29]);
            cte = Integer.parseInt(splits[30]);

            temp_pressureSensor = Integer.parseInt(splits[31]);
            temp_powerSupply = Integer.parseInt(splits[32]);
            temp_processingUnit = Integer.parseInt(splits[33]);
            temp_imu_x = Integer.parseInt(splits[34]);
            temp_imu_y = Integer.parseInt(splits[35]);
            temp_imu_z = Integer.parseInt(splits[36]);
            mainloopRate = Integer.parseInt(splits[37]);
            imuRateFrequency = Integer.parseInt(splits[38]);
            motor1_rpm = Integer.parseInt(splits[39]);
            motor2_rpm = Integer.parseInt(splits[40]);
            mainBattery = Integer.parseInt(splits[41]);
            supply_5v = Integer.parseInt(splits[42]);
            supply_5va = Integer.parseInt(splits[43]);
            supply_5v_servos = Integer.parseInt(splits[44]);
            supply_5v_imu = Integer.parseInt(splits[45]);
            supply_3v3 = Integer.parseInt(splits[46]);
            supply_3v3_analog = Integer.parseInt(splits[47]);

            external = new int[4];
            for (int i = 0; i != 4; i++) {
                external[i] = Integer.parseInt(splits[48 + i]);
            }

            servosOverLoad = Integer.parseInt(splits[52]);

            numberOfSatellites = new int[2];
            for (int i = 0; i != 2; i++) {
                numberOfSatellites[i] = Integer.parseInt(splits[53 + i]);
            }

            if (splits.length > 55) {
                compassRaw = new double[3];
                gyroRaw = new double[3];
                acclRaw = new double[3];
                compass_calibration_offset = new double[3];
                compass_calibration_rotation = new double[4];
                compass_calibration_diameter = new double[3];
                for (int i = 0; i != 3; i++) {
                    compassRaw[i] = parseDouble(splits[55 + i]);
                    gyroRaw[i] = parseDouble(splits[58 + i]);
                    acclRaw[i] = parseDouble(splits[61 + i]);
                    compass_calibration_offset[i] = parseDouble(splits[64 + i]);
                    compass_calibration_rotation[i] = parseDouble(splits[67 + i]);
                    compass_calibration_diameter[i] = parseDouble(splits[71 + i]);
                }

                compass_calibration_rotation[3] = parseDouble(splits[70]);

                compass_calibration_absoluteStrength = parseDouble(splits[74]);
                magnAbsoluteStrength = parseDouble(splits[75]);

                timing_MAIN = new int[2];
                // timing_IMU_MEASURE = new int[2];
                // timing_IMU_FILTER = new int[2];
                timing_LOG = new int[2];
                timing_STABILIZE = new int[2];
                // timing_PRESSURE = new int[2];
                // timing_COMPASS = new int[2];
                // timing_MANUAL = new int[2];
                timing_TINY = new int[2];
                timing_NAV = new int[2];
                timing_VM = new int[2];
                // timing_ROAD = new int[2];
                // timing_ALT = new int[2];
                timing_GPS = new int[2];
                // timing_MOTOR = new int[2];
                timing_HEALTH = new int[2];
                timing_DEBUG = new int[2];
                timing_PHOTO = new int[2];
                // timing_PHASE = new int[2];
                timing_CAN = new int[2];
                timing_SERVO = new int[2];
                timing_LO_RXTX = new int[2];
                timing_BB = new int[2];
                timing_DISP = new int[2];
                for (int i = 0; i != 2; i++) {
                    timing_MAIN[i] = Integer.parseInt(splits[76 + i]);
                    // timing_IMU_MEASURE[i] = Integer.parseInt(splits[79 + i]);
                    // timing_IMU_FILTER[i] = Integer.parseInt(splits[79 + i]);
                    timing_LOG[i] = Integer.parseInt(splits[78 + i]);
                    timing_STABILIZE[i] = Integer.parseInt(splits[80 + i]);
                    // timing_PRESSURE[i] = Integer.parseInt(splits[79 + i]);
                    // timing_COMPASS[i] = Integer.parseInt(splits[79 + i]);
                    // timing_MANUAL[i] = Integer.parseInt(splits[79 + i]);
                    timing_TINY[i] = Integer.parseInt(splits[82 + i]);
                    timing_NAV[i] = Integer.parseInt(splits[84 + i]);
                    timing_VM[i] = Integer.parseInt(splits[86 + i]);
                    // timing_ROAD[i] = Integer.parseInt(splits[79 + i]);
                    // timing_ALT[i] = Integer.parseInt(splits[79 + i]);
                    timing_GPS[i] = Integer.parseInt(splits[88 + i]);
                    // timing_MOTOR[i] = Integer.parseInt(splits[79 + i]);
                    timing_HEALTH[i] = Integer.parseInt(splits[90 + i]);
                    timing_DEBUG[i] = Integer.parseInt(splits[92 + i]);
                    timing_PHOTO[i] = Integer.parseInt(splits[94 + i]);
                    // timing_PHASE[i] = Integer.parseInt(splits[79 + i]);
                    timing_CAN[i] = Integer.parseInt(splits[96 + i]);
                    timing_SERVO[i] = Integer.parseInt(splits[98 + i]);
                    timing_LO_RXTX[i] = Integer.parseInt(splits[100 + i]);
                    timing_BB[i] = Integer.parseInt(splits[102 + i]);
                    timing_DISP[i] = Integer.parseInt(splits[104 + i]);
                }
            }

        } catch (Exception e) {
            Debug.getLog().log(Level.CONFIG, "problem parsing BBX logline", e);
            if (timestamp < 0) {
                throw e;
            }
        }
    }

    public static double parseDouble(String s) {
        s = s.trim();
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            if ("-inf".equalsIgnoreCase(s)) {
                return Double.NEGATIVE_INFINITY;
            }

            if ("inf".equalsIgnoreCase(s)) {
                return Double.POSITIVE_INFINITY;
            }

            if ("+inf".equalsIgnoreCase(s)) {
                return Double.POSITIVE_INFINITY;
            }

            return Double.NaN;
        }
    }

    public HealthData getPackageHealthData() {
        HealthData hd = new HealthData();

        // hd.absolute.add((float) gps_altitude_cm);
        hd.absolute.add((float)gps_bearing);
        // hd.absolute.add((float) groundspeed);
        hd.absolute.add((float)altitudeASL);
        hd.absolute.add((float)bearing_next_wp);
        hd.absolute.add((float)aerroravg / 100);
        hd.absolute.add((float)vario);
        hd.absolute.add((float)declination);
        hd.absolute.add((float)cte);
        hd.absolute.add((float)temp_pressureSensor);
        hd.absolute.add((float)temp_powerSupply);
        hd.absolute.add((float)temp_processingUnit);
        hd.absolute.add((float)temp_imu_x);
        hd.absolute.add((float)temp_imu_y);
        hd.absolute.add((float)temp_imu_z);
        hd.absolute.add((float)mainloopRate);
        hd.absolute.add((float)imuRateFrequency);
        hd.absolute.add((float)motor1_rpm);
        hd.absolute.add((float)motor2_rpm);
        hd.absolute.add((float)mainBattery / 10);
        hd.absolute.add((float)supply_5v / 10);
        hd.absolute.add((float)supply_5va / 10);
        hd.absolute.add((float)supply_5v_servos / 10);
        hd.absolute.add((float)supply_5v_imu / 10);
        hd.absolute.add((float)supply_3v3 / 10);
        hd.absolute.add((float)supply_3v3_analog / 10);
        hd.absolute.add((float)external[0] / 10);
        hd.absolute.add((float)external[1] / 10);
        hd.absolute.add((float)external[2] / 10);
        hd.absolute.add((float)external[3] / 10);
        hd.absolute.add((float)servosOverLoad);
        hd.absolute.add((float)numberOfSatellites[0]);
        hd.absolute.add((float)numberOfSatellites[1]);

        try {
            String[] names =
                new String[] {
                    "compassRaw",
                    "gyroRaw",
                    "acclRaw",
                    "compass_calibration_offset",
                    "compass_calibration_rotation",
                    "compass_calibration_diameter"
                };

            for (String name : names) {
                Field field = BBX_logline.class.getDeclaredField(name);
                double[] array = (double[])field.get(this);
                if (array != null) {
                    for (double d : array) {
                        hd.absolute.add((float)d);
                    }
                }
            }

            hd.absolute.add((float)compass_calibration_absoluteStrength);
            hd.absolute.add((float)magnAbsoluteStrength);
            names =
                new String[] {
                    "MAIN",
                    "LOG",
                    "STABILIZE",
                    "TINY",
                    "NAV",
                    "VM",
                    "GPS",
                    "HEALTH",
                    "DEBUG",
                    "PHOTO",
                    "CAN",
                    "SERVO",
                    "LO_RXTX",
                    "BB",
                    "DISP"
                };

            for (String name : names) {
                name = "timing_" + name;
                Field field = BBX_logline.class.getDeclaredField(name);
                int[] array = (int[])field.get(this);
                if (array != null && array.length > 1) {
                    hd.absolute.add((float)array[0]);
                    hd.absolute.add((float)array[1]);
                }
            }

        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "error preparing health data from blackBox entry", e);
        }

        // padding in case of old type data
        while (hd.absolute.size() < 82) {
            hd.absolute.add(0f);
        }

        while (hd.percent.size() < hd.absolute.size()) {
            hd.percent.add(PlaneConstants.minLevelForValidPercent - 1);
        }

        return hd;
    }

    public PositionOrientationData getPackagePositionOrientationData() {
        PositionOrientationData po = new PositionOrientationData();
        po.altitude = altitude;
        po.batteryPercent = 50;
        po.batteryVoltage = mainBattery;
        po.flightmode = flightmode;
        po.flightphase = flightphase;
        po.lat = lat;
        po.lon = lon;
        po.roll = Math.toDegrees(orientation_euler[0] / 32767. * Math.PI);
        po.pitch = Math.toDegrees(orientation_euler[1] / 32767. * Math.PI);
        po.yaw = Math.toDegrees(orientation_euler[2] / 32767. * Math.PI);
        po.reentrypoint = 0;
        po.time_sec = timestamp;
        po.time_usec = 0;
        po.fromSession = false;
        return po;
    }

    public DebugData getPackageDebugData() {
        DebugData d = new DebugData();
        d.accl.set(0, acclRaw[0]);
        d.accl.set(1, acclRaw[1]);
        d.accl.set(2, acclRaw[2]);
        // d.airborneTime = -1; //not avaliable
        d.ausage = -1;
        d.cross_track_error = cte;
        d.debug0 = -1;
        d.debug1 = -1;
        d.debug2 = -1;
        d.dtmax = -1;
        d.dtmax = -1;
        d.fromSession = false;
        d.gpsAltitude = gps_altitude;
        d.groundDistance = shortRangeAltitude;
        d.groundspeed = groundspeed;
        d.gyropitch = -1;
        d.gyroroll = -1;
        d.gyroyaw = -1;
        d.heading = gps_bearing;
        d.mainlooprate = mainloopRate;
        for (int i = 0; i != 8; i++) {
            d.manualServos.set(i, servo_values[i]);
        }

        d.setpointRoll = Math.toDegrees(setpoint_orientation_euler[0] / 32767. * Math.PI);
        d.setpointPitch = Math.toDegrees(setpoint_orientation_euler[1] / 32767. * Math.PI);
        d.setpointYaw = Math.toDegrees(setpoint_orientation_euler[2] / 32767. * Math.PI);
        d.time_sec = timestamp;
        d.time_usec = 0;
        return d;
    }

    public static PlaneInfo getPlaneInfo(IAirplane plane) {
        final PlaneInfo pi = new PlaneInfo();
        SingleHealthDescription shd;
        pi.servoChannelCount = 8;

        if (plane != null) {
            // fake compatible camera to remove validation
            pi.releaseVersion =
                plane.getHardwareConfiguration()
                    .getPlatformDescription()
                    .getAPtype()
                    .getCompatibleReleaseVersionString();
        }

        shd = new SingleHealthDescription();
        shd.name = "gps_bearing";
        shd.unit = "deg";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "altitudeASL";
        shd.unit = "cm";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "bearing_next_wp";
        shd.unit = "deg";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "aerroravg";
        shd.unit = "";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "vario";
        shd.unit = "cm/sec";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "declination";
        shd.unit = "deg";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "cte";
        shd.unit = "cm";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "temp_pressureSensor";
        shd.unit = "°C";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "temp_powerSupply";
        shd.unit = "°C";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "temp_processingUnit";
        shd.unit = "°C";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "temp_imu_x";
        shd.unit = "°C";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "temp_imu_y";
        shd.unit = "°C";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "temp_imu_z";
        shd.unit = "°C";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "mainloopRate";
        shd.unit = "Hz";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "imuRateFrequency";
        shd.unit = "Hz";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "Motor1";
        shd.unit = "rpm";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "Motor2";
        shd.unit = "rpm";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "Battery";
        shd.unit = "V";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "supply_5v";
        shd.unit = "V";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "supply_5va";
        shd.unit = "V";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "supply_5v_servos";
        shd.unit = "V";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "supply_5v_imu";
        shd.unit = "V";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "supply_3v3";
        shd.unit = "V";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "supply_3v3_analog";
        shd.unit = "V";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "external0";
        shd.unit = "V";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "external1";
        shd.unit = "V";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "external2";
        shd.unit = "V";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "external3";
        shd.unit = "V";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "servosOverLoad";
        shd.unit = "";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "GPS";
        shd.unit = "sat";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "numberOfSatellites2";
        shd.unit = "sat";
        pi.healthDescriptions.add(shd);

        String[] names =
            new String[] {
                "compassRaw",
                "gyroRaw",
                "acclRaw",
                "compass_calibration_offset",
                "compass_calibration_rotation",
                "compass_calibration_diameter"
            };

        for (String name : names) {
            int max = 3;
            if (name.equals("compass_calibration_rotation")) {
                max = 4;
            }

            for (int i = 0; i != max; i++) {
                shd = new SingleHealthDescription();
                shd.name = name + i;
                shd.unit = "";
                pi.healthDescriptions.add(shd);
            }
        }

        shd = new SingleHealthDescription();
        shd.name = "compass_calibration_absoluteStrength";
        shd.unit = "";
        pi.healthDescriptions.add(shd);

        shd = new SingleHealthDescription();
        shd.name = "magnAbsoluteStrength";
        shd.unit = "";
        pi.healthDescriptions.add(shd);

        names =
            new String[] {
                "MAIN",
                "LOG",
                "STABILIZE",
                "TINY",
                "NAV",
                "VM",
                "GPS",
                "HEALTH",
                "DEBUG",
                "PHOTO",
                "CAN",
                "SERVO",
                "LO_RXTX",
                "BB",
                "DISP"
            };

        for (String name : names) {
            name = "timing_" + name;
            shd = new SingleHealthDescription();
            shd.name = name + "-min";
            shd.unit = "";
            pi.healthDescriptions.add(shd);

            shd = new SingleHealthDescription();
            shd.name = name + "-max";
            shd.unit = "";
            pi.healthDescriptions.add(shd);
        }

        return pi;
    }

    public static void main(String[] args) {
        System.out.println("" + parseDouble("-inf"));
        System.out.println("" + parseDouble("inf"));
        System.out.println("" + parseDouble("+inf"));
        System.out.println("" + parseDouble("+nan"));
        System.out.println("" + parseDouble("-nan"));
        System.out.println("" + parseDouble("+15"));

        PlaneInfo pi = getPlaneInfo(null);
        System.out.println("healthsize=" + pi.healthDescriptions.size());

        BBX_logline l =
            new BBX_logline(
                "MVLG	1382539844	8	0	49.334190	8.672687	16800	172	0	16800	1	0	30	0	-4551	159	-4427	0	3640	-4427	1	190	35	106	190	128	128	128	0	1	0	29	84	0	-16	-17	-9	999	17756	0	0	204	48	0	0	0	0	0	0	0	0	0	0	5	0	0.439256	0.546906	0.799135	-0.069019	-0.153250	0.088350	-0.014319	0.437229	-0.891441	35.997158	-167.829941	40.559769	0.989573	-0.038175	0.002545	0.138856	512.354797	491.689240	457.873108	48398.136719	48392.792969	46	65535	155	214	9	93	10	80	1	59	5	114	16	65535	175	3460	15	61	1	75	10	330	16	148	7	569	1	14	59	151");
        System.out.println(l);
        for (Field f : BBX_logline.class.getDeclaredFields()) {
            System.out.println(f);
        }
        // Field f = BBX_logline.class.getDeclaredField("timing_TINY");
        // l.getPackageHealthData();
    }

}
