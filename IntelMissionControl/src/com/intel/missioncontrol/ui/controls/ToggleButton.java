/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.SuppressFBWarnings;
import com.intel.missioncontrol.ui.accessibility.IShortcutAware;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class ToggleButton extends javafx.scene.control.ToggleButton implements IShortcutAware {

    private final StringProperty shortcut = new SimpleStringProperty();

    public StringProperty shortcutProperty() {
        return shortcut;
    }

    @Nullable
    public String getShortcut() {
        return shortcut.get();
    }

    public void setShortcut(@Nullable String shortcut) {
        this.shortcut.set(shortcut);
    }

}
