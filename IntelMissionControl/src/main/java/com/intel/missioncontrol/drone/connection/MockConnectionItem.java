/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.NotImplementedException;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;

public class MockConnectionItem implements IConnectionItem {

    private final AsyncStringProperty name = new SimpleAsyncStringProperty(this);
    private final AsyncStringProperty descriptionId = new SimpleAsyncStringProperty(this);
    private final AsyncBooleanProperty isOnline = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty isKnown = new SimpleAsyncBooleanProperty(this);

    MockConnectionItem() {
        name.set("Mock Drone");
        descriptionId.set("MockDrone");
        isOnline.set(true);
        isKnown.set(true);
    }

    @Override
    public AsyncStringProperty nameProperty() {
        return name;
    }

    @Override
    public AsyncStringProperty descriptionIdProperty() {
        return descriptionId;
    }

    @Override
    public AsyncBooleanProperty isOnlineProperty() {
        return isOnline;
    }

    @Override
    public AsyncBooleanProperty isKnownProperty() {
        return isKnown;
    }

    @Override
    public IConnectionItem createMutableCopy() {
        return new MockConnectionItem();
    }

    @Override
    public boolean isSameConnection(IReadOnlyConnectionItem other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return true;
    }

    @Override
    public void set(IConnectionItem other) {
        if (!(other instanceof LegacyLocalSimulationConnectionItem)) {
            throw new IllegalArgumentException("Invalid IConnectionItem type");
        }
    }

    @Override
    public void bindContent(IConnectionItem other) {
        throw new NotImplementedException("bindContent not implemented");
    }

    @Override
    public void unbind() {
        throw new NotImplementedException("unbind not implemented");
    }

    @Override
    public boolean isBound() {
        return false;
    }

}
