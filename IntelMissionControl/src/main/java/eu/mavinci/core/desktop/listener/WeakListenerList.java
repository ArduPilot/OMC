/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.desktop.listener;

import com.intel.missioncontrol.helper.Ensure;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;

public class WeakListenerList<T> implements Iterable<T> {

    private static WeakListenerList<WeakListenerList<?>> allLists;
    private static Future<Void> cleanupFuture;
    private static int cleanupIntervalInMills = 10000;
    private static int cleanupStartupDelay = 3 * cleanupIntervalInMills;

    public static void init() {
        allLists = new WeakListenerList<>("MetaListOfAllWeakLists");
        allLists.add(allLists);

        Dispatcher dispatcher = Dispatcher.background();
        cleanupFuture =
            dispatcher.runLaterAsync(
                () -> {
                    @SuppressWarnings("unused")
                    int totalSize = 0;
                    for (WeakListenerList<?> weakList : allLists) {
                        Ensure.notNull(weakList, "weakList");
                        WeakListenerListIterator<?> it = weakList.iterator();
                        while (it.hasNext()) {
                            it.next(); // do the cleanup job internally
                        }

                        totalSize += it.remainingCount;

                        if (weakList.addsEver > weakList.addsEverLast) {
                            weakList.addsEverLast = weakList.addsEver;
                        }
                    }
                },
                Duration.ofMillis(cleanupStartupDelay),
                Duration.ofMillis(cleanupIntervalInMills));
    }

    public final String listName;

    private long addsEver = 0;
    private long addsEverLast = 0;

    public WeakListenerList(String name) {
        listName = name;
        if (allLists != null) {
            allLists.addDirect(this);
        }
    }

    private ConcurrentLinkedDeque<ListenerItem<? extends T, T>> listeners = new ConcurrentLinkedDeque<>();

    /**
     * Doesent check for allready containing! Using this speeds up adding a new listener into a very long list A LOT
     *
     * @param listener
     */
    public void addDirect(T listener) {
        listeners.add(new ListenerItem<>(listener, null, null));
    }

    public <T2 extends T> void addDirect(T2 listener, Class<T> listenerInterface, Executor executor) {
        listeners.add(new ListenerItem<>(listener, listenerInterface, executor));
    }

    public boolean add(T listener) {
        if (!contains(listener)) {
            addDirect(listener);
            return true;
        }

        return false;
    }

    public boolean add(T listener, Class<T> listenerInterface, Executor executor) {
        if (!contains(listener)) {
            addDirect(listener, listenerInterface, executor);
            return true;
        }

        return false;
    }

    // /**
    // * Add the listener at first position, or at the end of the list, if the position is beyond the end
    // *
    // * @param listener
    // */
    //
    public void addAtBegin(T listener) {
        remove(listener);
        listeners.addFirst(new ListenerItem<>(listener, null, null));
    }

    public void addAtSecond(T listener) {
        remove(listener);
        ListenerItem<? extends T, T> first = listeners.pollFirst();
        listeners.addFirst(new ListenerItem<>(listener, null, null));
        if (first != null) {
            listeners.addFirst(first);
        }
    }

    public boolean remove(Object listener) {
        Iterator<ListenerItem<? extends T, T>> it = listeners.iterator();
        while (it.hasNext()) {
            ListenerItem<? extends T, T> elem = it.next();
            T val = elem.getListener();
            if (val != null && val == listener) {
                it.remove();
                return true;
            }
        }

        return false;
    }

    public boolean contains(Object listener) {
        for (ListenerItem<? extends T, T> elem : listeners) {
            T val = elem.getListener();
            if (val != null && val == listener) {
                return true;
            }
        }

        return false;
    }

    @Override
    public WeakListenerListIterator<T> iterator() {
        return new WeakListenerListIterator<T>(listeners.iterator());
    }

    public ReversedWeakListenerList reversed() {
        return new ReversedWeakListenerList();
    }

    @Override
    public String toString() {
        return listeners.toString();
    }

    public class ReversedWeakListenerList implements Iterable<T> {
        public WeakListenerListIterator<T> iterator() {
            // the cloning prevents for concurrent modification exceptions -> DONT clone, otherwise cleanup will fail!!
            return new WeakListenerListIterator<T>(listeners.descendingIterator());
        }
    }

    public int size() {
        return listeners.size();
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    public void clear() {
        listeners.clear();
    }

}
