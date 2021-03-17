/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.logfile;

import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneMsgType;
import eu.mavinci.core.plane.BackToRawConverter;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackendConnectionLost;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerRawBackendStrings;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import java.io.IOException;
import java.util.logging.Level;

public class LogWriterVLG extends ALogWriter
        implements IAirplaneListenerRawBackendStrings,
            IAirplaneListenerConnectionState,
            IAirplaneListenerBackendConnectionLost {

    BackToRawConverter toRawConv;

    public LogWriterVLG(IAirplane plane) {
        super(plane);
        suffix = "vlg";
        toRawConv = new BackToRawConverter(this);
        autoChangeEnability();
    }

    @Override
    public void rawDataFromBackend(String line) {
        if (m_do_log) {
            try {
                write(System.currentTimeMillis() + TIMESEPERATOR + line.trim());
            } catch (IOException e) {
                Debug.getLog().log(Level.WARNING, "Error writing Backend String to Logfile", e);
            }
        }
    }

    public static final String TIMESEPERATOR = ":";
    public static final String SEND_INDICATOR = "#>"; // just comment them out

    @Override
    public void rawDataToBackend(String line) {
        if (m_do_log) {
            try {
                write(SEND_INDICATOR + System.currentTimeMillis() + TIMESEPERATOR + line.trim());
            } catch (IOException e) {
                Debug.getLog().log(Level.WARNING, "Error writing Send String to Logfile", e);
            }
        }
    }

    @Override
    protected void requestAll() {
        m_plane.getAirplaneCache().invokeWithCacheValues(toRawConv);
    }

    @Override
    public void connectionStateChange(AirplaneConnectorState newState) {
        if (toRawConv == null) {
            return;
        }

        toRawConv.recv_msg(AirplaneMsgType.INFORMATION.ordinal(), "InsideLog: Connection state changed: " + newState);
    }

    @Override
    public void err_backendConnectionLost(ConnectionLostReasons reason) {
        if (toRawConv == null) {
            return;
        }

        toRawConv.recv_msg(AirplaneMsgType.POPUP_MUST_CONFIRM.ordinal(), "InsideLog: Connection lost: " + reason);
    }

    @Override
    public void close() throws IOException {
        Debug.getLog().log(Level.WARNING, "Closing Log file");
    }

}
