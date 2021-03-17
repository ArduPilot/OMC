/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerBackendConnectionLost extends IAirplaneListener {

    /** Connection Lost */
    public void err_backendConnectionLost(ConnectionLostReasons reason);

    /**
     * runtime extendable list of connection lost reasons... due to the extendability i cant use an enum
     *
     * @author caller
     */
    public class ConnectionLostReasons {

        public static final ConnectionLostReasons DISCONNECTED_BY_USER =
            new ConnectionLostReasons("disconnect by user");
        public static final ConnectionLostReasons WRONG_AP_RELEASE =
            new ConnectionLostReasons("Open Mission Control and Autopilot have different release numbers");
        public static final ConnectionLostReasons TCP_CONNECTION_LOST =
            new ConnectionLostReasons("tcp connection lost", true);
        public static final ConnectionLostReasons CONNECTOR_REQUEST_TIMEOUT =
            new ConnectionLostReasons("Connector request timeout", true);
        public static final ConnectionLostReasons LOGFILE_AT_END = new ConnectionLostReasons("logfile at end");
        public static final ConnectionLostReasons LOGFILE_REPLAY_STOPPED =
            new ConnectionLostReasons("logfile replay stopped");
        public static final ConnectionLostReasons CONNECTION_REMOVED =
            new ConnectionLostReasons("removed connection from airplane");
        public static final ConnectionLostReasons TIMEOUT = new ConnectionLostReasons("connection timeout", true);
        public static final ConnectionLostReasons DEVICE_CONNECTING_TIMEOUT =
            new ConnectionLostReasons("connecting device timeout", true);
        public static final ConnectionLostReasons PORTLIST_TIMEOUT =
            new ConnectionLostReasons("portlist waiting timeout");
        public static final ConnectionLostReasons ALLREADY_CONNECTED =
            new ConnectionLostReasons("allready connected. so closing now to cleanup");
        public static final ConnectionLostReasons DEVICE_NOTEXISTING =
            new ConnectionLostReasons("device is not existing");

        private final String reason;
        private final boolean autoReconnect;

        public boolean shouldAutoReconnect() {
            return autoReconnect;
        }

        public ConnectionLostReasons(String reason) {
            this(reason, false);
        }

        public ConnectionLostReasons(String reason, boolean autoReconnect) {
            this.reason = reason;
            this.autoReconnect = autoReconnect;
        }

        @Override
        public String toString() {
            return reason;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof ConnectionLostReasons) {
                ConnectionLostReasons other = (ConnectionLostReasons)obj;
                return other.reason.equals(this.reason);
            }

            return false;
        }

    }
}
