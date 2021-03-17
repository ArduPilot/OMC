/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.ui.validation.airplane.AirplaneValidationAggregator;
import com.intel.missioncontrol.ui.validation.airplane.FlightplanAirplanePair;
import com.intel.missioncontrol.ui.validation.aoi.AoiValidationAggregator;
import com.intel.missioncontrol.ui.validation.flightplan.FlightplanValidationAggregator;
import com.intel.missioncontrol.ui.validation.matching.MatchingValidationAggregator;
import eu.mavinci.flightplan.PicArea;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

public class ValidationService implements IValidationService {

    private final ObjectProperty<FlightPlan> observedFlightPlan =
        new SimpleObjectProperty<>() {
            @Override
            protected void invalidated() {
                super.invalidated();
                initFlightPlan(get());
            }
        };

    private final ObjectProperty<Matching> observedMatching =
        new SimpleObjectProperty<>() {
            @Override
            protected void invalidated() {
                super.invalidated();
                initMatching(get());
            }
        };

    private final ListProperty<ResolvableValidationMessage> flightValidationMessages =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<ResolvableValidationMessage> planningValidationMessages =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<ResolvableValidationMessage> datasetValidationMessages =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<ResolvableValidationStatus> flightAndPlanAllValidationStatuses =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final BooleanProperty canExecuteFlight = new SimpleBooleanProperty(true);
    private final BooleanProperty canExportFlight = new SimpleBooleanProperty(true);
    private final BooleanProperty canExportDataset = new SimpleBooleanProperty(true);

    private final List<InvalidationListener> listeners = new ArrayList<>();
    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final IQuantityStyleProvider quantityStyleProvider;

    private final List<AoiValidationAggregator> aoiValidationAggregators = new ArrayList<>();
    private FlightplanValidationAggregator.Factory flightplanValidationAggregatorFactory;
    private MatchingValidationAggregator.Factory matchingValidationAggregatorFactory;
    private AirplaneValidationAggregator.Factory airplaneValidationAggregatorFactory;
    private AoiValidationAggregator.Factory aoiValidationAggregatorFactory;
    private AirplaneValidationAggregator airplaneValidationAggregator;
    private FlightplanValidationAggregator flightplanValidationAggregator;
    private MatchingValidationAggregator matchingValidationAggregator;
    private boolean isAirPlaneConnected;

    @Inject
    public ValidationService(
            FlightplanValidationAggregator.Factory flightplanValidationAggregatorFactory,
            AirplaneValidationAggregator.Factory airplaneValidationAggregatorFactory,
            AoiValidationAggregator.Factory aoiValidationAggregatorFactory,
            MatchingValidationAggregator.Factory matchingValidationAggregatorFactory,
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            GeneralSettings generalSettings,
            IQuantityStyleProvider quantityStyleProvider) {
        this.flightplanValidationAggregatorFactory = flightplanValidationAggregatorFactory;
        this.airplaneValidationAggregatorFactory = airplaneValidationAggregatorFactory;
        this.aoiValidationAggregatorFactory = aoiValidationAggregatorFactory;
        this.matchingValidationAggregatorFactory = matchingValidationAggregatorFactory;
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        this.quantityStyleProvider = quantityStyleProvider;
        applicationContext.currentMissionProperty().addListener(this::currentMissionChanged);

        observedFlightPlanProperty()
            .bind(
                PropertyPath.from(applicationContext.currentMissionProperty())
                    .selectReadOnlyObject(Mission::currentFlightPlanProperty));

        observedMatchingProperty()
            .bind(
                PropertyPath.from(applicationContext.currentMissionProperty())
                    .selectReadOnlyObject(Mission::currentMatchingProperty));

        generalSettings.systemOfMeasurementProperty().addListener((observable, oldValue, newValue) -> revalidateAll());
    }

    protected void revalidateAll() {
        aoiValidationAggregators.stream().forEach(a -> revalidateAll(a));
        revalidateAll(airplaneValidationAggregator);
        revalidateAll(flightplanValidationAggregator);
        revalidateAll(matchingValidationAggregator);
    }

    private <T> void revalidateAll(ValidationAggregator<T> aggregator) {
        if (aggregator == null) {
            return;
        }

        for (ValidatorBase<T> validator : aggregator.getValidators()) {
            validator.invalidate();
        }
    }

    public ObjectProperty<FlightPlan> observedFlightPlanProperty() {
        return observedFlightPlan;
    }

    public ObjectProperty<Matching> observedMatchingProperty() {
        return observedMatching;
    }

    public ReadOnlyListProperty<ResolvableValidationMessage> planningValidationMessagesProperty() {
        return planningValidationMessages;
    }

    public ReadOnlyListProperty<ResolvableValidationMessage> flightValidationMessagesProperty() {
        return flightValidationMessages;
    }

    public ReadOnlyListProperty<ResolvableValidationMessage> datasetValidationMessagesProperty() {
        return datasetValidationMessages;
    }

    public ReadOnlyListProperty<ResolvableValidationStatus> flightAndPlanAllValidationStatusesProperty() {
        return flightAndPlanAllValidationStatuses;
    }

    public ReadOnlyBooleanProperty canExecuteFlightProperty() {
        return canExecuteFlight;
    }

    public ReadOnlyBooleanProperty canExportDatasetProperty() {
        return canExportDataset;
    }

    public ReadOnlyBooleanProperty canExportFlightProperty() {
        return canExportFlight;
    }

    private void currentMissionChanged(
            ObservableValue<? extends Mission> observable, Mission oldValue, Mission newValue) {
        if (newValue != null) {
            if (isAirPlaneConnected) {
                initAirPlaneValidator(isAirPlaneConnected);
            }
        }
    }

    @Override
    public void notifyAirplaneConnected() {
        isAirPlaneConnected = true;
        initAirPlaneValidator(isAirPlaneConnected);
    }

    @Override
    public void notifyAirplaneDisconnected() {
        isAirPlaneConnected = false;
        initAirPlaneValidator(isAirPlaneConnected);
    }

    @Override
    public void addValidatorsChangedListener(InvalidationListener listener) {
        listeners.add(new WeakInvalidationListener(listener));
    }

    private void initMatching(Matching matching) {
        matchingValidationAggregator = null;

        if (matching != null) {
            matchingValidationAggregator = matchingValidationAggregatorFactory.create(matching);

            for (ResolvableValidationStatus validationStatus : matchingValidationAggregator.getValidationStatuses()) {
                validationStatus
                    .getResolveableMessages()
                    .addListener((InvalidationListener)observable -> refreshMessagesDataset());
            }
        }

        refreshMessagesDataset();
    }

    private void initFlightPlan(FlightPlan flightPlan) {
        flightplanValidationAggregator = null;
        aoiValidationAggregators.clear();

        if (flightPlan != null) {
            flightplanValidationAggregator = flightplanValidationAggregatorFactory.create(flightPlan);
            initAoiValidators(flightPlan);

            flightPlan
                .areasOfInterestProperty()
                .addListener(
                    (ListChangeListener<AreaOfInterest>)
                        l -> {
                            aoiValidationAggregators.clear();
                            initAoiValidators(flightPlan);
                            initListeners();
                        });

            initListeners();
        }

        refreshMessagesFlightplan();
        updateAllStatuses();
    }

    private void initAirPlaneValidator(boolean isAirPlaneConnected) {
        Mission currentMission = applicationContext.getCurrentMission();
        if (currentMission != null
                && isAirPlaneConnected
                && currentMission.uavProperty().get() != null
                && observedFlightPlan.get() != null
                && currentMission.uavProperty().get().getLegacyPlane() != null) {
            airplaneValidationAggregator =
                airplaneValidationAggregatorFactory.create(
                    observedFlightPlan.get().getLegacyFlightplan(),
                    currentMission.uavProperty().get().getLegacyPlane());
            airplaneValidationAggregator
                .getValidationStatuses()
                .forEach(
                    item ->
                        item.getMessages()
                            .addListener((InvalidationListener)observable -> refreshMessagesFlightplan()));
        } else {
            airplaneValidationAggregator = null;
        }

        refreshMessagesFlightplan();
        updateAllStatuses();
    }

    private void initAoiValidators(FlightPlan flightPlan) {
        for (AreaOfInterest aoi : flightPlan.areasOfInterestProperty()) {
            aoiValidationAggregators.add(aoiValidationAggregatorFactory.create(aoi.getPicArea()));
        }

        refreshMessagesFlightplan();
        updateAllStatuses();
    }

    private void initListeners() {
        if (flightplanValidationAggregator != null) {
            for (ResolvableValidationStatus validationStatus : flightplanValidationAggregator.getValidationStatuses()) {
                validationStatus
                    .getResolveableMessages()
                    .addListener((InvalidationListener)observable -> refreshMessagesFlightplan());
            }
        }

        for (AoiValidationAggregator aoiValidationAggregator : aoiValidationAggregators) {
            for (ResolvableValidationStatus validationStatus : aoiValidationAggregator.getValidationStatuses()) {
                validationStatus
                    .getResolveableMessages()
                    .addListener((InvalidationListener)observable -> refreshMessagesFlightplan());
            }
        }
    }

    private void refreshMessagesFlightplan() {
        ArrayList<ResolvableValidationMessage> updatedFlyingList = new ArrayList<>();
        ArrayList<ResolvableValidationMessage> updatedPlanningList = new ArrayList<>();

        if (airplaneValidationAggregator != null) {
            for (ResolvableValidationStatus validationStatus : airplaneValidationAggregator.getValidationStatuses()) {
                updatedFlyingList.addAll(validationStatus.getResolveableMessages());
            }
        }

        if (flightplanValidationAggregator != null) {
            for (ResolvableValidationStatus validationStatus : flightplanValidationAggregator.getValidationStatuses()) {
                updatedFlyingList.addAll(validationStatus.getResolveableMessages());
                updatedPlanningList.addAll(validationStatus.getResolveableMessages());
            }
        }

        for (AoiValidationAggregator aggregator : aoiValidationAggregators) {
            for (ResolvableValidationStatus validationStatus : aggregator.getValidationStatuses()) {
                updatedFlyingList.addAll(validationStatus.getResolveableMessages());
                updatedPlanningList.addAll(validationStatus.getResolveableMessages());
            }
        }

        canExecuteFlight.setValue(containsNoError(updatedFlyingList));
        canExportFlight.setValue(containsNoError(updatedPlanningList));
        flightValidationMessages.setAll(updatedFlyingList);
        planningValidationMessages.setAll(updatedPlanningList);
    }

    private void refreshMessagesDataset() {
        ArrayList<ResolvableValidationMessage> updatedDatasetList = new ArrayList<>();

        if (matchingValidationAggregator != null) {
            matchingValidationAggregator
                .getValidationStatuses()
                .forEach(m -> updatedDatasetList.addAll(m.getResolveableMessages()));
        }

        canExportDataset.setValue(containsNoError(updatedDatasetList));
        datasetValidationMessages.setAll(updatedDatasetList);
    }

    private boolean containsNoError(List<ResolvableValidationMessage> messages) {
        return messages.stream().noneMatch(m -> m.getCategory() == ValidationMessageCategory.BLOCKING);
    }

    public ValidatorBase<FlightPlan> getValidator(
            Class<? extends ValidatorBase<FlightPlan>> type, FlightPlan validatedObject) {
        Expect.isTrue(validatedObject != observedFlightPlan.get(), "validatedObject");
        return flightplanValidationAggregator.getValidator(type);
    }

    public ValidatorBase<PicArea> getValidator(Class<? extends ValidatorBase<PicArea>> type, PicArea validatedObject) {
        for (AoiValidationAggregator aoiValidationAggregator : aoiValidationAggregators) {
            if (validatedObject != aoiValidationAggregator.getPicArea()) {
                continue;
            }

            return aoiValidationAggregator.getValidator(type);
        }

        throw new IllegalArgumentException("Validator not found: " + type);
    }

    public ValidatorBase<Matching> getValidator(
            Class<? extends ValidatorBase<Matching>> type, Matching validatedObject) {
        Expect.isTrue(
            validatedObject != observedMatching.get(),
            "validatedObject: validated:" + validatedObject + "  observed:" + observedMatching.get());
        return matchingValidationAggregator.getValidator(type);
    }

    public ValidatorBase<FlightplanAirplanePair> getValidator(
            Class<? extends ValidatorBase<FlightplanAirplanePair>> type) {
        return airplaneValidationAggregator.getValidator(type);
    }

    public void updateAllStatuses() {
        flightAndPlanAllValidationStatuses.clear();

        if (airplaneValidationAggregator != null) {
            flightAndPlanAllValidationStatuses.addAll(airplaneValidationAggregator.getValidationStatuses());
        }

        if (flightplanValidationAggregator != null) {
            flightAndPlanAllValidationStatuses.addAll(flightplanValidationAggregator.getValidationStatuses());
        }

        aoiValidationAggregators
            .stream()
            .map((aoiValidationAggregator -> aoiValidationAggregator.getValidationStatuses()))
            .forEach((aoiStatuses -> flightAndPlanAllValidationStatuses.addAll(aoiStatuses)));
    }

}
