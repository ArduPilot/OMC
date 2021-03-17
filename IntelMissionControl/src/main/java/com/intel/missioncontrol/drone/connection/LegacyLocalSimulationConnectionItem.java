/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import org.apache.commons.lang3.NotImplementedException;

public class LegacyLocalSimulationConnectionItem implements IConnectionItem {
    private final AsyncStringProperty name = new SimpleAsyncStringProperty(this);
    private final AsyncStringProperty descriptionId = new SimpleAsyncStringProperty(this);
    private final AsyncBooleanProperty isOnline = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty isKnown = new SimpleAsyncBooleanProperty(this);

    private final Mission mission;
    private final FlightPlan flightPlan;

    public LegacyLocalSimulationConnectionItem(Mission mission, FlightPlan flightPlan) {
        this.mission = mission;
        this.flightPlan = flightPlan;
        isOnline.set(true);
        isKnown.set(true);
    }

    Mission getMission() {
        return mission;
    }

    FlightPlan getFlightPlan() {
        return flightPlan;
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
        return new LegacyLocalSimulationConnectionItem(mission, flightPlan);
    }

    @Override
    public boolean isSameConnection(IReadOnlyConnectionItem other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        LegacyLocalSimulationConnectionItem o = (LegacyLocalSimulationConnectionItem)other;

        return getMission().equals(o.getMission()) && getFlightPlan().equals((o.getFlightPlan()));
    }

    @Override
    public void set(IConnectionItem other) {
        // TODO
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
