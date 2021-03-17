/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.logfile;

import com.sun.jna.ptr.IntByReference;
import eu.mavinci.core.flightplan.PhotoLogLineType;
import eu.mavinci.core.helper.FiniteQue;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.gui.asctec.AntennaInformation;
import eu.mavinci.desktop.gui.asctec.FalconLogLib;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.PhotoLogLine;
import eu.mavinci.plane.IAirplane;
import org.openide.util.NotImplementedException;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public class LogReaderAscTec extends ALogReader {

    PhotoLogLineType bestTriggerType;
    PhotoLogLineType worstTriggerType;

    public LogReaderAscTec(IAirplane plane, File inputFile) {
        Debug.getLog().fine("init ascTec Logfile replay file=" + inputFile);
        this.inputFile = inputFile;
        m_start_timestamp = -1;
        try {
            AntennaInformation info = new AntennaInformation();
            IntByReference num = new IntByReference();
            List<PhotoLogLine> logs = FalconLogLib.getPhotoLogLines(inputFile, info, num);
            for (PhotoLogLine log : logs) {
                try {
                    //System.out.println();
                    Line l = new Line(log, log.getTimestamp() * 1000);
                    addLine(l);
                } catch (Exception e) {
                }
            }
        } finally {
            if (getLineCount() != 0) {
                double startTime = lines.get(0).time;
                double endTime = lines.get(lines.size() - 1).time;
                totalTime = endTime - startTime;
            }
        }

        System.out.println(
            "end reading asctec log: found " + getTotalTime() + " seconds in " + getLineCount() + " lines");
        int ESTIMATOR_QUE_SIZE = getEstimatorQueSize();
        que_realtime = new FiniteQue<Double>(ESTIMATOR_QUE_SIZE);
        que_timestamp = new FiniteQue<Double>(ESTIMATOR_QUE_SIZE);
        init(plane);
    }

    @Override
    protected int getEstimatorQueSize() {
        return 10;
    }

    protected void processLine(PhotoLogLine plg) throws Exception {
        Debug.getLog().log(Level.FINEST, "Reading one line");

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

    @Override
    protected double extractTimestamp(String line) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    protected void processLine(String line) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    protected void processCurrentLine() throws Exception {
        processLine((PhotoLogLine)getCurrentLineObject());
    }
}
