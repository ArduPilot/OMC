/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.intel.missioncontrol.Localizable;

public enum Theme implements Localizable {
    MOUSE(
        "/com/intel/missioncontrol/styles/themes/colors-light.css",
        "/com/intel/missioncontrol/styles/icons.css",
        "/com/intel/missioncontrol/styles/themes/sizes-desktop.css",
        "/com/intel/missioncontrol/styles/controls.css"),

    TOUCH(
        "/com/intel/missioncontrol/styles/themes/colors-light.css",
        "/com/intel/missioncontrol/styles/icons.css",
        "/com/intel/missioncontrol/styles/themes/sizes-touch.css",
        "/com/intel/missioncontrol/styles/controls.css");

    public static class Accessor {
        public static void setCurrentTheme(Theme theme) {
            currentTheme = theme;
        }
    }

    private static Theme currentTheme = MOUSE;
    private final String colorsSchemeUrl;
    private final String iconsUrl;
    private final String controlSkinUrl;
    private final String sizesUrl;

    Theme(String colorsScheme, String icons, String sizes, String controlSkin) {
        Class<? extends Theme> thisClass = getClass();
        colorsSchemeUrl = thisClass.getResource(colorsScheme).toExternalForm();
        iconsUrl = thisClass.getResource(icons).toExternalForm();
        sizesUrl = thisClass.getResource(sizes).toExternalForm();
        controlSkinUrl = thisClass.getResource(controlSkin).toExternalForm();
    }

    public String[] getStylesheets() {
        return new String[] {colorsSchemeUrl, iconsUrl, sizesUrl, controlSkinUrl};
    }

    public static Theme currentTheme() {
        return currentTheme;
    }
}
