/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.imageio;

import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import java.util.Map;
import javafx.scene.image.Image;

public class ImageLoaderInstaller {

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
        settings.themeProperty()
            .addListener(
                (observable, oldTheme, newTheme) -> {
                    try {
                        java.lang.reflect.Field imageCacheField =
                            com.sun.javafx.css.StyleManager.class.getDeclaredField("imageCache");
                        imageCacheField.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        Map<String, Image> imageCache =
                            (Map<String, Image>)imageCacheField.get(com.sun.javafx.css.StyleManager.getInstance());
                        if (imageCache != null) {
                            imageCache.clear();
                        }
                    } catch (NoSuchFieldException
                        | SecurityException
                        | IllegalArgumentException
                        | IllegalAccessException e) {
                        // Should not happen :-)
                    }
                });
    }

}
