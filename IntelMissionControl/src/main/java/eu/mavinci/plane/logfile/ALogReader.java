/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.logfile;

import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import eu.mavinci.core.helper.FiniteQue;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.IAirplaneConnector;
import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackendConnectionLost;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerGuiClose;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.protocol.ProtocolInvoker;
import eu.mavinci.core.plane.protocol.ProtocolTokens;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.core.plane.tcp.AAirplaneConnector;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.zip.ZipInputStream;
import org.asyncfx.concurrent.Dispatcher;

public abstract class ALogReader extends AAirplaneConnector
        implements IAirplaneConnector,
            IAirplaneListenerGuiClose,
            IAirplaneListenerFlightphase,
            IAirplaneListenerPosition {

    public boolean dispatchEventsInUIthread = true;

    protected double m_start_timestamp = -1; // in msec
    protected double m_cur_timestamp = -1; // in msec

    private int lineNumber = 0;

    protected double m_cur_realtime = -1; // in msec

    private float m_sim_speed;
    protected boolean m_skip_to_next_phase = false;

    private boolean m_zip = false;
    private LineReaderThread m_reader_thread;
    protected int m_last_flight_phase;

    public static class Line {
        public Line(Object str, double time) {
            this.line = str;
            this.time = time;
        }

        public Object line;
        public double time; // ms
    }

    protected Vector<Line> lines = new Vector<Line>();
    protected double totalTime;

    protected ProtocolInvoker invoker = new ProtocolInvoker();

    public int getLineCount() {
        return lines.size();
    }

    public int getCurrentLine() {
        return lineNumber;
    }

    public boolean hasMoreLines() {
        if (m_sim_speed >= 0) {
            return lineNumber < lines.size() - 1;
        } else {
            return lineNumber > 0;
        }
    }

    public void jumpToLine(int lineNumber) {
        // System.out.println("jump to Line:" + lineNumber);
        if (lineNumber < 0 || lineNumber >= lines.size()) {
            return;
        }

        if (this.lineNumber == lineNumber) {
            return;
        }

        this.lineNumber = lineNumber;
        try {
            processCurrentLine();
            elapsedSimTime((lines.get(lineNumber).time - m_start_timestamp) / 1000., totalTime / 1000.);
        } catch (InterruptedException e) {
            // ignore
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "Problems reading one line of logile: No=" + lineNumber, e);
        }
    }

    public void jumpToNextLine() {
        if (m_sim_speed >= 0) {
            lineNumber++;
        } else {
            lineNumber--;
        }
        // System.out.println("jumpToNextLine:" + lineNumber);
    }

    public String getCurrentLineAndJump() {
        if (!hasMoreLines()) {
            return null;
        }

        jumpToNextLine();
        return getCurrentLineString();
    }

    public String getCurrentLineString() {
        return (String)lines.get(lineNumber).line;
    }

    public Object getCurrentLineObject() {
        return lines.get(lineNumber).line;
    }

    /**
     * in milliseconds
     *
     * @return
     */
    public double getCurrentLineTime() {
        return lines.get(lineNumber).time;
    }

    public boolean isSkippingPhase() {
        return m_skip_to_next_phase;
    }

    protected ALogReader(ICAirplane plane, File inputFile) {
        initFromFile(inputFile);
        init(plane);
    }

    protected ALogReader() {}

    protected File inputFile;

    public File getReplayedFile() {
        return inputFile;
    }

    public static ALogReader logReaderFactory(
            IAirplane plane, File inputFile, IHardwareConfigurationManager hwManager) {
        String file_name = inputFile.getName().toLowerCase();
        if (file_name.endsWith(".plg") || file_name.endsWith(".plg.zip")) {
            return new LogReaderPLG(plane, inputFile);
        } else if (file_name.endsWith(".vlg") || file_name.endsWith(".vlg.zip")) {
            return new LogReaderVLG(plane, inputFile);
        } else if (file_name.endsWith(".flg") || file_name.endsWith(".flg.zip")) {
            return new LogReaderFLG(plane, inputFile);
        } else if (file_name.endsWith(".bbx") || file_name.endsWith(".bbx.zip")) {
            return new LogReaderBBX(plane, inputFile);
        } else if (MFileFilter.ascTecLogFolder.acceptTrinityLog(inputFile)) {
            return new LogReaderAscTec(plane, inputFile);
        } else {
            Debug.getLog().log(Level.WARNING, "cannot open this file");
            return null;
        }
    }

    protected void initFromFile(File input_file) {
        Debug.getLog().fine("init Logfile replay file=" + input_file);
        this.inputFile = input_file;
        m_start_timestamp = -1;
        String file_name = inputFile.getName();
        m_zip = file_name.toLowerCase().endsWith(".zip");
        assert (inputFile.canRead());

        BufferedReader reader = null;
        try (InputStream fis = new FileInputStream(inputFile)) {
            InputStream in;
            if (m_zip) {
                in = new ZipInputStream(fis);
                ((ZipInputStream)in).getNextEntry();
            } else {
                in = fis;
            }

            reader = new BufferedReader(new InputStreamReader(in, ProtocolTokens.encoding));
            lineNumber = -1;
            lines.clear();
            String line;
            while (reader.ready() && (line = reader.readLine()) != null) {
                addLine(line.trim());
            }

            if (lines.size() == 0) {
                throw new EOFException("logfile contains no data");
            }
        } catch (EOFException e) {
            Debug.getLog()
                .log(
                    Debug.WARNING,
                    "Compressed Logfile seems corrupted:" + file_name + " Replaying what is readable.",
                    e);
        } catch (FileNotFoundException e) {
            Debug.getLog().log(Level.WARNING, "Logfile not found", e);
            return;
        } catch (IOException e) {
            Debug.getLog()
                .log(
                    Debug.WARNING,
                    "Compressed Logfile seems corrupted:" + file_name + " Replaying what is readable.",
                    e);
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "Error opening file " + file_name, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }

            if (lines.size() != 0) {
                double startTime = lines.get(0).time;
                double endTime = lines.get(lines.size() - 1).time;
                totalTime = endTime - startTime;
            }
        }

        int ESTIMATOR_QUE_SIZE = getEstimatorQueSize();
        que_realtime = new FiniteQue<Double>(ESTIMATOR_QUE_SIZE);
        que_timestamp = new FiniteQue<Double>(ESTIMATOR_QUE_SIZE);
    }

    protected abstract int getEstimatorQueSize();

    protected void addLine(Line line) {
        lines.add(line);
    }

    protected void addLine(String line) {
        if (line.length() == 0) {
            return;
        }

        if (line.startsWith("#")) {
            return;
        }

        try {
            double time = extractTimestamp(line);
            Line l = new Line(line, time);
            lines.add(l);
        } catch (Exception e) {
        }
    }

    /**
     * @param line
     * @return timestamp in milli seconds (most likely unix GPS time)
     * @throws Exception
     */
    protected abstract double extractTimestamp(String line) throws Exception;

    protected ICAirplane plane;

    protected void init(ICAirplane plane) {
        m_cur_timestamp = -1;
        this.plane = plane;
        setRootHandler(plane.getRootHandler());
        plane.setAirplaneConnector(this);
        fireConnectionState(AirplaneConnectorState.waitingForLogreplayStart);
    }

    public void startSimulation(Float speed) {
        Debug.getLog().fine("init Logfile startSimulation speed=" + speed);
        setSimulationSpeed(speed);
        fireConnectionState(AirplaneConnectorState.fullyConnected);
        fireReplayStopped(false);
        fireReplayPaused(false);
        rootHandler.recv_powerOn(); // reset everything
        sleepUebertrag = 0;
        if (m_reader_thread != null) {
            stopSimulation();
        }

        m_reader_thread = new LineReaderThread();
        if (speed != 0) {
            m_reader_thread.start();
        }
    }

    /**
     * for logfiles speed has a slightly different meaning.
     *
     * <p>speed 0 is similar to pause. speed infinity is as fast as possible, negative speeds mean backward in time
     * replay
     */
    @Override
    public void setSimulationSpeed(Float speed) {
        m_sim_speed = speed;
        final Float sim_speed = speed;
        sleepUebertrag = 0;
        doRunnableLater(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (rootHandler != null) {
                            rootHandler.recv_simulationSpeed(sim_speed);
                        }
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for Simulation speed", e);
                    }
                }
            });
        if (speed == 0) {
            pauseSimulation();
        } else if (isPaused()) {
            resumeSimulation();
        }
    }

    protected void doRunnableLater(Runnable r) {
        if (dispatchEventsInUIthread) {
            Dispatcher.platform().runLater(r);
        } else {
            r.run();
        }
    }

    public Float getSimulationSpeed() {
        return m_sim_speed;
    }

    public void skipToNextPhase(boolean shouldSkip) {
        if (shouldSkip == m_skip_to_next_phase) {
            return;
        }

        m_skip_to_next_phase = shouldSkip;
        fireReplaySkipPhase(m_skip_to_next_phase);
    }

    @SuppressWarnings("deprecation")
    public void pauseSimulation() {
        // System.out.println("pauseSim");
        fireReplayPaused(true);
        sleepUebertrag = 0;
        if (m_reader_thread != null) {
            m_reader_thread.suspend();
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    @SuppressWarnings("deprecation")
    public void resumeSimulation() {
        // Debug.getLog().log(Level.CONFIG,"resume simulation", new Exception());
        fireReplayPaused(false);
        sleepUebertrag = 0;
        m_cur_timestamp = -1;
        if (m_reader_thread != null) {
            m_reader_thread.resume();
        }
    }

    public FiniteQue<Double> que_realtime; // = new FiniteQue<Double>(ESTIMATOR_QUE_SIZE);
    public FiniteQue<Double> que_timestamp; // = new FiniteQue<Double>(ESTIMATOR_QUE_SIZE);

    private long sleepUebertrag = 0;

    public double getSimSpeedRealEstim() {
        if (que_realtime.isEmpty() || que_timestamp.isEmpty()) {
            return 0;
        }

        double diffReal = que_realtime.getFirst() - que_realtime.getLast();
        double diffTimestamp = que_timestamp.getFirst() - que_timestamp.getLast();

        // System.out.print("diffTimeStamp:"+diffTimestamp);
        // System.out.println(" diffReal:"+diffReal);
        // System.out.println(" estSpeed:"+ (diffTimestamp / diffReal));

        return diffTimestamp / diffReal;
    }

    /**
     * in seconds
     *
     * @return
     */
    public double getElapsedTime() {
        return (m_cur_timestamp - m_start_timestamp) / 1000.;
    }

    public double getTotalTime() {
        return totalTime / 1000.;
    }

    public static final double maxSpleepingTimestam = 5000; // ms

    private class LineReaderThread extends Thread {

        public LineReaderThread() {
            super("log replay thread");
            setPriority(MIN_PRIORITY);
            // (new Exception()).printStackTrace();
            // System.out.println("new thread hash" + hashCode());
        }

        private boolean exit = false;

        private long calculateSleepTime(boolean phase_change) {
            double m_last_timestamp = m_cur_timestamp;
            m_cur_timestamp = lines.get(lineNumber).time;
            if (m_last_timestamp == -1) {
                return 0;
            }

            if (m_skip_to_next_phase) {
                if (phase_change) {
                    skipToNextPhase(false);
                }

                return 0;
            }

            if (m_sim_speed == 0) {
                return 0;
            }

            if (isPaused()) {
                return 0;
            }

            long sleep_time = 0;
            long time_diff_milliseconds = Math.abs((long)(m_cur_timestamp - m_last_timestamp));
            Debug.getLog().log(Level.FINEST, "waiting for " + time_diff_milliseconds + "ms");

            sleep_time =
                (long)
                    Math.min(
                        Math.round(time_diff_milliseconds / Math.abs(m_sim_speed)),
                        (maxSpleepingTimestam / Math.abs(m_sim_speed)));

            // System.out.println("timeDiff:" + time_diff_milliseconds + " speed:"+m_sim_speed + " sleep_time:" +
            // sleep_time);

            return sleep_time;
        }

        public void run() {
            int last_flight_phase = 0;
            try {
                boolean change = false;
                while (hasMoreLines() && !exit) {
                    jumpToNextLine();
                    long sleep_time = calculateSleepTime(change);
                    Debug.getLog().log(Level.FINEST, "LogReaderThread is sleeping for [ms]:" + sleep_time);

                    if (m_cur_realtime != -1) {
                        // System.out.println("---\nbefore="+sleep_time);
                        sleep_time -= (System.currentTimeMillis() - m_cur_realtime);
                        // System.out.println("after="+sleep_time);
                    }
                    // System.out.println("shift ÜBertrag " + sleepUebertrag);
                    sleep_time += sleepUebertrag;

                    if (sleep_time > 0) {
                        sleepUebertrag = 0;
                        // System.out.println("doSleep " + sleep_time);
                        try {
                            Thread.sleep(sleep_time);
                        } catch (InterruptedException e1) {
                        }
                    } else {
                        // System.out.println("newÜBertrag " + sleep_time);
                        sleepUebertrag = Math.max(sleep_time, -1000); // maximal üebertrag 1 sec
                    }

                    if (exit) {
                        return;
                    }

                    m_cur_realtime = System.currentTimeMillis();
                    if (m_cur_timestamp != -1) {
                        que_realtime.add(m_cur_realtime);
                        que_timestamp.add(m_cur_timestamp);
                    }

                    try {
                        // System.out.println("processLine");
                        processCurrentLine();
                    } catch (InterruptedException e) {
                        // ignore
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "Problems reading one line of logile: No=" + lineNumber, e);
                    }

                    if (m_start_timestamp == -1) {
                        m_start_timestamp = m_cur_timestamp;
                    }

                    elapsedSimTime((m_cur_timestamp - m_start_timestamp) / 1000., totalTime / 1000.);
                    change = (m_last_flight_phase != last_flight_phase);
                    last_flight_phase = m_last_flight_phase;
                }

                doRunnableLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                fireReplayFinished();
                                if (rootHandler != null) {
                                    rootHandler.err_backendConnectionLost(
                                        IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.LOGFILE_AT_END);
                                }
                            } catch (Exception e) {
                                Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for Connection Lost", e);
                            }
                        }
                    });

            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "Cannot read from Logfile", e);
                doRunnableLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                fireReplayFinished();
                                if (rootHandler != null) {
                                    rootHandler.err_backendConnectionLost(
                                        IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.LOGFILE_AT_END);
                                }
                            } catch (Exception e) {
                                Debug.getLog().log(Level.WARNING, "ErrorCallingHandler for Connection Lost", e);
                            }
                        }
                    });
            }
        }

        public void exit() {
            exit = true;
            this.interrupt();
        }
    };

    protected abstract void processLine(String line) throws Exception;

    protected double safeDoubleRead(String temp) {
        double val = 0;

        if (temp.equals("null")) {
            val = 0;
        } else {
            try {
                val = Double.valueOf(temp);
            } catch (java.lang.NumberFormatException e) {
                val = 0;
            }
        }

        return val;
    }

    @Override
    public void connect(String port) {
        // ignore
    }

    @Override
    public boolean isWriteable() {
        return false; // ... so nothing will be send to Backend..
    }

    public void stopSimulation() {
        fireReplayStopped(true);
        fireConnectionState(AirplaneConnectorState.waitingForLogreplayStart);
        try {
            if (m_reader_thread == null) {
                return;
            }

            m_reader_thread.exit();
            m_reader_thread = null;
        } catch (Throwable t) {
            return;
        }

        if (rootHandler != null) {
            rootHandler.err_backendConnectionLost(
                IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.LOGFILE_REPLAY_STOPPED);
        }
    }

    public boolean isStopped() {
        return m_reader_thread == null;
    }

    protected void processCurrentLine() throws Exception {
        processLine(getCurrentLineString());
    }

    public void rewindAndStartSimulation() {
        fireReplayStopped(false);
        que_realtime.clear();
        que_timestamp.clear();
        lineNumber = -1;
        m_start_timestamp = -1;
        m_cur_timestamp = -1;
        m_cur_realtime = -1;
        sleepUebertrag = 0;
        startSimulation(1.0f);
    }

    @Override
    public void guiClose() {
        stopSimulation();
    }

    @Override
    public boolean guiCloseRequest() {
        return true;
    }

    @Override
    public void storeToSessionNow() {}

    @Override
    public void recv_flightPhase(Integer fp) {
        m_last_flight_phase = fp;
    }

    @Override
    public void recv_position(PositionData p) {
        m_last_flight_phase = p.flightphase;
        // m_last_flight_mode = p.flightmode;
    }

    protected void invokeMessage(Runnable r) {
        try {
            if (dispatchEventsInUIthread) {
                Dispatcher.platform().runLater(r);
            } else {
                r.run();
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "problems invoking while log read", e);
        }
    }

    @Override
    public void dbgCommand0() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgCommand1() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgCommand2() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgCommand3() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgCommand4() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgCommand5() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgCommand6() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgCommand7() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgCommand8() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgCommand9() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgExitAutopilot() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgRToff() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgRTon() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void dbgResetDebug() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void expertRecalibrate() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void expertTrimOff() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void expertTrimOn() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void expertUpdateFirmware(String firmware) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void mavinci(Integer version) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestAirplaneName() {
        Debug.getLog().log(Level.CONFIG, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestPlaneInfo() {
        Debug.getLog().log(Level.CONFIG, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestConfig() {
        Debug.getLog().log(Level.CONFIG, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestFixedOrientation() {
        Debug.getLog().log(Level.CONFIG, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestFlightPhase() {
        Debug.getLog().log(Level.CONFIG, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestFlightPlanASM() {
        Debug.getLog().log(Level.CONFIG, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestFlightPlanXML() {
        Debug.getLog().log(Level.CONFIG, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestIsSimulation() {
        Debug.getLog().log(Level.CONFIG, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestBackend() {
        Debug.getLog().log(Level.CONFIG, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestSimulationSpeed() {
        Debug.getLog().log(Level.CONFIG, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestStartpos() {
        Debug.getLog().log(Level.CONFIG, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void setAirplaneName(String newName) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void setConfig(Config_variables c) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void setFixedOrientation(Float roll, Float pitch, Float yaw) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void setFlightPhase(AirplaneFlightphase p) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void setFlightPlanASM(String plan, Integer entrypoint) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void setFlightPlanXML(String plan, Integer entrypoint) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void setStartpos(Double lon, Double lat) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void shutdown() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void saveConfig() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    public void fireLoggingChangedTCP(boolean is_logging_tcp) {
        if (rootHandler != null) {
            rootHandler.loggingStateChangedTCP(is_logging_tcp);
        }
    }

    public void fireLoggingChangedFLG(boolean is_logging_flg) {
        if (rootHandler != null) {
            rootHandler.loggingStateChangedFLG(is_logging_flg);
        }
    }

    public void fireReplaySkipPhase(final boolean isSkipping) {
        doRunnableLater(
            new Runnable() {

                @Override
                public void run() {
                    if (rootHandler != null) {
                        rootHandler.replaySkipPhase(isSkipping);
                    }
                }
            });
    }

    private boolean isPaused = false;

    public void fireReplayPaused(boolean paused) {
        isPaused = paused;
        if (rootHandler != null) {
            rootHandler.replayPaused(paused);
        }
    }

    public void fireReplayStopped(boolean stopped) {
        if (rootHandler != null) {
            rootHandler.replayStopped(stopped);
        }
    }

    public void fireReplayFinished() {
        Debug.getLog().fine("fireReplayFinished:" + getPlanePort());
        if (rootHandler != null) {
            rootHandler.replayFinished();
        }
    }

    public void elapsedSimTime(double secs, double secsTotal) {
        if (rootHandler != null) {
            rootHandler.elapsedSimTime(secs, secsTotal);
        }
    }

    @Override
    public Object getPlanePort() {
        if (inputFile != null && plane instanceof IAirplane) {
            return inputFile;
        } else {
            return "Stream Log Reader";
        }
    }

    boolean isClosed = false;

    @Override
    public void close() {
        isClosed = true;
        stopSimulation();
        plane.unsetAirplaneConnector();
    }

    @Override
    public boolean isReadable() {
        return !isClosed;
    }

    @Override
    public void updateAndroidState(AndroidState state) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void cancelReceiving(String path) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void cancelSending(String path) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void deleteFile(String path) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void getFile(String path) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void makeDir(String path) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestDirListing(String path) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void sendFile(String path) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void expertUpdateBackend(String rpmFile) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void requestSimulationSettings() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void setSimulationSettings(SimulationSettings settings) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void writeToFlash(String path) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void expertRecalibrateCompassStart() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void expertRecalibrateCompassStop() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public boolean isSimulation() {
        return false;
    }

    @Override
    public void expertUpdateBackendTopconOAF(String path) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void expertUpdateFirmwareTopconOAF(String path) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void expertSendSimulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void expertRequestSimulatedFails() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void clearNVRAM() {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void setManualServos(Vector<Integer> manualServos) {
        Debug.getLog().log(Level.WARNING, "Cant sending Command to LogReader_Airplane, skipping Request");
    }

    @Override
    public void cancelLanding() {
        // Nothing ???
    }

    @Override
    public void cancelLaunch() {
        // Nothing ???
    }
}
