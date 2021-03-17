/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.logfile;

import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.PhotoLogLineType;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.PhotoLogLineType;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class LogReaderPLG extends ALogReader {

    PhotoLogLineType bestTriggerType;
    PhotoLogLineType worstTriggerType;

    public LogReaderPLG(IAirplane plane, File inputFile) {
        super(plane, inputFile);
    }

    @Override
    protected int getEstimatorQueSize() {
        return 10;
    }

    protected double extractTimestamp(String line) throws Exception {
        CPhotoLogLine plg = new CPhotoLogLine(line);
        if (plg.type != null) {
            if (bestTriggerType == null) {
                bestTriggerType = plg.type;
                worstTriggerType = plg.type;
            } else {
                if (plg.type.isBetterThan(bestTriggerType)) {
                    bestTriggerType = plg.type;
                }

                if (!plg.type.isBetterThan(worstTriggerType)) {
                    worstTriggerType = plg.type;
                }
            }
        }
        // System.out.println("type: " + plg.type + " best:" + bestTriggerType + " worst:"+worstTriggerType + "
        // hash:"+hashCode());
        return plg.getTimestamp() * 1000;
    }

    protected void processLine(String line) throws Exception {
        Debug.getLog().log(Level.FINEST, "Reading one line");
        if (line == null) {
            return;
        }

        if (line.length() == 0) {
            throw new IOException("lines should not be empty");
        }

        if (!CPhotoLogLine.isMaybeParseableLine(line)) {
            Debug.getLog().log(Level.FINEST, "Skipping unused line:" + line);
            return;
        }

        final CPhotoLogLine plg = new CPhotoLogLine(line);

        m_cur_gps_lat = plg.lat;
        m_cur_gps_lon = plg.lon;

        // m_cur_flight_mode = AirplaneFlightmode.AutomaticFlight.ordinal();
        m_last_flight_phase = AirplaneFlightphase.airborne.ordinal();

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

        final OrientationData m_cur_orientation = plg.getPackageOrientationData();
        final PositionData m_cur_position = plg.getPackagePositionData();

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

        // System.out.println("type:" + plg.type + " worst:"+worstTriggerType+ " hash:"+hashCode());
        if (plg.type == worstTriggerType) {
            final PhotoData cur_photo = plg.getPackagePhotoData();
            // System.out.println("fire!");
            invokeMessage(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (rootHandler != null) {
                                rootHandler.recv_photo(cur_photo);
                            }
                        } catch (Exception e) {
                            Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for recv_photo", e);
                        }
                    }
                });
        }
    }

    private double m_cur_gps_lat;
    private double m_cur_gps_lon;
    private boolean m_sent_start;
    private double m_start_lon;
    private double m_start_lat;

}
