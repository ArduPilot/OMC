/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.SuppressFBWarnings;
import com.intel.missioncontrol.ui.controls.skins.ToggleSwitchSkin;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Extension of {@link org.controlsfx.control.ToggleSwitch} with custom skin rendering, that places label text on the
 * right side of the toggle element.
 */
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class ToggleSwitch extends org.controlsfx.control.ToggleSwitch {

    public ToggleSwitch() {
        super();
        addKeyboardEventFilter();
    }

    public ToggleSwitch(String label) {
        super(label);
        addKeyboardEventFilter();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ToggleSwitchSkin(this);
    }

    private void addKeyboardEventFilter() {
        this.addEventFilter(
            KeyEvent.KEY_PRESSED,
            event -> {
                if (event.getCode() == KeyCode.SPACE) {
                    this.setSelected(!this.isSelected());
                }
            });
    }
}
