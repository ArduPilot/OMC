/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.IdentityHashMap;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.util.Callback;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AsyncObservable;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
final class AsyncElementObserver<E> {

    private static class ElementsMapElement {
        InvalidationListener listener;
        int counter;

        ElementsMapElement(InvalidationListener listener) {
            this.listener = listener;
            this.counter = 1;
        }

        void increment() {
            counter++;
        }

        int decrement() {
            return --counter;
        }

        private InvalidationListener getListener() {
            return listener;
        }
    }

    private Callback<E, AsyncObservable[]> extractor;
    private final Callback<E, InvalidationListener> listenerGenerator;
    private final AsyncObservableListBase<E> list;
    private Executor executor;
    private IdentityHashMap<E, ElementsMapElement> elementsMap = new IdentityHashMap<>();

    AsyncElementObserver(
            Callback<E, AsyncObservable[]> extractor,
            Callback<E, InvalidationListener> listenerGenerator,
            AsyncObservableListBase<E> list) {
        this.extractor = extractor;
        this.listenerGenerator = listenerGenerator;
        this.list = list;
    }

    void setExecutor(Executor executor) {
        this.executor = executor;
    }

    Executor getExecutor() {
        return executor;
    }

    void attachListener(final E e) {
        if (executor == null) {
            throw new IllegalStateException("Executor not initialized.");
        }

        if (elementsMap != null && e != null) {
            if (elementsMap.containsKey(e)) {
                elementsMap.get(e).increment();
            } else {
                InvalidationListener listener = listenerGenerator.call(e);
                for (AsyncObservable o : extractor.call(e)) {
                    o.addListener(listener, executor);
                }

                elementsMap.put(e, new AsyncElementObserver.ElementsMapElement(listener));
            }
        }
    }

    void detachListener(E e) {
        if (elementsMap != null && e != null) {
            AsyncElementObserver.ElementsMapElement el = elementsMap.get(e);
            for (AsyncObservable o : extractor.call(e)) {
                o.removeListener(el.getListener());
            }

            if (el.decrement() == 0) {
                elementsMap.remove(e);
            }
        }
    }

}
