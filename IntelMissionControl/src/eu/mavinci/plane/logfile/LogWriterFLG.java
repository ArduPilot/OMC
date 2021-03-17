/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.logfile;

import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;

import java.io.IOException;
import java.util.logging.Level;

public class LogWriterFLG extends ALogWriter implements IAirplaneListenerLogData {

    public static final String FLG_LINE_PREFIX = "$LG2";

    public LogWriterFLG(IAirplane plane) {
        super(plane);
        suffix = "flg";
        m_column_entries = new String[ENTRY_LAST_NUM];
    }

    @Override
    public void recv_orientation(OrientationData o) {
        m_column_entries[ENTRY_ROLL] = String.valueOf(o.roll);
        m_column_entries[ENTRY_PITCH] = String.valueOf(o.pitch);
        m_column_entries[ENTRY_YAW] = String.valueOf(o.yaw);
    }

    // double lastTime = 0;

    @SuppressWarnings("deprecation")
    @Override
    public void recv_position(PositionData p) {
        m_column_entries[ENTRY_GPS_LAT] = String.valueOf(p.lat);
        m_column_entries[ENTRY_GPS_LON] = String.valueOf(p.lon);
        m_column_entries[ENTRY_ALT] = String.valueOf(p.altitude);
        m_column_entries[ENTRY_GPS_ALT] = String.valueOf(p.gpsAltitude);
        m_column_entries[ENTRY_GPS_VEL] = String.valueOf(p.groundspeed);
        m_column_entries[ENTRY_FLIGHT_MODE] = String.valueOf(p.flightmode);
        m_column_entries[ENTRY_FLIGHT_PHASE] = String.valueOf(p.flightphase);
        m_column_entries[ENTRY_S_M_TEMP] = String.valueOf(p.temperature);
        m_column_entries[ENTRY_TIMESTAMP] = String.format("%.6f", p.getTimestamp());

        // double thisTime = p.time_sec
        // + p.time_usec / 1000000.0;
        //
        // System.out.println("pos pack" + p);
        // System.out.println("timestamp " + p.time_sec + " " + p.time_usec);
        // System.out.println("diff "+(thisTime - lastTime));
        // if (thisTime < lastTime) System.out.println("NEGATIVEEEEE!!!");
        // System.out.println();
        //
        // lastTime = thisTime;

        if (m_do_log) {
            makeRow();
        }
    }

    private void makeRow() {
        // TODO: Get these from somewhere ?
        m_column_entries[ENTRY_PROTOCOLL_VER] = FLG_LINE_PREFIX;
        m_column_entries[ENTRY_ALT_SONIC] = "-1";
        m_column_entries[ENTRY_GPS_BRG] = "-1";
        m_column_entries[ENTRY_D_YAW_TO_NEXT_WP] = "-1";
        m_column_entries[ENTRY_ALT_ERR_AVG] = "-1";
        m_column_entries[ENTRY_MXYAVG] = "-1";
        m_current_string =
            m_column_entries[ENTRY_PROTOCOLL_VER]
                + DELIMITER
                + m_column_entries[ENTRY_TIMESTAMP]
                + DELIMITER
                + m_column_entries[ENTRY_ALT]
                + DELIMITER
                + m_column_entries[ENTRY_ALT_SONIC]
                + DELIMITER
                + m_column_entries[ENTRY_GPS_LAT]
                + DELIMITER
                + m_column_entries[ENTRY_GPS_LON]
                + DELIMITER
                + m_column_entries[ENTRY_GPS_BRG]
                + DELIMITER
                + m_column_entries[ENTRY_GPS_VEL]
                + DELIMITER
                + m_column_entries[ENTRY_FLIGHT_MODE]
                + DELIMITER
                + m_column_entries[ENTRY_FLIGHT_PHASE]
                + DELIMITER
                + m_column_entries[ENTRY_D_YAW_TO_NEXT_WP]
                + DELIMITER
                + m_column_entries[ENTRY_ALT_ERR_AVG]
                + DELIMITER
                + m_column_entries[ENTRY_S_M_TEMP]
                + DELIMITER
                + m_column_entries[ENTRY_ROLL]
                + DELIMITER
                + m_column_entries[ENTRY_PITCH]
                + DELIMITER
                + m_column_entries[ENTRY_YAW]
                + DELIMITER
                + m_column_entries[ENTRY_MXYAVG]
                + DELIMITER
                + m_column_entries[ENTRY_GPS_ALT];
        try {
            write(m_current_string);
        } catch (IOException e) {
            Debug.getLog().log(Level.WARNING, "Cannot write to Stream", e);
        }
    }

    @Override
    protected String getHeader() {
        return FLG_LINE_PREFIX
            + ";\"time\";\"altitude\";\"altitude-ultrasonic\";\"gps-latitude\";\"gps-longitude\";\"aircraft.speed.groundspeedDirectionGPS\";\"aircraft.speed.groundspeed\";\"flightmode\";\"flightphase\";\"dYAW to next WP\";\"aerroravg/aerrorcnt\";\"sm_temperature\";\"roll\";\"pitch\";\"yaw\";\"mxyavg\";\"gps_alt\"";
    }

    private String[] m_column_entries;
    private String m_current_string;
    private static final String DELIMITER = ";";
    // TODO: pm: ich weiß, dass es in Java auch enums gibt - to be investigated
    private static final short ENTRY_PROTOCOLL_VER = 0;
    private static final short ENTRY_TIMESTAMP = 1;
    private static final short ENTRY_ALT = 2;
    private static final short ENTRY_ALT_SONIC = 3;
    private static final short ENTRY_GPS_LAT = 4;
    private static final short ENTRY_GPS_LON = 5;
    private static final short ENTRY_GPS_BRG = 6;
    private static final short ENTRY_GPS_VEL = 7;
    private static final short ENTRY_FLIGHT_MODE = 8;
    private static final short ENTRY_FLIGHT_PHASE = 9;
    private static final short ENTRY_D_YAW_TO_NEXT_WP = 10;
    private static final short ENTRY_ALT_ERR_AVG = 11;
    private static final short ENTRY_S_M_TEMP = 12;
    private static final short ENTRY_ROLL = 13;
    private static final short ENTRY_PITCH = 14;
    private static final short ENTRY_YAW = 15;
    private static final short ENTRY_MXYAVG = 16;
    private static final short ENTRY_GPS_ALT = 17;
    private static final short ENTRY_LAST_NUM = 18;

    @Override
    protected void requestAll() {
        m_plane.getAirplaneCache().invokeWithCacheValues(this);
    }

    @Override
    public void close() throws IOException {
        Debug.getLog().log(Level.WARNING, "Closing Log file");
    }

}
