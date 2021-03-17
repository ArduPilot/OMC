package com.intel.dronekit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import androidCompat.DesktopHelper;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.*;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;
import org.droidplanner.services.android.impl.utils.MissionUtils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Semaphore;

// this works, but you might have to run it twice because of timing issues...
public class BasicMissionUploadApp {

    public static void main(String[] args) {
        BasicMissionUploadApp app = new BasicMissionUploadApp();
        app.start();
    }

    private int droneType = Type.TYPE_UNKNOWN;

    private Context context;
    private ControlTower tower;
    private Drone drone;
    private Handler handler;

    private Semaphore semaphore = new Semaphore(1);

    void start() {
        handler = new Handler(Looper.getMainLooper());
        String property = System.getProperty("java.io.tmpdir");
        context = DesktopHelper.setup(Drone.class, new File(property));
        tower = new ControlTower(context);
        drone = new Drone(context);

        semaphore.acquireUninterruptibly();

        // after tower.connect();
        tower.registerDrone(drone, handler);
        drone.registerDroneListener(new DroneListener() {
            @Override
            public void onDroneEvent(String event, Bundle extras) {
                handleDroneEvent(event, extras);
            }

            @Override
            public void onDroneServiceInterrupted(String errorMsg) {
                alertUser(errorMsg);
            }
        });;

        ConnectionParameter params = ConnectionParameter
                .newTcpConnection("localhost", 5760, null);

        drone.connect(params, new LinkListener() {
            @Override
            public void onLinkStateUpdated(@NonNull LinkConnectionStatus connectionStatus) {
                switch (connectionStatus.getStatusCode()) {
                    case LinkConnectionStatus.FAILED:
                        Bundle extras = connectionStatus.getExtras();
                        String msg = null;
                        if (extras != null) {
                            msg = extras.getString(LinkConnectionStatus.EXTRA_ERROR_MSG);
                        }
                        alertUser("Connection Failed:" + msg);
                        break;

                    case LinkConnectionStatus.CONNECTED:
                        alertUser("Link connected:");
                        //handler.postDelayed(() -> startTest(), 4000);
                        break;

                    case LinkConnectionStatus.DISCONNECTED:
                        alertUser("linkstate updated: " + connectionStatus.getStatusCode());
                        break;
                }
            }
        });

        semaphore.acquireUninterruptibly();
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

    class CommandListener extends AbstractCommandListener {
        String name;

        public CommandListener(String commandName) {
            this.name = commandName;
        }

        @Override
        public void onSuccess() {
            alertUser(name + ": success");
        }

        @Override
        public void onError(int executionError) {
            alertUser(name + ": error "+executionError);
        }

        @Override
        public void onTimeout() {
            alertUser(name + ": timeout ");
        }
    }


    private Mission getMission() {
        Uri uri = null;
        try {
            URI missionUri = getClass().getResource("CMAC-circuit.txt").toURI();
            uri = new Uri(missionUri);
        } catch (URISyntaxException e) {
            return null;
        }

        return MissionUtils.loadMission(context, uri);
    }

    public void uploadMission() {
        MissionApi missionApi = MissionApi.getApi(drone);

        Mission mission = getMission();
        missionApi.setMission(mission, true);
    }

    private void armDrone() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        if (vehicleState.isArmed()) return;
        VehicleApi.getApi(drone).arm(true, new CommandListener("armDrone") {
            @Override
            public void onSuccess() {
                super.onSuccess();
                takeOffDrone();
            }
        });
    }

    public void setMode() {
        VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_AUTO, new CommandListener("setMode") {
            @Override
            public void onSuccess() {
                super.onSuccess();
            }
        });
    }

    private void takeOffDrone() {
        ControlApi.getApi(this.drone).takeoff(20.0, new CommandListener("takeoff") {
            @Override
            public void onSuccess() {
                super.onSuccess();
                uploadMission();
                handler.postDelayed(() -> setMode(), 10);
            }
        });
    }


    private void startMission() {
        MissionApi.getApi(drone).startMission(true, true,
                new CommandListener("startMission") {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();

                    }
                });
    }

    private void handleDroneEvent(String event, Bundle extras) {
        final boolean dumpEvents = false;

        if (event.contains("MISSION")) {//dumpEvents) {
            System.out.println("--- drone event: "
                    + event.substring("com.o3dr.services.android.lib.attribute.event.".length())
                    + " "
                    + extras.toString().replace("com.o3dr.services.android.lib.attribute.event.extra.", ""));
        }

        switch (event) {
            case AttributeEvent.MISSION_RECEIVED:
                startMission();
                break;


            case AttributeEvent.STATE_CONNECTED:
                alertUser("Drone Connected");
                armDrone();
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

            default:
                break;
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
