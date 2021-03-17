/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.aoi;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.flightplan.PicArea;
import java.util.LinkedList;

/** check A-17: Distance to object is too small or too large */
public class Restrictions3DValidator extends OnPicAreaChangedValidator {

    public interface Factory {
        Restrictions3DValidator create(PicArea picArea);
    }

    // delta is introduced to deal with rounding issues from the UI
    private static final double DELTA = 0.001;
    private final String className = Restrictions3DValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public Restrictions3DValidator(
            ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider, @Assisted PicArea picArea) {
        super(picArea, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(PicArea picArea) {
        if (!picArea.getPlanType().supportsCrop()) {
            return false;
        }

        if (!picArea.wasRecalconce()) {
            return false;
        }

        // put some safety maring here for numerical stability
        double dist = picArea.getAlt();
        double minDistComputed = picArea.getDistMinComputed();
        double minDist = picArea.getMinObjectDistance();
        double maxDist = picArea.getMaxObjectDistance();
        if (dist < minDist || dist > maxDist || minDistComputed < minDist) {
            double newDist = MathHelper.intoRange(dist, minDist, maxDist);
            if (minDistComputed < minDist) {
                newDist += (minDist - minDistComputed);
            }

            double gsd =
                CameraHelper.estimateGsdAtDistance(newDist, picArea.getFlightplan().getHardwareConfiguration());

            LinkedList<IResolveAction> actions = new LinkedList<>();
            actions.add(
                new SimpleResolveAction(
                    languageHelper.getString(className + ".changeGsd"),
                    () -> picArea.setGsd((dist < minDist || minDistComputed < minDist) ? gsd + DELTA : gsd - DELTA)));

            addWarning(
                languageHelper.getString(
                    className + ((dist < minDist || minDistComputed < minDist) ? ".tooClose" : ".tooFar")),
                ValidationMessageCategory.BLOCKING,
                actions.toArray(new IResolveAction[actions.size()]));
            return true;
        }

        return false;
    }

}
