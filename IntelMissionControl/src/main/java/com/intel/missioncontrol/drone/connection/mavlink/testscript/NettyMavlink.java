/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink.testscript;

import com.intel.missioncontrol.drone.connection.TcpIpTransportType;
import com.intel.missioncontrol.drone.connection.mavlink.CameraProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.CommandProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolReceiver;
import com.intel.missioncontrol.drone.connection.mavlink.ConnectionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.ExtendedParameter;
import com.intel.missioncontrol.drone.connection.mavlink.GrayhawkMissionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.IMavlinkParameter;
import com.intel.missioncontrol.drone.connection.mavlink.InvalidParamTypeException;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkEndpoint;
import com.intel.missioncontrol.drone.connection.mavlink.MavlinkHandler;
import com.intel.missioncontrol.drone.connection.mavlink.MissionProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.Parameter;
import com.intel.missioncontrol.drone.connection.mavlink.ParameterProtocolSender;
import com.intel.missioncontrol.drone.connection.mavlink.StatusTextException;
import com.intel.missioncontrol.drone.connection.mavlink.TcpClient;
import com.intel.missioncontrol.drone.connection.mavlink.TelemetryReceiver;
import com.intel.missioncontrol.drone.connection.mavlink.UdpBroadcastListener;
import io.dronefleet.mavlink.common.AutopilotVersion;
import io.dronefleet.mavlink.common.FlightInformation;
import io.dronefleet.mavlink.common.GlobalPositionInt;
import io.dronefleet.mavlink.common.MavMissionType;
import io.dronefleet.mavlink.common.MavModeFlag;
import io.dronefleet.mavlink.util.EnumValue;
import io.netty.util.internal.SocketUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Futures;

public class NettyMavlink {

    private static final MavlinkEndpoint targetEndpoint =
        // new MavlinkEndpoint(TcpIpTransportType.UDP, SocketUtils.socketAddress("127.0.0.1", 14570), 1, 1);
        // new MavlinkEndpoint(TcpIpTransportType.UDP, SocketUtils.socketAddress("10.62.220.150", 14570), 1, 1);
        // new MavlinkEndpoint(TcpIpTransportType.TCP, SocketUtils.socketAddress("10.62.220.150", 5760), 1, 1);
        // new MavlinkEndpoint(TcpIpTransportType.UDP, SocketUtils.socketAddress("127.0.0.1", 56630), 1, 100);
        new MavlinkEndpoint(TcpIpTransportType.UDP, SocketUtils.socketAddress("192.168.200.254", 14551), 1, 1);

    public static void main(String[] args) throws InterruptedException {
        float altitude = 20;

        CancellationSource cancellationSource = new CancellationSource();

        MavlinkHandler handler;
        try {
            if (targetEndpoint.getTcpIpTransportType().equals(TcpIpTransportType.TCP)) {
                System.out.println("Connecting to " + targetEndpoint);
                Duration tcpConnectTimeout = Duration.ofSeconds(5);
                TcpClient tcpClient = new TcpClient();
                tcpClient.connectAsync(targetEndpoint.getAddress(), tcpConnectTimeout, cancellationSource).get();
                handler = tcpClient.getHandler();
                System.out.println("Connected");
            } else {
                UdpBroadcastListener listener = new UdpBroadcastListener();
                listener.bindAsync(14550, cancellationSource).get();
                handler = listener.getHandler();
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        CommandProtocolSender commandProtocolSender =
            new CommandProtocolSender(targetEndpoint, handler, cancellationSource);
        ParameterProtocolSender parameterProtocolSender =
            new ParameterProtocolSender(targetEndpoint, handler, cancellationSource);
        ConnectionProtocolReceiver connectionProtocolReceiver =
            new ConnectionProtocolReceiver(targetEndpoint, handler, cancellationSource);
        ConnectionProtocolSender connectionProtocolSender =
            new ConnectionProtocolSender(targetEndpoint, handler, cancellationSource);
        MissionProtocolSender missionProtocolSender =
            // new MissionProtocolSender(targetEndpoint, handler, cancellationSource);
            new GrayhawkMissionProtocolSender(targetEndpoint, handler, cancellationSource);
        TelemetryReceiver telemetryReceiver = new TelemetryReceiver(targetEndpoint, handler, cancellationSource);
        //        CameraProtocolSender cameraProtocolSender =
        //            new CameraProtocolSender(targetEndpoint, handler, cancellationSource);

        System.out.println("start");

        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.runLater(
            () -> {
                try {
                    // testTakeoffAndRequestFlightInformation(altitude, commandProtocolSender, telemetryReceiver);
                    // testRequestFlightInformation(commandProtocolSender);
                    // testParallelRequestFlightInformation(commandProtocolSender);
                    // testRequestVideoStreamInformation(cameraProtocolSender);
                    testRequestAndSendMissionItems(missionProtocolSender);
                    // testRequestAndSendMissionItemsAndCancel(missionProtocolSender);
                    // testGetAndSetMessageInterval(commandProtocolSender);
                    // testTakeOffAndLanding(altitude, commandProtocolSender);
                    // testParameters(parameterProtocolSender);
                    // testGetAndSetParameter(parameterProtocolSender);
                    // testExtParameters(parameterProtocolSender);
                    // testSetUnknownParameter(parameterProtocolSender);
                    // testReceiveHeartbeat(connectionProtocolReceiver);

                    //                    var cs = new CancellationSource();
                    //                    ConnectionProtocolReceiver r =
                    //                            new ConnectionProtocolReceiver(targetEndpoint, handler, cs);
                    //                    testReceiveHeartbeatOnce(r, cs);

                    // testTelemetryReceiver(telemetryReceiver);
                    // test();
                    // testRequestAutopilotVersion(commandProtocolSender);
                } catch (Exception e) {
                    System.out.println("Synchronous call exception: " + e.getMessage());
                }
            },
            Duration.ofSeconds(2));

        connectionProtocolSender
            .startSendingHeartbeatsAsync()
            .whenFailed(
                e -> {
                    System.out.println("Send heartbeat error: " + e.getMessage());
                    e.printStackTrace();
                });

        while (true) {
            Thread.sleep(100);
        }
    }

    private static void testRequestAndSendMissionItems(MissionProtocolSender sender) {
        sender.requestMissionItemsAsync(MavMissionType.MAV_MISSION_TYPE_MISSION)
            .whenSucceeded(
                list -> {
                    System.out.println("requestMissionItemsAsync: received Mission items: " + list.size());
                })
            .whenFailed(
                e -> {
                    System.out.println("requestMissionItemsAsync error: " + e.getMessage());
                    e.printStackTrace();
                })
            .thenApplyAsync(x -> Dispatcher.background().getLaterAsync(() -> x, Duration.ofMillis(3000)))
            .thenAcceptAsync(
                // just send the same:
                list -> sender.sendMissionItemsAsync(MavMissionType.MAV_MISSION_TYPE_MISSION, list))
            .whenSucceeded(v -> System.out.println("sendMissionItemsAsync success"))
            .whenFailed(
                e -> {
                    System.out.println("sendMissionItemsAsync error: " + e.getMessage());
                    e.printStackTrace();
                });
    }

    private static void testRequestAndSendMissionItemsAndCancel(MissionProtocolSender sender) {
        var f =
            sender.requestMissionItemsAsync(MavMissionType.MAV_MISSION_TYPE_MISSION)
                .whenSucceeded(
                    list -> {
                        System.out.println("requestMissionItemsAsync: received Mission items: " + list.size());
                    })
                .whenFailed(
                    e -> {
                        System.out.println("requestMissionItemsAsync error: " + e.getMessage());
                        e.printStackTrace();
                    })
                .thenApplyAsync(x -> Dispatcher.background().getLaterAsync(() -> x, Duration.ofMillis(100)))
                .thenAcceptAsync(
                    list -> {
                        // just send the same:
                        return sender.sendMissionItemsAsync(MavMissionType.MAV_MISSION_TYPE_MISSION, list);
                    })
                .whenSucceeded(
                    v -> System.out.println("error: sendMissionItemsAsync succeeded, but should have been cancelled"))
                .whenFailed(
                    e -> {
                        System.out.println(
                            "sendMissionItemsAsync error (should have been cancelled): " + e.getMessage());
                        e.printStackTrace();
                    })
                .whenCancelled(() -> System.out.println("sendMissionItemsAsync successfully cancelled"));

        Dispatcher.background()
            .runLater(f::cancel, Duration.ofMillis(1500)); // adjust timeout to cancel while transfer is in progress
    }

    private static void testTelemetryReceiver(TelemetryReceiver receiver) {
        receiver.registerTelemetryCallbackAsync(
                GlobalPositionInt.class,
                receivedPayload ->
                    System.out.println("received GlobalPositionInt: " + receivedPayload.getPayload().toString()),
                Duration.ofMillis(250),
                () -> {
                    System.out.println("GlobalPositionInt timeout");
                })
            .whenSucceeded(v -> System.out.println("Receiver finished"))
            .whenFailed(e -> System.out.println("receiver error: " + e.getMessage()));
    }

    private static void testGetMessageInterval(CommandProtocolSender sender) {
        sender.getMessageIntervalAsync(GlobalPositionInt.class)
            .whenSucceeded(
                interval -> {
                    System.out.println("get GlobalPositionInt message interval: " + interval.toString());
                })
            .whenFailed(e -> System.out.println("get GlobalPositionInt message interval error: " + e.getMessage()));
    }

    private static void testRequestAutopilotVersion(CommandProtocolSender sender) {
        sender.requestAutopilotCapabilitiesAsync()
            .whenSucceeded(
                apVersion -> {
                    System.out.println("requestAutopilotVersion success: " + apVersion);
                })
            .whenFailed(e -> System.out.println("requestAutopilotVersion failure" + e.getMessage()));
    }

    private static void testGetAndSetMessageInterval(CommandProtocolSender sender) {
        var messageClass = AutopilotVersion.class;
        sender.getMessageIntervalAsync(messageClass)
            .whenSucceeded(
                interval -> {
                    System.out.println("get message interval: " + interval.toString());
                })
            .whenFailed(e -> System.out.println("get message interval error: " + e.getMessage()))
            .thenGetAsync(
                () -> {
                    Duration interval = Duration.ofMillis(500);
                    return sender.setMessageIntervalAsync(messageClass, interval)
                        .whenSucceeded(vv -> System.out.println("set message interval success: " + interval.toString()))
                        .whenFailed(e -> System.out.println("set message interval error: " + e.getMessage()));
                })
            .thenGetAsync(
                () ->
                    sender.getMessageIntervalAsync(messageClass)
                        .whenSucceeded(interval -> System.out.println("get message interval: " + interval.toString()))
                        .whenFailed(e -> System.out.println("get message interval error: " + e.getMessage())));
    }

    private static void testReceiveHeartbeat(ConnectionProtocolReceiver receiver) {
        receiver.registerHeartbeatHandlerAsync(
                receivedPayload -> System.out.println("received heartbeat: " + receivedPayload.getPayload().toString()),
                () -> System.out.println("heartbeat timeout"))
            .whenSucceeded(v -> System.out.println("Receiver finished"))
            .whenFailed(e -> System.out.println("receiver error: " + e.getMessage()));
    }

    private static void testReceiveHeartbeatOnce(ConnectionProtocolReceiver receiver, CancellationSource cs) {
        receiver.registerHeartbeatHandlerAsync(
                receivedPayload -> {
                    System.out.println("received heartbeat: " + receivedPayload.getPayload().toString());
                    cs.cancel();
                },
                () -> System.out.println("heartbeat timeout"))
            .whenSucceeded(v -> System.out.println("Receiver finished"))
            .whenFailed(e -> System.out.println("receiver error: " + e.getMessage()))
            .whenCancelled(() -> System.out.println("Receiver cancelled"));
    }

    private static void testGetAndSetParameter(ParameterProtocolSender sender) {
        Parameter paramToSet = Parameter.createInt32("COM_TAKEOFF_ACT", 1);
        sender.requestParamByIdAsync(paramToSet.getId())
            .whenSucceeded(parameter -> System.out.println("requestParamByIdAsync: " + parameter.toString()))
            .whenFailed(e -> System.out.println("requestParamByIdAsync error: " + e.getMessage()))
            .thenGetAsync(
                () -> {
                    return sender.setParamAsync(paramToSet)
                        .whenSucceeded(vv -> System.out.println("setParamAsync success: " + paramToSet))
                        .whenFailed(e -> System.out.println("setParamAsync error: " + e.getMessage()));
                })
            .thenGetAsync(
                () ->
                    sender.requestParamByIdAsync(paramToSet.getId())
                        .whenSucceeded(
                            parameter -> {
                                System.out.println("requestParamByIdAsync success: " + parameter.toString());
                            })
                        .whenFailed(e -> System.out.println("requestParamByIdAsync error: " + e.getMessage())));
    }

    private static void testSetParameterType(ParameterProtocolSender sender) {
        // use wrong target type to test exceptions:
        sender.setParamAsync(Parameter.createInt8("RTL_LAND_DELAY", 1))
            .whenSucceeded(vvv -> System.out.println("setParam succeeded, but should have thrown"))
            .whenFailed(
                e -> {
                    if (e.getCause() instanceof InvalidParamTypeException) {
                        System.out.println("success: setParam threw InvalidParamTypeException as expected.");
                    } else {
                        System.out.println("setParam error : " + e.getMessage());
                    }
                })
            .thenGetAsync(
                () ->
                    sender.setParamAsync(Parameter.createFloat("RTL_LAND_DELAY", 0))
                        .whenSucceeded(vvv -> System.out.println("setParam success"))
                        .whenFailed(e -> System.out.println("setParam Error: " + e.getMessage())));
    }

    private static void testSetUnknownParameter(ParameterProtocolSender sender) {
        // use unknown parameter to test exceptions:
        sender.setParamAsync(Parameter.createInt32("INVALID_SFDJSGHF", 1))
            .whenSucceeded(vvv -> System.out.println("setParam succeeded, but should have thrown"))
            .whenFailed(
                e -> {
                    if (e.getCause() instanceof StatusTextException) {
                        System.out.println(
                            "success: setParam threw StatusTextException as expected: " + e.getCause().getMessage());
                    } else {
                        System.out.println("setParam error : " + e.getCause().getMessage());
                    }
                });
    }

    private static void testParameters(ParameterProtocolSender sender) {
        sender.requestParamByIdAsync("COM_RC_LOSS_T")
            .whenSucceeded(
                param -> {
                    System.out.println("requestParamById success: " + param.toString());
                })
            .whenFailed(e -> System.out.println("requestParamById Error: " + e.getMessage()))
            .thenGetAsync(
                () ->
                    sender.requestParamsListAsync()
                        .whenSucceeded(
                            paramList -> {
                                System.out.println("requestParamsList success:");
                                for (var p : paramList) {
                                    System.out.println(p.toString());
                                }
                            })
                        .whenFailed(e -> System.out.println("requestParamsList Error: " + e.getMessage())));
    }

    private static void testSetExtParameters(ParameterProtocolSender sender) {
        ArrayList<IMavlinkParameter> list = new ArrayList<>();
        list.add(ExtendedParameter.createUInt32("TEST", 12345));
        list.add(ExtendedParameter.createFloat("CAM_GAIN", 9.2f));
        list.add(ExtendedParameter.createInt32("CAM_EXPTIME", 12345));
        list.add(ExtendedParameter.createUInt32("CAM_METERING", 1));
        list.add(ExtendedParameter.createFloat("CAM_EVCOMP", -1.3f)); // 1.0f));

        sender.setParamsAsync(list)
            .whenSucceeded(param -> System.out.println("setExtParamAsync success"))
            .whenFailed(e -> System.out.println("setExtParamAsync Error: " + e.getMessage()));
    }

    private static void testTakeOffAndLanding(float altitude, CommandProtocolSender sender) {
        System.out.println("Send takeoff command");

        sender.sendTakeOffAsync(altitude)
            .thenRunAsync(() -> sender.sendArmDisarmAsync(true))
            .thenRunAsync(() -> Dispatcher.background().runLaterAsync(() -> {}, Duration.ofSeconds(10)))
            .thenRunAsync(
                () -> {
                    System.out.println("Send land command");
                    return sender.sendSetModeAsync(
                        EnumValue.create(
                                MavModeFlag.MAV_MODE_FLAG_SAFETY_ARMED,
                                MavModeFlag.MAV_MODE_FLAG_STABILIZE_ENABLED,
                                MavModeFlag.MAV_MODE_FLAG_GUIDED_ENABLED,
                                MavModeFlag.MAV_MODE_FLAG_AUTO_ENABLED,
                                MavModeFlag.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED)
                            .value(),
                        4, // PX4CustomMode.MainMode.PX4_CUSTOM_MAIN_MODE_AUTO.getValue(),
                        6); // PX4CustomMode.SubModeAuto.PX4_CUSTOM_SUB_MODE_AUTO_LAND.getValue());
                })
            .whenSucceeded(vv -> System.out.println("testTakeOffAndLanding success"))
            .whenFailed(e -> System.out.println("testTakeOffAndLanding error: " + e.getMessage()));
    }

    private static void testRequestFlightInformation(CommandProtocolSender sender) {
        System.out.println("Request Flight Information");

        sender.requestFlightInformationAsync()
            .whenSucceeded(
                fi -> {
                    System.out.println("testRequestFlightInformation success: " + fi);
                })
            .whenFailed(e -> System.out.println("testRequestFlightInformation error: " + e.getMessage()));
    }

    private static void testTakeoffAndRequestFlightInformation(
            float altitude, CommandProtocolSender sender, TelemetryReceiver telemetryReceiver) {
        System.out.println("Send takeoff command");

        Dispatcher.background()
            .runLaterAsync(sender::requestFlightInformationAsync, Duration.ofSeconds(0), Duration.ofMillis(100));

        telemetryReceiver.registerTelemetryCallbackAsync(
            FlightInformation.class,
            p -> {
                FlightInformation flightInformation = p.getPayload();
                System.out.println(flightInformation);
            },
            Duration.ofSeconds(5),
            () -> {
                System.out.println("receiver timeout");
            });

        sender.sendTakeOffAsync(altitude)
            .thenGetAsync(() -> sender.sendArmDisarmAsync(true))
            .thenRunAsync(() -> Dispatcher.background().runLaterAsync(() -> {}, Duration.ofSeconds(10)))
            .thenRunAsync(
                () -> {
                    System.out.println("Send land command");
                    return sender.sendSetModeAsync(
                        EnumValue.create(
                                MavModeFlag.MAV_MODE_FLAG_SAFETY_ARMED,
                                MavModeFlag.MAV_MODE_FLAG_STABILIZE_ENABLED,
                                MavModeFlag.MAV_MODE_FLAG_GUIDED_ENABLED,
                                MavModeFlag.MAV_MODE_FLAG_AUTO_ENABLED,
                                MavModeFlag.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED)
                            .value(),
                        4, // PX4CustomMode.MainMode.PX4_CUSTOM_MAIN_MODE_AUTO.getValue(),
                        6); // PX4CustomMode.SubModeAuto.PX4_CUSTOM_SUB_MODE_AUTO_LAND.getValue());
                })
            .whenSucceeded(vv -> System.out.println("testTakeOffAndLanding success"))
            .whenFailed(e -> System.out.println("testTakeOffAndLanding error: " + e.getMessage()));
    }

    private static void test() {
        Futures.failed(new RuntimeException("test1"))
            .thenGetAsync(
                () ->
                    Dispatcher.background()
                        .runLaterAsync(
                            () -> {
                                System.out.println("test2");
                            },
                            Duration.ofSeconds(1)));
    }

    private static void testRequestVideoStreamInformation(CameraProtocolSender sender) {
        System.out.println("Request Video stream Information");

        sender.requestVideoStreamInformationAsync()
            .whenSucceeded(vi -> System.out.println("testRequestVideoStreamInformation success: " + vi))
            .whenFailed(e -> System.out.println("testRequestVideoStreamInformation error: " + e.getMessage()));
    }
}
