/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.desktop.main.debug.Debug;
import java.util.logging.Level;

public abstract class CAFlightplanstatement implements IFlightplanRelatedObject {

    protected IFlightplanContainer cont;

    protected CAFlightplanstatement(IFlightplanContainer fp) {
        this.cont = fp;
    }

    protected CAFlightplanstatement() {}

    public CFlightplan getFlightplan() {
        // make this threadsave
        IFlightplanContainer cont = this.cont;
        if (cont == null) {
            Debug.getLog().log(Level.WARNING, "cont is null in method getFlightplan of CAFlightplanstatement.java");
            return null;
        }

        return cont.getFlightplan();
    }

    public void informChangeListener() {
        // make this threadsave
        IFlightplanContainer cont = this.cont;
        if (cont != null) {
            cont.flightplanStatementChanged(this);
        }
    }

    public IFlightplanContainer getParent() {
        return cont;
    }

    public void setParent(IFlightplanContainer container) {
        cont = container;
    }

}
