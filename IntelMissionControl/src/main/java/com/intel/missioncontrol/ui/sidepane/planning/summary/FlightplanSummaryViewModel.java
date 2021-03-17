/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.summary;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Dimension.Area;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Time;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.planning.EditWaypointsViewModel;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerCoverage;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.FPcoveragePreview;
import eu.mavinci.desktop.helper.IRecomputeListener;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.computation.FPsim;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.concurrent.Dispatcher;
import org.jetbrains.annotations.Nullable;

public class FlightplanSummaryViewModel extends ViewModelBase {

    @InjectScope
    private MainScope mainScope;

    private final IApplicationContext applicationContext;
    private final IQuantityStyleProvider quantityStyleProvider;

    private final QuantityProperty<Time> flightTime;
    private final QuantityProperty<Length> distance;
    private final QuantityProperty<Area> trueOrthoArea;
    private final QuantityProperty<Area> pseudoOrthoArea;
    private final IntegerProperty imageCount = new SimpleIntegerProperty();
    private final DoubleProperty dataSize = new SimpleDoubleProperty();
    private final DoubleProperty trueOrthoCoverageRatio = new SimpleDoubleProperty();
    private final DoubleProperty pseudoOrthoCoverageRatio = new SimpleDoubleProperty();
    private final BooleanProperty trueOrthoCoverageRatioEnabled = new SimpleBooleanProperty();
    private final BooleanProperty pseudoOrthoCoverageRatioEnabled = new SimpleBooleanProperty();
    private final StringProperty notes = new SimpleStringProperty();

    private final IRecomputeListener recomputeReadyListener;
    private final IFlightplanChangeListener flightplanChangeListener =
        new IFlightplanChangeListener() {
            @Override
            public void flightplanStructureChanged(IFlightplanRelatedObject fp) {
                Dispatcher.platform().runLater(() -> updateRecomputedValues());
            }

            @Override
            public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
                Dispatcher.platform().runLater(() -> updateRecomputedValues());
            }

            @Override
            public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {}

            @Override
            public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {}
        };

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Nullable
    private Flightplan currentLegacyFlightplan;

    public BooleanProperty pseudoOrthoCoverageRatioEnabledProperty() {
        return pseudoOrthoCoverageRatioEnabled;
    }

    public BooleanProperty trueOrthoCoverageRatioEnabledProperty() {
        return trueOrthoCoverageRatioEnabled;
    }

    private final Command showEditWayointsDialogCommand;
    private final BooleanProperty canShowEditWayointsDialogCommand = new SimpleBooleanProperty(true);

    @Inject
    public FlightplanSummaryViewModel(
            IApplicationContext applicationContext, ISettingsManager settingsManager, IDialogService dialogService) {
        this.applicationContext = applicationContext;
        this.quantityStyleProvider = settingsManager.getSection(GeneralSettings.class);
        this.flightTime = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.TIME_MINUTES);
        this.distance = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.LOCALIZED_LENGTH);
        this.trueOrthoArea = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.LOCALIZED_AREA);
        this.pseudoOrthoArea = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.LOCALIZED_AREA);
        this.recomputeReadyListener =
            (a, b, c) -> {
                Dispatcher.platform().runLater(this::updateRecomputedValues);
            };

        showEditWayointsDialogCommand =
            new DelegateCommand(
                () -> {
                    canShowEditWayointsDialogCommand.set(false);
                    Futures.addCallback(
                        dialogService.requestDialogAsync(this, EditWaypointsViewModel.class, false),
                        new FutureCallback<>() {
                            @Override
                            public void onSuccess(EditWaypointsViewModel editWaypointsViewModel) {
                                canShowEditWayointsDialogCommand.set(true);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                canShowEditWayointsDialogCommand.set(true);
                            }

                        });
                },
                canShowEditWayointsDialogCommand);

        notes.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectString(FlightPlan::notesProperty));
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        Expect.notNull(mainScope, "mainScope");

        Mission currentMission = applicationContext.getCurrentMission();
        if (currentMission == null) {
            return;
        }

        currentFlightPlanChanged(
            currentMission.currentFlightPlanProperty(), null, currentMission.currentFlightPlanProperty().get());
    }

    public QuantityProperty<Time> flightTimeProperty() {
        return flightTime;
    }

    public QuantityProperty<Length> distanceProperty() {
        return distance;
    }

    public QuantityProperty<Area> trueOrthoAreaProperty() {
        return trueOrthoArea;
    }

    public QuantityProperty<Area> pseudoOrthoAreaProperty() {
        return pseudoOrthoArea;
    }

    public IntegerProperty imageCountProperty() {
        return imageCount;
    }

    public DoubleProperty dataSizeProperty() {
        return dataSize;
    }

    public DoubleProperty trueOrthoCoverageRatioProperty() {
        return trueOrthoCoverageRatio;
    }

    public DoubleProperty pseudoOrthoCoverageRatioProperty() {
        return pseudoOrthoCoverageRatio;
    }

    public Quantity<Time> getFlightTime() {
        return flightTime.get();
    }

    public Quantity<Length> getDistance() {
        return distance.get();
    }

    public Quantity<Area> getTrueOrthoArea() {
        return trueOrthoArea.get();
    }

    public Quantity<Area> getPseudoOrthoArea() {
        return pseudoOrthoArea.get();
    }

    public int getImageCount() {
        return imageCount.get();
    }

    public double getDataSize() {
        return dataSize.get();
    }

    public double getTrueOrthoCoverageRatio() {
        return trueOrthoCoverageRatio.get();
    }

    public double getPseudoOrthoCoverageRatio() {
        return pseudoOrthoCoverageRatio.get();
    }

    private void currentFlightPlanChanged(
            ObservableValue<? extends FlightPlan> observable, FlightPlan oldValue, FlightPlan newValue) {
        if (oldValue != null) {
            Flightplan legacyFlightplan = oldValue.getLegacyFlightplan();
            Expect.notNull(legacyFlightplan, "legacyFlightplan");
            FPcoveragePreview fpCoveragePreview = legacyFlightplan.getFPcoverage();
            fpCoveragePreview.removeRecomputeListener(recomputeReadyListener);
            legacyFlightplan.removeFPChangeListener(flightplanChangeListener);
            currentLegacyFlightplan = null;
        }

        if (newValue != null) {
            Flightplan legacyFlightplan = newValue.getLegacyFlightplan();
            Expect.notNull(legacyFlightplan, "legacyFlightplan");
            FPcoveragePreview fpCoveragePreview = legacyFlightplan.getFPcoverage();
            fpCoveragePreview.addRecomputeListener(recomputeReadyListener);
            legacyFlightplan.addFPChangeListener(flightplanChangeListener);
            currentLegacyFlightplan = legacyFlightplan;
            updateRecomputedValues();
        }
    }

    private void updateRecomputedValues() {
        Flightplan legacyFlightplan = currentLegacyFlightplan;
        if (legacyFlightplan == null) {
            return;
        }

        double trueOrthoCoverageRatio = 0;
        double pseudoOrthoCoverageRatio = 0;
        double trueOrthoArea = 0;
        double pseudoOrthoArea = 0;
        FPsim fpSim = legacyFlightplan.getFPsim();
        AMapLayerCoverage fpCoveragePreview = legacyFlightplan.getFPcoverage();
        // see https://jira.drones.intel.com/browse/IMC-2339
        if (fpCoveragePreview != null && !fpCoveragePreview.containsNonCoverageAbleAOIs()) {
            trueOrthoCoverageRatio = fpCoveragePreview.getCoverageRatioOrtho();
            pseudoOrthoCoverageRatio = fpCoveragePreview.getCoverageRatioPseudoOrtho();
            trueOrthoCoverageRatioEnabled.set(trueOrthoCoverageRatio >= 0);
            pseudoOrthoCoverageRatioEnabled.set(pseudoOrthoCoverageRatio >= 0);
            trueOrthoCoverageRatio = MathHelper.intoRange(trueOrthoCoverageRatio, 0, 1);
            pseudoOrthoCoverageRatio = MathHelper.intoRange(pseudoOrthoCoverageRatio, 0, 1);
            trueOrthoArea = fpCoveragePreview.getQmOK();
            pseudoOrthoArea = fpCoveragePreview.getQmMedium();
        } else {
            trueOrthoCoverageRatioEnabled.set(false);
            pseudoOrthoCoverageRatioEnabled.set(false);
        }

        double distanceInMeters = 0;
        double flightTimeInSeconds = 0;
        int images = 0;
        double estimatedSizeInMb = 0;
        FPsim.SimResultData simResultData = fpSim.getSimResult();
        if (simResultData != null) {
            distanceInMeters = simResultData.distance;
            flightTimeInSeconds = simResultData.flightTime;
            images = simResultData.pic_count;
            estimatedSizeInMb =
                images
                    * legacyFlightplan
                        .getHardwareConfiguration()
                        .getPrimaryPayload(IGenericCameraConfiguration.class)
                        .getDescription()
                        .getPictureSizeInMB();
        }

        imageCount.set(images);
        dataSize.set(estimatedSizeInMb);
        flightTime.set(Quantity.of(flightTimeInSeconds, Unit.SECOND).convertTo(Unit.MINUTE));

        SystemOfMeasurement systemOfMeasurement = quantityStyleProvider.getSystemOfMeasurement();

        distance.set(
            Quantity.of(distanceInMeters, Unit.METER)
                .convertTo(distance.getUnitInfo().getPreferredUnit(systemOfMeasurement)));

        this.trueOrthoArea.set(
            Quantity.of(trueOrthoArea, Unit.SQUARE_METER)
                .convertTo(this.trueOrthoArea.getUnitInfo().getPreferredUnit(systemOfMeasurement)));

        this.pseudoOrthoArea.set(
            Quantity.of(pseudoOrthoArea, Unit.SQUARE_METER)
                .convertTo(this.pseudoOrthoArea.getUnitInfo().getPreferredUnit(systemOfMeasurement)));

        this.trueOrthoCoverageRatio.set(trueOrthoCoverageRatio);
        this.pseudoOrthoCoverageRatio.set(pseudoOrthoCoverageRatio);
    }

    public Command getShowEditWayointsDialogCommand() {
        return showEditWayointsDialogCommand;
    }

    public StringProperty notesProperty() {
        return notes;
    }

}
