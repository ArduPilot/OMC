package com.intel.dronekitsample.model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidCompat.DesktopHelper;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.CapabilityApi;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.*;
import com.o3dr.services.android.lib.model.AbstractCommandListener;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

public class DroneModel {
    private int droneType = Type.TYPE_UNKNOWN;

    private Context context;
    private ControlTower tower;
    public Drone drone;
    private Handler handler;

    private DroneAttributes attributes;

    private Semaphore semaphore = new Semaphore(1);

    public final ExecutorService service;

    public DroneModel() {
        this.service = Executors.newSingleThreadExecutor();
    }

    public Context getContext() {
        return context;
    }

    public void start() {
        handler = new Handler(Looper.getMainLooper());
        String property = System.getProperty("java.io.tmpdir");
        context = DesktopHelper.setup(Drone.class, new File(property));
        tower = new ControlTower(context);
        drone = new Drone(context);

        semaphore.acquireUninterruptibly();

        // after tower.connect();
        tower.registerDrone(drone, handler);

        attributes = new DroneAttributes();
        attributes.init(drone);

        drone.registerDroneListener(new DroneListener() {
            @Override
            public void onDroneEvent(String event, Bundle extras) {
                handleDroneEvent(event, extras);
            }

            @Override
            public void onDroneServiceInterrupted(String errorMsg) {
                alertUser(errorMsg);
            }
        });

    }

    public DroneAttributes getAttributes() {
        return attributes;
    }

    /*
    1. upload mission
       precondition: state connected, parameters loaded

    2. arm:
       precondition: armed

    3. takeoff,
       precondition: armed, mission uploaded

    4. execute mission,
       precondition: flying

     */

    public CompletableFuture setArm(boolean arm) {
        CommandFuture f = new CommandFuture(arm ? "arm" : "disarm");
        VehicleApi.getApi(drone).arm(arm, f.listener);
        return f;
    }

    public Future<Boolean> takeOff() {
        CommandFuture f = new CommandFuture("takeoff");
        ControlApi.getApi(this.drone).takeoff(25.0, f.listener);
        return f;
    }

    public CompletableFuture<Boolean> setMode(VehicleMode mode) {
        CommandFuture f = new CommandFuture("set mode "+mode);
        VehicleApi.getApi(drone).setVehicleMode(mode, f.listener);
        return f;
    }

    public void climbTo(double altitude) {
        ControlApi.getApi(drone).climbTo(altitude);
    }

    public static final class CommandExecutionException extends Exception {
        final int executionError;

        CommandExecutionException(String name, int executionError) {
            super("CommandExecutionException: " + name + " : errorNum="+ executionError);
            this.executionError = executionError;
        }

        public int getExecutionError() {
            return executionError;
        }
    }

    public static final class CommandFuture extends CompletableFuture<Boolean> {
        final String commandName;

        public CommandFuture(String commandName) {
            this.commandName = commandName;
        }

        final AbstractCommandListener listener = new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                System.out.println("command "+ commandName + " success");
                complete(true);
            }

            @Override
            public void onError(int executionError) {
                System.out.println("command "+ commandName + " failed: "+executionError);

                completeExceptionally(new CommandExecutionException(commandName, executionError));
            }

            @Override
            public void onTimeout() {
                System.out.println("command "+ commandName + " timed out");

                complete(false);
            }
        };
    }

//    public Future<Boolean> land() {
//        CompletableFuture<Boolean> f = new CompletableFuture<>();
//        ControlApi.getApi(this.drone).climbTo(
//    }

//    private void armDrone() {
//        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
//        if (vehicleState.isArmed()) return;
//        VehicleApi.getApi(drone).arm(true, new CommandListener("armDrone") {
//            @Override
//            public void onSuccess() {
//                super.onSuccess();
//                takeOffDrone();
//            }
//        });
//    }

//    public void setMode() {
//        VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_AUTO, new CommandListener("setMode") {
//            @Override
//            public void onSuccess() {
//                super.onSuccess();
//            }
//        });
//    }
//
//    private void takeOffDrone() {
//        ControlApi.getApi(this.drone).takeoff(20.0, new CommandListener("takeoff") {
//            @Override
//            public void onSuccess() {
//                super.onSuccess();
//                uploadMission();
//                handler.postDelayed(() -> setMode(), 10);
//            }
//        });
//    }
//
//
//    private void startMission() {
//        MissionApi.getApi(drone).startMission(true, true,
//                new CommandListener("startMission") {
//                    @Override
//                    public void onSuccess() {
//                        super.onSuccess();
//
//                    }
//                });
//    }
//

    private CommandFuture loadFuture;

    public CompletionStage<Boolean> loadMission(Mission mission) {
        loadFuture = new CommandFuture("loadMission");
        MissionApi.getApi(drone).setMission(mission, true);

        return loadFuture;
    }

    public CompletableFuture<Boolean>  pauseMission() {
        CommandFuture f = new CommandFuture("pause mission");
        MissionApi.getApi(drone).pauseMission(f.listener);
        return f;
    }

    public CompletableFuture<Boolean> startMission() {
        CommandFuture f = new CommandFuture("start mission");
        MissionApi.getApi(drone).startMission(true, true, f.listener);
        return f;
    }

    private void handleDroneEvent(String event, Bundle extras) {
        final boolean dumpEvents = false;

        if (event.contains("MISSION") && dumpEvents) {//dumpEvents) {
            System.out.println("--- drone event: "
                    + event.substring("com.o3dr.services.android.lib.attribute.event.".length())
                    + " "
                    + extras.toString().replace("com.o3dr.services.android.lib.attribute.event.extra.", ""));
        }

        switch (event) {
            case AttributeEvent.MISSION_UPDATED:
                alertUser("mission updated");
                break;

            case AttributeEvent.MISSION_SENT:
                alertUser("Mission sent");
                if (loadFuture != null) {
                    loadFuture.complete(true);
                }
                break;

            case AttributeEvent.MISSION_RECEIVED:
                //startMission();
                alertUser("Mission recieved");
                break;


            case AttributeEvent.STATE_CONNECTED:
                alertUser("Drone Connected");
//                armDrone();
                break;

            case AttributeEvent.STATE_DISCONNECTED:
                alertUser("Drone Disconnected");
                break;

            case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                alertUser("++++++ Got all parameters");
                break;

            case AttributeEvent.STATE_UPDATED:
            case AttributeEvent.STATE_ARMING:
                updateState();
                break;

            case AttributeEvent.TYPE_UPDATED:
                Type newDroneType = drone.getAttribute(AttributeType.TYPE);
                if (newDroneType.getDroneType() != this.droneType) {
                    this.droneType = newDroneType.getDroneType();
                    updateVehicleModesForType(this.droneType);
                }
                break;

            case AttributeEvent.STATE_VEHICLE_MODE:
                updateVehicleMode();
                break;

            case AttributeEvent.SPEED_UPDATED:
                updateSpeed();
                break;

            case AttributeEvent.ALTITUDE_UPDATED:
                updateAltitude();
                break;

            case AttributeEvent.GPS_POSITION:
            case AttributeEvent.ATTITUDE_UPDATED:
            case AttributeEvent.STATE_EKF_REPORT:
            case AttributeEvent.STATE_VEHICLE_VIBRATION:
                // skip
                break;

            default:
                System.out.println(" drone event: " + event + " extras " +extras);

        }

    }

    private void updateState() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        if (vehicleState.isConnected() && vehicleState.isFlying()) {
            // Land
            alertUser("state: FLYING");
        } else if (vehicleState.isArmed()) {
            // Take off
            alertUser("state: ARMED");
        } else if (vehicleState.isConnected()) {
            // Connected but not Armed
            alertUser("state: CONNECTED");
        }

    }


    protected void updateVehicleModesForType(int droneType) {
        List<VehicleMode> vehicleModes = VehicleMode.getVehicleModePerDroneType(droneType);
        System.out.println("got vehicle modes: " + vehicleModes);
    }


    protected void updateVehicleMode() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        VehicleMode vehicleMode = vehicleState.getVehicleMode();

        System.out.println("MODE " + vehicleMode.getLabel());
    }

    protected void updateAltitude() {
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        System.out.println(String.format("ALTITUDE %3.1f", droneAltitude.getAltitude()) + "m");
    }

    protected void updateSpeed() {
        Speed droneSpeed = this.drone.getAttribute(AttributeType.SPEED);
        System.out.println(String.format("SPEED %3.1f", droneSpeed.getGroundSpeed()) + "m/s");
    }

    private void alertUser(String message) {
        System.out.println("drone event: " + message);
    }
}
