/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanStatement;

public abstract class FlightplanStatement implements IFlightplanStatement {

    private IFlightplanContainer container;

    protected FlightplanStatement(IFlightplanContainer container) {
        this.container = container;
    }

    protected FlightplanStatement() {}

    public CFlightplan getFlightplan() {
        // make this threadsafe
        if (this.container == null) {
            return null;
        }

        return this.container.getFlightplan();
    }

    protected void notifyStatementChanged() {
        // make this threadsafe
        if (this.container != null) {
            this.container.flightplanStatementChanged(this);
        }
    }

    public IFlightplanContainer getParent() {
        return this.container;
    }

    public void setParent(IFlightplanContainer container) {
        this.container = container;
    }

}
