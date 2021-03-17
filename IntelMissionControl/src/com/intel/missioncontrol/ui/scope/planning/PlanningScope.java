/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.scope.planning;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.NotificationObjectProperty;
import com.intel.missioncontrol.api.IFlightPlanService;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.beans.property.PropertyPathStore;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.custom.MuteObjectProperty;
import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import de.saxsys.mvvmfx.Scope;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import java.io.File;
import java.util.Iterator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** EditFlightplanViewModel} */
public class PlanningScope implements Scope {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanningScope.class);

    public static final String EVENT_ON_FLIGHT_PLAN_SAVE = "onFlightPlanSave";
    public static final String EVENT_ON_FLIGHT_PLAN_REVERT_CHANGES = "onFlightPlanRevertChanges";

    private final MuteObjectProperty<FlightPlan> currentFlightplan = new MuteObjectProperty<>();
    private final ListProperty<AreaOfInterest> currentAois =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<FlightPlanTemplate> selectedTemplate = new SimpleObjectProperty<>();
    private final BooleanProperty missionRenameRequested = new SimpleBooleanProperty();
    private final BooleanProperty launchConfirmed = new SimpleBooleanProperty();

    private final MuteObjectProperty<IHardwareConfiguration> selectedHardwareConfiguration =
        new NotificationObjectProperty<>();

    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final IHardwareConfigurationManager hardwareConfigurationManager;
    private final IFlightPlanService planService;

    private final ObservableValue<FlightPlanTemplate> fpTemplatePropery;
    private final ChangeListener<IHardwareConfiguration> iHardwareConfigurationChangeListener;
    private static final String FML = ".fml";
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Inject
    public PlanningScope(
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            IHardwareConfigurationManager hardwareConfigurationManager,
            IFlightPlanService flightPlanService) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        this.hardwareConfigurationManager = hardwareConfigurationManager;
        this.planService = flightPlanService;

        propertyPathStore
            .from(currentFlightplan)
            .selectReadOnlyList(FlightPlan::areasOfInterestProperty)
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (currentFlightplan.get() != null
                            && !currentFlightplan.get().isNameSetProperty().get()
                            && newValue != null
                            && !newValue.isEmpty()) {
                        currentFlightplan
                            .get()
                            .nameProperty()
                            .set(planService.generateDefaultName(newValue.get(0).getType()));
                        currentFlightplan.get().getLegacyFlightplan().setName(currentFlightplan.get().getName());
                        currentFlightplan
                            .get()
                            .getLegacyFlightplan()
                            .setFile(
                                new File(
                                    currentFlightplan.get().getResourceFile().getParent(),
                                    currentFlightplan.get().getName().concat(FML)));
                    }
                });

        fpTemplatePropery =
            PropertyPath.from(currentFlightplan).selectReadOnlyObject(FlightPlan::basedOnTemplateProperty);

        selectedTemplate.bind(
            Bindings.createObjectBinding(
                () -> currentFlightplan.get() == null ? null : currentFlightplan.get().basedOnTemplateProperty().get(),
                currentFlightplan,
                fpTemplatePropery));

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .selectReadOnlyObject(Mission::currentFlightPlanProperty)
            .addListener((observable, oldValue, newValue) -> setCurrentFlightplan(newValue));
        applicationContext
            .currentMissionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        onMissionUnMount();
                    }
                });

        this.missionRenameRequested.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue) {
                    Dispatcher.postToUI(
                        () -> {
                            applicationContext.renameCurrentMission();
                        });

                    missionRenameRequested.set(false);
                }
            });

        selectedHardwareConfiguration.set(hardwareConfigurationManager.getImmutableDefault());
        iHardwareConfigurationChangeListener =
            (observable, oldValue, newValue) -> {
                if (onSettingCurrentFP) {
                    return;
                }

                FlightPlan flightPlan = getCurrentFlightplan();
                if (flightPlan != null) {
                    Flightplan legacyFlightplan = getLegacyFlightplan();
                    legacyFlightplan.updateHardwareConfiguration(newValue);
                    // call map layer redraw on platform type change
                    legacyFlightplan.flightplanStatementChanged(legacyFlightplan);
                }
            };
        selectedHardwareConfiguration.addListener(new WeakChangeListener<>(iHardwareConfigurationChangeListener));
    }

    private Flightplan getLegacyFlightplan() {
        return getCurrentFlightplan().getLegacyFlightplan();
    }

    public ReadOnlyObjectProperty<FlightPlan> currentFlightplanProperty() {
        return currentFlightplan;
    }

    public FlightPlan getCurrentFlightplan() {
        return currentFlightplan.get();
    }

    public ObjectProperty<IHardwareConfiguration> selectedHardwareConfigurationProperty() {
        return selectedHardwareConfiguration;
    }

    public IHardwareConfiguration getSelectedHardwareConfiguration() {
        return selectedHardwareConfiguration.get();
    }

    public ObjectProperty<FlightPlanTemplate> selectedTemplateProperty() {
        return selectedTemplate;
    }

    public BooleanProperty missionRenameRequestedProperty() {
        return missionRenameRequested;
    }

    public BooleanProperty launchConfirmedProperty() {
        return launchConfirmed;
    }

    boolean onSettingCurrentFP;

    private void setCurrentFlightplan(FlightPlan currentFp) {
        if (currentFp == null) {
            onFlightPlanUnMount();
            return;
        }

        onSettingCurrentFP = true;
        currentFlightplan.setValueSilently(currentFp);

        selectedHardwareConfiguration.set(currentFp.getLegacyFlightplan().getHardwareConfiguration());

        currentAois.bindContent(currentFp.areasOfInterestProperty());
        selectedHardwareConfiguration.setValueSilently(currentFp.getLegacyFlightplan().getHardwareConfiguration());

        currentFlightplan.fireChangeEvent();
        onSettingCurrentFP = false;
    }

    public void publishFlightplanSave() {
        publish(EVENT_ON_FLIGHT_PLAN_SAVE);
    }

    /** Should be called be consumer when flight plan is not defined. */
    private void onFlightPlanUnMount() {
        currentFlightplan.set(null);
        currentAois.unbind();
        currentAois.clear();
    }

    /** Should be called be consumer when mission is not defined or is going to be changed. */
    private void onMissionUnMount() {
        onFlightPlanUnMount();
    }

    public int indexOfAoi(AreaOfInterest aoi) {
        return currentAois.indexOf(aoi);
    }

    public void addAoi(int index, AreaOfInterest aoi) {
        currentAois.add(index, aoi);
    }

    public AreaOfInterest removeAoi(int index) {
        return currentAois.remove(index);
    }

    public ReadOnlyIntegerProperty aoiCountProperty() {
        return currentAois.sizeProperty();
    }

    /**
     * Search index of the AOI at flight plan container by given position at the `area` property. There is difference in
     * the indexing of these two lists because flight plan container contains additional items other than PickArea. like
     * starting procedure and landing waypoints.
     *
     * @param areasListId element index of the AOI at flighplan area property
     * @return element index of the pick area at flight plan container
     */
    public int searchContainerIndex(int areasListId) {
        AreaOfInterest areaOfInterest = currentAois.get(areasListId);
        if (areaOfInterest != null) {
            PicArea picArea = areaOfInterest.getPicArea();
            int pickAreaIndex = 0;
            for (Iterator<IFlightplanStatement> it = getLegacyFlightplan().iterator(); it.hasNext(); pickAreaIndex++) {
                // looking for exact object
                if (it.next() == picArea) {
                    return pickAreaIndex;
                }
            }
        }

        return -1;
    }

    public void changeAoiPosition(int sourceId, int destId) {
        int flighplanContainerIndex = searchContainerIndex(destId);
        if (flighplanContainerIndex >= 0) {
            AreaOfInterest movedAoi = removeAoi(sourceId);
            if (movedAoi == null) {
                return;
            }

            addAoi(destId, movedAoi);

            // removing AOI updates legacyFlightplan, while adding does not
            // see FlightPlan.onRemoveAreaOfInterest(AreaOfInterest)
            FlightPlan flightPlan = getCurrentFlightplan();
            if (flightPlan == null) {
                return;
            }

            Flightplan legacyFp = flightPlan.getLegacyFlightplan();
            if (legacyFp == null) {
                return;
            }

            legacyFp.removeFromFlightplanContainer(movedAoi.getPicArea());
            try {
                getLegacyFlightplan().addToFlightplanContainer(flighplanContainerIndex, movedAoi.getPicArea());
            } catch (FlightplanContainerFullException | FlightplanContainerWrongAddingException e) {
                removeAoi(destId);
                addAoi(sourceId, movedAoi);
            }
        }
    }

    public void generateDefaultName(PlanType planType) {
        FlightPlan currentFlightplan = getCurrentFlightplan();

        if (currentFlightplan.getName() == null || !currentFlightplan.isNameSetProperty().getValue()) {
            currentFlightplan.nameProperty().set(planService.generateDefaultName(planType));
            currentFlightplan.isNameSetProperty().set(true);
        }
    }
}
