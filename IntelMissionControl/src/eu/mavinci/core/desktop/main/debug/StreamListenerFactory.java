/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.desktop.main.debug;

import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.core.plane.listeners.IAirplaneListenerAll;

import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class StreamListenerFactory {

    /**
     * Factory Method to create new Stream Handles
     *
     * @param plane
     * @param out
     * @return
     */
    public static IAirplaneListenerAll createStreamHandle(ICAirplane plane, PrintStream out) {
        StreamHandler handler = new StreamHandler(plane, out);
        IAirplaneListenerAll proxy =
            (IAirplaneListenerAll)
                Proxy.newProxyInstance(
                    IAirplaneListenerAll.class.getClassLoader(), new Class[] {IAirplaneListenerAll.class}, handler);

        plane.addListener(proxy);
        return proxy;
    }

    private static class StreamHandler implements InvocationHandler {

        private final PrintStream out;

        public StreamHandler(ICAirplane plane, PrintStream out) {
            this.out = out;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            out.print(method.getName() + ": ");
            if (args != null) {
                boolean first = true;
                for (Object o : args) {
                    out.print(o.toString());
                    if (first) {
                        first = false;
                    } else {
                        out.print(" ,");
                    }
                }
            }

            out.println();

            return null;
        }
    }

}
