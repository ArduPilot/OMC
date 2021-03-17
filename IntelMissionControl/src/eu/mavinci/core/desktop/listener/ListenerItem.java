/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.desktop.listener;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Executor;

class ListenerItem<TListener extends TListenerInterface, TListenerInterface> {

    private class ListenerInvocationHandler implements InvocationHandler {
        private WeakReference<TListener> instance;
        private Executor executor;

        ListenerInvocationHandler(WeakReference<TListener> instance, Executor executor) {
            this.instance = instance;
            this.executor = executor;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] objects) {
            final TListener strongRef = instance.get();
            if (strongRef != null) {
                executor.execute(() -> {
                    try {
                        method.invoke(strongRef, objects);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            return null;
        }
    }

    private final WeakReference<TListener> listener;
    private final TListenerInterface wrapper;

    @SuppressWarnings("unchecked")
    ListenerItem(TListener listener, Class<TListenerInterface> listenerInterface, Executor executor) {
        WeakReference<TListener> weakRef = new WeakReference<>(listener);

        if (executor == null) {
            this.wrapper = null;
        } else {
            this.wrapper =
                    (TListenerInterface)
                            Proxy.newProxyInstance(
                                    listener.getClass().getClassLoader(),
                                    new Class<?>[] {listenerInterface},
                                    new ListenerInvocationHandler(weakRef, executor));
        }

        this.listener = weakRef;
    }

    TListener getListener() {
        return listener.get();
    }

    TListenerInterface getListenerProxy() {
        TListener strongRef = listener.get();
        if (strongRef != null) {
            return wrapper != null ? wrapper : strongRef;
        }

        return null;
    }

}
