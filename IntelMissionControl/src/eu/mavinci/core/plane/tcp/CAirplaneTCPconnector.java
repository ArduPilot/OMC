/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.tcp;

import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.utils.IVersionProvider;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackend;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackendConnectionLost;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionEstablished;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPlaneInfo;
import eu.mavinci.core.plane.management.CAirport;
import eu.mavinci.core.plane.protocol.IInvokeable;
import eu.mavinci.core.plane.protocol.ObjectPacking;
import eu.mavinci.core.plane.protocol.ProtocolInvoker;
import eu.mavinci.core.plane.protocol.ProtocolTokens;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Level;

public class CAirplaneTCPconnector extends AAirplaneConnector
        implements IAirplaneListenerBackend, IAirplaneListenerPlaneInfo {

    // public static final int SOCKET_TIMEOUT = 6000;
    public static final int SOCKET_CONNECT_TIMEOUT = 6000;
    public static final int INPUT_READY_TIMEOUT = 6000;
    public static final int DEVICE_CONNECT_TIMEOUT = 15000;
    public static final int RECEIVE_PORTLIST_CONNECT_TIMEOUT = 15000;
    public static final int REQUEST_PORTLIST_INTERVALL =
        4500; // this value has to be much less than the 10sec. read timeout in backend but
    // not to small, to not trigger concurrent bug in old backends!!!
    public static final int REQUEST_PORTLIST_RESPONSE_TIMEOUT =
        18000; // effectly this is checked only in the REQUEST_PORTLIST_INTERVALL
    // interval, so we have to round this up to the next integer
    // multiplier of REQUEST_PORTLIST_INTERVALL
    public static final int REQUEST_PORTLIST_MAX_DENGLING = 3;
    public static final Optional<PortListWaiter> DEFAULT_PORT_WAITER = Optional.empty();
    private final ConnectionHandler handler;
    private final PortListWaiter portListWaiter;

    private String connectedPlanePort;

    private BufferedReader input;
    public static final int INPUT_BUFFER_SIZE = 128 * 1024;

    protected PrintStream output;

    private Socket server;

    public static final Level logLevel = Level.SEVERE;

    public static final String KEY = "eu.mavinci.core.plane.tcp.CAirplaneTCPconnector";

    private ProtocolInvoker invoker = new ProtocolInvoker();
    private DirectListener directListener = new DirectListener();

    /** Signalling to other users of connection, that the lost was intentionally by user */
    protected boolean disconnecting = false;

    /**
     * User caused closing of Connection
     *
     * @see eu.mavinci.core.plane.IAirplaneExternal#close()
     */
    public synchronized void close() {
        Debug.getLog().config("closing connection" + getPlanePort());
        // (new Exception()).printStackTrace();
        // System.out.println("close called");
        if (server != null) {
            disconnecting = true;
            // (new Exception()).printStackTrace();
            fireBackendConnectionLost(
                IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.DISCONNECTED_BY_USER);
        } else {
            // force it!
            closeInternal();
        }
    }

    /**
     * Close connection for a given reason
     *
     * @param reason
     */
    private synchronized void fireBackendConnectionLost(
            final IAirplaneListenerBackendConnectionLost.ConnectionLostReasons reason) {
        if (getConnectionState() != AirplaneConnectorState.unconnected) {
            // (new Exception()).printStackTrace();
            // System.out.println("fireConLost" + reason);
            if (reason != IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.DISCONNECTED_BY_USER) {
                CAirport.getInstance().backendNotAvailable(host, hostPort);
            }
            // rootHandler.err_backendConnectionLost(reason);
            fireConnectionState(AirplaneConnectorState.unconnected);
            try {
                // invokeAndWaitOnUiThread -> will cause deadlocks!!!
                Dispatcher.postToUI(
                    new Runnable() {
                        public void run() {
                            rootHandler.err_backendConnectionLost(reason);
                        }
                    });
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "Problems propagate connection lost", e);
            }
        }

        closeInternal();
    }

    /** Assure connection is closed */
    protected synchronized void closeInternal() {
        fireConnectionState(AirplaneConnectorState.unconnected);

        // System.out.println("close internally");
        // rootHandler.removeListener(this);
        Thread t = portListRepollThread;
        if (t != null) {
            portListRepollThreadRunning = false;
            t.interrupt();
            portListRepollThread = null;
        }

        // if backend closes connection directly after selection of device, this device doen't exist
        t = deviceConnectTimeoutThread;
        if (t != null) {
            deviceConnectTimeoutThreadRunning = false;
            t.interrupt();
            deviceConnectTimeoutThread = null;
        }

        t = waitingForPortlistThread;
        if (t != null) {
            waitingForPortlistThreadRunning = false;
            t.interrupt();
            waitingForPortlistThread = null;
        }

        if (server != null) {
            try {
                server.shutdownInput();
            } catch (Throwable e) {
                // e.printStackTrace();
            }

            try {
                server.shutdownOutput();
            } catch (Throwable e) {
                // e.printStackTrace();
            }

            try {
                server.close();
            } catch (Throwable e) {
                // e.printStackTrace();
            }
        }

        Debug.getLog().fine("Ready shutdown TCP");
        server = null;
        output = null;
        input = null;

        // System.gc(); //try to kick socket by garbage collection finally (espaciall for android)

    }

    public CAirplaneTCPconnector(ICAirplane plane, ConnectionHandler handler, Optional<PortListWaiter> portListWaiter) {
        this(plane.getRootHandler(), handler, portListWaiter);
        plane.setAirplaneConnector(this);
    }

    public CAirplaneTCPconnector(
            IAirplaneListenerDelegator rootHandler,
            ConnectionHandler handler,
            Optional<PortListWaiter> portListWaiter) {
        setRootHandler(rootHandler);
        this.handler = handler;
        this.portListWaiter = portListWaiter.orElseGet(PortListWaiterImpl::new);
    }

    public TCPConnection getPlanePort() {
        // if (connectedPlanePort == null)
        // return new TCPConnection(host, hostPort, shouldConnectToPort);
        // else
        return new TCPConnection(host, hostPort, connectedPlanePort);
    }

    public boolean isWriteable() {
        // (new Exception("isConnected-Called")).printStackTrace();
        // System.out.println(connectedPlanePort);
        // System.out.println(shouldConnectToPort);
        // System.out.println("con:" + + this.hashCode() + " - server:"+(server == null ? "null":server.toString() + "
        // isCon"+server.isConnected()) + "conPort"+(connectedPlanePort == null?"null":connectedPlanePort));
        return getConnectionState() != AirplaneConnectorState.unconnected
            && server != null
            && server.isConnected()
            && connectedPlanePort != null
            && connectedPlanePort.length() != 0;
    }

    public boolean isReadable() {
        return isWriteable();
    }

    private String host;
    private int hostPort;

    public String getHost() {
        return host;
    }

    public int getHostPort() {
        return hostPort;
    }

    /** to this Port will automatically connected, if the portlist was received.. */
    private volatile String shouldConnectToPort = null;

    /**
     * Process received strings and call the handlers
     *
     * @param msg
     * @throws Exception
     */
    private void receiveString(final String msg) throws Exception {
        Dispatcher.postToUI(
            new Runnable() {
                public void run() {
                    rootHandler.rawDataFromBackend(msg);
                }
            });
        invoker.processMessage(msg, null);
        Debug.getLog().info("Is data ok? - " + invoker.isDataOk());
        try {
            invoker.fireEventsDirectly(directListener);
        } catch (NoSuchMethodException t) {
            // ignore!
        } catch (Throwable t) {
            // System.out.println(msg);
            Debug.getLog().log(Level.WARNING, "problems crocessing received data", t);
        }

        invoker.fireEventsInUIthread(rootHandler, false);

        // invoker.fireEventsForMessageOnUIthread(msg, rootHandler);
        // ObjectParser.fireEventsForMessage(msg, rootHandler);
    }

    public synchronized void socketConnect(TCPConnection con) throws Exception {
        shouldConnectToPort = con.getPortBackend();
        // System.out.println("socketConnect(TCPConnection con)" + con);
        socketConnect(con.getHost(), con.getPortTcp());
    }

    public synchronized void socketConnect(String host, int port) throws Exception {
        Debug.getLog().fine("connecting host:" + host + " port=" + port);
        disconnecting = false;
        timeStampLastRecvBackend = -1;
        timeLastRequestBackend = -1;
        denglingBackendReqests = 0;

        if (server != null && !server.isClosed()) {
            shouldConnectToPort = null; // do not connect port if already connected
            if (!isWriteable()) {
                fireBackendConnectionLost(
                    IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.ALLREADY_CONNECTED);
                throw new Exception("inconsistent tcp connection - connection closed");
            }

            throw new Exception("Already Connected");
        }
        // rootHandler.addListenerAtBegin(this);

        // System.out.println("try to connect to server " + host + " on Port " + port);

        fireConnectionState(AirplaneConnectorState.connectingTCP);

        try {
            server = new Socket();
            SocketAddress adr = new InetSocketAddress(host, port);
            // server.setSoTimeout(SOCKET_TIMEOUT);
            server.connect(adr, SOCKET_CONNECT_TIMEOUT);

        } catch (Exception e) {
            // CAirport.getInstance().backendNotAvailable(host, port);
            fireBackendConnectionLost(
                new IAirplaneListenerBackendConnectionLost.ConnectionLostReasons(e.getLocalizedMessage()));
            throw e;
        }

        input =
            new BufferedReader(
                new InputStreamReader(server.getInputStream(), ProtocolTokens.encoding), INPUT_BUFFER_SIZE);
        output = new PrintStream(server.getOutputStream(), true, ProtocolTokens.encoding);

        mavinci(ProtocolTokens.PROTOCOLL_VERSION); // tell the Backend the
        // protocoll version
        this.host = host;
        this.hostPort = port;

        fireConnectionState(AirplaneConnectorState.connectedTCP);
        handler.onConnectedTcp();

        waitingForPortlistThread =
            new Thread("Waiting For Portlist Thread") {
                public void run() {
                    portListWaiter.waitForUavPortsList();
                    waitingForPortlistThread = null;
                }
            };
        waitingForPortlistThread.setPriority(Thread.MIN_PRIORITY);
        waitingForPortlistThread.start();

        Thread portListenThread = new Thread(portListenRunnable, "TCP-Backend Connector");
        portListenThread.setPriority(Thread.MAX_PRIORITY);
        portListenThread.start();
    }

    public interface PortListWaiter {
        void waitForUavPortsList();
    }

    public class PortListWaiterImpl implements CAirplaneTCPconnector.PortListWaiter {

        @Override
        public void waitForUavPortsList() {
            Debug.getLog().info("Waiting for ports");
            waitingForPortlistThreadRunning = true;
            try {
                Thread.sleep(CAirplaneTCPconnector.RECEIVE_PORTLIST_CONNECT_TIMEOUT);
            } catch (InterruptedException e) {
                if (!waitingForPortlistThreadRunning) {
                    return;
                }

                if (getConnectionState().compareTo(AirplaneConnectorState.portlistReceived) < 0) {
                    // e.printStackTrace();
                    // if (e.getCause() != null)
                    // e.getCause().printStackTrace();
                    Debug.getLog()
                        .info("RECEIVE_PORTLIST_CONNECT_TIMEOUT: interruptet too eraly?" + getConnectionState());
                }
            }

            if (!waitingForPortlistThreadRunning) {
                return;
            }

            if (getConnectionState().compareTo(AirplaneConnectorState.portlistReceived) < 0) {
                Debug.getLog().info("Backend don't send portlist in time....-> timeout disconnection TCP");
                fireBackendConnectionLost(
                    IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.PORTLIST_TIMEOUT);
            }
        }
    }

    volatile Thread waitingForPortlistThread = null;
    volatile boolean waitingForPortlistThreadRunning = false;

    private Runnable portListenRunnable =
        new Runnable() {
            public void run() {
                UavConnectionPortListener listener =
                    new UavConnectionPortListenerImpl(
                        new RequestProcessorImpl(), new ConnectionLostHandlerImpl(), input);
                listener.listen();
            }
        };

    public interface RequestProcessor {
        void process(String request) throws Exception;
    }

    private class RequestProcessorImpl implements RequestProcessor {
        @Override
        public void process(String request) throws Exception {
            receiveString(request);
        }
    }

    public interface ConnectionLostHandler {
        void onConnectionLost(IAirplaneListenerBackendConnectionLost.ConnectionLostReasons reasons, Throwable e);
    }

    private class ConnectionLostHandlerImpl implements ConnectionLostHandler {
        @Override
        public void onConnectionLost(
                IAirplaneListenerBackendConnectionLost.ConnectionLostReasons reasons, Throwable e) {
            if (reasons == IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.TCP_CONNECTION_LOST) {
                Debug.getLog()
                    .log(
                        Level.INFO,
                        "TCP connection lost reason. Was disconnecting=" + CAirplaneTCPconnector.this.disconnecting,
                        e);
            }

            if (!CAirplaneTCPconnector.this.disconnecting) {
                Dispatcher.postToUI(
                    new Runnable() {
                        public void run() {
                            // CAirport.getInstance().backendNotAvailable(
                            // host, hostPort);
                            CAirplaneTCPconnector.this.fireBackendConnectionLost(reasons);
                        }

                    });
            }
        }
    }

    volatile Thread deviceConnectTimeoutThread = null;
    volatile boolean deviceConnectTimeoutThreadRunning = false;

    volatile Thread portListRepollThread;
    volatile boolean portListRepollThreadRunning = false;

    public synchronized void connect(String port) {
        // System.out.println("connecting " + port);
        Debug.getLog().info("connecting internalPort: " + port);
        shouldConnectToPort = null;
        fireConnectionState(AirplaneConnectorState.connectingDevice);
        handler.onConnectingDevice();
        try {
            sendStringWithoutBackendPort(ObjectPacking.encodeFkt("connect", port, "port"));
            if (output == null) {
                fireConnectionState(AirplaneConnectorState.unconnected);
                return;
            }

            Debug.getLog().info("Waiting for device connection");
            deviceConnectTimeoutThread =
                new Thread("Device Connect Timeout Thread") {
                    public void run() {
                        deviceConnectTimeoutThreadRunning = true;
                        try {
                            sleep(DEVICE_CONNECT_TIMEOUT);
                        } catch (InterruptedException e) {
                            if (!deviceConnectTimeoutThreadRunning) {
                                return;
                            }

                            if (getConnectionState().compareTo(AirplaneConnectorState.fullyConnected) < 0) {
                                Debug.getLog()
                                    .info(
                                        "DEVICE_CONNECT_TIMEOUT: interrupted too early? Current state: "
                                            + getConnectionState());
                            }
                        }

                        if (!deviceConnectTimeoutThreadRunning) {
                            return;
                        }

                        if (getConnectionState().compareTo(AirplaneConnectorState.fullyConnected) < 0) {
                            Debug.getLog().warning("Backend not responding onto connection to a device");
                            fireBackendConnectionLost(
                                IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.DEVICE_CONNECTING_TIMEOUT);
                        }

                        deviceConnectTimeoutThread = null;
                    };
                };
            deviceConnectTimeoutThread.setPriority(Thread.MIN_PRIORITY);
            deviceConnectTimeoutThread.start();
        } catch (Exception e) {
            fireConnectionState(AirplaneConnectorState.unconnected);
            Debug.getLog().log(Level.INFO, "error connecting to Port", e);
        }
    }

    public class DirectListener
            implements IAirplaneListenerConnectionEstablished,
                IAirplaneListenerBackend,
                IAirplaneListenerPlaneInfo,
                IInvokeable {

        public void recv_connectionEstablished(String port) {
            Debug.getLog().info("Receive connection established");
            connectedPlanePort = port;
            fireConnectionState(AirplaneConnectorState.fullyConnected);
            handler.onFullyConnected();
            Thread t = deviceConnectTimeoutThread;
            if (t != null) {
                t.interrupt();
                deviceConnectTimeoutThread = null;
            }

            // ThreadingHelper.getThreadingHelper().invokeLaterOnUiThread(new Runnable() {
            // public void run() {
            // //this listener is not anymore needed! it is triggered here,
            // //because old instances of this object otherwise will trigger such events
            // //before the garbage collector removes them from the memory
            // rootHandler.removeListener(CAirplaneTCPconnector.this);
            // }
            // });
        }

        public void recv_backend(Backend host, MVector<Port> ports) {
            // System.out.println("recv_ports:"+System.currentTimeMillis());

            Debug.getLog().info("Receiving backend");
            synchronized (this) {
                timeStampLastRecvBackend = System.currentTimeMillis();
                denglingBackendReqests = 0;
            }

            protocolVersion = host.info.protocolVersion;
            ftpPortBackend = host.ftpPort;
            if (getConnectionState().compareTo(AirplaneConnectorState.portlistReceived) < 0
                    && getConnectionState() != AirplaneConnectorState.unconnected) {
                fireConnectionState(AirplaneConnectorState.portlistReceived);
                handler.onPortListReceived();
            }

            Debug.getLog().info("Ports received");
            // if (!host.info.releaseVersion.equals(GlobalSettings.releaseVersion)){
            // fireBackendConnectionLost(ConnectionLostReasons.WRONG_AP_RELEASE);
            // throw new RuntimeException("Backend has other Release Version than Open Mission Control. Connection
            // closed!");
            // }

            Thread t = waitingForPortlistThread;
            if (t != null) {
                t.interrupt(); // stopping the portlist receive wait timer
                waitingForPortlistThread = null;
            }

            Debug.getLog().info("Port to connect to - " + shouldConnectToPort);
            if (shouldConnectToPort != null) {
                for (Port p : ports) {
                    if (!p.isCompatible()
                            && DependencyInjector.getInstance()
                                    .getInstanceOf(ISettingsManager.class)
                                    .getSection(GeneralSettings.class)
                                    .getOperationLevel()
                                != OperationLevel.DEBUG) {
                        Debug.getLog().info("Port incompatibility");
                        // closeInternal();
                        fireBackendConnectionLost(
                            IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.WRONG_AP_RELEASE);
                        shouldConnectToPort = null;
                        throw new RuntimeException(
                            "Backend has other Release Version than Open Mission Control. Connection closed!");
                    }
                }

                connect(shouldConnectToPort);
            }

            Debug.getLog().info("Start port List re-poll");
            if (portListRepollThread == null) {
                portListRepollThread =
                    new Thread(
                        new Runnable() {

                            public void run() {
                                portListRepollThreadRunning = true;
                                while (true) {
                                    try {
                                        Thread.sleep(REQUEST_PORTLIST_INTERVALL);
                                    } catch (InterruptedException e) {
                                        // System.out.println("interrupted!");
                                        continue;
                                    }

                                    if (!portListRepollThreadRunning) {
                                        return;
                                    }
                                    // System.out.println("requested");
                                    if (getConnectionState() == AirplaneConnectorState.fullyConnected) {
                                        boolean shouldDisconnect;
                                        synchronized (this) {
                                            shouldDisconnect =
                                                timeStampLastRecvBackend > 0
                                                    && (timeLastRequestBackend - timeStampLastRecvBackend)
                                                        > REQUEST_PORTLIST_RESPONSE_TIMEOUT
                                                    && denglingBackendReqests > REQUEST_PORTLIST_MAX_DENGLING;
                                        }

                                        Debug.getLog().info("Should we disconnect ? " + shouldDisconnect);
                                        if (shouldDisconnect) {
                                            fireBackendConnectionLost(
                                                IAirplaneListenerBackendConnectionLost.ConnectionLostReasons
                                                    .CONNECTOR_REQUEST_TIMEOUT);
                                            // this thead will be stopped asynchronously!
                                        } else {
                                            requestBackend();
                                        }
                                    }
                                }
                            }

                        },
                        "Portlist Polling Thread");
                portListRepollThread.setPriority(
                    Thread.NORM_PRIORITY + 1); // Min prio will cause under load that we are not requesting
                // often enough
                portListRepollThread.start();
                // } else {
                // portListRepollThread.interrupt();
            }
        }

        public void recv_planeInfo(PlaneInfo info) {
            ftpHostUAV = info.ftpHost;
            ftpPortUAV = info.ftpPort;
            planeFTPinfoReceived();
        }

    }

    protected void planeFTPinfoReceived() {}

    protected int protocolVersion = -1;

    protected String ftpHostUAV = null;
    protected int ftpPortUAV = -1;

    protected int ftpPortBackend = -1;

    long timeStampLastRecvBackend = -1;

    public void recv_backend(Backend host, MVector<Port> ports) {
        // System.out.println("new broadcast backend");
        if (getConnectionState() == AirplaneConnectorState.unconnected) {
            directListener.recv_backend(host, ports);
        }
    }

    /**
     * send data to Backend as long a OutputStream is open
     *
     * @param line
     */
    protected void sendStringWithoutBackendPort(final String line) {
        Debug.getLog().info("Sending " + line + " to uav");
        Debug.getLog().info("output is null -> " + (output == null));
        if (output != null) {
            // System.out.println("sending:" + line); // FIXME TODO remove me

            output.print(line + ProtocolTokens.mend);
            rootHandler.rawDataToBackend(line);
        }
    }

    public void sendString(String line) {
        if (!isWriteable()) {
            Debug.getLog()
                .log(
                    Debug.WARNING,
                    "cant send stuff to airplane until connected to a backend Port: " + line,
                    new Exception("send data to Backend even it wasnt writeable"));
        } else {
            sendStringWithoutBackendPort(line);
        }
    }

    public void dbgExitAutopilot() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgExitAutopilot"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgExitAutopilot\" command", e);
        }
    }

    public void dbgResetDebug() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgResetDebug"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgResetDebug\" command", e);
        }
    }

    public void dbgRToff() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgRToff"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgRToff\" command", e);
        }
    }

    public void dbgRTon() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgRTon"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgRTon\" command", e);
        }
    }

    public void expertRecalibrate() {
        try {
            sendString(ObjectPacking.encodeFkt("expertRecalibrate"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"expertRecalibrate\" command", e);
        }
    }

    public void expertTrimOff() {
        try {
            sendString(ObjectPacking.encodeFkt("expertTrimOff"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"expertTrimOff\" command", e);
        }
    }

    public void expertTrimOn() {
        try {
            sendString(ObjectPacking.encodeFkt("expertTrimOn"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"expertTrimOn\" command", e);
        }
    }

    public void expertUpdateFirmware(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("expertUpdateFirmware", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"expertUpdateFirmware\" command", e);
        }
    }

    public void mavinci(Integer version) {
        try {
            sendStringWithoutBackendPort(ObjectPacking.encodeFkt("mavinci", version, "version"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"mavinci\" command", e);
        }
    }

    public void requestPlaneInfo() {
        try {
            sendString(ObjectPacking.encodeFkt("requestPlaneInfo"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestPlaneInfo\" command", e);
        }
    }

    public void requestAirplaneName() {
        try {
            sendString(ObjectPacking.encodeFkt("requestAirplaneName"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestAirplaneName\" command", e);
        }
    }

    public void requestConfig() {
        try {
            sendString(ObjectPacking.encodeFkt("requestConfig"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestConfig\" command", e);
        }
    }

    public void requestFlightPhase() {
        try {
            sendString(ObjectPacking.encodeFkt("requestFlightPhase"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestFlightPhase\" command", e);
        }
    }

    public void requestIsSimulation() {
        try {
            sendString(ObjectPacking.encodeFkt("requestIsSimulation"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestIsSimulation\" command", e);
        }
    }

    long timeLastRequestBackend = -1;
    int denglingBackendReqests = 0;

    public void requestBackend() {
        Debug.getLog().info("Requesting backend");
        try {
            sendString(ObjectPacking.encodeFkt("requestBackend"));
            synchronized (this) {
                timeLastRequestBackend = System.currentTimeMillis();
                denglingBackendReqests++;
            }
            // System.out.println("\nrequestBackend:"+timeLastRequestBackend);
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestBackend\" command", e);
        }
    }

    public void requestStartpos() {
        try {
            sendString(ObjectPacking.encodeFkt("requestStartpos"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestStartpos\" command", e);
        }
    }

    public void setAirplaneName(String newName) {
        try {
            if (newName.length() > PlaneConstants.PLANE_NAME_MAX_SIZE
                    || !newName.matches(PlaneConstants.PLANE_NAME_REGEX)) {
                return;
            }

            sendString(ObjectPacking.encodeFkt("setAirplaneName", newName, "newName"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"setAirplaneName\" command", e);
        }
    }

    public void setConfig(Config_variables c) {
        try {
            sendString(ObjectPacking.encodeFkt("setConfig", c, "c"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"setConfig\" command", e);
        }
    }

    public void setFlightPhase(AirplaneFlightphase p) {
        try {
            sendString(ObjectPacking.encodeFkt("setFlightPhase", p.ordinal(), "p"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"setFlightPhase\" command", e);
        }
    }

    public void setFlightPlanASM(String plan, Integer entrypoint) {
        Vector<Object> args = new Vector<Object>(2);
        Vector<String> names = new Vector<String>(2);

        args.add(plan);
        names.add("plan");

        args.add(entrypoint);
        names.add("entrypoint");

        try {
            sendString(ObjectPacking.encodeFkt("setFlightPlanASM", args, names));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"setFlightPlanASM\" command", e);
        }
    }

    public void setFlightPlanXML(String plan, Integer entrypoint) {
        Vector<Object> args = new Vector<Object>(2);
        Vector<String> names = new Vector<String>(2);

        args.add(plan);
        names.add("plan");

        args.add(entrypoint);
        names.add("entrypoint");

        try {
            sendString(ObjectPacking.encodeFkt("setFlightPlanXML", args, names));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"setFlightPlanXML\" command", e);
        }
    }

    public void setStartpos(Double lon, Double lat) {
        Vector<Object> args = new Vector<Object>(2);
        Vector<String> names = new Vector<String>(2);

        args.add(lon);
        names.add("lon");

        args.add(lat);
        names.add("lat");

        try {
            sendString(ObjectPacking.encodeFkt("setStartpos", args, names));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"setStartpos\" command", e);
        }
    }

    public void shutdown() {
        try {
            sendString(ObjectPacking.encodeFkt("shutdown"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"shutdown\" command", e);
        }
    }

    public void saveConfig() {
        try {
            sendString(ObjectPacking.encodeFkt("saveConfig"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"saveConfig\" command", e);
        }
    }

    public void requestFixedOrientation() {
        try {
            sendString(ObjectPacking.encodeFkt("requestFixedOrientation"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestFixedOrientation\" command", e);
        }
    }

    public void setFixedOrientation(Float roll, Float pitch, Float yaw) {
        Vector<Object> args = new Vector<Object>(3);
        Vector<String> names = new Vector<String>(3);

        args.add(roll);
        names.add("roll");

        args.add(pitch);
        names.add("pitch");

        args.add(yaw);
        names.add("yaw");

        try {
            sendString(ObjectPacking.encodeFkt("setFixedOrientation", args, names));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"setFixedOrientation\" command", e);
        }
    }

    public void requestFlightPlanASM() {
        try {
            sendString(ObjectPacking.encodeFkt("requestFlightPlanASM"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestFlightPlanASM\" command", e);
        }
    }

    public void requestFlightPlanXML() {
        try {
            sendString(ObjectPacking.encodeFkt("requestFlightPlanXML"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestFlightPlanXML\" command", e);
        }
    }

    public void requestSimulationSpeed() {
        try {
            sendString(ObjectPacking.encodeFkt("requestSimulationSpeed"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestSimulationSpeed\" command", e);
        }
    }

    public void setSimulationSpeed(Float speed) {
        try {
            sendString(ObjectPacking.encodeFkt("setSimulationSpeed", speed, "speed"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"setSimulationSpeed\" command", e);
        }
    }

    public void dbgCommand0() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgCommand0"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgCommand0\" command", e);
        }
    }

    public void dbgCommand1() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgCommand1"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgCommand1\" command", e);
        }
    }

    public void dbgCommand2() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgCommand2"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgCommand2\" command", e);
        }
    }

    public void dbgCommand3() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgCommand3"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgCommand3\" command", e);
        }
    }

    public void dbgCommand4() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgCommand4"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgCommand4\" command", e);
        }
    }

    public void dbgCommand5() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgCommand5"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgCommand5\" command", e);
        }
    }

    public void dbgCommand6() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgCommand6"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgCommand6\" command", e);
        }
    }

    public void dbgCommand7() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgCommand7"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgCommand7\" command", e);
        }
    }

    public void dbgCommand8() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgCommand8"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgCommand8\" command", e);
        }
    }

    public void dbgCommand9() {
        try {
            sendString(ObjectPacking.encodeFkt("dbgCommand9"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"dbgCommand9\" command", e);
        }
    }

    public void updateAndroidState(AndroidState state) {
        try {
            sendString(ObjectPacking.encodeFkt("updateAndroidState", state, "state"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"updateAndroidState\" command", e);
        }
    }

    public void cancelReceiving(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("cancelReceiving", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"updateAndroidState\" command", e);
        }
    }

    public void cancelSending(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("cancelSending", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"updateAndroidState\" command", e);
        }
    }

    public void deleteFile(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("deleteFile", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"updateAndroidState\" command", e);
        }
    }

    public void getFile(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("getFile", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"updateAndroidState\" command", e);
        }
    }

    public void makeDir(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("makeDir", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"updateAndroidState\" command", e);
        }
    }

    public void requestDirListing(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("requestDirListing", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"updateAndroidState\" command", e);
        }
    }

    public void sendFile(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("sendFile", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"sendFile\" command", e);
        }
    }

    public void expertUpdateBackend(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("expertUpdateBackend", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"expertUpdateBackend\" command", e);
        }
    }

    public void requestSimulationSettings() {
        try {
            sendString(ObjectPacking.encodeFkt("requestSimulationSettings"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"requestSimulationWind\" command", e);
        }
    }

    public void setSimulationSettings(SimulationSettings settings) {
        try {
            sendString(ObjectPacking.encodeFkt("setSimulationSettings", settings, "settings"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"setSimulationSettings\" command", e);
        }
    }

    public void writeToFlash(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("writeToFlash", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"writeToFlash\" command", e);
        }
    }

    public void expertRecalibrateCompassStart() {
        try {
            sendString(ObjectPacking.encodeFkt("expertRecalibrateCompassStart"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"expertRecalibrateCompassStart\" command", e);
        }
    }

    public void expertRecalibrateCompassStop() {
        try {
            sendString(ObjectPacking.encodeFkt("expertRecalibrateCompassStop"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"expertRecalibrateCompassStop\" command", e);
        }
    }

    public void recv_planeInfo(PlaneInfo info) {
        if (!info.releaseVersion.equals(
                    DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).getAppMajorVersion())
                && isWriteable()) {
            // fireBackendConnectionLost(ConnectionLostReasons.WRONG_AP_RELEASE);
            // throw new RuntimeException("Backend has other Release Version than Open Mission Control. Connection
            // closed!");
        }
    }

    public boolean isSimulation() {
        if (getPlanePort() == null) {
            return false;
        }

        return getPlanePort().isSimulation();
    }

    @Override
    public void cancelLaunch() {
        setFlightPhase(AirplaneFlightphase.landing);
    }

    @Override
    public void cancelLanding() {
        setFlightPhase(AirplaneFlightphase.airborne);
    }

    public void expertUpdateBackendTopconOAF(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("expertUpdateBackendTopconOAF", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"expertUpdateBackendTopconOAF\" command", e);
        }
    }

    public void expertUpdateFirmwareTopconOAF(String path) {
        try {
            sendString(ObjectPacking.encodeFkt("expertUpdateFirmwareTopconOAF", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"expertUpdateFirmwareTopconOAF\" command", e);
        }
    }

    public void expertSendSimulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3) {
        Vector<Object> args = new Vector<Object>(2);
        Vector<String> names = new Vector<String>(2);

        args.add(failBitMask);
        names.add("failBitMask");

        args.add(debug1);
        names.add("debug1");

        args.add(debug2);
        names.add("debug2");

        args.add(debug3);
        names.add("debug3");

        try {
            sendString(ObjectPacking.encodeFkt("expertSendSimulatedFails", args, names));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"expertSendSimulatedFails\" command", e);
        }
    }

    public void expertRequestSimulatedFails() {
        try {
            sendString(ObjectPacking.encodeFkt("expertRequestSimulatedFails"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"expertRequestSimulatedFails\" command", e);
        }
    }

    public void clearNVRAM() {
        try {
            sendString(ObjectPacking.encodeFkt("clearNVRAM"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"clearNVRAM\" command", e);
        }
    }

    public void setManualServos(Vector<Integer> manualServos) {
        try {
            sendString(ObjectPacking.encodeFkt("setManualServos", manualServos, "manualServos"));
            System.out.println(ObjectPacking.encodeFkt("setManualServos", manualServos, "manualServos"));
        } catch (Exception e) {
            Debug.getLog().log(logLevel, "error sending \"setManualServos\" command", e);
        }
    }

    public static final ConnectionHandler DUMMY_CONNECTION_HANDLER =
        new ConnectionHandler() {
            @Override
            public void onConnectedTcp() {}

            @Override
            public void onPortListReceived() {}

            @Override
            public void onConnectingDevice() {}

            @Override
            public void onFullyConnected() {}

        };

    public interface ConnectionHandler {
        void onConnectedTcp();

        void onPortListReceived();

        void onConnectingDevice();

        void onFullyConnected();
    }
}
