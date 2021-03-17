/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.utils.IVersionProvider;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackend;
import eu.mavinci.core.plane.listeners.IAirplaneListenerGuiClose;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.listeners.IAirplaneListenerStartPos;
import eu.mavinci.core.plane.protocol.Base64;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.bluetooth.BTService;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser.StatisticEntry;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser.StatisticEntryProvider;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.rs232.MSerialPort;
import eu.mavinci.desktop.rs232.Rs232Params;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.plane.nmea.NMEA;
import gov.nasa.worldwind.geom.Position;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

public class RtkClient
        implements IAirplaneListenerBackend,
            IAirplaneListenerGuiClose,
            IAirplaneListenerStartPos,
            IAirplaneListenerPosition,
            IRtkClient {

    public static final String[] HEADERS_STR =
        new String[] {
            "type",
            "mountpoint",
            "identifier",
            "format",
            "format-details",
            "carrier",
            "nav-system",
            "network",
            "country",
            "latitude",
            "longitude",
            "nmea",
            "solution",
            "generator",
            "compr-encryp",
            "authentication",
            "fee",
            "bitrate"
        };
    public static final String[] HEADERS_CAS =
        new String[] {
            "type",
            "host",
            "port",
            "identifier",
            "operator",
            "nmea",
            "country",
            "latitude",
            "longitude",
            "fallback_host",
            "fallback_port"
        };
    public static final String[] HEADERS_NET =
        new String[] {"type", "identifier", "operator", "authentication", "fee", "web-net", "web-str", "web-reg"};

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripClient";

    protected NtripConnectionSettings con;
    IAirplane plane;
    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);
    DatagramSocket socket;

    IElevationModel elevationModel = DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);
    public static final int timerTickInterval = 2000;

    protected Runnable runnableSetTimeTick =
        new Runnable() {
            @Override
            public void run() {
                sentTimerTick();
            }
        };

    public RtkClient(IAirplane plane) {
        this.plane = plane;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        } catch (SocketException e) {
            Debug.getLog().log(Level.WARNING, "could not create UDP socket to forward RTCM messages", e);
        }

        if (plane != null) {
            plane.addListener(this);
        }

        //        Application.scheduleWeak(runnableSetTimeTick, timerTickInterval, timerTickInterval, false);
    }

    @Override
    public boolean setConnection(NtripConnectionSettings con) {
        if (isConnected()) {
            return false;
        }

        this.con = con;
        return true;
    }

    int udpPort = 7002;
    InetAddress inetAddressBackend;

    @Override
    public void setConnectorTcpPort(InetAddress inetAddressBackend, int tcpPort) {
        this.udpPort = tcpPort + 2;
        this.inetAddressBackend = inetAddressBackend;
    }

    @Override
    public void setBroadcastPort(int udpPort) {
        this.udpPort = udpPort;
    }

    protected WeakListenerList<IRtkStatisticListener> listeners =
        new WeakListenerList<>("ntrip client" + this.hashCode());

    public ArrayList<NtripSourceTableEntry> getSources() {
        ArrayList<NtripSourceTableEntry> sources = new ArrayList<NtripSourceTableEntry>();
        // http://igs.bkg.bund.de/root_ftp/NTRIP/documentation/NtripDocumentation.pdf
        // Create connection
        try (ConnectionObjects connection = getConnection(null)) {
            // Get Response
            String newLine;
            while ((newLine = connection.readLine()) != null) {
                NtripSourceTableEntry.parse(newLine).ifPresent(sources::add);
            }

            if (sources.isEmpty()) {
                throw new Exception("no entries in source table");
            }

        } catch (SocketException e2) {
            return sources;
        } catch (Exception e1) {
            Debug.getLog().log(Debug.WARNING, "Problem getting Ntrip sources list", e1);
            return null;
        }

        return sources;
    }

    public static final int NTRIP_RESEND_INTERVAL = 5 * 1000;

    public static final int MAX_CONNECTING_ATTTEMPTS = 3;

    public static final int WAIT_AFTER_CONNECTING_FAIL = 10 * 1000;

    public static final int CONNECT_TIMEOUT = 15 * 1000;
    public static final int lastReceiveTimeoutNtrip = 20 * 1000;
    public static final int READ_TIMEOUT = Math.min(lastReceiveTimeoutNtrip, NTRIP_RESEND_INTERVAL);

    private ConnectionObjects getConnection(String stream)
            throws MalformedURLException, IOException, ProtocolException {
        URL url =
            new URL(
                (con.isHttps() ? "https" : "http"), con.getHost(), con.getPort(), (stream != null ? "/" + stream : ""));
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Host", con.getHost());
        connection.setRequestProperty("Ntrip-Version", "Ntrip/2.0");
        connection.setRequestProperty(
            "User-Agent",
            "NTRIP mavinci-desktop/"
                + DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).getHumanReadableVersion());
        if (con.getUser() != null && !con.getUser().isEmpty()) {
            String authStr = con.getUser() + ":" + Base64.decodeString(con.getPassword());
            String authEncoded = Base64.encodeString(authStr);
            connection.setRequestProperty("Authorization", "Basic " + authEncoded);
        }

        connection.setRequestProperty("Connection", "close");
        connection.setRequestProperty("Accept-Language", "en,*");
        connection.setRequestProperty("Ntrip-Version", "Ntrip/2.0");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        ConnectionObjects conObj = new ConnectionObjects(connection);

        if (lastPosWGS84 != null) {
            conObj.print("Ntrip-GGA: " + NMEA.createGPGGA(lastPosWGS84) + "\r\n");
        }

        conObj.flush();

        return conObj;
    }

    protected long connectTime = -1;
    protected long disconnectTime = -1;

    protected volatile boolean isConnected = false;

    public synchronized boolean isConnected() {
        return isConnected;
    }

    public volatile ConnectionObjects connection = null;

    @Override
    public synchronized void disconnect() {
        boolean wasConnected = isConnected;
        isConnected = false;
        isConnectedNtrip = false;
        isConnectedRs232 = false;
        isConnectedUDP = false;
        isConnectedBluetooth = false;
        for (IRtkStatisticListener listener : listeners) {
            if (listener == null) {
                continue;
            }

            listener.connectionStateChanged(NtripConnectionState.unconnected, -1);
        }

        if (wasConnected) {
            disconnectTime = System.currentTimeMillis();
        }

        sentTimerTick();

        if (!wasConnected) {
            return;
        }

        try {
            if (serialPort != null) {
                serialPort.closePort();
            }
        } catch (Exception e) {
        }

        try {
            if (readThread != null) {
                readThread.interrupt();
            }
        } catch (Exception e) {
        }

        try {
            connection.close();
        } catch (Exception e) {
        }

        readThread = null;
        connection = null;
        updPort = -1;
    }

    protected Thread readThread = null;

    String connectedStream;
    // LatLon refPosition = LatLon.fromDegrees(52, 10);

    protected boolean isConnectedNtrip = false;
    protected boolean isConnectedRs232 = false;
    protected boolean isConnectedUDP = false;
    protected boolean isConnectedBluetooth = false;

    public boolean isConnectedBluetooth() {
        return isConnectedBluetooth;
    }

    public boolean isConnectedRs232() {
        return isConnectedRs232;
    }

    public boolean isConnectedUDP() {
        return isConnectedUDP;
    }

    public boolean isConnectedNtrip() {
        return isConnectedNtrip;
    }

    Rs232Params rs232params;

    MSerialPort serialPort;

    public static final long lastReceiveTimeoutRs232 = 10000;
    public static final long lastReceiveTimeoutUDP = 10000;

    @Override
    public void connect(Rs232Params rs232params) {
        String port = rs232params.getPort();
        if (port == null || port.isEmpty()) {
            return;
        }

        updPort = -1;
        lastTrafficIn = 0;
        lastTrafficOut = 0;
        connection = null;
        isConnected = true;
        isConnectedUDP = false;
        isConnectedRs232 = true;
        isConnectedNtrip = false;
        isConnectedBluetooth = false;
        connectTime = System.currentTimeMillis();
        this.rs232params = rs232params;
        for (IRtkStatisticListener listener : listeners) {
            if (listener == null) {
                continue;
            }

            listener.connectionStateChanged(NtripConnectionState.connecting, -1);
        }

        try {
            serialPort = rs232params.openPort();
        } catch (IOException e) {
            serialPort = null;
        }

        if (serialPort == null) {
            disconnect();
            return;
        }

        ntripParser.resetAll();

        readThread =
            new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            long lastReceive = System.currentTimeMillis();
                            long lastOkCounter = 0;
                            long counter = 0;
                            while (isConnected) {
                                if (lastReceive + lastReceiveTimeoutRs232 < System.currentTimeMillis()
                                        && lastOkCounter != counter) {
                                    throw new Exception(
                                        "RS232 timeout: have not received RTCM data for more than "
                                            + (lastReceiveTimeoutRs232 / 1000)
                                            + " seconds");
                                }

                                counter++;
                                Thread.sleep(50);
                                byte[] buffIn = serialPort.readBytes();
                                if (buffIn == null) {
                                    continue;
                                }

                                if (buffIn.length == 0) {
                                    continue;
                                }

                                lastTrafficIn += buffIn.length;
                                lastReceive = System.currentTimeMillis();
                                lastOkCounter = counter;
                                ntripParser.addToBuffer(buffIn, 0, buffIn.length);
                            }

                        } catch (Exception e1) {
                            if (isConnected) {
                                Debug.getLog()
                                    .log(Debug.WARNING, "RS232 RTCM reading problems....closing connection ", e1);
                            }

                            disconnect();
                        }
                    }
                },
                "RS232 RTCM read thread");

        readThread.start();
        sentTimerTick();
    }

    protected int updPort = -1;

    @Override
    public void connect(final int updPort) {
        if (updPort < 0 || updPort > 65535) {
            return;
        }

        lastTrafficIn = 0;
        lastTrafficOut = 0;
        connection = null;
        isConnected = true;
        isConnectedUDP = true;
        isConnectedRs232 = false;
        isConnectedNtrip = false;
        isConnectedBluetooth = false;
        connectTime = System.currentTimeMillis();
        for (IRtkStatisticListener listener : listeners) {
            if (listener == null) {
                continue;
            }

            listener.connectionStateChanged(NtripConnectionState.connecting, -1);
        }

        this.udpPort = updPort;

        ntripParser.resetAll();

        readThread =
            new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        DatagramSocket dsocket = null;
                        try {
                            long lastReceive = System.currentTimeMillis();
                            long lastOkCounter = 0;
                            long counter = 0;

                            Debug.getLog().log(Level.FINE, "start RTCM BroadcastListening:");
                            // Create a socket to listen on the Port.
                            try {
                                // dsocket = new DatagramSocket(BROADCAST_PORT);
                                dsocket = new DatagramSocket(null);
                                dsocket.setReuseAddress(true);
                                dsocket.setSoTimeout(300); // max time the socket is
                                // blocked
                                dsocket.setBroadcast(true);
                                dsocket.bind(new InetSocketAddress(RtkClient.this.udpPort));
                                //
                            } catch (BindException e) {
                                DependencyInjector.getInstance()
                                    .getInstanceOf(IApplicationContext.class)
                                    .addToast(
                                        Toast.of(ToastType.ALERT)
                                            .setText(
                                                "Can't listen to RTCM broadcasts. Probably an other instance of Open Mission Control is allready in use")
                                            .create());
                                Debug.getLog().log(Level.FINE, "Error binding broadcast Listening.");
                                return;
                            } catch (SocketException e) {
                                Debug.getLog().log(Level.SEVERE, "Could not open RTCM broadcast listener", e);
                                return;
                            }

                            // Create a buffer to read datagrams into. If a
                            // packet is larger than this buffer, the
                            // excess will simply be discarded!
                            byte[] buffer = new byte[1024 * 4];

                            // Create a packet to receive data into the buffer
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                            Debug.getLog().log(Level.CONFIG, "Open Backend broadcast listener on Port: " + updPort);

                            // System.out.println("open poard");
                            // Now loop forever, waiting to receive packets and printing
                            // them.
                            while (isConnected) {
                                // System.out.println("head:"+System.currentTimeMillis());
                                if (lastReceive + lastReceiveTimeoutUDP < System.currentTimeMillis()
                                        && lastOkCounter != counter) {
                                    throw new Exception(
                                        "UDP timeout: have not received RTCM data for more than "
                                            + (lastReceiveTimeoutUDP / 1000)
                                            + " seconds");
                                }

                                counter++;
                                Debug.getLog().log(Level.FINE, "try recv UDP RTCM:" + counter);
                                // Wait to receive a datagram
                                try {
                                    dsocket.receive(packet);
                                } catch (SocketTimeoutException e) {
                                    // CDebug
                                    // .getLog().info("Backend broadcast listener
                                    // timeout");
                                    continue;
                                }

                                if (packet.getLength() == 0) { // empty packet
                                    // Reset the length of the packet before reusing it.
                                    packet.setLength(buffer.length);
                                    continue;
                                }

                                Debug.getLog().log(Level.FINE, "recv UDP RTCM bytes:" + packet.getLength());
                                lastReceive = System.currentTimeMillis();
                                lastOkCounter = counter;
                                lastTrafficIn += packet.getLength();
                                ntripParser.addToBuffer(buffer, 0, packet.getLength());

                                // Reset the length of the packet before reusing it.
                                packet.setLength(buffer.length);
                            }

                        } catch (Exception e1) {
                            if (isConnected) {
                                Debug.getLog()
                                    .log(Debug.WARNING, "UPD RTCM reading problems....closing connection ", e1);
                            }
                        } finally {
                            if (dsocket != null) {
                                try {
                                    dsocket.close();
                                } catch (Exception e) {
                                }
                            }

                            disconnect();
                        }
                    }
                },
                "UDP RTCM read thread");

        readThread.start();
        sentTimerTick();
    }

    @Override
    public synchronized void connect(final String stream) {
        isConnected = true;
        isConnectedRs232 = false;
        isConnectedUDP = false;
        isConnectedNtrip = true;
        isConnectedBluetooth = false;
        updPort = -1;
        ntripParser.resetAll();
        connectTime = System.currentTimeMillis();
        connectedStream = stream;
        // this.refPosition=refPosition;
        for (IRtkStatisticListener listener : listeners) {
            if (listener == null) {
                continue;
            }

            listener.connectionStateChanged(NtripConnectionState.connecting, -1);
        }

        readThread =
            new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        int connectionAttempts = 0;
                        int ntripTimeoutCounter = 0;

                        while (true) {
                            try {
                                // Create connection
                                try {
                                    connection = getConnection(stream);
                                } catch (Exception e1) {
                                    // here it makes sense to first wait!
                                    connectionAttempts++;
                                    if (connectionAttempts > MAX_CONNECTING_ATTTEMPTS) {
                                        Debug.getLog()
                                            .log(
                                                Debug.WARNING,
                                                "NTRIP connecting problems.... giving up after "
                                                    + MAX_CONNECTING_ATTTEMPTS
                                                    + " attempts!!",
                                                e1);
                                        disconnect();
                                        return;
                                    }

                                    if (isConnected) {
                                        Debug.getLog()
                                            .log(
                                                Debug.WARNING,
                                                "NTRIP connecting problems.... trying reconnect no."
                                                    + connectionAttempts
                                                    + "!!",
                                                e1);
                                    } else {
                                        return;
                                    }

                                    for (int i = WAIT_AFTER_CONNECTING_FAIL; i > 0; i -= 1000) {
                                        for (IRtkStatisticListener listener : listeners) {
                                            if (listener == null) {
                                                continue;
                                            }

                                            listener.connectionStateChanged(NtripConnectionState.waitingReconnect, i);
                                        }

                                        Thread.sleep(1000);
                                    }

                                    continue;
                                }

                                connectionAttempts = 0;

                                long lastGGAsent = 0;

                                if (lastPosWGS84 != null) {
                                    lastGGAsent = System.currentTimeMillis();
                                    Debug.getLog()
                                        .fine(
                                            "NTRIP-GGA sent " + lastPosWGS84 + " -> " + NMEA.createGPGGA(lastPosWGS84));
                                    String toSend = "Ntrip-GGA: " + NMEA.createGPGGA(lastPosWGS84) + "\r\n";
                                    connection.print(toSend);
                                    connection.flush();
                                }

                                // Get Response
                                byte[] buffRaw = new byte[RtcmParser.inBufferSize];
                                int buffRawLevel = 0;
                                ntripParser.resetReconnect();

                                // boolean seenRTCM=false;
                                boolean headerDone = connection.expectHeader();
                                boolean chunkedEncoding = false;
                                long lastRead = System.currentTimeMillis();
                                while (isConnected) {
                                    if (buffRawLevel > buffRaw.length - 100) {
                                        buffRawLevel = 0;
                                        // System.out.println("reset raw buffer!");
                                    }

                                    int len;
                                    try {
                                        len =
                                            connection.read(
                                                buffRaw, buffRawLevel, Math.min(400, buffRaw.length - buffRawLevel));
                                    } catch (SocketTimeoutException e) {
                                        len = 0;
                                    }

                                    if (!isConnected) {
                                        return;
                                    }

                                    if (len <= 0) {
                                        if (lastRead + lastReceiveTimeoutNtrip < System.currentTimeMillis()) {
                                            ntripTimeoutCounter++;
                                            throw new IOException(
                                                "NTRIP source provided no RTCM data within "
                                                    + (lastReceiveTimeoutNtrip / 1000)
                                                    + " seconds, disconnecting!");
                                        }
                                    } else {
                                        ntripTimeoutCounter = 0;
                                        lastRead = System.currentTimeMillis();
                                        buffRawLevel += len;
                                    }

                                    if (!headerDone) {
                                        // search and detect header
                                        String bufStr = new String(buffRaw, 0, buffRawLevel);
                                        // System.out.println(bufStr);

                                        int pos = bufStr.indexOf("\r\n\r\n");
                                        if (pos < 0) {
                                            pos = bufStr.indexOf("\n\n"); // maybe
                                        }
                                        // helping
                                        // for non
                                        // std.
                                        // ntrip
                                        // servers
                                        if (pos >= 0) {
                                            headerDone = true;
                                            Debug.getLog().info("NTRIP response header:" + bufStr.substring(0, pos));
                                            // delete header from buffer
                                            int k = 0;
                                            for (int j = pos + 4; j < buffRawLevel; j++) {
                                                buffRaw[k] = buffRaw[j];
                                                k++;
                                            }

                                            buffRawLevel = k;

                                            // parse special flags in header
                                            bufStr = bufStr.toLowerCase().replaceAll(" ", "");
                                            pos = bufStr.indexOf("transfer-encoding:chunked");
                                            if (pos >= 0) {
                                                Debug.getLog().info("chunked encoding detected");
                                                chunkedEncoding = true;
                                            }

                                            // System.out.println("---- header found and
                                            // removed! remaining buffer
                                            // ("+buffRawLevel+"):\n");
                                            // System.out.println(new
                                            // String(buffRaw,0,buffRawLevel));
                                        }
                                    }

                                    // if (!headerDone) continue; //MM: topnet UK is NOT
                                    // sending end of header messages, and this
                                    // workaround seems to work very fine for all ntrip
                                    // casters I had I my list

                                    if (lastPosWGS84 != null
                                            && (System.currentTimeMillis() - lastGGAsent >= NTRIP_RESEND_INTERVAL)) {
                                        lastGGAsent = System.currentTimeMillis();
                                        Debug.getLog()
                                            .fine(
                                                "NTRIP-GGA sent "
                                                    + lastPosWGS84
                                                    + " -> "
                                                    + NMEA.createGPGGA(lastPosWGS84));
                                        String toSend = "Ntrip-GGA: " + NMEA.createGPGGA(lastPosWGS84) + "\r\n";
                                        connection.print(toSend);
                                        connection.flush();
                                    }

                                    if (chunkedEncoding) {

                                        // search chunks and parse them
                                        while (true) {
                                            // search next chunck size
                                            if (buffRawLevel < 6) {
                                                break;
                                            }
                                            // search first linebreak
                                            int i = 1;
                                            boolean found = true;
                                            for (; i < 5; i++) {
                                                if (i + 1 >= buffRawLevel) {
                                                    break;
                                                }

                                                if (buffRaw[i] != '\r') {
                                                    continue;
                                                }

                                                if (buffRaw[i + 1] != '\n') {
                                                    continue;
                                                }

                                                found = true;
                                                break;
                                            }

                                            if (!found) {
                                                break;
                                            }

                                            String hexLen = new String(buffRaw, 0, i);
                                            int lenChunk = Integer.parseInt(hexLen, 16);
                                            if (i + lenChunk + 4 > buffRawLevel) {
                                                break;
                                            }

                                            // System.out.println("chunk len:" + hexLen
                                            // + " -> "+ lenChunk);
                                            //
                                            // System.out.print("buffLevel " + buffLevel
                                            // + " -> ");
                                            ntripParser.addToBuffer(buffRaw, i + 2, lenChunk);

                                            // System.out.print("----"+new
                                            // String(buffRaw,0,buffRawLevel));

                                            // remove from raw buffer
                                            int k = 0;
                                            for (int n = i + lenChunk + 4; n < buffRawLevel; n++) {
                                                buffRaw[k] = buffRaw[n];
                                                k++;
                                            }
                                            // System.out.println("buffLevel Raw " +
                                            // buffRawLevel + " -> "+ k);
                                            buffRawLevel = k;
                                        }
                                    } else {
                                        ntripParser.addToBuffer(buffRaw, 0, buffRawLevel);
                                        buffRawLevel = 0;
                                    }
                                }

                            } catch (Exception e1) {
                                if (isConnected) {
                                    if (ntripTimeoutCounter > MAX_CONNECTING_ATTTEMPTS) {
                                        Debug.getLog()
                                            .log(
                                                Debug.WARNING,
                                                "NTRIP source provided no RTCM data, disconnecting!",
                                                e1);
                                        disconnect();
                                        return;
                                    } else {
                                        Debug.getLog()
                                            .log(
                                                Debug.WARNING,
                                                "NTRIP connection problems.... trying reconnect!!",
                                                e1);
                                    }
                                } else {
                                    return;
                                }
                            }
                        }
                    }
                },
                "NTRIP read thread");

        readThread.start();
    }

    BTService connectedBtService;

    @Override
    public synchronized void connect(final BTService btService) {
        isConnected = true;
        isConnectedRs232 = false;
        isConnectedUDP = false;
        isConnectedNtrip = false;
        isConnectedBluetooth = true;
        updPort = -1;
        ntripParser.resetAll();
        connectTime = System.currentTimeMillis();
        connectedBtService = btService;
        // this.refPosition=refPosition;
        for (IRtkStatisticListener listener : listeners) {
            if (listener == null) {
                continue;
            }

            listener.connectionStateChanged(NtripConnectionState.connecting, -1);
        }

        readThread =
            new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            // Create connection
                            connection = new ConnectionObjects(btService);

                            // Get Response
                            byte[] buffRaw = new byte[RtcmParser.inBufferSize];
                            int buffRawLevel = 0;
                            ntripParser.resetReconnect();

                            long lastRead = System.currentTimeMillis();
                            while (isConnected) {
                                if (buffRawLevel > buffRaw.length - 100) {
                                    buffRawLevel = 0;
                                    // System.out.println("reset raw buffer!");
                                }

                                int len;
                                try {
                                    len =
                                        connection.read(
                                            buffRaw, buffRawLevel, Math.min(400, buffRaw.length - buffRawLevel));
                                } catch (SocketTimeoutException e) {
                                    len = 0;
                                }

                                if (!isConnected) {
                                    return;
                                }

                                if (len < 0) {
                                    if (lastRead + lastReceiveTimeoutNtrip > System.currentTimeMillis()) {
                                        throw new IOException(
                                            "Bluetooth source proviced no RTCM data within "
                                                + (lastReceiveTimeoutNtrip / 1000)
                                                + " seconds, disconnecting!");
                                    }
                                } else {
                                    lastRead = System.currentTimeMillis();
                                    buffRawLevel += len;
                                }

                                String bufStr = new String(buffRaw, 0, buffRawLevel);
                                System.out.println(bufStr);

                                ntripParser.addToBuffer(buffRaw, 0, buffRawLevel);
                                buffRawLevel = 0;
                            }

                        } catch (Exception e1) {
                            if (isConnected) {
                                Debug.getLog().log(Debug.WARNING, "Bluetooth connection problems", e1);
                            }
                        }
                    }
                },
                "Bluetooth read thread");

        readThread.start();

        sentTimerTick();
    }

    @Override
    public Map<Integer, StatisticEntry> getStatistics() {
        return ntripParser.getStatistics();
    }

    @Override
    public RtcmParser getParser() {
        return ntripParser;
    }

    long lastTrafficIn = 0;
    long lastTrafficOut = 0;

    protected void sentTimerTick() {
        final long connectedTimeF = ((isConnected ? System.currentTimeMillis() : disconnectTime) - connectTime);
        final long trafficIn = connection == null ? lastTrafficIn : connection.getTrafficIn();
        final long trafficOut = connection == null ? lastTrafficOut : connection.getTrafficOut();
        lastTrafficIn = trafficIn;
        lastTrafficOut = trafficOut;
        for (IRtkStatisticListener l : listeners) {
            l.timerTickWhileConnected(connectedTimeF, trafficIn, trafficOut);
        }
    }

    public RtcmParser ntripParser =
        new RtcmParser(new StatisticEntryProvider()) {

            @Override
            protected void sendRTCMmessage(final byte[] msg, final int rtype) throws IOException {
                for (IRtkStatisticListener l : listeners) {
                    l.packageReceived(msg, rtype);
                }

                if (udpPort <= 0 || inetAddressBackend == null) {
                    return;
                }

                // Initialize a datagram packet with data and address
                DatagramPacket packet = new DatagramPacket(msg, msg.length, inetAddressBackend, udpPort); // TCP
                // port
                // +2

                Debug.getLog().info(rtype + " -> " + inetAddressBackend + ":" + udpPort);

                socket.send(packet);
                Debug.getLog().log(Level.FINER, "sent broadcast packet on " + inetAddressBackend.getHostAddress());
            }

            @Override
            protected void seenFirstRTCM() {
                for (IRtkStatisticListener listener : listeners) {
                    listener.connectionStateChanged(NtripConnectionState.connected, -1);
                }
            }

        };

    @Override
    public void addListener(IRtkStatisticListener l) {
        listeners.add(l);
    }

    public void addListenerAtBegin(IRtkStatisticListener l) {
        listeners.addAtBegin(l);
    }

    @Override
    public void removeListener(IRtkStatisticListener l) {
        listeners.remove(l);
    }

    // Position lastPosAboveMSL = null;
    Position lastPosWGS84 = Position.fromDegrees(49.2466, 8.64037, 140);

    int lastPosFrom = 0; // 0==backend,1==startpos,2==realPos

    @Override
    public void setLastPosWGS84(Position pos) {
        lastPosWGS84 = pos;
    }

    @Override
    public Position getLastPosWGS84() {
        return lastPosWGS84;
    }

    @Override
    public void recv_backend(Backend host, MVector<Port> ports) {
        try {
            setConnectorTcpPort(plane.getAirplaneCache().getBackendStateOffline().getHost().getAddress(), host.port);
            if (host.hasFix && lastPosFrom == 0) {
                lastPosWGS84 = Position.fromDegrees(host.lat, host.lon, host.alt / 100.);
            }
        } catch (AirplaneCacheEmptyException e) {
        }
    }

    @Override
    public void guiClose() {
        if (isConnected()) {
            disconnect();
        }

        socket.close();
    }

    @Override
    public boolean guiCloseRequest() {
        if (isConnected()) {
            disconnect();
        }

        return true;
    }

    @Override
    public void storeToSessionNow() {}

    @Override
    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        if (pressureZero == 1 && lastPosFrom <= 1) {
            double elevWGS84;
            try {
                elevWGS84 = plane.getAirplaneCache().getStartElevOverWGS84();
                // - plane.getAirplaneCache().getStartElevEGMoffset();
            } catch (AirplaneCacheEmptyException e) {
                elevWGS84 = elevationModel.getElevationAsGoodAsPossible(lon, lat);
                // - WWFactory.egm96.getOffset(Angle.fromDegreesLatitude(lat), Angle.fromDegreesLongitude(lon));
            }

            lastPosWGS84 = Position.fromDegrees(lat, lon, elevWGS84);
            lastPosFrom = 1;
        }
    }

    @Override
    public void recv_position(PositionData p) {
        try {
            lastPosWGS84 =
                Position.fromDegrees(
                    p.lat, p.lon, (p.gpsAltitude + plane.getAirplaneCache().getDebugData().gps_ellipsoid) / 100.);
            lastPosFrom = 2;
        } catch (AirplaneCacheEmptyException e) {
        }
    }

    public void sendFile(File config) {
        if (!isConnectedRs232()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(config))) {
            String line;
            while ((line = br.readLine()) != null) {
                serialPort.writeString(line + "\r\n");
                int len = line.length() + 2;
                len *= (rs232params.getDataBits() + rs232params.getStopBits());
                double time = ((double)len) / rs232params.getBitRate(); // to
                // sec
                time *= 1000; // to ms
                try {
                    Thread.sleep((int)(time + 1));
                    Thread.sleep(10); // some constant offset could never be
                    // wrong?
                } catch (InterruptedException e) {
                    // anyway
                }
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "could not send config file " + config + " to basestation", e);
        }
    }

    public void sendConfigFile(File config, Supplier<Boolean> isCanceled) {
        if (!isConnected()) {
            return;
        }

        if (!isConnectedRs232() && !isConnectedBluetooth()) {
            throw new IllegalStateException("Send config functionality is only supported for Rs232 or Bluetooth");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(config))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (isCanceled.get()) {
                    return;
                }

                sendStringToDevice(line);

                // Sleep 0.5 second according to requirements
                try {
                    Thread.sleep(500);
                } catch (InterruptedException expected) {
                    // Ignore
                }
            }
        } catch (IOException e) {
            Debug.getLog().log(Level.SEVERE, "could not send config file " + config + " to basestation", e);
        }
    }

    private void sendStringToDevice(String line) throws IOException {
        if (isConnectedRs232()) {
            serialPort.writeString(line + "\r\n");
        } else if (isConnectedBluetooth() && connection != null) {
            connection.print(line + "\r\n");
            connection.flush();
        }
    }

    @Override
    public String toString() {
        if (!isConnected) {
            return languageHelper.getString(KEY + ".unconnected");
        }

        if (isConnectedBluetooth) {
            return languageHelper.getString(KEY + ".connectedBluetooth");
        }

        if (isConnectedNtrip) {
            return languageHelper.getString(KEY + ".connectedNtrip");
        }

        if (isConnectedUDP) {
            return languageHelper.getString(KEY + ".connectedUdp");
        }

        if (isConnectedRs232) {
            return languageHelper.getString(KEY + ".connectedRs232");
        }

        return super.toString();
    }
}
