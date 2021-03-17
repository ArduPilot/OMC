/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.tcp;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.core.plane.management.BackendState;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.core.plane.management.BackendState;
import eu.mavinci.core.plane.sendableobjects.Port;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.Level;

public class TCPConnection {
    private String host = "";
    private int portTcp = DEFAULT_TCP_PORT;
    private String portBackend = "";

    public static final int DEFAULT_TCP_PORT = 7000;
    public static final int DEFAULT_FTP_PORT = 7001;
    public static final String DEFAULT_FTP_USER = "user";
    public static final String DEFAULT_FTP_PW = "pass";

    public static final int FTP_TO_AP_STARTING_PROTOCOL_VERSION = 3;

    public static final int AUTOCONNECT_TCP_PORT = 6999;
    public static final String LOCALHOST = "localhost";
    public static final String AUTOCONNECT_HOST = LOCALHOST; // "192.168.2.101"; //FIXME this should be LOCALHOST
    public static final String BACKEND_PORT_SIMULATION_NEW = "simulation";
    public static final String BACKEND_PORT_DEFAULT = "/dev/ttyUSB0";

    // public static final TCPConnection AUTO_CONNECTION = new TCPConnection(AUTOCONNECT_HOST, AUTOCONNECT_TCP_PORT,
    // BACKEND_PORT_SIMULATION_NEW);

    public TCPConnection(BackendState backend, String device) {
        this(backend.getHost().getHostString(), backend.getBackend().port, device);
    }

    public TCPConnection(BackendState backend, Port p) {
        this(backend, p.device);
    }

    public TCPConnection(String host, int portTcp, String portBackend) {
        this.host = host;
        this.portTcp = portTcp;
        this.portBackend = portBackend;
    }

    public TCPConnection() {
        this(LOCALHOST, DEFAULT_TCP_PORT, BACKEND_PORT_SIMULATION_NEW);
    }

    public TCPConnection(int portTCP) {
        this(LOCALHOST, portTCP, BACKEND_PORT_SIMULATION_NEW);
    }

    public TCPConnection(String ref) {
        StringTokenizer tok = new StringTokenizer(ref, ":", false);
        try {
            this.host = tok.nextToken();
            this.portTcp = Integer.parseInt(tok.nextToken());
            this.portBackend = tok.nextToken();
            if (tok.hasMoreTokens()) {
                this.portBackend += ":" + tok.nextToken();
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "Cant Parste Host String: " + ref, e);
            this.host = LOCALHOST;
            this.portTcp = DEFAULT_TCP_PORT;
            this.portBackend = BACKEND_PORT_SIMULATION_NEW; // better than nothing here...
        }
    }

    @Override
    public String toString() {
        return host + ":" + portTcp + ":" + portBackend;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPortTcp() {
        return portTcp;
    }

    public void setPortTcp(int portTcp) {
        this.portTcp = portTcp;
    }

    public String getPortBackend() {
        return portBackend;
    }

    public void setPortTcp(String portBackend) {
        this.portBackend = portBackend;
    }

    public boolean isSimulation() {
        return portBackend != null && portBackend.startsWith(BACKEND_PORT_SIMULATION_NEW);
    }

    public static String getOwnIP() {
        Enumeration<NetworkInterface> e1;
        try {
            e1 = (Enumeration<NetworkInterface>)NetworkInterface.getNetworkInterfaces();
            while (e1.hasMoreElements()) {
                NetworkInterface ni = e1.nextElement();

                Enumeration<InetAddress> e2 = ni.getInetAddresses();
                while (e2.hasMoreElements()) {
                    InetAddress ownIP = e2.nextElement();

                    if (ownIP.getAddress().length == 4 && !ownIP.isLoopbackAddress()) {
                        return ownIP.getHostAddress();
                    }
                }
            }
        } catch (SocketException e3) {

        }

        return LOCALHOST;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TCPConnection) {
            TCPConnection con = (TCPConnection)obj;
            if (con.portTcp != portTcp) {
                return false;
            }

            if (con.host == null && host != null) {
                return false;
            }

            if (con.portBackend == null && portBackend != null) {
                return false;
            }

            Ensure.notNull(con.portBackend, "con.portBackend");
            Ensure.notNull(con.host, "con.host");

            return con.host.equals(host) && con.portBackend.equals(portBackend);
        } else {
            return false;
        }
    }
}
