/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model;

import com.intel.missioncontrol.mission.Uav;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;
import eu.mavinci.core.plane.AirplaneMsgType;
import eu.mavinci.core.plane.UavCommand;
import eu.mavinci.core.plane.UavCommandResult;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;
import eu.mavinci.core.plane.sendableobjects.CommandResultData;
import eu.mavinci.plane.IAirplane;

public class DroneControl {
    private static final long MISSION_START_TIMEOUT = 25000; //milliseconds
    private static final double ALTITUDE = 10.0; //meters
    IAirplane plane;
    Drone drone;
    private IAirplaneListenerDelegator delegator;

    public DroneControl(IAirplane plane, Drone drone) {
        this.plane = plane;
        this.drone = drone;
        this.delegator = this.plane.getRootHandler();
    }

    public void takeoff() {
        takeoff(true);
    }

    public void takeoff(boolean startMission) {
        State droneState = drone.getAttribute(AttributeType.STATE);
        //     if(droneState.isConnected() && droneState.isArmed() && !droneState.isFlying()){
        ControlApi.getApi(drone).takeoff(ALTITUDE, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                userInfo("Takeoff successful.");
                delegator.recv_cmd_result(new CommandResultData(UavCommand.TAKE_OFF, UavCommandResult.SUCCESS, "Takeoff Successful"));

                //setModeAuto();
            }

            @Override
            public void onError(int executionError) {
                if (droneState.isFlying()) {
                    userInfo("UAV is already in air");
                    delegator.recv_cmd_result(new CommandResultData(UavCommand.TAKE_OFF, UavCommandResult.ERROR, "UAV is already in air"));

                } else {
                    userInfo("UAV unable to take off. Please try again");
                    delegator.recv_cmd_result(new CommandResultData(UavCommand.TAKE_OFF, UavCommandResult.ERROR, "UAV unable to take off. Please try again"));

                }
            }

            @Override
            public void onTimeout() {
                userInfo("UAV take off timed out.");
                delegator.recv_cmd_result(new CommandResultData(UavCommand.TAKE_OFF, UavCommandResult.TIMEOUT, "UAV take off timed out."));

            }
        });
    }


    public void arm() {
        State droneState = drone.getAttribute(AttributeType.STATE);
        if (droneState.isConnected() && !droneState.isArmed()) {
            Drone drone = this.drone;
            VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_GUIDED, new SimpleCommandListener() {
                @Override
                public void onError(int executionError) {
                    userInfo("Unable to change mode of UAV");
                    userInfo("Error message is: " + executionError);
                }

                @Override
                public void onSuccess() {
                    userInfo("UAV mode is Guided");
                    VehicleApi.getApi(drone).arm(true, false, new SimpleCommandListener() {
                        @Override
                        public void onError(int executionError) {
                            userInfo("Unable to arm the UAV");
                            userInfo("Error message is: " + executionError);
                        }

                        @Override
                        public void onSuccess() {
                            userInfo("UAV is armed");
                        }

                        @Override
                        public void onTimeout() {
                            userInfo("UAV arming operation timed out.");
                        }
                    });

                }

                @Override
                public void onTimeout() {
                    userInfo("UAV arming operation timed out.");
                }
            });
        }
    }

    public void armAndTakeoff() {

    }

    //TODO: Works for APM only. Make it work for other drones. 08/28/2018
    public void pauseMission() {
/*
        MissionApi.getApi(this.drone).pauseMission(new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                userInfo("Pause successful");
                //setMode();
            }

            @Override
            public void onError(int executionError) {
                userInfo("Unable to pause mission");
            }

            @Override
            public void onTimeout() {
                userInfo("Mission pause timed out.");
            }
        });
        */
        VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_BRAKE, new SimpleCommandListener() {
            @Override
            public void onSuccess() {
                userInfo("Mission pause successful");
                delegator.recv_cmd_result(new CommandResultData(UavCommand.PAUSE_MISSION, UavCommandResult.SUCCESS, "UAV mission pause successful."));

            }

            @Override
            public void onError(int executionError) {
                userInfo("Mission pause error.");
                delegator.recv_cmd_result(new CommandResultData(UavCommand.PAUSE_MISSION, UavCommandResult.ERROR, "UAV mission pause error. Error code: " + executionError + ". Please try again"));

            }

            @Override
            public void onTimeout(){
                userInfo("Mission pause timed out. Please try again");
                //delegator.recv_cmd_result(new CommandResultData(UavCommand.PAUSE_MISSION, UavCommandResult.TIMEOUT, "UAV mission pause timed out. Please try again."));

            }
        });

        //setMode(VehicleMode.COPTER_BRAKE);
    }

    public void changeVehicleSpeed(int speed) {
        MissionApi.getApi(this.drone).setMissionSpeed(speed, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                userInfo("Speed change successful");

                //setMode();
            }

            @Override
            public void onError(int executionError) {
                userInfo("Error for speed reduction is: " + executionError);
                userInfo("Unable to change speed");
            }

            @Override
            public void onTimeout() {
                userInfo("Mission speed change timed out.");
            }
        });
    }

    public void resumeMission() {
        MissionApi.getApi(this.drone).startMission(true, true, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                userInfo("Mission resume successful");
                delegator.recv_cmd_result(new CommandResultData(UavCommand.RESUME_MISSION, UavCommandResult.SUCCESS, "UAV mission resume successful."));

                //setModeAuto();
            }

            @Override
            public void onError(int executionError) {
                userInfo("Resumed mission");
                delegator.recv_cmd_result(new CommandResultData(UavCommand.RESUME_MISSION, UavCommandResult.ERROR, "Error in resuming the mission. Error code: " + executionError+ ". Please try again."));

            }

            @Override
            public void onTimeout() {
                userInfo("Mission resume timed out.");
                //delegator.recv_cmd_result(new CommandResultData(UavCommand.RESUME_MISSION, UavCommandResult.TIMEOUT, "UAV mission resume timed out. Please try again."));

            }
        });
    }

    public void startMission() {

        Thread t = new Thread(new MissionStarter(this.drone));
        t.start();
    }


    public void armAndTakeoff(boolean startMission) {
        State droneState = drone.getAttribute(AttributeType.STATE);
        if (droneState.isConnected()) {
            if (!droneState.isArmed()) {
                Drone drone = this.drone;
                VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_GUIDED, new SimpleCommandListener() {
                    @Override
                    public void onError(int executionError) {
                        userInfo("Unable to change mode of UAV");
                        userInfo("Error message is: " + executionError);
                    }

                    @Override
                    public void onSuccess() {
                        userInfo("UAV mode is Guided");
                        VehicleApi.getApi(drone).arm(true, false, new SimpleCommandListener() {
                            @Override
                            public void onError(int executionError) {
                                userInfo("Unable to arm the UAV");
                                userInfo("Error message is: " + executionError);
                            }

                            @Override
                            public void onSuccess() {
                                userInfo("UAV is armed");
                                takeoff(startMission);
                            }

                            @Override
                            public void onTimeout() {
                                userInfo("UAV arming operation timed out.");
                            }
                        });

                    }

                    @Override
                    public void onTimeout() {
                        userInfo("UAV arming operation timed out.");
                    }
                });
            } else { // is armed
                takeoff(startMission);
            }
        }
    }

    public void sendMission() {

    }

    public void returnToLaunch() {

    }

    public void setHomePosition() {

    }

    public void goToWaypoint(int waypointIndex) {
        State droneState = drone.getAttribute(AttributeType.STATE);
        if (droneState.isConnected() && droneState.isArmed() && droneState.isFlying()) {
            MissionApi.getApi(this.drone).gotoWaypoint(waypointIndex, new AbstractCommandListener() {
                @Override
                public void onSuccess() {
                    userInfo("Flying to waypoint index: " + waypointIndex);
                    //setModeAuto();
                }

                @Override
                public void onError(int executionError) {
                    userInfo("Error executing go to waypoint command");
                }

                @Override
                public void onTimeout() {
                    userInfo("Go to waypoint timed out.");
                }
            });
        }
    }

    public void setModeAuto() {
        setMode(VehicleMode.COPTER_AUTO);
    }

    public void setModeGuided() {
        setMode(VehicleMode.COPTER_GUIDED);
    }

    public void setModeRTL() {
        setMode(VehicleMode.COPTER_RTL);
    }

    public void setModeLand() {
        setMode(VehicleMode.COPTER_LAND);
    }

    private void setMode(VehicleMode vehicleMode) {
        UavCommand command = getCommand(vehicleMode);
        VehicleApi.getApi(this.drone).setVehicleMode(vehicleMode, new SimpleCommandListener() {
            @Override
            public void onSuccess() {
                userInfo("Mode change successful");
                delegator.recv_cmd_result(new CommandResultData(command, UavCommandResult.SUCCESS, command.getDisplayName() + " successful"));
                //super.onSuccess();
            }

            @Override
            public void onError(int executionError) {
                userInfo("Mode change error.");
                delegator.recv_cmd_result(new CommandResultData(command, UavCommandResult.ERROR, "Error: " + command.getDisplayName() + "; Error code: " + executionError+ ". Please try again."));
                //super.onError(executionError);
            }

            @Override
            public void onTimeout(){
                userInfo("Mode change timed out. Please try again");
            }
        });
    }

    private UavCommand getCommand(VehicleMode vehicleMode){
        UavCommand command = null;
        if(vehicleMode != null){
            switch (vehicleMode) {
                case COPTER_BRAKE:
                    return UavCommand.PAUSE_MISSION;
                case COPTER_RTL:
                    return UavCommand.RETURN_TO_LAUNCH;
                case COPTER_AUTO:       //This can be used both for start and resume. Using it for resume. For start it is called separately.
                    return UavCommand.RESUME_MISSION;
                case COPTER_LAND:
                    return UavCommand.UAV_LAND;
                default:
                    return UavCommand.OTHER;
            }
        }
        return command;
    }

    private void userInfo(String info) {
        System.out.println("Drone event message: " + info);
    }

    private void cmdResult(String message, int logLevel) {
        plane.getRootHandler().recv_msg(logLevel, message);
    }

    private class MissionStarter implements Runnable {
        private Boolean done = false;
        private Drone drone;

        public MissionStarter(Drone drone) {
            this.drone = drone;
        }
        // We're not sure when we want to start the mission if the drone has finished receiving the mission yet; so
        // we keep trying to get the mission started for MISSION_START_TIMEOUT seconds.

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();         // Remember when we start
            int counter = 0;
            UavCommand command = UavCommand.RUN_MISSION;
            //System.out.println("Starting mode: " + ((State)drone.getAttribute(AttributeType.STATE)).getVehicleMode().getMode());
            while (!done &&
                    (System.currentTimeMillis() < startTime + MISSION_START_TIMEOUT) &&
                    ((State) drone.getAttribute(AttributeType.STATE)).getVehicleMode().getMode() != VehicleMode.COPTER_AUTO.getMode()) {
                try {
                    //System.out.println("Sending mission start at t + " + (System.currentTimeMillis()- startTime)/1000 + " sec;  done = " + done);
                    if(counter %2 == 0)
                        cmdResult("Almost ready to run plan. Hang on...", AirplaneMsgType.INFORMATION.getSeverityLevel());
                    MissionApi.getApi(drone).startMission(true, true, new AbstractCommandListener() {
                        @Override
                        public void onSuccess() {
                            userInfo("Mission start successful");
                            done = true;
                            System.out.println(System.currentTimeMillis());
                        }

                        @Override
                        public void onError(int executionError) {
                            userInfo("Mission start returned error." + executionError);
                        }

                        @Override
                        public void onTimeout() {
                            userInfo("Mission start timed out.");
                        }
                    });

                    Thread.sleep(2000);
                    counter++;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            if (((State) drone.getAttribute(AttributeType.STATE)).getVehicleMode() == VehicleMode.COPTER_AUTO) {
                delegator.recv_cmd_result(new CommandResultData(command, UavCommandResult.SUCCESS, command
                        .getDisplayName() + " successful"));
//                cmdResult("Run plan started...", AirplaneMsgType.INFORMATION.getSeverityLevel());
            } else {
                delegator.recv_cmd_result(new CommandResultData(command, UavCommandResult.ERROR, "Error: " + command.getDisplayName()));
                cmdResult("Could not run plan. Please retry...", AirplaneMsgType.INFORMATION.getSeverityLevel());
            }
        }
    }
}