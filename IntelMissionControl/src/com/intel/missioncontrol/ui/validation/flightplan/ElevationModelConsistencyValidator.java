/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.ElevationModelRequestException;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.flightplan.computation.FPsim;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * check A-19: check if the entire mission is simulated on top of the same elevation model, or if more then one
 * elevation model is covered by this mission
 */
public class ElevationModelConsistencyValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        ElevationModelConsistencyValidator create(FlightPlan flightPlan);
    }

    private static Logger LOGGER = LoggerFactory.getLogger(ElevationModelConsistencyValidator.class);
    private final String className = ElevationModelConsistencyValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public ElevationModelConsistencyValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        FPsim.SimResultData simResult = flightplan.getLegacyFlightplan().getFPsim().getSimResult();
        if (simResult == null) {
            return false;
        }

        if (simResult.simDistances.isEmpty()) {
            return true;
        }

        CompoundElevationModel.ElevationModelRerence takeOffElevationModelReference =
            new CompoundElevationModel.ElevationModelRerence();
        CompoundElevationModel.ElevationModelRerence refPointElevationModelReference =
            new CompoundElevationModel.ElevationModelRerence();
        Position takeOffPosition = flightplan.takeoffPositionProperty().get();
        Position refPointPosition = flightplan.refPointPositionProperty().get();
        IElevationModel elevationModel = DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);
        try {
            elevationModel.getElevation(
                takeOffPosition, true, IElevationModel.MIN_RESOLUTION_REQUEST_METER, takeOffElevationModelReference);
            elevationModel.getElevation(
                refPointPosition, true, IElevationModel.MIN_RESOLUTION_REQUEST_METER, refPointElevationModelReference);

            if (takeOffElevationModelReference.elevationModel == null
                    || refPointElevationModelReference.elevationModel == null) {
                LOGGER.error("Not possible to find the elevation model");
                return false;
            }

            if (takeOffElevationModelReference.elevationModel != refPointElevationModelReference.elevationModel) {
                addWarning(
                    languageHelper.getString(className + ".moreThenOnceSource"), ValidationMessageCategory.NORMAL);
                return true;
            }

        } catch (ElevationModelRequestException e) {
            LOGGER.info("Not possible to find the elevation model", e);
            return false;
        }

        for (FPsim.SimDistance simPoint : simResult.simDistances) {
            if (simPoint.elevationSource.get() != takeOffElevationModelReference.elevationModel) {
                addWarning(
                    languageHelper.getString(className + ".moreThenOnceSource"), ValidationMessageCategory.NORMAL);
                return true;
            }
        }

        return true;
    }

}
