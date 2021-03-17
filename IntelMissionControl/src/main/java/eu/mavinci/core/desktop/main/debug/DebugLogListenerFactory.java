/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.desktop.main.debug;

import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.core.plane.listeners.IAirplaneListenerAll;

import java.lang.reflect.Proxy;

public class DebugLogListenerFactory {

    public static IAirplaneListenerAll createStreamHandle(ICAirplane plane) {
        DebugLogListenerHandler handler = new DebugLogListenerHandler(plane);
        IAirplaneListenerAll proxy =
            (IAirplaneListenerAll)
                Proxy.newProxyInstance(
                    IAirplaneListenerAll.class.getClassLoader(), new Class[] {IAirplaneListenerAll.class}, handler);

        plane.addListener(proxy);
        return proxy;
    }

}
