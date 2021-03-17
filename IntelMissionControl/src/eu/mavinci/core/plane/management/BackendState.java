/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.management;

import com.intel.missioncontrol.settings.ExpertSettings;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.core.plane.tcp.TCPConnection;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

/**
 * all saved host InetSocketAddress are the addresses to connect to and not the address where the broadcast comes from
 *
 * @author peter
 */
public class BackendState {

    public static final int PING_TIME_OUT = 1000; // in msec

    public static final int TIMEOUT = 20000; // in msec

    public static final int WINDOW_ACCEPTING_NOTOLDER_PACKS = 5; // in sec

    private static final float PING_DAMPING_FACTOR = 0.2f;
    /** the ping time in milliseconds or -1 if not reachable */
    public long defaultPingTime = 0;

    private volatile long pingTimeNanos = defaultPingTime;
    private Date lastUpdate;
    private Backend b;
    private Vector<Port> ports;
    private InetSocketAddress host;

    public BackendState(Backend b, InetSocketAddress from, Vector<Port> ports) {
        setLastUpdateToNow();
        this.b = b;
        this.host = new InetSocketAddress(from.getAddress(), b.port);
        this.ports = ports;
        this.hosts.put(this.host, new Long(defaultPingTime));
    }

    private Map<InetSocketAddress, Long> hosts = new HashMap<InetSocketAddress, Long>();

    public long getPingTimeNanos() {
        return pingTimeNanos;
    }

    /** @return if backend is not reachable by ping */
    public boolean isUnreachable() {
        return pingTimeNanos < 0;
    }

    Date avaliableSince = null;

    private void setLastUpdateToNow() {
        lastUpdate = Calendar.getInstance().getTime();
        if (isUnreachable() || avaliableSince == null) {
            avaliableSince = lastUpdate;
            // System.out.println("set avaliable Since to:" + avaliableSince);
        }
    }

    public boolean allowReconnect() {
        // FIXME TODO reenable reconnection, as soon it works fine
        // return false;
        // if (avaliableSince!=null) System.out.println("allow Reconn: " + (!isUnreachable() ) + " " +
        // (Calendar.getInstance().getTime().getTime()- avaliableSince.getTime() >= ALLOW_RECONNECT_AFTER));
        if (b == null) {
            return false;
        }

        if (b.info.protocolVersion >= 3) {
            return avaliableSince != null && !isUnreachable();
        } else {
            return avaliableSince != null
                && !isUnreachable()
                && Calendar.getInstance().getTime().getTime() - avaliableSince.getTime()
                    >= DependencyInjector.getInstance()
                        .getInstanceOf(ExpertSettings.class)
                        .getAllowReconnectAfterMs();
        }
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public Backend getBackend() {
        return b;
    }

    public Vector<Port> getPorts() {
        return ports;
    }

    public InetSocketAddress getHost() {
        return host;
    }

    @Override
    public String toString() {
        return "update=" + lastUpdate + ";host=" + host.getHostString() + ";Backend=(" + b + ");ports=" + ports;
    }

    /** @return if reachable/unreachable changed */
    public boolean updatePingTime() {
        // InetSocketAddress backend = new InetSocketAddress(getHost().getAddress(), getBackend().port);

        for (Entry<InetSocketAddress, Long> e : hosts.entrySet()) {
            long newPing = ping(e.getKey(), PING_TIME_OUT);
            long ping = e.getValue();
            // Debug.getLog().info("ping: "+e.getKey()+": "+newPing);
            if (ping < 0) {
                ping = newPing;
            } else if (newPing < 0) {
                ping = -1;
            } else {
                ping = (long)((PING_DAMPING_FACTOR * newPing) + ((1 - PING_DAMPING_FACTOR) * ping));
            }
            // Debug.getLog().info("update "+e.getKey()+": "+e.getValue()+"->"+ping);
            hosts.put(e.getKey(), ping);
        }

        return updatePreferredHost();
    }

    /** @return if reachable/unreachable changed */
    public boolean updatePreferredHost() {
        InetSocketAddress best = null;
        long minPing = Long.MAX_VALUE;
        for (Entry<InetSocketAddress, Long> e : hosts.entrySet()) {
            // System.out.println("hosts entry: key= " + e.getKey() + " -> val=" + e.getValue());
            if (e.getValue() < minPing && e.getValue() >= 0) {
                minPing = e.getValue();
                best = e.getKey();
            } else if (e.getValue() == minPing) {
                // some magic to prefer local host, if two host are equivalent...
                if (e.getKey().getAddress().isLoopbackAddress()) {
                    best = e.getKey();
                } else if (e.getKey().getAddress().isAnyLocalAddress()) {
                    best = e.getKey();
                } else if (e.getKey().getAddress().getHostAddress().startsWith("192.168.")) {
                    best = e.getKey();
                }
            }
        }

        boolean wasUnReachable = isUnreachable();
        // System.out.println("wasUnreach" +wasUnReachable + " newBestHost"+best );
        if (best == null) { // is now unreachable
            pingTimeNanos = -1;
            avaliableSince = null;
            // System.out.println("set unreachable!");
            return !wasUnReachable;
        }
        // System.out.println("changed: "+wasUnReachable+" "+pingTimeNanos+"->"+minPing);
        host = best;
        pingTimeNanos = minPing;
        return wasUnReachable;
    }

    /** @return the time in ns or -1 if not reachable */
    public static long ping(InetSocketAddress addr, int timeout) {
        if (addr.isUnresolved()) {
            Debug.getLog().config("addr: " + addr + " unresolved");
            return -1;
        }

        Socket socket = new Socket();
        try {
            long startTime = System.nanoTime();
            // socket.bind(new InetSocketAddress(0));
            socket.connect(addr, timeout);
            socket.close();
            long dt = System.nanoTime() - startTime;
            // System.out.println("Connection ok (port " + addr.getPort() + ", Time = "
            // + dt + " ms). \n" + "Host Address = "
            // + addr.getAddress().getHostAddress() + "\n" + "Host Name = "
            // + addr.getAddress().getHostString());
            return dt;
        } catch (Exception e) {
            // System.out.println("Ping failed: " + e);

            try {
                socket.close();
            } catch (IOException ioe) {
                // TODO Auto-generated catch block
                ioe.printStackTrace();
            }

            return -1;
        }
    }

    /**
     * @param addr
     * @return if a change was made
     */
    public boolean setUnreachable(InetSocketAddress addr) {
        // if (CAirport.getInstance().isPingBackends()){
        // //set only this path unreachable
        // if (hosts.containsKey(addr)) {
        // hosts.put(addr, new Long(-1));
        // return updatePreferredHost();
        // }
        // return false;
        // } else {

        if (hosts.size() == 0) {
            return false;
        }

        if (addr == null) {
            // set every path unreachable
            for (InetSocketAddress addrO : hosts.keySet()) {
                hosts.put(addrO, new Long(-1));
            }
        } else {
            // set only ever path unreachable, if the address belongs to this backend
            if (!hosts.containsKey(addr)) {
                return false;
            }

            for (InetSocketAddress addrO : hosts.keySet()) {
                hosts.put(addrO, new Long(-1));
            }
        }

        return updatePreferredHost();
        // }
    }

    boolean lastAllowReconnectOnUpdate = false;

    /**
     * @param from
     * @param backend
     * @param ports
     * @return if structure (ports) changed
     */
    public boolean update(InetSocketAddress from, Backend backend, Vector<Port> ports) {
        boolean structureChange = false;
        int timeDiff = backend.time_sec - getBackend().time_sec;
        boolean acceptPack = timeDiff > 0 || -timeDiff > WINDOW_ACCEPTING_NOTOLDER_PACKS;

        // even for very old packages, update the host list
        InetSocketAddress newAddr = new InetSocketAddress(from.getAddress(), backend.port);
        if (!hosts.containsKey(newAddr)) {
            hosts.put(newAddr, new Long(defaultPingTime));
            structureChange = updatePreferredHost();
        }

        // only use newer values, or very old packes, if timer of backend has changed
        if (acceptPack) {
            setLastUpdateToNow();
            this.b = backend;
            if (!getPorts().equals(ports)) {
                this.ports = ports;
                // System.out.println("updated backendList:" + hosts);
                structureChange = true;
            }
        }

        boolean allowReconnectOnUpdate = allowReconnect();
        if (lastAllowReconnectOnUpdate != allowReconnectOnUpdate) {
            structureChange = true;
        }

        lastAllowReconnectOnUpdate = allowReconnectOnUpdate;
        // System.out.println("backendListUnchanged" + hosts);
        return structureChange;
    }

    public String getFTPurl() {
        return "ftp://"
            + TCPConnection.DEFAULT_FTP_USER
            + ":"
            + TCPConnection.DEFAULT_FTP_PW
            + "@"
            + getHost().getHostString()
            + ":"
            + getBackend().ftpPort
            + "/";
    }

    public String getHTTPurl() {
        return "http://" + getHost().getHostString() + ":" + getBackend().port + "/";
    }

    public boolean isCompatible() {
        return b.isCompatible();
    }
}
