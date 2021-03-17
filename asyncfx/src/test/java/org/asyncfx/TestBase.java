/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.testfx.api.FxToolkit;

@SuppressWarnings("WeakerAccess")
public class TestBase {

    static {
        AsyncFX.setRunningTests(true);

        try {
            System.setProperty("testfx.verbose", "true");
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("glass.verbose", "true");
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.verbose", "true");
            System.setProperty("monocle.platform", "Headless");
            System.setProperty("prism.verbose", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");

            FxToolkit.registerPrimaryStage();

            while (!FxToolkit.isFXApplicationThreadRunning()) {
                Thread.sleep(100);
            }
        } catch (TimeoutException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private final Map<String, Object> data = new HashMap<>();

    @BeforeEach
    public void reset() {
        data.clear();
    }

    public static void loadClass(Class<?> clazz) {
        try {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public synchronized void store(String key, Object value) {
        data.put(key, value);
    }

    public void storeBoolean(String key, boolean value) {
        store(key, value);
    }

    public void storeInt(String key, int value) {
        store(key, value);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T load(String key) {
        return (T)data.get(key);
    }

    public boolean loadBoolean(String key) {
        Boolean value = load(key);
        return value != null && value;
    }

    public int loadInt(String key) {
        Integer value = load(key);
        return value == null ? 0 : value;
    }

}
