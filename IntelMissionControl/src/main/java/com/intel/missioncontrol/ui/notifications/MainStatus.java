/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.notifications;

import java.util.Objects;

public class MainStatus {

    private String text;
    private MainStatusType type;

    public MainStatus(String text, MainStatusType type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public MainStatusType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MainStatus)) {
            return false;
        }

        MainStatus other = (MainStatus)obj;
        return Objects.equals(text, other.text) && Objects.equals(type, other.type);
    }

}
