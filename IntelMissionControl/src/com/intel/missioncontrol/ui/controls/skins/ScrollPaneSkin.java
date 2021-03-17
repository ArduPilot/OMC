/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls.skins;

import java.lang.reflect.Field;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

/**
 * The scroll pane implementation in JDK 8, 9 and 10 produces blurry content at times. Disabling caching for the scroll
 * pane's content seems to be a workaround for this problem. This skin should be set to all scroll panes by default via
 * the -fx-skin CSS property.
 */
@SuppressWarnings("unused")
public class ScrollPaneSkin extends javafx.scene.control.skin.ScrollPaneSkin {

    private static Field viewRectField;

    static {
        try {
            viewRectField = javafx.scene.control.skin.ScrollPaneSkin.class.getDeclaredField("viewRect");
            viewRectField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public ScrollPaneSkin(final ScrollPane scrollpane) {
        super(scrollpane);

        try {
            StackPane viewRect = (StackPane)viewRectField.get(this);
            viewRect.setCache(false);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
