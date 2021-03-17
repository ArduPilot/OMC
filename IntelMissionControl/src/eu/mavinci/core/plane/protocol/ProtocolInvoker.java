/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

/** */
package eu.mavinci.core.plane.protocol;

import com.intel.missioncontrol.concurrent.Dispatcher;
import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPositionOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerSendingHost;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.desktop.main.debug.Debug;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

public class ProtocolInvoker {

    protected boolean isReceiveOrientation = false;
    protected OrientationData lastOrientation;

    protected boolean isReceivePosition = false;
    protected PositionData lastPosition;

    protected String fctName;

    public Object[] arguments;
    public Class<?>[] parameterTypes;

    protected InetSocketAddress sender;
    protected String msg;

    protected boolean dataOk;
    private boolean isNMEA;
    private boolean isAscTec;

    public void processMessage(final String msg, final InetSocketAddress sender) throws Exception {
        try {
            // Splitting string
            this.sender = sender;
            this.msg = msg;
            dataOk = false;
            if (isAscTec) {
                newAscTecMsg(msg);
                return;
            }

            if (msg.startsWith("$GP")) {
                isNMEA = true;
                // NMEA PARSING
                newNmea(msg);
                return;
            }

            StringTokenizer argTkn = new StringTokenizer(msg, ProtocolTokens.sep + ProtocolTokens.mend, false);

            fctName = argTkn.nextToken();
            if (!fctName.startsWith(ProtocolTokens.mbegin)) {
                throw new Exception("Wrong Start of Message");
            }

            fctName = fctName.substring(1, fctName.length()); // remove beginning
            // mbegin

            // some shortcuts for very often packages
            if (fctName.equals(ObjectParser.recv_orientationToken)) {
                // Generate Agrument List
                ObjectParser parser = new ObjectParser();
                parser.decodeObject(argTkn.nextToken());
                isReceiveOrientation = true;
                isReceivePosition = false;
                lastOrientation = (OrientationData)parser.value;
            } else if (fctName.equals(ObjectParser.recv_positionToken)) {
                // Generate Agrument List
                ObjectParser parser = new ObjectParser();
                parser.decodeObject(argTkn.nextToken());

                isReceiveOrientation = false;
                isReceivePosition = true;
                lastPosition = (PositionData)parser.value;
            } else {

                // Generate Agrument List
                final List<Object> args = new ArrayList<Object>();
                List<Class<?>> parameterTypesVec = new ArrayList<Class<?>>();
                while (argTkn.hasMoreTokens()) {
                    ObjectParser parser = new ObjectParser();
                    parser.decodeObject(argTkn.nextToken());
                    Object o = parser.value;
                    args.add(o);
                    parameterTypesVec.add(parser.type);
                }

                if (args.size() == 2 && args.get(0) instanceof Backend && args.get(1) instanceof MVector) {
                    Backend b = (Backend)args.get(0);
                    @SuppressWarnings("unchecked")
                    MVector<Port> ports = (MVector<Port>)args.get(1);
                    for (Port p : ports) {
                        p.backend = b;
                    }
                    // System.out.println("patch backend into port packages");
                }

                // call Funktion fctName with args as arguments
                arguments = args.toArray();
                parameterTypes = new Class<?>[parameterTypesVec.size()];
                for (int i = 0; i != parameterTypesVec.size(); i++) {
                    parameterTypes[i] = parameterTypesVec.get(i);
                }

                isReceiveOrientation = false;
                isReceivePosition = false;

                // method = IAirplaneListenerAllExternal.class.getMethod(fctName, parameterTypes);

                // System.out.println(msg);
                // System.out.println(args.toString());
            }

            // System.out.println(msg);
            // System.out.println(isReceiveOrientation);
            // System.out.println(isReceivePosition);nmeaLine
            // System.out.println(lastOrientation);
            // System.out.println(lastPosition);
            // System.out.println(fctName);
            // System.out.println(arguments);
            // System.out.println(parameterTypes);
            // System.out.println(sender);

            dataOk = true;
        } catch (Throwable t) {
            throw new Exception("problem parsing message:" + msg, t);
        }
    }

    private void newAscTecMsg(String msg2) {
        // TODO Auto-generated method stub

    }

    public boolean isDataOk() {
        return dataOk;
    }

    public void fireEventsDirectly(IInvokeable handler) throws Exception {
        fireEventsDirectly(
            handler,
            isReceiveOrientation,
            isReceivePosition,
            lastOrientation,
            lastPosition,
            fctName,
            arguments,
            parameterTypes,
            sender);
    }

    public static void fireEventsDirectly(
            IInvokeable handler,
            boolean isReceiveOrientation,
            boolean isReceivePosition,
            OrientationData lastOrientation,
            PositionData lastPosition,
            String fctName,
            Object[] arguments,
            Class<?>[] parameterTypes,
            InetSocketAddress sender)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // some shortcuts for very often packages
        if (sender != null && handler instanceof IAirplaneListenerSendingHost) {
            IAirplaneListenerSendingHost listener = (IAirplaneListenerSendingHost)handler;
            listener.setSendHostOfNextReceive(sender);
        }

        if (isReceiveOrientation) {
            handleOrientation(handler, lastOrientation);
        } else if (isReceivePosition) {
            handlePosition(handler, lastPosition);
        } else {
            if (arguments.length > 0 && handlePositionOrientation(handler, arguments[0])) {
                return;
            }

            if (fctName == null) {
                return;
            }

            Method method = handler.getClass().getMethod(fctName, parameterTypes);
            method.invoke(handler, arguments);
        }
    }

    private static boolean handlePositionOrientation(IInvokeable handler, Object argument)
            throws NoSuchMethodException {
        if (handler instanceof IAirplaneListenerPositionOrientation && argument instanceof PositionOrientationData) {
            IAirplaneListenerPositionOrientation airplaneListenerPositionOrientation =
                (IAirplaneListenerPositionOrientation)handler;
            airplaneListenerPositionOrientation.recv_positionOrientation((PositionOrientationData)argument);
            return true;
        }

        return false;
    }

    private static void handlePosition(IInvokeable handler, PositionData lastPosition) throws NoSuchMethodException {
        if (handler instanceof IAirplaneListenerPosition) {
            IAirplaneListenerPosition positionListener = (IAirplaneListenerPosition)handler;
            positionListener.recv_position(lastPosition);
        } else {
            throw new NoSuchMethodException("Handler does not support: recv_position");
        }
    }

    private static void handleOrientation(IInvokeable handler, OrientationData lastOrientation)
            throws NoSuchMethodException {
        if (handler instanceof IAirplaneListenerOrientation) {
            IAirplaneListenerOrientation orientationListener = (IAirplaneListenerOrientation)handler;
            orientationListener.recv_orientation(lastOrientation);
        } else {
            throw new NoSuchMethodException("Handler does not support: recv_orientation");
        }
    }

    public void fireEventsInUIthread(final IInvokeable handler, boolean wait) {
        final boolean isReceiveOrientation = this.isReceiveOrientation;
        final OrientationData lastOrientation = this.lastOrientation;

        final boolean isReceivePosition = this.isReceivePosition;
        final PositionData lastPosition = this.lastPosition;

        final String fctName = this.fctName;

        final Object[] arguments = this.arguments;
        final Class<?>[] parameterTypes = this.parameterTypes;

        final InetSocketAddress sender = this.sender;
        final String msg = this.msg;
        // final Exception outerStack = new Exception("cause of " + fctName);
        // if (fctName.equals("recv_backend") ) outerStack.printStackTrace();
        Runnable r =
            new Runnable() {
                public void run() {
                    try {
                        fireEventsDirectly(
                            handler,
                            isReceiveOrientation,
                            isReceivePosition,
                            lastOrientation,
                            lastPosition,
                            fctName,
                            arguments,
                            parameterTypes,
                            sender);
                    } catch (Exception e) {
                        // System.out.println(msg);
                        // System.out.println(isReceiveOrientation);
                        // System.out.println(isReceivePosition);
                        // System.out.println(lastOrientation);
                        // System.out.println(lastPosition);
                        // System.out.println(fctName);
                        // for (int i = 0; i != arguments.length; ++i){
                        // System.out.print(arguments[i]);
                        // System.out.println(" @ "+parameterTypes[i]);
                        // }
                        // System.out.println(sender);
                        Debug.getLog().log(Level.SEVERE, "For message" + msg + "  \nErrorCallingHandler", e);
                        // CDebug.getLog().log(Level.SEVERE, "caused by", outerStack);
                    }
                }
            };

        if (wait) {
            try {
                Dispatcher.runOnUI(r);
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "problems invoking packagereveive", e);
            }
        } else {
            Dispatcher.postToUI(r);
        }
    }

    public void fireEvents(final IInvokeable handler, boolean inUIthread) throws Exception {
        if (inUIthread) {
            fireEventsInUIthread(handler, false);
        } else {
            fireEventsDirectly(handler);
        }
    }

    public void fireEventsForMessageInUIthread(final String msg, final IInvokeable handler) throws Exception {
        fireEventsForMessageInUIthread(msg, handler, false);
    }

    public void fireEventsForMessageInUIthread(final String msg, final IInvokeable handler, boolean wait)
            throws Exception {
        processMessage(msg, null);
        fireEventsInUIthread(handler, wait);
    }

    public void fireEventsForMessageInUIthread(
            final String msg, final IInvokeable handler, final InetSocketAddress sender) throws Exception {
        processMessage(msg, sender);
        fireEventsInUIthread(handler, false);
    }

    private void newNmea(String nmeaLine) {
        if (!CNMEA.checkNmeaSum(nmeaLine)) {
            // System.out.println("checksum failed");
            Debug.getLog().log(Level.FINE, "NEMA checksum failed:" + nmeaLine);
            return;
        }

        int pos = nmeaLine.indexOf("*");

        if (pos > 0) {
            nmeaLine = nmeaLine.substring(0, pos);
        }
        // nmeaLine = nmeaLine.toUpperCase();
        // System.out.println("nmea:"+nmeaLine);
        String parts[] = nmeaLine.split(",");
        if (parts == null || parts.length < 2) {
            // strange broken
            Debug.getLog().log(Level.FINE, "NEMA stange splitting:" + nmeaLine);
            return;
        }

        newNmea(parts);
    }

    double nmeaLat;
    double nmeaLon;
    double nmeaAlt;
    double nmeaGeoidHeight;
    Long nmeaTime;
    boolean nmeaHasGPS;
    protected Double startElevation;

    private void newNmea(String[] nmea) {

        // System.out.println("newNMEA:"+Arrays.asList(nmea));
        nmea[0] = nmea[0].toUpperCase();
        try {
            switch (nmea[0]) {
            case "$GPGGA":
                // System.out.println("newGGA:"+Arrays.asList(nmea));
                nmea[0] = "GPGGA"; // WWJ par

                NmeaGGAPoint point = new NmeaGGAPoint(nmea);
                nmeaHasGPS = (point.getLatitude() != 0 && point.getLongitude() != 0);
                // System.out.println(point);
                // the build in toPosition() is ignoring geoid seperation
                nmeaLat = point.getLatitude();
                nmeaLon = point.getLongitude();
                nmeaAlt = point.altitude;
                nmeaGeoidHeight = point.geoidHeight;
                // System.out.println(ownPosWGS84);
                PositionData p = new PositionData();
                p.gpsAltitude = (int)Math.round((nmeaAlt + nmeaGeoidHeight) * 100);
                //////////
                if (startElevation == null) {
                    // TODO compute start ground elevation somehow
                    // startElevation = EarthElevationModel.getElevation(startPosBaro);
                } else {
                    p.altitude = (int)((nmeaAlt - startElevation) * 100);
                }
                //////////
                p.lat = nmeaLat;
                p.lon = nmeaLon;
                p.flightmode = AirplaneFlightmode.ManualControl.ordinal();
                p.flightphase = AirplaneFlightphase.airborne.ordinal();
                // po.groundspeed=??
                // po.cross_track_error=??
                // po.reentrypoint=0;
                Long time = nmeaTime;
                if (time != null) {
                    p.time_sec = (int)(time / 1000);
                    p.time_usec = (int)(time - p.time_sec * 1000) * 1000;
                }

                lastPosition = p;
                isReceivePosition = true;
                dataOk = true;

                break;
            case "$GPRMC":
                nmeaTime = CNMEA.parseUtcTimestamp(nmea[1], nmea[9]);
            }

        } catch (Exception e) {
            Debug.getLog().log(Level.CONFIG, "could not parse:" + Arrays.asList(nmea), e);
        }
    }

    public boolean isNMEA() {
        return isNMEA;
    }

    public boolean isAscTec() {
        return isAscTec;
    }

    public void setAscTec(boolean isAscTec) {
        this.isAscTec = isAscTec;
    }
}
