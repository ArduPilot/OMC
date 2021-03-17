package com.intel.dronekit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import androidCompat.DesktopHelper;
import com.MAVLink.Messages.MAVLinkMessage;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.MavlinkObserver;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.property.*;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;
import org.droidplanner.services.android.impl.utils.MissionUtils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class BasicTelemetryTestApp implements TowerListener {

    public static void main(String[] args) {
        BasicTelemetryTestApp app = new BasicTelemetryTestApp();
        app.start();
    }

    private int droneType = Type.TYPE_UNKNOWN;

    Context context;
    ControlTower tower;
    Drone drone;
    Handler handler;

    Semaphore semaphore = new Semaphore(1);

    void start() {
        handler = new Handler(Looper.getMainLooper());
        String property = System.getProperty("java.io.tmpdir");
        context = DesktopHelper.setup(Drone.class, new File(property));
        tower = new ControlTower(context);
        drone = new Drone(context);

        semaphore.acquireUninterruptibly();

        // after tower.connect();
        tower.registerDrone(drone, handler);
        drone.registerDroneListener(this.droneListener);
        drone.addMavlinkObserver(new MavlinkObserver() {
            @Override
            public void onMavlinkMessageReceived(MavlinkMessageWrapper mavlinkMessageWrapper) {
                MAVLinkMessage mavLinkMessage = mavlinkMessageWrapper.getMavLinkMessage();
                //System.out.println("got mavlink message: " + mavLinkMessage);
            }
        });

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
                        handler.postDelayed(() -> startTest(), 4000);
                        break;

                    default:
                        alertUser("linkstate updated: " + connectionStatus.getStatusCode());
                }
            }
        });

        semaphore.acquireUninterruptibly();
    }


    private void startTest() {
        Mission mission = drone.getAttribute(AttributeType.MISSION);
        List<MissionItem> missionItems = mission.getMissionItems();


        System.out.println("starting waypoint thing...");
        MissionApi missionApi = MissionApi.getApi(drone);

        Uri uri = null;
        try {
            URI missionUri = getClass().getResource("CMAC-circuit.txt").toURI();
            uri = new Uri(missionUri);
        } catch (URISyntaxException e) {
            return;
        }

        missionApi.loadAndSetMission(uri, new MissionApi.LoadingCallback<Mission>() {
            @Override
            public void onLoadingStart() {
                System.out.println("loading waypoints...");
            }

            @Override
            public void onLoadingComplete(Mission loaded) {

                System.out.println("loading complete..." + loaded);

            }

            @Override
            public void onLoadingFailed() {
                System.out.println("loading failed...");
            }
        });

//        missionApi.loadMission();
    }


    Runnable pending;

    private void startMission(Mission loaded) {
        armDrone();

        pending = () -> {
            System.out.println("Attempting to start mission...");

            MissionApi missionApi = MissionApi.getApi(drone);
            missionApi.startMission(true, true, new AbstractCommandListener() {
                @Override
                public void onSuccess() {
                    System.out.println("Starting mission...");
                }

                @Override
                public void onError(int executionError) {
                    System.out.println("Failed to start mission..." + executionError);
                }

                @Override
                public void onTimeout() {

                }
            });
        };
    }

    private void executeMission() {
        Mission mission = drone.getAttribute(AttributeType.MISSION);

        int currentMissionItem = mission.getCurrentMissionItem();
        alertUser("currentMissionItem: " + currentMissionItem);
        if (pending != null) {
            pending.run();
        }
    }



    private void takeOffDrone() {
        ControlApi.getApi(this.drone).takeoff(10, new AbstractCommandListener() {

            @Override
            public void onSuccess() {
                alertUser("Taking off...");
                executeMission();
            }

            @Override
            public void onError(int i) {
                alertUser("Unable to take off.");
            }

            @Override
            public void onTimeout() {
                alertUser("Unable to take off.");
            }
        });

    }

    private void armDrone() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        if (vehicleState.isConnected() && !vehicleState.isArmed()) {
            // Connect
            alertUser("arming drone...");
            // Connected but not Armed
            VehicleApi.getApi(this.drone).arm(true, false, new SimpleCommandListener() {
                @Override
                public void onError(int executionError) {
                    alertUser("Unable to arm vehicle.");
                }

                @Override
                public void onSuccess() {
                    takeOffDrone();
                    super.onSuccess();
                }

                @Override
                public void onTimeout() {
                    alertUser("Arming operation timed out.");
                }
            });
        }
    }


    private DroneListener droneListener = new DroneListener() {
        @Override
        public void onDroneEvent(String event, Bundle extras) {
            handleDroneEvent(event, extras);
        }

        @Override
        public void onDroneServiceInterrupted(String errorMsg) {
            alertUser(errorMsg);
        }
    };

    private void handleDroneEvent(String event, Bundle extras) {
        final boolean dumpEvents = false;

        if (event.contains("MISSION")) {//dumpEvents) {
            System.out.println("--- drone event: "
                    + event.substring("com.o3dr.services.android.lib.attribute.event.".length())
                    + " "
                    + extras.toString().replace("com.o3dr.services.android.lib.attribute.event.extra.", ""));
        }

        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                alertUser("Drone Connected");
//                    updateConnectedButton(this.drone.isConnected());
                updateArmButton();
//                    checkSoloState();
                break;

            case AttributeEvent.GPS_POSITION:
                updateDistanceFromHome();
                break;

            case AttributeEvent.STATE_DISCONNECTED:
                alertUser("Drone Disconnected");
//                    updateConnectedButton(this.drone.isConnected());
                updateArmButton();
                break;

            case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                alertUser("++++++ Got all parameters");
                handler.postDelayed(() -> {
                    startMission(null);

                }, 2000);
                break;

            case AttributeEvent.STATE_UPDATED:
            case AttributeEvent.STATE_ARMING:
                updateArmButton();
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

            case AttributeEvent.HOME_UPDATED:
                updateDistanceFromHome();
                break;


            default:
                //alertUser("unknown, " + event);
                break;
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

    protected double distanceBetweenPoints(LatLongAlt pointA, LatLongAlt pointB) {
        if (pointA == null || pointB == null) {
            return 0;
        }
        double dx = pointA.getLatitude() - pointB.getLatitude();
        double dy = pointA.getLongitude() - pointB.getLongitude();
        double dz = pointA.getAltitude() - pointB.getAltitude();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    protected void updateDistanceFromHome() {
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        double vehicleAltitude = droneAltitude.getAltitude();
        Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        LatLong vehiclePosition = droneGps.getPosition();

        double distanceFromHome = 0;

        if (droneGps.isValid()) {
            LatLongAlt vehicle3DPosition = new LatLongAlt(vehiclePosition.getLatitude(), vehiclePosition.getLongitude(), vehicleAltitude);
            Home droneHome = this.drone.getAttribute(AttributeType.HOME);
            distanceFromHome = distanceBetweenPoints(droneHome.getCoordinate(), vehicle3DPosition);
        } else {
            distanceFromHome = 0;
        }

        //System.out.println(String.format("DISTANCE %3.1f", distanceFromHome) + "m");
    }

    protected void updateArmButton() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
//        Button armButton = (Button) findViewById(R.id.btnArmTakeOff);
//
//        if (!this.drone.isConnected()) {
//            armButton.setVisibility(View.INVISIBLE);
//        } else {
//            armButton.setVisibility(View.VISIBLE);
//        }

        if (vehicleState.isFlying()) {
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


    private void alertUser(String message) {
        System.out.println("drone event: " + message);
    }


    @Override
    public void onTowerConnected() {
//        tower.registerDrone(drone, );
    }

    @Override
    public void onTowerDisconnected() {

    }
}
