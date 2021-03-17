/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.intel.missioncontrol.Localizable;
import eu.mavinci.core.obfuscation.IKeepAll;

/**
 * Themes are collections of CSS files that define colors and common controls. When a view extends the {@link
 * com.intel.missioncontrol.ui.ViewBase} class, the FXML file will inherit all CSS files that are provided by the theme.
 */
@Localizable
public enum Theme implements IKeepAll {
    LIGHT("/com/intel/missioncontrol/styles/themes/colors-light.css", "/com/intel/missioncontrol/styles/controls.css");

    public static class Accessor {
        public static void setCurrentTheme(Theme theme) {
            currentTheme = theme;
        }
    }

    private static Theme currentTheme = LIGHT;
    private final String colorsSchemeUrl;
    private final String controlSkinUrl;

    Theme(String colorsScheme, String controlSkin) {
        Class<? extends Theme> thisClass = getClass();
        colorsSchemeUrl = thisClass.getResource(colorsScheme).toExternalForm();
        controlSkinUrl = thisClass.getResource(controlSkin).toExternalForm();
    }

    public String[] getStylesheets() {
        return new String[] {colorsSchemeUrl, controlSkinUrl};
    }

    public static Theme currentTheme() {
        return currentTheme;
    }
}
