package org.droidplanner.services.android.impl.utils;

import com.o3dr.services.android.lib.model.ICommandListener;
import org.droidplanner.services.android.impl.core.drone.autopilot.Drone;

/**
 * Created by Fredia Huya-Kouadio on 7/29/15.
 */
public class SoloApiUtils {

    //Private to prevent instantiation.
    private SoloApiUtils() {
    }


    public static boolean isSoloLinkFeatureAvailable(Drone drone, ICommandListener listener) {

        return false;
    }


}
