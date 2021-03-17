/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.logfile;

import eu.mavinci.core.flightplan.CDump;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.core.flightplan.CDump;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;

public class LogReaderFLG extends ALogReader {

    public LogReaderFLG(IAirplane plane, File inputFile) {
        super(plane, inputFile);
    }

    @Override
    protected int getEstimatorQueSize() {
        return 20;
    }

    protected double extractTimestamp(String line) throws Exception {
        StringTokenizer tok = new StringTokenizer(line, DELIMITER);
        tok.nextToken();
        String temp = tok.nextToken();
        int pos = temp.indexOf(".");
        if (pos != 0 && temp.length() - pos <= 6) { // only if the suffix has less than 6 bytes
            String tmpA = temp.substring(0, pos);
            String tmpB = temp.substring(pos + 1);
            int a = Integer.parseInt(tmpA);
            int b = Integer.parseInt(tmpB);
            // System.out.println("a=" + a + " b=" + b);
            return ((double)a + (double)(b) / 1e6d) * 1000.;
        }

        return Double.valueOf(temp) * 1000.;
    }

    private static final String DELIMITER = ";";

    @SuppressWarnings("deprecation")
    protected void processLine(String line) throws IOException {
        Debug.getLog().log(Level.FINEST, "Reading one line");
        if (line == null) {
            return;
        }

        if (line.length() == 0) {
            throw new IOException("lines should not be empty");
        }

        if (line.startsWith("#")) {
            Debug.getLog().log(Level.FINEST, "Skipping comment");
            return;
        }

        StringTokenizer tok = new StringTokenizer(line, DELIMITER);
        // Check start of line
        try {
            String head = tok.nextToken();
            if (!head.equals(LogWriterFLG.FLG_LINE_PREFIX)) {
                if (!head.equals(CDump.logLineFirstElement)) {
                    Debug.getLog().log(Level.WARNING, "Wrong protocoll version:" + head);
                }

                return;
            }

            tok.nextElement(); //
            // timestamp is allready parsed
            // m_cur_timestamp = parseBrokenTimeRead(tok.nextToken());
            //
            m_cur_alt = safeDoubleRead(tok.nextToken());
            safeDoubleRead(tok.nextToken()); // alt-ultrasonic
            m_cur_gps_lat = safeDoubleRead(tok.nextToken());
            m_cur_gps_lon = safeDoubleRead(tok.nextToken());
            safeDoubleRead(tok.nextToken()); // groundspeedDirectionGPS
            m_cur_gps_vel = safeDoubleRead(tok.nextToken());
            m_cur_flight_mode = Integer.valueOf(tok.nextToken());
            m_last_flight_phase = m_cur_flight_phase;
            m_cur_flight_phase = Integer.valueOf(tok.nextToken());
            safeDoubleRead(tok.nextToken()); // dYAW to next WP
            safeDoubleRead(tok.nextToken()); // aerroravg/aerrorcnt
            m_cur_s_m_temp = safeDoubleRead(tok.nextToken());
            m_cur_roll = safeDoubleRead(tok.nextToken());
            m_cur_pitch = safeDoubleRead(tok.nextToken());
            m_cur_yaw = safeDoubleRead(tok.nextToken());
            safeDoubleRead(tok.nextToken()); // mxyavg
            m_cur_gps_alt = safeDoubleRead(tok.nextToken());

            // irgnore the following entrys
            // /* 17 */ servoArray[SERVO_MOTOR].currentValue, // Throttle servo [0..255]
            // /* 18 */ servoArray[SERVO_AILERON].currentValue, // Aileron
            // /* 19 */ servoArray[SERVO_ELEVATOR].currentValue, // Elevator
            // /* 20 */ servoArray[SERVO_RUDDER].currentValue, // Rudder
            // /* 21 */ r2d(setpoint_orientation[0]),
            // /* 22 */ r2d(setpoint_orientation[1]),
            // /* 23 */ r2dd(setpoint_orientation[2]),
            //
            // /* 24 */ aircraft.environment.humidity,
            // /* 25 */ aircraft.environment.surfaceTemperature,
            // /* 26 */ aircraft.environment.airTemperature
        } catch (java.util.NoSuchElementException e) {
            return;
        }

        if (!m_sent_start && m_cur_gps_lat != 0 && m_cur_gps_lon != 0) {
            m_start_lat = m_cur_gps_lat;
            m_start_lon = m_cur_gps_lon;
            invokeMessage(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (rootHandler != null) {
                                rootHandler.recv_startPos(m_start_lon, m_start_lat, 1);
                            }
                        } catch (Exception e) {
                            Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for recv_startPos Data", e);
                        }
                    }
                });
            m_sent_start = true;
        }

        final OrientationData m_cur_orientation = new OrientationData();
        m_cur_orientation.roll = m_cur_roll;
        m_cur_orientation.pitch = m_cur_pitch;
        m_cur_orientation.yaw = m_cur_yaw;
        final PositionData m_cur_position = new PositionData();
        m_cur_position.altitude = (int)m_cur_alt;
        m_cur_position.flightmode = m_cur_flight_mode;
        m_cur_position.flightphase = m_cur_flight_phase;
        m_cur_position.gpsAltitude = (int)m_cur_gps_alt;
        m_cur_position.groundspeed = (int)m_cur_gps_vel;
        m_cur_position.lat = (float)m_cur_gps_lat;
        m_cur_position.lon = (float)m_cur_gps_lon;
        m_cur_position.temperature = (int)m_cur_s_m_temp;
        m_cur_position.time_sec = (int)(m_cur_timestamp / 1000);
        m_cur_position.time_usec = (int)(modf(m_cur_timestamp / 1000)[1] * 1000000);
        invokeMessage(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (rootHandler != null) {
                            rootHandler.recv_orientation(m_cur_orientation);
                        }
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for Orientation Data", e);
                    }
                }
            });
        invokeMessage(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (rootHandler != null) {
                            rootHandler.recv_position(m_cur_position);
                        }
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for Position Data", e);
                    }
                }
            });
        invokeMessage(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (rootHandler != null) {
                            rootHandler.recv_flightPhase(m_last_flight_phase);
                        }
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for recv_flightPhase", e);
                    }
                }
            });
    }

    private double m_cur_alt;
    private double m_cur_gps_lat;
    private double m_cur_gps_lon;
    private double m_cur_gps_vel;
    private int m_cur_flight_mode;
    private int m_cur_flight_phase;
    private double m_cur_s_m_temp;
    private double m_cur_roll;
    private double m_cur_pitch;
    private double m_cur_yaw;
    private double m_cur_gps_alt;
    private boolean m_sent_start;
    private double m_start_lon;
    private double m_start_lat;

    private double[] modf(double fullDouble) {
        long intVal = (long)fullDouble;
        double remainder = fullDouble - intVal;

        double[] retVal = new double[2];
        retVal[0] = intVal;
        retVal[1] = remainder;

        return retVal;
    }

}
