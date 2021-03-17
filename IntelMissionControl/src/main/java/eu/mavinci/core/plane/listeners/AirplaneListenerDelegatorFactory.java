/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.listeners.IListenerManager;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.listeners.IListenerManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;

@Deprecated
public class AirplaneListenerDelegatorFactory {

    public static IAirplaneListenerDelegator createNewAirplaneListenerDelegator() {
        AirplaneListenerHandle handler = new AirplaneListenerHandle();
        IAirplaneListenerDelegator proxy =
            (IAirplaneListenerDelegator)
                Proxy.newProxyInstance(
                    IAirplaneListenerDelegator.class.getClassLoader(),
                    new Class[] {IAirplaneListenerDelegator.class},
                    handler);
        return proxy;
    }

    private static class AirplaneListenerHandle implements InvocationHandler {

        private final WeakListenerList<IAirplaneListener> listeners;
        private final Method methodGuiClose;
        private final Method methodGuiCloseRequest;
        private final Method methodAdd;
        private final Method methodAddAtBegin;
        private final Method methodAddAtSecond;
        private final Method methodRemove;
        // private final static String name = "AirplaneListenerDelegator";

        public AirplaneListenerHandle() {
            listeners = new WeakListenerList<IAirplaneListener>("AirplaneListenerHandle");
            methodGuiClose = IAirplaneListenerGuiClose.class.getMethods()[0];
            methodGuiCloseRequest = IAirplaneListenerGuiClose.class.getMethods()[1];
            methodAdd = IListenerManager.class.getMethods()[0];
            methodAddAtBegin = IListenerManager.class.getMethods()[1];
            methodRemove = IListenerManager.class.getMethods()[2];
            methodAddAtSecond = IAirplaneListenerDelegator.class.getMethods()[0];
        }

        // int cnt = 0;

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.equals(methodAdd)) {
                // System.out.println("addlistener " + args[0] + " to " +this.hashCode());
                listeners.add((IAirplaneListener)args[0]);
            } else if (method.equals(methodAddAtBegin)) {
                listeners.addAtBegin((IAirplaneListener)args[0]);
            } else if (method.equals(methodAddAtSecond)) {
                listeners.addAtSecond((IAirplaneListener)args[0]);
            } else if (method.equals(methodRemove)) {
                listeners.remove(args[0]);
            } else if (method.equals(methodGuiClose)) {
                for (IAirplaneListener listener : listeners.reversed()) {
                    try {
                        method.invoke(listener, args);
                    } catch (IllegalArgumentException e) { // if the current method is not implemented
                    } catch (Throwable e) {
                        if (e.getCause() != null) {
                            e = e.getCause();
                        }

                        Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
                    }
                }
            } else if (method.equals(methodGuiCloseRequest)) {
                for (IAirplaneListener listener : listeners.reversed()) {
                    boolean allowClosing = true;
                    try {
                        allowClosing = (Boolean)method.invoke(listener, args);
                    } catch (IllegalArgumentException e) { // if the current method is not implemented
                    } catch (InvocationTargetException e) { // returning null of the invoked one allows closing ;-)
                        if (!(e.getCause()
                                instanceof
                                NullPointerException)) { // returning null of the invoked one allows closing ;-)
                            Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
                        }
                    } catch (Throwable e) {
                        if (e.getCause() != null) {
                            e = e.getCause();
                        }

                        Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
                    }

                    if (!allowClosing) {
                        return false;
                    }
                }

                return true;
            } else {
                // cnt++;
                // int i = 0;
                for (IAirplaneListener listener : listeners) {
                    // i++;
                    // System.out.println("listnerNo:"+i);i++;
                    try {
                        method.invoke(listener, args);
                    } catch (IllegalArgumentException e) { // if the current method is not implemented
                    } catch (Throwable e) {
                        if (e.getCause() != null) {
                            e = e.getCause();
                        }
                        // Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener "+method+"No" + i +"
                        // cnt" + cnt +" "+
                        // listener + listener.hashCode(), e);
                        Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
                    }
                }
            }

            return null;
        }
    }

}
