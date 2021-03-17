/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.imageio;

import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import java.lang.reflect.Field;
import java.util.Map;

public class ImageLoaderInstaller {

    @SuppressWarnings("FieldCanBeLocal")
    private static ColorCache colorCache;

    public static void installThreadContextClassLoader() {
        var thread = Thread.currentThread();
        var classLoader = thread.getContextClassLoader();
        assert !(classLoader instanceof AnnotatedSvgClassLoader);
        thread.setContextClassLoader(new AnnotatedSvgClassLoader(classLoader));
    }

    public static void install(ISettingsManager settingsManager) {
        colorCache = new ColorCache(settingsManager);
        SvgImageLoaderFactory.install(colorCache);
        GeneralSettings settings = settingsManager.getSection(GeneralSettings.class);
        settings.themeProperty().addListener((observable, oldTheme, newTheme) -> clearJfx11ImageCache());
    }

    @SuppressWarnings("unchecked")
    private static void clearJfx10ImageCache() {
        try {
            java.lang.reflect.Field imageCacheField =
                com.sun.javafx.css.StyleManager.class.getDeclaredField("imageCache");
            imageCacheField.setAccessible(true);
            Map imageCache = (Map)imageCacheField.get(com.sun.javafx.css.StyleManager.getInstance());
            if (imageCache != null) {
                imageCache.clear();
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // Should not happen :-)
        }
    }

    @SuppressWarnings("unchecked")
    private static void clearJfx11ImageCache() {
        try {
            Field field = com.sun.javafx.css.StyleManager.class.getDeclaredField("imageCache");
            field.setAccessible(true);
            Object imageCache = field.get(com.sun.javafx.css.StyleManager.getInstance());
            field = imageCache.getClass().getDeclaredField("imageCache");
            field.setAccessible(true);
            Map map = (Map)field.get(imageCache);
            map.clear();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // Should not happen :-)
        }
    }

}
