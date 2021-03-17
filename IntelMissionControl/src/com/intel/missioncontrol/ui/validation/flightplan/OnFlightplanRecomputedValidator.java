/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.validation.ValidatorBase;
import eu.mavinci.desktop.helper.IRecomputeListener;
import javafx.application.Platform;

public abstract class OnFlightplanRecomputedValidator extends ValidatorBase<FlightPlan> {

    private IRecomputeListener recomputeListener =
        (recomputer, anotherRecomputeIsWaiting, runNo) -> Platform.runLater(this::invalidate);

    protected OnFlightplanRecomputedValidator(FlightPlan flightplan, IQuantityStyleProvider quantityStyleProvider) {
        super(flightplan, quantityStyleProvider);
        flightplan.getLegacyFlightplan().getFPcoverage().addRecomputeListener(recomputeListener);
    }

    @Override
    protected void onValidationValueChanged(FlightPlan oldFlightplan, FlightPlan newFlightplan) {
        if (oldFlightplan != null) {
            oldFlightplan.getLegacyFlightplan().getFPcoverage().removeRecomputeListener(recomputeListener);
        }

        if (newFlightplan != null) {
            newFlightplan.getLegacyFlightplan().getFPcoverage().addRecomputeListener(recomputeListener);
        }
    }

}
