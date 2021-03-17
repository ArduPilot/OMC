/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.desktop.main.debug;

import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.desktop.main.debug.Debug;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class DebugLogListenerHandler implements InvocationHandler {

    private final WeakReference<ICAirplane> plane;

    private final Level levDef = Level.FINE;
    private final Level levVerbose = Level.FINEST;

    private final String[] methodsVerbose = {
        "rawDataFromBackend", "setSendHostOfNextReceive", "recv_orientation", "recv_position", "recv_fixedOrientation",
            "recv_health",
        "rawDataToBackend", "loggingStateChangedTCP", "loggingStateChangedFLG", "recv_photo", "recv_linkInfo",
            "recv_androidState",
        "recv_positionOrientation", "recv_debug"
    };

    public DebugLogListenerHandler(ICAirplane plane) {
        this.plane = new WeakReference<ICAirplane>(plane);
        // System.out.println("DEBUG LOG LISTENER CREATED!!");
    }

    private String getNamePrefix() {
        String name = "";
        try {
            name = "plane:" + plane.get().getAirplaneCache().getName() + " ";
        } catch (AirplaneCacheEmptyException e) {
        } catch (NullPointerException e) {
        }

        return name;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // System.out.println("invoked" + method);
        String methodName = method.getName();
        String msg = getNamePrefix() + methodName + ": ";
        if (args != null) {
            boolean first = true;
            for (Object o : args) {
                if (first) {
                    first = false;
                } else {
                    msg += " ,";
                }

                msg += o == null ? "null" : o.toString();
            }
        }

        for (int i = 0; i != methodsVerbose.length; i++) {
            if (methodsVerbose[i].equals(methodName)) {
                Debug.getLog().log(levVerbose, msg);
                return null;
            }
        }

        Debug.getLog().log(levDef, msg);
        return null;
    }
}
