/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.management;

import com.intel.missioncontrol.concurrent.Dispatcher;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.core.plane.listeners.IAirplaneListenerGuiClose;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPing;
import eu.mavinci.core.plane.listeners.IAirplaneListenerSendingHost;
import eu.mavinci.core.plane.listeners.IBackendBroadcastListener;
import eu.mavinci.core.plane.protocol.IInvokeable;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.desktop.main.debug.Debug;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CAirport {

    protected static CAirport me;

    public static CAirport getInstance() {
        return me;
    }

    public static void setInstance(CAirport newMe) {
        me = newMe;
    }

    public CAirport() {}

    public final LinkedList<ICAirplane> planes = new LinkedList<ICAirplane>();
    public final LinkedList<planeDeregistrat> planesDereg = new LinkedList<planeDeregistrat>();

    private final WeakListenerList<INewConnectionCallback> newConnectionListeners =
        new WeakListenerList<INewConnectionCallback>("newConnectionListeners");

    public boolean newPlaneConnectionAchieved(ICAirplane plane) throws Exception {
        try {
            planes.add(plane);
            planeDeregistrat dereg = new planeDeregistrat(plane);
            planesDereg.add(dereg);
            plane.addListener(dereg);

            for (INewConnectionCallback listener : newConnectionListeners) {
                if (listener == null) {
                    continue;
                }

                listener.newTcpConnectionArchieved(plane);
            }

            return true;
        } catch (Exception t) {
            Debug.getLog().log(Level.SEVERE, "problem publising new Airplane -> closing it", t);
            try {
                plane.close();
            } catch (Throwable t2) {
            }

            try {
                plane.fireGuiClose();
            } catch (Throwable t2) {
            }

            throw t;
        }
    }

    public void addNewConnectionListener(INewConnectionCallback listn) {
        newConnectionListeners.add(listn);
    }

    // recognizing deconnections
    public class planeDeregistrat implements IAirplaneListenerGuiClose {

        private WeakReference<ICAirplane> plane;

        public planeDeregistrat(ICAirplane plane) {
            this.plane = new WeakReference<ICAirplane>(plane);
        }

        public void guiClose() {
            removeMe();
        }

        private void removeMe() {
            if (plane == null) {
                return;
            }

            ICAirplane planeRef = plane.get();
            if (planeRef != null) {
                planes.remove(planeRef);
            }

            planesDereg.remove(this);
            plane = null;
        }

        public boolean guiCloseRequest() {
            return true;
        }

        public void storeToSessionNow() {}
    }

    private final WeakListenerList<IBackendBroadcastListener> backendBroadcastListener =
        new WeakListenerList<IBackendBroadcastListener>("backendBroadcastListener");

    public void addBackendBroadcastListener(final IBackendBroadcastListener listener) {
        // preventing concurrent modification exception
        // ThreadingHelper.getThreadingHelper().invokeLaterOnUiThread(new Runnable() {
        // public void run() {
        backendBroadcastListener.add(listener);
        listener.backendListChanged();
        // }
        // });

    }

    public void removeBackendBroadcastListener(final IBackendBroadcastListener listener) {
        // preventing concurrent modification exception
        // ThreadingHelper.getThreadingHelper().invokeLaterOnUiThread(new Runnable() {
        // public void run() {
        backendBroadcastListener.remove(listener);
        // }
        // });

    }

    /** maps a serial number to a list of broadcasts corresponding to this serial number */
    private final Map<String, BackendState> backends = new TreeMap<String, BackendState>();
    // private final Map<String,BackendState> backends = new LinkedHashMap<String,BackendState>();

    protected InetSocketAddress nextHost = null;

    public Map<String, BackendState> getBackends() {
        return backends;
    }

    /**
     * select the connection with the fastest ping time for every backend
     *
     * @return
     */
    public Vector<BackendState> getBackendList() {
        synchronized (backends) {
            // System.out.println("backendlist: "+new Vector<BackendState>(backends.values()));
            return new Vector<BackendState>(backends.values());
        }
    }

    public void backendListChanged() {
        Dispatcher.postToUI(
            new Runnable() {
                public void run() {
                    for (IBackendBroadcastListener listener : backendBroadcastListener) {
                        if (listener == null) {
                            continue;
                        }

                        listener.backendListChanged();
                    }
                }
            });
    }

    /**
     * remove the described Backend from available backends list, because of a connection failure sets the backend
     * somewhat faster unreachable than the ping
     *
     * @param host
     * @param port
     */
    public void backendNotAvailable(final String host, final int port) {
        if (host == null || port < 0) {
            return;
        }

        // System.out.println("setting unreachable");
        boolean changed = false;
        InetSocketAddress addr = new InetSocketAddress(host, port);
        synchronized (backends) {
            for (BackendState b : backends.values()) {
                changed |= b.setUnreachable(addr);
            }
        }

        if (changed) {
            Dispatcher.postToUI(
                new Runnable() {
                    public void run() {
                        backendListChanged();
                    }
                });
        }
    }

    public BackendState findBackendState(String serialNumber) {
        synchronized (backends) {
            return backends.get(serialNumber);
        }
    }

    /**
     * since broadcasts could execute every funtion in an object, encapsulate them!!
     *
     * @author marco
     */
    public class CAirportBroadcastListener implements IAirplaneListenerSendingHost, IInvokeable, IAirplaneListenerPing {

        public void recv_backend(Backend host, MVector<Port> ports) {}

        public void ping(String senderID) {}

        public void setSendHostOfNextReceive(InetSocketAddress host) {}

    }

    CAirportBroadcastListener broadcastListener = new CAirportBroadcastListener();

    public interface BroadcastHandler {
        void onConnectionEstablished();

        void onConnectionClosed();

        void onMessageConsumed();
    }

    public interface TimeoutHandler {
        void timeoutSleep();

        void onTimeoutCheckFinished();

        void onTimeoutCheckStopped();

        boolean isTimeout(BackendState backendState);
    }
}
