/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.listeners;

import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.desktop.main.debug.Debug;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;

public class ListenerDelegatorFactory<T> {

    @SuppressWarnings("unchecked")
    public T createNew(Class<T> cls) {
        ListenerHandle<T> handler = new ListenerHandle<T>(cls.toString());
        T proxy = (T)Proxy.newProxyInstance(cls.getClassLoader(), new Class[] {cls}, handler);
        return proxy;
    }

    protected static class ListenerHandle<T> implements InvocationHandler {

        private final WeakListenerList<IListener> listeners;
        private final Method methodAdd;
        private final Method methodRemove;

        public ListenerHandle(String name) {
            listeners = new WeakListenerList<IListener>("ListenerDelegatorFactory" + name);
            methodAdd = IListenerManager.class.getMethods()[0];
            methodRemove = IListenerManager.class.getMethods()[1];
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.equals(methodAdd)) {
                listeners.add((IListener)args[0]);
            } else if (method.equals(methodRemove)) {
                listeners.remove(args[0]);
            } else {
                for (IListener listener : listeners) {
                    try {
                        method.invoke(listener, args);
                    } catch (IllegalArgumentException e) { // if the current method is not implemented
                    } catch (Throwable e) {
                        if (e.getCause() != null) {
                            e = e.getCause();
                        }

                        Debug.getLog().log(Level.SEVERE, "Problem invoking Listener", e);
                    }
                }
            }

            return null;
        }
    }

}
