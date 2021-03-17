/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.imageio;

import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.Theme;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides access to the color values of the current theme stylesheet.
 *
 * @author mstrauss
 */
public class ColorCache {

    private static Map<String, Color> colors = new HashMap<>();
    private Theme theme;
    private boolean invalidated;

    public ColorCache(ISettingsManager settingsManager) {
        GeneralSettings settings = settingsManager.getSection(GeneralSettings.class);
        theme = settings.themeProperty().get();
        settings.themeProperty()
            .addListener(
                (obsevable, oldTheme, newTheme) -> {
                    theme = newTheme;
                    invalidated = true;
                });
    }

    public Color get(String name) {
        if (invalidated) {
            colors.clear();
            invalidated = false;
        }

        Color color = colors.get(name);
        if (color == null) {
            color = getColorFromStyle(name);
            colors.put(name, color);
        }

        return color;
    }

    private Color getColorFromStyle(String name) {
        Pane pane = new Pane();
        Scene scene = new Scene(pane);
        scene.getStylesheets().addAll(theme.getStylesheets());
        pane.setStyle("-fx-background-color: " + name);
        pane.applyCss();
        return (Color)pane.getBackground().getFills().get(0).getFill();
    }

}
