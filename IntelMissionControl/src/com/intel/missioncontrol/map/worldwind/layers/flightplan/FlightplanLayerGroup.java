/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.flightplan;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.binding.LifecycleValueConverter;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.beans.property.PropertyPathStore;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.collections.LockedList;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.LayerGroup;
import com.intel.missioncontrol.map.LayerGroupType;
import com.intel.missioncontrol.map.LayerName;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.map.worldwind.WorldWindowProvider;
import com.intel.missioncontrol.map.worldwind.layers.aircraft.AircraftLayerVisibilitySettings;
import com.intel.missioncontrol.map.worldwind.layers.dataset.DatasetLayerVisibilitySettings;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import eu.mavinci.core.obfuscation.IKeepClassname;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;

public class FlightplanLayerGroup extends LayerGroup implements IKeepClassname {

    private ChangeListener<Mission> missionChangeListener;

    private class ListenerAdapter implements MouseListener, MouseMotionListener, PositionListener {
        private final FlightplanLayer layer;
        private final FlightPlan flightPlan;

        ListenerAdapter(FlightplanLayer layer, FlightPlan flightPlan) {
            this.layer = layer;
            this.flightPlan = flightPlan;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (currentFlightPlan.get() == flightPlan) {
                layer.getWrappedLayer().mouseClicked(e);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (currentFlightPlan.get() == flightPlan) {
                layer.getWrappedLayer().mousePressed(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (currentFlightPlan.get() == flightPlan) {
                layer.getWrappedLayer().mouseReleased(e);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (currentFlightPlan.get() == flightPlan) {
                layer.getWrappedLayer().mouseEntered(e);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (currentFlightPlan.get() == flightPlan) {
                layer.getWrappedLayer().mouseExited(e);
            }
        }

        @Override
        public void moved(PositionEvent event) {
            if (currentFlightPlan.get() == flightPlan) {
                layer.getWrappedLayer().moved(event);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (currentFlightPlan.get() == flightPlan) {
                layer.getWrappedLayer().mouseDragged(e);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (currentFlightPlan.get() == flightPlan) {
                layer.getWrappedLayer().mouseMoved(e);
            }
        }
    }

    private final SynchronizationRoot syncRoot;
    private final IMapModel mapModel;
    private final IWWMapView mapView;
    private final IMapController mapController;
    private final IElevationModel elevationModel;
    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final ILanguageHelper languageHelper;
    private final GeneralSettings generalSettings;
    private final DatasetLayerVisibilitySettings datasetLayerVisibilitySettings;
    private final FlightplanLayerVisibilitySettings flightplanLayerVisibilitySettings;
    private final AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings;
    private final ISelectionManager selectionManager;
    private final IWWGlobes globes;

    private final AsyncObjectProperty<FlightPlan> currentFlightPlan = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Mission> currentMission = new SimpleAsyncObjectProperty<>(this);
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Inject
    public FlightplanLayerGroup(
            @Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot,
            IMapModel mapModel,
            IWWGlobes globes,
            IWWMapView mapView,
            IMapController mapController,
            IElevationModel elevationModel,
            INavigationService navigationService,
            ILanguageHelper languageHelper,
            IApplicationContext applicationContext,
            ISelectionManager selectionManager,
            WorldWindowProvider worldWindowProvider,
            GeneralSettings generalSettings,
            FlightplanLayerVisibilitySettings flightplanLayerVisibilitySettings,
            AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings,
            DatasetLayerVisibilitySettings datasetLayerVisibilitySettings) {
        super(LayerGroupType.FLIGHT_PLAN_GROUP);
        this.syncRoot = syncRoot;
        this.globes = globes;
        this.mapModel = mapModel;
        this.mapView = mapView;
        this.mapController = mapController;
        this.elevationModel = elevationModel;
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.languageHelper = languageHelper;
        this.selectionManager = selectionManager;
        this.generalSettings = generalSettings;
        this.flightplanLayerVisibilitySettings = flightplanLayerVisibilitySettings;
        this.aircraftLayerVisibilitySettings = aircraftLayerVisibilitySettings;
        this.datasetLayerVisibilitySettings = datasetLayerVisibilitySettings;

        setName(new LayerName("%" + getClass().getName()));

        currentMission.bind(applicationContext.currentMissionProperty());

        datasetLayerVisibilitySettings.showCurrentFlightplanProperty().addListener(observable -> fixVisibility());
        this.aircraftLayerVisibilitySettings.flightPlanProperty().addListener(observable -> fixVisibility());
        flightplanLayerVisibilitySettings.showCurrentFlightplanProperty().addListener(observable -> fixVisibility());
        flightplanLayerVisibilitySettings.showOtherFlightplansProperty().addListener(observable -> fixVisibility());
        navigationService.workflowStepProperty().addListener(observable -> fixVisibility());
        currentFlightPlan.addListener(observable -> fixVisibility());
        currentFlightPlan.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .selectReadOnlyObject(Mission::currentFlightPlanProperty));

        worldWindowProvider.whenAvailable(this::onWorldWindowAvailable);
        internalProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () ->
                        navigationService.getWorkflowStep() != WorkflowStep.PLANNING
                            && generalSettings.getOperationLevel() != OperationLevel.DEBUG,
                    navigationService.workflowStepProperty(),
                    generalSettings.operationLevelProperty()));
    }

    private void onWorldWindowAvailable(WorldWindow worldWindow) {
        LifecycleValueConverter<FlightPlan, ILayer> valueConverter =
            new LifecycleValueConverter<>() {
                @Override
                public ILayer convert(FlightPlan flightPlan) {
                    FlightplanLayer layer =
                        new FlightplanLayer(
                            syncRoot,
                            flightPlan,
                            currentMission.get().uavProperty().get(),
                            globes,
                            mapModel,
                            mapView,
                            mapController,
                            elevationModel,
                            navigationService,
                            selectionManager,
                            languageHelper,
                            generalSettings,
                            flightplanLayerVisibilitySettings);

                    ListenerAdapter listenerAdapter = new ListenerAdapter(layer, flightPlan);
                    worldWindow.getInputHandler().addMouseListener(listenerAdapter);
                    worldWindow.getInputHandler().addMouseMotionListener(listenerAdapter);
                    missionChangeListener =
                        (observable, oldValue, newValue) -> {
                            if (newValue == null && oldValue != null) {
                                worldWindow.getInputHandler().removeMouseListener(listenerAdapter);
                                worldWindow.getInputHandler().removeMouseMotionListener(listenerAdapter);
                                worldWindow.removePositionListener(listenerAdapter);
                            }
                        };
                    applicationContext
                        .currentMissionProperty()
                        .addListener(new WeakChangeListener<>(missionChangeListener));
                    worldWindow.addPositionListener(listenerAdapter);

                    boolean showOther;
                    boolean showCurrent;
                    FlightPlan currentFp = currentFlightPlan.get();

                    switch (navigationService.getWorkflowStep()) {
                    case PLANNING:
                        showOther = flightplanLayerVisibilitySettings.isShowOtherFlightplans();
                        showCurrent = flightplanLayerVisibilitySettings.isShowCurrentFlightplan();
                        break;
                    case DATA_PREVIEW:
                        showOther = false;
                        showCurrent = datasetLayerVisibilitySettings.isShowCurrentFlightplan();
                        break;
                    case FLIGHT:
                        showOther = false;
                        showCurrent = aircraftLayerVisibilitySettings.isFlightPlan();
                        break;
                    case NONE:
                    default:
                        showOther = false;
                        showCurrent = false;
                        break;
                    }

                    if (currentFp == layer.getWrappedLayer().getFlighPlan()) {
                        layer.setEnabledAsync(showCurrent);
                    } else {
                        layer.setEnabledAsync(showOther);
                    }

                    return layer;
                }

                @Override
                public void update(FlightPlan sourceValue, ILayer targetValue) {}

                @Override
                public void remove(ILayer value) {
                    worldWindow.getInputHandler().removeMouseListener(((FlightplanLayer)value).getWrappedLayer());
                    worldWindow.getInputHandler().removeMouseMotionListener(((FlightplanLayer)value).getWrappedLayer());
                    worldWindow.removePositionListener(((FlightplanLayer)value).getWrappedLayer());
                }
            };

        subLayersProperty()
            .bindContent(
                propertyPathStore
                    .from(applicationContext.currentMissionProperty())
                    .selectReadOnlyList(Mission::flightPlansProperty),
                valueConverter);
    }

    private void fixVisibility() {
        boolean showOther;
        boolean showCurrent;
        FlightPlan currentFp = currentFlightPlan.get();

        switch (navigationService.getWorkflowStep()) {
        case PLANNING:
            showOther = flightplanLayerVisibilitySettings.isShowOtherFlightplans();
            showCurrent = flightplanLayerVisibilitySettings.isShowCurrentFlightplan();
            break;
        case DATA_PREVIEW:
            showOther = false;
            showCurrent = datasetLayerVisibilitySettings.isShowCurrentFlightplan();
            break;
        case FLIGHT:
            showOther = false;
            showCurrent = aircraftLayerVisibilitySettings.isFlightPlan();
            break;
        case NONE:
        default:
            showOther = false;
            showCurrent = false;
            break;
        }

        try (LockedList<ILayer> layers = subLayersProperty().lock()) {
            layers.forEach(
                layer -> {
                    FlightplanLayer fpLayer = (FlightplanLayer)layer;
                    if (currentFp == fpLayer.getWrappedLayer().getFlighPlan()) {
                        layer.setEnabledAsync(showCurrent);
                    } else {
                        layer.setEnabledAsync(showOther);
                    }
                });
        }
    }

    public FlightplanLayerVisibilitySettings getFlightplanLayerVisibilitySettings() {
        return flightplanLayerVisibilitySettings;
    }
}
