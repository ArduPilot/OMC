package com.intel.dronekit;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;

import java.io.File;
import java.util.concurrent.Semaphore;

public class DroneHelper {

    public Context context;
    public ControlTower tower;
    public Handler handler;
    public Drone drone;

    Semaphore semaphore = new Semaphore(1);

    interface LinkConnectionListener {
        void onConnected();
        void onFailed(LinkError error, String message);
        void onDisconnected();
    }

    public enum LinkError {
        SYSTEM_UNAVAILABLE,
        LINK_UNAVAILABLE,
        PERMISSION_DENIED,
        INVALID_CREDENTIALS,
        TIMEOUT,
        ADDRESS_IN_USE,
        UNKNOWN;

        public static LinkError fromInt(int x) {
            switch (x) {
                case LinkConnectionStatus.SYSTEM_UNAVAILABLE:
                    return SYSTEM_UNAVAILABLE;
                case LinkConnectionStatus.LINK_UNAVAILABLE:
                    return LINK_UNAVAILABLE;
                case LinkConnectionStatus.PERMISSION_DENIED:
                    return PERMISSION_DENIED;
                case LinkConnectionStatus.INVALID_CREDENTIALS:
                    return INVALID_CREDENTIALS;
                case LinkConnectionStatus.TIMEOUT:
                    return TIMEOUT;
                case LinkConnectionStatus.ADDRESS_IN_USE:
                    return ADDRESS_IN_USE;
                case LinkConnectionStatus.UNKNOWN:
                default:
                    return UNKNOWN;
            }
        }
    }

    void stop() {

    }

    void start(Context context, DroneListener listener, LinkConnectionListener connectionListener, ConnectionParameter params) {
        handler = new Handler(Looper.getMainLooper());
        String property = System.getProperty("java.io.tmpdir");
        tower = new ControlTower(context);
        drone = new Drone(context);

        semaphore.acquireUninterruptibly();

        // after tower.connect();
        tower.registerDrone(drone, handler);
        drone.registerDroneListener(listener);

        drone.connect(params, new LinkListener() {
            @Override
            public void onLinkStateUpdated(@NonNull LinkConnectionStatus connectionStatus) {
                switch (connectionStatus.getStatusCode()) {
                    case LinkConnectionStatus.FAILED:
                        Bundle extras = connectionStatus.getExtras();
                        String msg = "";
                        LinkError error = LinkError.UNKNOWN;
                        if (extras != null) {
                            msg = extras.getString(LinkConnectionStatus.EXTRA_ERROR_MSG);
                            error = LinkError.fromInt(extras.getInt(LinkConnectionStatus.EXTRA_ERROR_CODE,
                                    LinkConnectionStatus.UNKNOWN));
                        }
                        connectionListener.onFailed(error, msg);
                        break;

                    case LinkConnectionStatus.CONNECTED:
                        connectionListener.onConnected();
                    case LinkConnectionStatus.DISCONNECTED:
                        connectionListener.onDisconnected();
                }
            }
        });
    }

}