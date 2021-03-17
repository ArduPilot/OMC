/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncStringProperty;

public interface IConnectionItem extends IReadOnlyConnectionItem {
    AsyncStringProperty nameProperty();

    AsyncStringProperty descriptionIdProperty();

    AsyncBooleanProperty isOnlineProperty();

    AsyncBooleanProperty isKnownProperty();

    void set(IConnectionItem other);
    void bindContent(IConnectionItem other);
    void unbind();
    boolean isBound();
}
