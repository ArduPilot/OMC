/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.logfile;

import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class LogReaderBBX extends ALogReader {

    public LogReaderBBX(IAirplane plane, File inputFile) {
        super(plane, inputFile);

        final PlaneInfo pi = BBX_logline.getPlaneInfo(plane);

        invokeMessage(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (rootHandler != null) {
                            rootHandler.recv_planeInfo(pi);
                        }
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for PlaneInfo", e);
                    }
                }
            });
    }

    @Override
    protected int getEstimatorQueSize() {
        return 10;
    }

    String lastLine;
    BBX_logline parsed;

    protected void doSplit(String line) {
        if (lastLine == line) {
            return;
        }

        lastLine = line;
        parsed = new BBX_logline(line);
    }

    protected double extractTimestamp(String line) throws Exception {
        if (line == null) {
            return -1;
        }

        doSplit(line);
        return parsed.timestamp * 1000;
    }

    private boolean m_sent_start;

    protected void processLine(String line) throws Exception {
        Debug.getLog().log(Level.FINEST, "Reading one line");

        if (line == null) {
            return;
        }

        doSplit(line);

        if (line.length() == 0) {
            throw new IOException("lines should not be empty");
        }

        m_last_flight_phase = parsed.flightphase;

        if (!m_sent_start && parsed.lat != 0 && parsed.lon != 0) {
            invokeMessage(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (rootHandler != null) {
                                rootHandler.recv_startPos(parsed.lon, parsed.lat, 1);
                            }
                        } catch (Exception e) {
                            Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for recv_startPos Data", e);
                        }
                    }
                });
            m_sent_start = true;
        }

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

        final PositionOrientationData m_cur_Pos_orientation = parsed.getPackagePositionOrientationData();
        invokeMessage(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (rootHandler != null) {
                            rootHandler.recv_positionOrientation(m_cur_Pos_orientation);
                        }
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for Position Orientation Data", e);
                    }
                }
            });

        final DebugData m_debug = parsed.getPackageDebugData();
        invokeMessage(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (rootHandler != null) {
                            rootHandler.recv_debug(m_debug);
                        }
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for Debug Data", e);
                    }
                }
            });

        final HealthData m_hd = parsed.getPackageHealthData();
        invokeMessage(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (rootHandler != null) {
                            rootHandler.recv_health(m_hd);
                        }
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for Health Data", e);
                    }
                }
            });
    }

}
