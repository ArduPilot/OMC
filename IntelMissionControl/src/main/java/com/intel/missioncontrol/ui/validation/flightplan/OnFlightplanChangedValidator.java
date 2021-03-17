/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.validation.ValidatorBase;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import org.asyncfx.concurrent.Dispatcher;

public abstract class OnFlightplanChangedValidator extends ValidatorBase<FlightPlan> {

    private IFlightplanChangeListener flightplanChangeListener =
        new IFlightplanChangeListener() {
            @Override
            public void flightplanStructureChanged(IFlightplanRelatedObject fp) {
                Dispatcher.platform().runLater(() -> invalidate());
            }

            @Override
            public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
                Dispatcher.platform().runLater(() -> invalidate());
            }

            @Override
            public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {}

            @Override
            public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {}
        };

    protected OnFlightplanChangedValidator(FlightPlan flightplan, IQuantityStyleProvider quantityStyleProvider) {
        super(flightplan, quantityStyleProvider);
        flightplan.getLegacyFlightplan().addFPChangeListener(flightplanChangeListener);
    }

    @Override
    protected void onValidationValueChanged(FlightPlan oldFlightplan, FlightPlan newFlightplan) {
        if (oldFlightplan != null) {
            oldFlightplan.getLegacyFlightplan().removeFPChangeListener(flightplanChangeListener);
        }

        if (newFlightplan != null) {
            newFlightplan.getLegacyFlightplan().addFPChangeListener(flightplanChangeListener);
        }
    }

}
