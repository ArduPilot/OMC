/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

import java.util.LinkedList;

public class FiniteQue<T> extends LinkedList<T> {

    private static final long serialVersionUID = -4776452073881691631L;

    private int maxSize;

    public FiniteQue(int maxSize) {
        setMaxSize(maxSize);
    }

    public synchronized int getMaxSize() {
        return maxSize;
    }

    public synchronized void setMaxSize(int maxSize) {
        if (maxSize <= 0) {
            throw new RuntimeException("size must bigger than 0");
        }

        this.maxSize = maxSize;
        if (size() > maxSize) {
            remove(size() - 1);
        }
    }

    /**
     * Add an element at the beginning of the que
     *
     * <p>return if something was changed
     */
    public synchronized boolean add(T e) {
        super.add(0, e);

        if (size() > maxSize) {
            remove(size() - 1);
        }

        return true;
    }

    /** add an element at the end of the que */
    public synchronized boolean addAtEnd(T e) {
        super.add(e);
        if (size() > maxSize) {
            remove(size() - 1);
        }

        return true;
    }

    public synchronized void save(IProperties properties, String baseKey) {
        for (int i = 0; i != size(); i++) {
            String key = baseKey + "." + i;
            properties.setProperty(key, get(i).toString());
        }

        for (int i = size(); i != maxSize; i++) {
            String key = baseKey + "." + i;
            properties.remove(key);
        }
    }

    public synchronized void load(IProperties properties, String baseKey, Factory<T> factory) {
        for (int i = 0; i != maxSize; i++) {
            String key = baseKey + "." + i;
            if (properties.containsKey(key)) {
                T elem = factory.fromString(properties.getProperty(key));
                if (elem != null) {
                    addAtEnd(elem);
                }
            }
        }
    }

    public synchronized int size() {
        return super.size();
    }

    public synchronized T getFirst() {
        return get(0);
    }

    public synchronized T getLast() {
        return get(size() - 1);
    }

    public interface Factory<T> {
        public T fromString(String str);
    }

}
