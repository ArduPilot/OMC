/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.airplane;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.IVeryUglyDialogHelper;
import com.intel.missioncontrol.ui.dialogs.ProgressTask;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.desktop.gui.widgets.IHintServiceBase;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.plane.IAirplane;
import java.util.logging.Level;
import javafx.util.Duration;

/** check B-04: "automatic landing position" was not measured during this drone power cycle (sirius). */
public class AutoLandingSetupValidator extends AirplaneValidatorBase {

    public interface Factory {
        AutoLandingSetupValidator create(FlightplanAirplanePair flightplanAirplanePair);
    }

    private final String className = AutoLandingSetupValidator.class.getName();

    @Inject
    public AutoLandingSetupValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightplanAirplanePair flightplanAirplanePair) {
        super(languageHelper, quantityStyleProvider, flightplanAirplanePair);
        addPlaneInfoListener();
        addFlightplanChangeListener();
    }

    @Override
    protected boolean onInvalidated(FlightplanAirplanePair flightplanAirplanePair) {
        final Flightplan fp = flightplanAirplanePair.getFlightplan();
        if (fp == null) {
            return false;
        }

        final IAirplane plane = flightplanAirplanePair.getPlane();

        boolean bad = false;
        try {
            if (fp.getLandingpoint().getMode() == LandingModes.DESC_FULL3d) {
                if (!fp.getLandingpoint().isLastAutoLandingRefStartPosValid(plane)) {
                    bad = true;
                }

                setOkMessage(languageHelper.getString(className + ".okMessage"));
            } else {
                setOkMessage("");
            }
        } catch (AirplaneCacheEmptyException e) {
            return false;
        }

        if (bad) {
            addWarning(
                languageHelper.getString(className + ".autoLandingPointMeasurementsTooOld"),
                ValidationMessageCategory.BLOCKING,
                new SimpleResolveAction(
                    languageHelper.getString(className + ".remeasure"),
                    () -> {
                        IDialogService dialogService =
                            DependencyInjector.getInstance().getInstanceOf(IDialogService.class);
                        IVeryUglyDialogHelper dialogHelper =
                            DependencyInjector.getInstance().getInstanceOf(IVeryUglyDialogHelper.class);

                        final IHintServiceBase hintService =
                            new IHintServiceBase() {
                                private IApplicationContext applicationContext =
                                    DependencyInjector.getInstance().getInstanceOf(IApplicationContext.class);

                                @Override
                                public void showHint(String text) {
                                    applicationContext.addToast(Toast.of(ToastType.INFO).setText(text).create());
                                }

                                @Override
                                public void showHint(String text, int ttlSeconds) {
                                    applicationContext.addToast(
                                        Toast.of(ToastType.INFO)
                                            .setText(text)
                                            .setTimeout(Duration.seconds(ttlSeconds))
                                            .create());
                                }

                                @Override
                                public void showAlert(String text) {
                                    applicationContext.addToast(Toast.of(ToastType.ALERT).setText(text).create());
                                }

                                @Override
                                public void showAlert(String text, int ttlSeconds) {
                                    applicationContext.addToast(
                                        Toast.of(ToastType.ALERT)
                                            .setText(text)
                                            .setTimeout(Duration.seconds(ttlSeconds))
                                            .create());
                                }

                            };

                        ProgressTask progressMonitor =
                            new ProgressTask(
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.planning.landing.StartingLandingView.askUavLandingPosTitle"),
                                dialogService,
                                10_000) {
                                @Override
                                protected Void call() {
                                    try {
                                        fp.getLandingpoint()
                                            .setFromCurrentAirplane(plane, this, hintService, languageHelper);
                                    } catch (AirplaneCacheEmptyException e) {
                                        Debug.getLog()
                                            .log(Level.SEVERE, "Can not update landing position from airplane.", e);
                                    }

                                    return null;
                                }
                            };
                        dialogHelper.createProgressDialogFromTask(
                            progressMonitor,
                            languageHelper.getString(
                                "com.intel.missioncontrol.ui.planning.landing.StartingLandingView.askUavLandingPosTitle"),
                            languageHelper.getString(
                                "com.intel.missioncontrol.ui.planning.landing.StartingLandingView.askUavLandingPosHeader"));
                    }),
                new SimpleResolveAction(
                    languageHelper.getString(className + ".switchToStayAirborne"),
                    () -> fp.getLandingpoint().setMode(LandingModes.DESC_STAYAIRBORNE)));
        }

        return true;
    }

}
