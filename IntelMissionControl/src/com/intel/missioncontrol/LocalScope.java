/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import java.util.HashMap;
import java.util.Map;

public class LocalScope implements Scope {

    private final Map<Key<?>, Object> store = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Provider<T> scope(Key<T> key, Provider<T> provider) {
        return () -> {
            Object instance = store.get(key);
            if (instance == null) {
                instance = provider.get();
                store.put(key, instance);
            }

            return (T)instance;
        };
    }

    private static final LocalScope INSTANCE = new LocalScope();

    public static void newScope() {
        INSTANCE.store.clear();
    }

    public static LocalScope getInstance() {
        return INSTANCE;
    }

}
