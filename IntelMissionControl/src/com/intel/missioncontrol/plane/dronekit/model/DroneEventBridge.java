/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model;

import static eu.mavinci.core.plane.AirplaneMsgType.ALERT;
import static eu.mavinci.core.plane.AirplaneMsgType.CRITICAL;
import static eu.mavinci.core.plane.AirplaneMsgType.DEBUG;
import static eu.mavinci.core.plane.AirplaneMsgType.EMERGENCY;
import static eu.mavinci.core.plane.AirplaneMsgType.ERROR;
import static eu.mavinci.core.plane.AirplaneMsgType.INFORMATION;
import static eu.mavinci.core.plane.AirplaneMsgType.NOTICE;
import static eu.mavinci.core.plane.AirplaneMsgType.WARNING;

import android.os.Bundle;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_home_position;
import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_mission_count;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_mission_item_int;
import com.MAVLink.common.msg_mission_item_reached;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.common.msg_wind_cov;
import com.MAVLink.enums.MAV_MISSION_RESULT;
import com.MAVLink.enums.MAV_SEVERITY;
import com.intel.missioncontrol.plane.dronekit.ConnectionManager;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.MavlinkObserver;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneMsgType;
import eu.mavinci.core.plane.UavCommand;
import eu.mavinci.core.plane.UavCommandResult;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;
import eu.mavinci.core.plane.sendableobjects.CommandResultData;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.plane.AirplaneCache;

final class DroneEventBridge {
    private IAirplaneListenerDelegator airplaneBroadcaster;
    private Drone drone;
    private AirplaneCache airplaneCache;
    private int currentMissionWaypoint = -1;
    private String previousStatusText = "";
//    private BufferedWriter bufferedWriter = null;
//    private PrintWriter printWriter = null;

    // we've recieved parameters and ar fully ready
    private boolean fullyConnected = false;
    private boolean startPositionSet = false;
    private int count = 0;

    public void setup(Drone drone, IAirplaneListenerDelegator rootHandler, AirplaneCache airplaneCache) {
        this.drone = drone;
        drone.addMavlinkObserver(new MavlinkObserver() {
            @Override
            public void onMavlinkMessageReceived(MavlinkMessageWrapper mavlinkMessageWrapper) {
                onMavLinkMessage(mavlinkMessageWrapper.getMavLinkMessage());
            }
        });

        drone.registerDroneListener(new DroneListener() {
            @Override
            public void onDroneEvent(String event, Bundle bundle) {
                onDroneKitEvent(event, bundle);
            }

            @Override
            public void onDroneServiceInterrupted(String s) {

            }
        });
        this.airplaneBroadcaster = rootHandler;
        this.airplaneCache = airplaneCache;

        fullyConnected = false;
        airplaneBroadcaster.connectionStateChange(AirplaneConnectorState.connectingDevice);
    }

    private void onMavLinkMessage(MAVLinkMessage msg) {
        switch (msg.msgid) {
            case msg_wind_cov.MAVLINK_MSG_ID_WIND_COV: {
                msg_wind_cov wind_cov = (msg_wind_cov) msg;
                // do something with wind
            }

            case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:
                //System.out.println("Got Status text message in DroneEventBridge");
                break;

            case msg_mission_item_reached.MAVLINK_MSG_ID_MISSION_ITEM_REACHED:
                msg_mission_item_reached msg_mission_item_reached = (msg_mission_item_reached) msg;
                processMessage(msg_mission_item_reached);
                break;

            case msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK: {
                msg_mission_ack msgMissionAck = (msg_mission_ack) msg;
                String information;
                UavCommandResult result;
                switch (msgMissionAck.type) {
                    case MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED:
                        result = UavCommandResult.SUCCESS;
                        information = "Mission accepted by the drone";
                        System.out.println(information);
                        break;
                    case MAV_MISSION_RESULT.MAV_MISSION_ERROR:
                        result = UavCommandResult.ERROR;
                        information = "Error loading the mission";
                        System.out.println(information);
                        break;
                    case MAV_MISSION_RESULT.MAV_MISSION_INVALID:
                        result = UavCommandResult.INVALID;
                        information = "Mission is invalid. Send failed";
                        System.out.println(information);
                        break;
                    case MAV_MISSION_RESULT.MAV_MISSION_DENIED:
                        result = UavCommandResult.DENIED;
                        information = "Mission is denied. Not accepting any mission commands from this UAV ";
                        System.out.println(information);
                        break;

                    default:
                        result = UavCommandResult.OTHER;
                        information = "Other send mission result ack";
                        System.out.println(information);
                        break;
                }
                airplaneBroadcaster.recv_cmd_result(new CommandResultData(UavCommand.SEND_MISSION, result, information));
            }
                break;
            case msg_home_position.MAVLINK_MSG_ID_HOME_POSITION:
                msg_home_position homePosition = (msg_home_position) msg;
                double lat = homePosition.latitude * 10e-8;
                double lon = homePosition.longitude * 10e-8;
                double alt = homePosition.altitude * 10e-2;
                Home home = drone.getAttribute(AttributeType.HOME);
                home.setCoordinate(new LatLongAlt(lat, lon, alt));
                if (!startPositionSet) {
                    airplaneCache.recv_startPos(lon, lat, 0);
                    airplaneCache.recv_startPos(lon, lat, 1);
                    startPositionSet = true;
                    System.out.println("Start position sent.");
                }
                break;
/*            case msg_mission_item_int.MAVLINK_MSG_ID_MISSION_ITEM_INT: {
                msg_mission_item_int msgMissionItemInt = (msg_mission_item_int)msg;
                UavCommandResult result = UavCommandResult.SUCCESS;
                Integer sequence = msgMissionItemInt.seq;
                airplaneBroadcaster.recv_cmd_result(new CommandResultData(UavCommand.MISSION_REQUEST_ITEM, result, sequence.toString()));
            }
                break;
            case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM: {
                msg_mission_item msgMissionItem = (msg_mission_item)msg;
                UavCommandResult result = UavCommandResult.SUCCESS;
                Integer sequence = msgMissionItem.seq;
                airplaneBroadcaster.recv_cmd_result(new CommandResultData(UavCommand.MISSION_REQUEST_ITEM, result, sequence.toString()));
            }
                break;
            case msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT: {
                msg_mission_count msgMissionCount = (msg_mission_count) msg;
                Integer missionCount = msgMissionCount.count;
                UavCommandResult result = UavCommandResult.SUCCESS;
                String information = missionCount.toString();
                airplaneBroadcaster.recv_cmd_result(new CommandResultData(UavCommand.MISSION_REQUEST_LIST, result, information));
            }
                break;
                */
            default:
                break;
        }
    }

    private void onDroneKitEvent(String event, Bundle bundle) {
        //System.out.println("event---" + event);
        //writeEventsToLog(event.toString());
        switch (event) {
            case AttributeEvent.GPS_FIX:
            case AttributeEvent.GPS_COUNT:
            case AttributeEvent.BATTERY_UPDATED:
                fullyConnected();
                onBatteryUpdated();
                break;
            case AttributeEvent.AUTOPILOT_MESSAGE:
                onAutopilotMessage(bundle);
                break;
            case AttributeEvent.MISSION_ITEM_REACHED:
                break;
            case AttributeEvent.MISSION_SENT:
                Mission mission = drone.getAttribute(AttributeType.MISSION);
                System.out.println("Mission sent");
            case AttributeEvent.ALTITUDE_UPDATED:
            case AttributeEvent.GPS_POSITION:
                onPositionUpdate();
                break;
            case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                fullyConnected();
            case AttributeEvent.HOME_UPDATED: {
/*                if (!startPositionSet) {
                    sendHomeInfo();
                    startPositionSet = true;
                }*/
            }
            break;
//            case AttributeEvent.STATE_DISCONNECTED:
//                closeLogfile();
//                break;
//            case AttributeEvent.STATE_CONNECTED:
//                initMavlinkLogFile();
//                break;
//            case AttributeEvent.STATE_CONNECTING:
//                break;

        }
    }

    private void processMessage(msg_mission_item_reached msg) {

        System.out.println("Misison item reached is: " + msg.seq);
        this.currentMissionWaypoint = msg.seq;
    }

    private void sendHomeInfo() {
        Home home = drone.getAttribute(AttributeType.HOME);
        double homeLat = home.getCoordinate().getLatitude();
        double homeLon = home.getCoordinate().getLongitude();

        airplaneCache.recv_startPos(homeLon, homeLat, 0);
        airplaneCache.recv_startPos(homeLon, homeLat, 1);
    }

    /**
     * maps MAVLink Common GPS_FIX_TYPE (https://mavlink.io/en/messages/common.html#GPS_FIX_TYPE)
     * to {@link eu.mavinci.core.flightplan.GPSFixType}
     *
     * @param mavlinkGPS
     * @return int
     */
    private int getGPSFixType(int mavlinkGPS) {
        switch (mavlinkGPS) {
            case 0:
                return 9; // GPS_FIX_TYPE_NO_GPS <-> unknown
            case 1:
                return 0; // GPS_FIX_TYPE_NO_FIX <-> noFix
            case 2:
            case 3:
                return 1; // GPS_FIX_TYPE_2D_FIX/GPS_FIX_TYPE_3D_FIX <-> gpsFix
            case 4:
                return 2; // GPS_FIX_TYPE_DGPS <-> dgps
            case 5:
                return 4; // GPS_FIX_TYPE_RTK_FLOAT <-> rtkFloatingBL
            case 6:
                return 5; // GPS_FIX_TYPE_RTK_FIXED <-> rtkFixedBL
            case 7:
                return 10; // GPS_FIX_TYPE_STATIC (MAVLink specific)
            case 8:
                return 11; // GPS_FIX_TYPE_PPP (MAVLink specific)
            default:
                return 0;

        }
    }

    /**
     * maps MAVLink APM submode (https://mavlink.io/en/messages/ardupilotmega.html#COPTER_MODE)
     * to {@link eu.mavinci.core.plane.AirplaneFlightmode}
     *
     * @param flightMode
     * @return int
     */
    private int getFlightMode(int flightMode) {
        switch (flightMode) {
            case 3: // COPTER_AUTO
                return 1; // AirplaneFlightmode.AutomaticFlight
            case 17: // COPTER_BRAKE
                return 7; //AirplaneFlightmode.Brake
            default:
                return flightMode; // 4 - VehicleMode.COPTER_GUIDED; 6 -  VehicleMode.COPTER_RTL; 9 - VehicleMode.COPTER_LAND, 17 - VehicleMode.COPTER_BRAKE ;

        }
    }

    private void onPositionUpdate() {

        PositionOrientationData po = new PositionOrientationData();

        Gps gps = drone.getAttribute(AttributeType.GPS);
        LatLong position = gps.getPosition();
        po.lat = position.getLatitude();
        po.lon = position.getLongitude();

        Altitude altitude = drone.getAttribute(AttributeType.ALTITUDE);
        double alt = altitude.getAltitude();
        po.altitude = (float) alt * 100;

        Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
        po.pitch = attitude.getPitch();
        po.yaw = attitude.getYaw();
        po.roll = attitude.getRoll();
        State droneState = drone.getAttribute(AttributeType.STATE);
        po.flightmode = getFlightMode(droneState.getVehicleMode().getMode());

        //TODO: Add more flight phases depending in the ack messages and other parameters of the drone.

        if (droneState.isArmed() && droneState.isFlying()) {
            po.flightphase = 2;
        } else {
            po.flightphase = 0;
        }

        long time = System.currentTimeMillis();
        po.time_sec = (int) (time / 1e3);
        po.elapsed_time = drone.getFlightTime();

        Speed speed = drone.getAttribute(AttributeType.SPEED);
        //writeEventsToLog("speed-----: " + speed);

        Battery battery = drone.getAttribute(AttributeType.BATTERY);
        float batteryVoltage = (float) battery.getBatteryVoltage();
        float remainingBatteryPercentage = (float) battery.getBatteryRemain();
        po.batteryPercent = remainingBatteryPercentage;
        po.batteryVoltage = batteryVoltage;

        drone.getAttribute(AttributeEvent.AUTOPILOT_MESSAGE);
        Mission mission = drone.getAttribute(AttributeType.MISSION);

        if (this.currentMissionWaypoint > 0) {
            po.reentrypoint = this.currentMissionWaypoint;
        }

        if ((droneState.getVehicleMode() != VehicleMode.COPTER_AUTO) && droneState.getVehicleMode() != VehicleMode.COPTER_BRAKE && currentMissionWaypoint > 0) {
            this.currentMissionWaypoint = -1;
            po.reentrypoint = -1;
        }
        airplaneBroadcaster.recv_positionOrientation(po);
        //airplaneBroadcaster.recv_startPos(po.lon, po.lon);
    }

    private void onAutopilotMessage(Bundle bundle) {
        if (bundle.keySet().size() > 0) {
            String message = bundle.getString(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE);
            int logLevel = bundle.getInt(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE_LEVEL);
            boolean skip = false;

            if(message.equals(previousStatusText)
                    && (message.toLowerCase()).contains("wp reached")) {
                skip = true;
            } else {
                skip = false;
            }

            if (message != null && message != "" && !(message.equals(previousStatusText)
                    && (message.toLowerCase()).contains("wp reached"))) { //filter same multiple way point reached messages.

                AirplaneMsgType msgLevel = null;
                switch (logLevel) {
                    case MAV_SEVERITY.MAV_SEVERITY_ALERT:
                        msgLevel = ALERT;
                        break;
                    case MAV_SEVERITY.MAV_SEVERITY_EMERGENCY:
                        msgLevel = EMERGENCY;
                        break;
                    case MAV_SEVERITY.MAV_SEVERITY_CRITICAL:
                        msgLevel = CRITICAL;
                        break;
                    case MAV_SEVERITY.MAV_SEVERITY_ERROR:
                        msgLevel = ERROR;
                        break;
                    case MAV_SEVERITY.MAV_SEVERITY_WARNING:
                        msgLevel = WARNING;
                        break;
                    case MAV_SEVERITY.MAV_SEVERITY_NOTICE:
                        msgLevel = NOTICE;
                        break;
                    case MAV_SEVERITY.MAV_SEVERITY_INFO:
                        msgLevel = INFORMATION;
                        break;
                    case MAV_SEVERITY.MAV_SEVERITY_DEBUG:
                        msgLevel = DEBUG;
                        break;
                }
                airplaneBroadcaster.recv_msg(msgLevel.getSeverityLevel(), message);
                previousStatusText = message;
                //writeEventsToLog(message);
            } else {
                System.out.println("Ignoring status text sent as message is empty or same as the previous message: " + previousStatusText);
            }
        }
    }

    private void foo() {


    }

    private void onGpsUpdate() {
        Gps gps = drone.getAttribute(AttributeType.GPS);
        int satCount = gps.getSatellitesCount();
        System.out.println("Drone satellite count is: " + satCount);
        String gpsFixStatus = gps.getFixStatus();
        System.out.println("Drone gps fix status is: " + gpsFixStatus);
        int gpsFixType = gps.getFixType();
        System.out.println("Drone gps fix type is: " + gpsFixType);

        State state = drone.getAttribute(AttributeType.STATE);
        System.out.println("Drone mode is: " + state.getVehicleMode());
        //System.out.println("Drone state is: " + state);

        Speed speed = drone.getAttribute(AttributeType.SPEED);
        System.out.println("Drone speed is: " + speed.toString());
    }

    private void onBatteryUpdated() {
        Battery battery = drone.getAttribute(AttributeType.BATTERY);
        HealthDataBuilder builder = new HealthDataBuilder();
        float batteryVoltage = (float) battery.getBatteryVoltage();
        float remainingBatteryPercentage = (float) battery.getBatteryRemain();
        builder.setValue(HealthDataField.Battery, batteryVoltage, remainingBatteryPercentage);

        Gps gps = drone.getAttribute(AttributeType.GPS);
        int satCount = gps.getSatellitesCount();
        int gpsFix = gps.getFixType();
        int gpsFixMapped = getGPSFixType(gpsFix);
        builder.setValue(HealthDataField.SatCount, satCount, satCount);
        builder.setValue(HealthDataField.GpsStatus, gpsFixMapped, gpsFixMapped);

        String batteryValues = "Battery values are: " + batteryVoltage + "; percentage:" + remainingBatteryPercentage + "; GpsFix status is: " + gpsFix + " which is mapped to IMC value: " + gpsFixMapped + "; Satellite Count is: " + satCount;
        //System.out.println(batteryValues);
        //writeEventsToLog(batteryValues);
        airplaneBroadcaster.recv_health(builder.build());
    }

    private void fullyConnected() {
        if (!fullyConnected) {
            fullyConnected = true;
            System.out.println("++++++ FULLY CONNECTED ++++++");
            airplaneBroadcaster.connectionStateChange(AirplaneConnectorState.fullyConnected);
        }
    }

    public void onConnectionStateChanged(ConnectionManager.ConnectionState newState) {
        AirplaneConnectorState state = AirplaneConnectorState.unconnected;
        switch (newState) {
            case DISCONNECTED:
                state = AirplaneConnectorState.unconnected;
                //System.out.println("DroneEventBridge: In Disconnected state. New state: " + state.name());
                startPositionSet = false;
                break;
            case CONNECTING:
                state = AirplaneConnectorState.connectingDevice;
                //System.out.println("DroneEventBridge: In Connecting state. New state: " + state.name());
                break;
            case CONNECTED:
                //System.out.println("DroneEventBridge: In Connected state. New state: ");
                // set fully connected after parameter exchange
                if (fullyConnected) {
                    System.out.println("DroneEventBridge: Fully connected.");
                    return;
                }
                state = AirplaneConnectorState.connectingDevice;
                //System.out.println("DroneEventBridge: In Connected state. Not fully connted. New state: " + state.name());

                break;
            case ERROR:
                state = AirplaneConnectorState.unconnected;
                //System.out.println("DroneEventBridge: In Error state (disconnected but user did not disconnect). New state: " + state.name());
                break;
        }
        fullyConnected = false;
        airplaneBroadcaster.connectionStateChange(state);

    }

    /**
     * See
     * eu/mavinci/desktop/rs232/asctec/ParametersCallback.java:238
     */
    enum HealthDataField {
        /**
         * main battery level in V
         */
        Battery(0),
        SatCount(1),
        /**
         * GPSFixType.toOrdinal
         */
        GpsStatus(2),

        /**
         * flight mode (integer)
         *
         * @see eu.mavinci.core.plane.AirplaneFlightmode
         */
        FlightMode(3),
        WindDirection(4),
        WindSpeed(5),
        ConnectionQuality(6);

        public final int index;

        HealthDataField(int i) {
            this.index = i;
        }
    }

    public final static class HealthDataBuilder {
        final HealthData h;

        HealthDataBuilder() {
            h = new HealthData();
        }

        HealthDataBuilder(HealthData data) {
            h = data;
        }

        HealthDataBuilder setValue(HealthDataField field, float value) {
            setValue(field, value, value);
            return this;
        }

        HealthDataBuilder setValue(HealthDataField field, float absValue, float pctValue) {
            h.absolute.setSize(3);
            h.percent.setSize(3);

            h.absolute.set(field.index, absValue);
            h.percent.set(field.index, pctValue);
            return this;
        }


        HealthData build() {
            return h;
        }
    }

//    private void writeEventsToLog(String event) {
//
//            if (bufferedWriter == null) {
//                System.out.println("Mavlinklog buffer writer is null!");
//                return;
//            }
//
//            try {
//                bufferedWriter.write(event);
//                bufferedWriter.newLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//    }
//
//    private void initMavlinkLogFile() {
//        if (printWriter == null) {
//            try {
//                printWriter = new PrintWriter("mavlinklog.txt");
//                if (bufferedWriter == null) {
//                    bufferedWriter = new BufferedWriter(printWriter);
//                    System.out.println("mavlinklog.txt is initialized-------------");
//                    bufferedWriter.write("--beginning----" + System.currentTimeMillis());
//                    bufferedWriter.newLine();
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                if (bufferedWriter != null) {
//                    try {
//                        bufferedWriter.close();
//                    } catch (IOException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//                printWriter.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//                if (bufferedWriter != null) {
//                    try {
//                        bufferedWriter.close();
//                    } catch (IOException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//                printWriter.close();
//            }
//        }
//    }
//
//    private void closeLogfile() {
//        if (bufferedWriter != null) {
//            try {
//                bufferedWriter.write("--closing file----" + System.currentTimeMillis());
//                bufferedWriter.newLine();
//                System.out.println("mavlinklog.txt closed-------------");
//                bufferedWriter.close();
//                bufferedWriter = null;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (printWriter != null) {
//            printWriter.close();
//            printWriter = null;
//        }
//    }
}
