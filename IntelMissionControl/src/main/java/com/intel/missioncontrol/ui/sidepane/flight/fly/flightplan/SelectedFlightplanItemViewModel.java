/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.FPcoveragePreview;
import eu.mavinci.desktop.helper.IRecomputeListener;
import eu.mavinci.flightplan.Flightplan;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;
import org.asyncfx.concurrent.Dispatcher;

public class SelectedFlightplanItemViewModel extends ViewModelBase {
    private final GeneralSettings quantityStyleProvider;

    @InjectScope
    private FlightScope flightScope;

    private final ILanguageHelper languageHelper;

    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<FlightPlan> activeFlightPlan = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<FlightPlan> selectedFlightPlan = new UIAsyncObjectProperty<>(this);

    private final UIAsyncIntegerProperty activeNextWaypointIndex = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty activeFlightPlanWaypointCount = new UIAsyncIntegerProperty(this);

    private final UIAsyncStringProperty waypointsCount = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty nameProperty = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty statusProperty = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty savedOnProperty = new UIAsyncStringProperty(this);
    private final QuantityProperty<Dimension.Time> flightTime;

    private final UIAsyncStringProperty imagesProperty = new UIAsyncStringProperty(this);

    private final UIAsyncObjectProperty<FlightSegment> flightSegment = new UIAsyncObjectProperty<>(this);

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

    private void updateRecomputedValues() {
        if (selectedFlightPlan.get() != null
                && selectedFlightPlan.get().getLegacyFlightplan() != null
                && selectedFlightPlan.get().getLegacyFlightplan().getFPsim() != null
                && selectedFlightPlan.get().getLegacyFlightplan().getFPsim().getSimResult() != null) {
            imagesProperty.set(
                String.valueOf(selectedFlightPlan.get().getLegacyFlightplan().getFPsim().getSimResult().pic_count));
        } else {
            imagesProperty.set("-");
        }

        try {
            double time = selectedFlightPlan.get().getLegacyFlightplan().getFPsim().getSimResult().flightTime;
            flightTime.set(Quantity.of(time, Unit.SECOND).convertTo(Unit.MINUTE));
        } catch (Exception e) {
            flightTime.set(null);
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    AsyncObjectProperty<Mission> mission = new SimpleAsyncObjectProperty<>(this);

    @Inject
    public SelectedFlightplanItemViewModel(
            ILanguageHelper languageHelper, IApplicationContext applicationContext, ISettingsManager settingsManager) {
        this.languageHelper = languageHelper;

        mission.bind(applicationContext.currentMissionProperty());
        selectedFlightPlan.bindBidirectional(
            propertyPathStore.from(mission).selectObject(Mission::currentFlightPlanProperty));
        this.quantityStyleProvider = settingsManager.getSection(GeneralSettings.class);
        this.flightTime = new SimpleQuantityProperty<>(quantityStyleProvider, UnitInfo.TIME_MINUTES);
        this.recomputeReadyListener =
            (a, b, c) -> {
                Dispatcher.platform().runLater(this::updateRecomputedValues);
            };
    }

    public void initializeViewModel() {
        super.initializeViewModel();

        drone.bind(flightScope.currentDroneProperty());
        flightSegment.bind(flightScope.flightSegmentProperty());
        activeFlightPlan.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::activeFlightPlanProperty));

        nameProperty.bind(
            Bindings.createStringBinding(
                () -> {
                    if (selectedFlightPlan.get() != null) {
                        return languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanItemView.nameHeader",
                            selectedFlightPlan.get().nameProperty().get());
                    } else {
                        return languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanItemView.nameHeader",
                            "-");
                    }
                },
                selectedFlightPlan,
                PropertyPath.from(selectedFlightPlan).selectString(FlightPlan::nameProperty)));

        savedOnProperty.bind(
            Bindings.createStringBinding(
                () -> {
                    if (selectedFlightPlan.get() != null) {
                        return selectedFlightPlan.get().saveDateStringProperty().get();
                    } else {
                        return "-";
                    }
                },
                selectedFlightPlan,
                PropertyPath.from(selectedFlightPlan).selectString(FlightPlan::saveDateStringProperty)));

        activeNextWaypointIndex.bind(
            PropertyPath.from(drone).selectReadOnlyAsyncInteger(IDrone::activeFlightPlanWaypointIndexProperty));

        selectedFlightPlan.addListener(
            (observable, oldValue, newValue) -> {
                if (oldValue != null) {
                    Flightplan legacyFlightplan = oldValue.getLegacyFlightplan();
                    Expect.notNull(legacyFlightplan, "legacyFlightplan");
                    FPcoveragePreview fpCoveragePreview = legacyFlightplan.getFPcoverage();
                    fpCoveragePreview.removeRecomputeListener(recomputeReadyListener);
                    legacyFlightplan.removeFPChangeListener(flightplanChangeListener);
                    updateRecomputedValues();
                }

                if (newValue != null) {
                    Flightplan legacyFlightplan = newValue.getLegacyFlightplan();
                    Expect.notNull(legacyFlightplan, "legacyFlightplan");
                    FPcoveragePreview fpCoveragePreview = legacyFlightplan.getFPcoverage();
                    fpCoveragePreview.addRecomputeListener(recomputeReadyListener);
                    legacyFlightplan.addFPChangeListener(flightplanChangeListener);
                    updateRecomputedValues();
                }
            });

        activeFlightPlanWaypointCount.bind(
            Bindings.createIntegerBinding(
                () -> activeFlightPlan.get() != null ? activeFlightPlan.get().waypointsProperty().getSize() : 0,
                activeFlightPlan,
                PropertyPath.from(activeFlightPlan).selectList(FlightPlan::waypointsProperty)));

        waypointsCount.bind(
            Bindings.createStringBinding(
                () -> {
                    if (selectedFlightPlan.get() == null) {
                        return languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanItemView.waypointsCount",
                            "0",
                            "0");
                    }

                    if (activeFlightPlan.get() != null && activeFlightPlan.get().equals(selectedFlightPlan.get())) {
                        return languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanItemView.waypointsCount",
                            activeNextWaypointIndex.get(),
                            activeFlightPlanWaypointCount.get());
                    }

                    return languageHelper.getString(
                        "com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanItemView.waypointsCount",
                        "0",
                        selectedFlightPlan.get().waypointsProperty().get().size());
                },
                selectedFlightPlan,
                activeFlightPlan,
                activeNextWaypointIndex,
                activeFlightPlanWaypointCount,
                PropertyPath.from(selectedFlightPlan).selectList(FlightPlan::waypointsProperty)));

        statusProperty.bind(
            Bindings.createStringBinding(
                () -> {
                    if (activeFlightPlan.get() != null && activeFlightPlan.get().equals(selectedFlightPlan.get())) {
                        if (flightSegment.get() == FlightSegment.UNKNOWN) {
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanItemView.statusOnGround");
                        } else if (flightSegment.get() == FlightSegment.ON_GROUND) {
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanItemView.statusUnknown");
                        } else if (flightSegment.get() == FlightSegment.HOLD) {
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanItemView.statusHold");
                        } else {
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanItemView.statusExecuting");
                        }
                    } else {
                        return languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan.FlightplanItemView.statusDefault");
                    }
                },
                selectedFlightPlan,
                activeFlightPlan,
                flightSegment));
    }

    public ObservableValue<? extends String> getFlightplanName() {
        return nameProperty;
    }

    public ObservableValue<? extends String> getStatus() {
        return statusProperty;
    }

    public ObservableValue<? extends String> getSavedOn() {
        return savedOnProperty;
    }

    public QuantityProperty<Dimension.Time> flightTimeProperty() {
        return flightTime;
    }

    public ObservableValue<? extends String> getImages() {
        return imagesProperty;
    }

    public ObservableValue<? extends String> getWaypointsCount() {
        return waypointsCount;
    }
}
