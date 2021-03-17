/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.custom;

import javafx.beans.property.SimpleObjectProperty;

public class MuteObjectProperty<T> extends SimpleObjectProperty<T> {
    private boolean isMuted = false;

    public void setValueSilently(T valueSilently) {
        isMuted = true;
        try {
            setValue(valueSilently);
        } finally {
            isMuted = false;
        }
    }

    @Override
    protected void fireValueChangedEvent() {
        if (!isMuted) {
            super.fireValueChangedEvent();
        }
    }

    public void fireChangeEvent() {
        super.fireValueChangedEvent();
    }
}
