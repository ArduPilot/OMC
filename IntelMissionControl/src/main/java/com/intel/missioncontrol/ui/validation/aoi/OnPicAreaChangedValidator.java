/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.aoi;

import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.ui.validation.ValidatorBase;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import org.asyncfx.concurrent.Dispatcher;

abstract class OnPicAreaChangedValidator extends ValidatorBase<PicArea> {

    private final IFlightplanChangeListener flightplanChangeListener =
        new IFlightplanChangeListener() {
            @Override
            public void flightplanStructureChanged(IFlightplanRelatedObject fp) {
                if (fp == getParentFlightPlan() || fp == getValidationValue()) {
                    Dispatcher.platform().runLater(() -> invalidate());
                }
            }

            @Override
            public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
                if (fpObj == getParentFlightPlan() || fpObj == getValidationValue()) {
                    Dispatcher.platform().runLater(() -> invalidate());
                }
            }

            @Override
            public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {}

            @Override
            public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {}
        };

    OnPicAreaChangedValidator(PicArea picArea, IQuantityStyleProvider quantityStyleProvider) {
        super(picArea, quantityStyleProvider);
        Flightplan flightplan = picArea.getFlightplan();
        if (flightplan != null) {
            flightplan.addFPChangeListener(flightplanChangeListener);
        }
    }

    @Override
    protected void onValidationValueChanged(PicArea oldPicArea, PicArea newPicArea) {
        if (oldPicArea != null) {
            Flightplan oldflightplan = oldPicArea.getFlightplan();
            if (oldflightplan != null) {
                oldflightplan.removeFPChangeListener(flightplanChangeListener);
            }
        }

        if (newPicArea != null) {
            Flightplan nflightplan = newPicArea.getFlightplan();
            if (nflightplan != null) {
                nflightplan.addFPChangeListener(flightplanChangeListener);
            }
        }
    }

    private Flightplan getParentFlightPlan() {
        PicArea picArea = getValidationValue();
        return picArea.getFlightplan();
    }

}
