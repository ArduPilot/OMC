package com.o3dr.android.client;

import android.content.Context;
import android.os.Handler;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.model.IApiListener;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;
import com.o3dr.services.android.lib.model.IDroneApi;
import org.droidplanner.services.android.impl.api.DroidPlannerService;

import java.lang.reflect.Constructor;

/**
 * Created by fhuya on 11/12/14.
 * Desktop version
 */
public class ControlTower {

    private static final String TAG = ControlTower.class.getSimpleName();

    IDroidPlannerServices.Stub dpService;
    IDroneApi droneApi;
    Context context;

    public ControlTower(Context context) {
        this.context = context;
    }

    public void registerDrone(Drone drone, Handler handler) {
        if (drone == null)
            return;

//        if (!isTowerConnected())
//            throw new IllegalStateException("Control Tower must be connected.");

        drone.init(this, handler);
        drone.start();
    }

    public void unregisterDrone(Drone drone) {
        if (drone != null)
            drone.destroy();
    }


    public boolean isTowerConnected() {
        return true;
    }

    public IDroneApi registerDroneApi() {
        if (droneApi == null) {
            initDpService();
        }
        return droneApi;
    }
    // use reflection to avoid DPServices
    public static IDroidPlannerServices.Stub createDpService(DroidPlannerService dps) {
        try {
            Class<?> c = Class.forName("org.droidplanner.services.android.impl.api.DPServices");
            Class[] type = { DroidPlannerService.class };
            Constructor<?> constructor = c.getDeclaredConstructor(type);
            constructor.setAccessible(true);
            IDroidPlannerServices.Stub stub = (IDroidPlannerServices.Stub) constructor.newInstance(dps);
            return stub;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void initDpService() {
        dpService = createDpService(new DroidPlannerService(context));

        IApiListener listener = new IApiListener() {
            @Override
            public int getApiVersionCode() {
                return 0;
            }

            @Override
            public void onConnectionFailed(ConnectionResult result) {

            }

            @Override
            public int getClientVersionCode() {
                return 0;
            }
        };

        droneApi = dpService.registerDroneApi(listener, "myappid");
    }

    public void releaseDroneApi(IDroneApi droneApi) {
        dpService.releaseDroneApi(droneApi);
        droneApi = null;
    }
}
