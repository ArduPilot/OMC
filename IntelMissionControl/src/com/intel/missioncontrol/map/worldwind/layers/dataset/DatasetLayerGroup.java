/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.dataset;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.beans.property.PropertyPathStore;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.collections.LockedList;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.LayerGroup;
import com.intel.missioncontrol.map.LayerGroupType;
import com.intel.missioncontrol.map.LayerName;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingViewOptions;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import eu.mavinci.core.obfuscation.IKeepClassname;
import javafx.beans.binding.Bindings;

public class DatasetLayerGroup extends LayerGroup implements IKeepClassname {

    private final INavigationService navigationService;
    private final DatasetLayerVisibilitySettings datasetLayerVisibilitySettings;

    @SuppressWarnings("FieldCanBeLocal")
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    private final AsyncListProperty<Matching> matchings =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<Matching>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());
    private final AsyncObjectProperty<Mission> currentMission = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Matching> currentMatching = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<MatchingViewOptions> currentViewOptions = new SimpleAsyncObjectProperty<>(this);

    @Inject
    public DatasetLayerGroup(
            @Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot,
            IMapController mapController,
            INavigationService navigationService,
            ISettingsManager settingsManager,
            IApplicationContext applicationContext,
            ISelectionManager selectionManager,
            GeneralSettings generalSettings) {
        super(LayerGroupType.DATASET_GROUP);
        this.navigationService = navigationService;

        setName(new LayerName("%" + getClass().getName()));

        currentMission.bind(applicationContext.currentMissionProperty());
        datasetLayerVisibilitySettings = settingsManager.getSection(DatasetLayerVisibilitySettings.class);

        datasetLayerVisibilitySettings.showCurrentDatasetsProperty().addListener(observable -> fixVisibility());
        datasetLayerVisibilitySettings.showOtherDatasetsProperty().addListener(observable -> fixVisibility());
        navigationService.workflowStepProperty().addListener(observable -> fixVisibility());
        currentMatching.addListener(observable -> fixVisibility());

        subLayersProperty()
            .bindContent(
                matchings,
                matching -> {
                    DatasetLayer layer = new DatasetLayer(syncRoot, matching, mapController, selectionManager);

                    boolean showOther;
                    boolean showCurrent;
                    Matching current = currentMatching.get();

                    switch (navigationService.getWorkflowStep()) {
                    case DATA_PREVIEW:
                        showOther = datasetLayerVisibilitySettings.isShowOtherDatasets();
                        showCurrent = datasetLayerVisibilitySettings.isShowCurrentDatasets();
                        break;
                    default:
                        showOther = false;
                        showCurrent = false;
                        break;
                    }

                    if (current == layer.getMatching()) {
                        layer.setEnabledAsync(showCurrent);
                    } else {
                        layer.setEnabledAsync(showOther);
                    }

                    return layer;
                });

        matchings.bindContent(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .selectReadOnlyList(Mission::matchingsProperty));

        currentMatching.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .selectReadOnlyObject(Mission::currentMatchingProperty));

        currentMatching.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue == null) {
                    currentViewOptions.set(null);
                } else {
                    currentViewOptions.set(newValue.getViewOptions());
                }
            });

        Matching matching = currentMatching.get();
        currentViewOptions.set(matching != null ? matching.getViewOptions() : null);

        internalProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () ->
                        navigationService.getWorkflowStep() != WorkflowStep.DATA_PREVIEW
                            && generalSettings.getOperationLevel() != OperationLevel.DEBUG,
                    navigationService.workflowStepProperty(),
                    generalSettings.operationLevelProperty()));
    }

    private void fixVisibility() {
        boolean showOther;
        boolean showCurrent;
        Matching current = currentMatching.get();

        switch (navigationService.getWorkflowStep()) {
        case DATA_PREVIEW:
            showOther = datasetLayerVisibilitySettings.isShowOtherDatasets();
            showCurrent = datasetLayerVisibilitySettings.isShowCurrentDatasets();
            break;
        default:
            showOther = false;
            showCurrent = false;
            break;
        }

        try (LockedList<ILayer> layers = subLayersProperty().lock()) {
            layers.forEach(
                layer -> {
                    DatasetLayer datasetLayer = (DatasetLayer)layer;
                    if (current == datasetLayer.getMatching()) {
                        layer.setEnabledAsync(showCurrent);
                    } else {
                        layer.setEnabledAsync(showOther);
                    }
                });
        }
    }

    public DatasetLayerVisibilitySettings getDatasetLayerVisibilitySettings() {
        return datasetLayerVisibilitySettings;
    }

    public AsyncObjectProperty<Matching> currentMatchingProperty() {
        return currentMatching;
    }

    public AsyncObjectProperty<MatchingViewOptions> currentViewOptionsProperty() {
        return currentViewOptions;
    }

}
