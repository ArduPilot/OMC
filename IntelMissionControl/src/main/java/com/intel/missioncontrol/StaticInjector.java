/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * DO NOT USE THIS CLASS.
 * This class is a workaround for the many places where static dependency injection is used.
 */
@Deprecated
public class StaticInjector {

    private static Injector injector;

    static void initialize(Injector injector) {
        StaticInjector.injector = injector;
    }

    public static synchronized <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    public static synchronized <T> T getNamedInstance(Class<T> clazz, String name) {
        return injector.getInstance(Key.get(clazz, Names.named(name)));
    }

}
