/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.logfile;

import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class LogReaderVLG extends ALogReader {

    public LogReaderVLG(ICAirplane plane, File inputFile) {
        super(plane, inputFile);
    }

    /** magic number */
    @Override
    protected int getEstimatorQueSize() {
        return 100;
    }

    protected double extractTimestamp(String line) throws Exception {
        int pos = line.indexOf(LogWriterVLG.TIMESEPERATOR);
        String prefix = line.substring(0, pos);
        return Double.valueOf(prefix);
    }

    protected void processLine(String line) throws IOException {
        if (line == null) {
            return;
        }

        if (line.trim().length() == 0) {
            return; // skip this line
        }

        try {
            int pos = line.indexOf(LogWriterVLG.TIMESEPERATOR);
            // timestamp allready parsed
            // String prefix = line.substring(0, pos);
            // if (prefix.endsWith(RawStringsLogWriter.SEND_INDICATOR)) return; //this was sended data!
            // m_cur_timestamp = Double.valueOf(prefix);

            String data = line.substring(pos + 1);
            // Integer fp = extractFlightphase(data);
            // if (fp != null)
            // m_last_flight_phase = fp;
            receiveString(data);
        } catch (InterruptedException e) {
            // Ignore
        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "cant compute receive string:" + line + " (" + getCurrentLine() + ")", e);
        }
    }

    /**
     * Process received strings and call the handlers
     *
     * @param msg
     * @throws Exception
     */
    long lastHealth = -1;

    long lastPos = -1;
    long lastOrientation = -1;

    boolean hasWarnedFlarm = false;

    private void receiveString(final String msg) throws Exception {
        int pos = msg.indexOf(";");
        String prefix = null;
        if (pos > 0) {
            prefix = msg.substring(0, pos);
            // System.out.println("prefix=" + prefix);

        }

        if (rootHandler != null) {

            // hack to downsample some packages due too much updates
            if (!isPaused()) {
                if (prefix != null) {
                    long now = System.currentTimeMillis();
                    // System.out.println("prefix=" + prefix);
                    double speed = Math.min(10, Math.abs(getSimulationSpeed()));
                    if (prefix.equals("#recv_health")) {
                        if ((now - lastHealth) < 1000 / speed) {
                            // System.out.println("skip");
                            return;
                        }

                        lastHealth = now;
                    } else if (prefix.equals("#recv_orientation")) {
                        if ((now - lastOrientation) < 200 / speed) {
                            // System.out.println("skip");
                            return;
                        }

                        lastOrientation = now;
                    } else if (prefix.equals("#recv_position")) {
                        if ((now - lastPos) < 250 / speed) {
                            // System.out.println("skip");
                            return;
                        }

                        lastPos = now;
                    }
                }
            }

            if (dispatchEventsInUIthread) {
                rootHandler.rawDataFromBackend(msg);
                invoker.fireEventsForMessageInUIthread(msg, rootHandler, true);
            } else {
                rootHandler.rawDataFromBackend(msg);
                invoker.processMessage(msg, null);
                invoker.fireEvents(rootHandler, false);
            }
            // ObjectParser.fireEventsForMessage(msg, rootHandler);
        }
    }

    public void rewindAndStartSimulation() {
        super.rewindAndStartSimulation();
        lastHealth = -1;
        lastPos = -1;
        lastOrientation = -1;
    }

    public boolean isNMEA() {
        return invoker.isNMEA();
    }

}
