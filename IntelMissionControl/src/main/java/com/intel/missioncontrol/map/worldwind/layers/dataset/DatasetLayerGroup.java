/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.dataset;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.IApplicationContext;
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
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Dispatcher;

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
            @Named(MapModule.DISPATCHER) Dispatcher dispatcher,
            IMapController mapController,
            INavigationService navigationService,
            ISettingsManager settingsManager,
            IApplicationContext applicationContext,
            ISelectionManager selectionManager,
            GeneralSettings generalSettings) {
        super(LayerGroupType.DATASET_GROUP);
        this.navigationService = navigationService;

        setName(new LayerName("%" + getClass().getName()));

        currentMission.bind(applicationContext.currentLegacyMissionProperty());
        datasetLayerVisibilitySettings = settingsManager.getSection(DatasetLayerVisibilitySettings.class);

        datasetLayerVisibilitySettings.showCurrentDatasetsProperty().addListener(observable -> fixVisibility());
        datasetLayerVisibilitySettings.showOtherDatasetsProperty().addListener(observable -> fixVisibility());
        navigationService.workflowStepProperty().addListener(observable -> fixVisibility());
        currentMatching.addListener(observable -> fixVisibility());

        subLayersProperty()
            .bindContent(
                matchings,
                matching -> {
                    DatasetLayer layer = new DatasetLayer(dispatcher, matching, mapController, selectionManager);

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
                        PropertyHelper.setValueSafe(layer.enabledProperty(), showCurrent);
                    } else {
                        PropertyHelper.setValueSafe(layer.enabledProperty(), showOther);
                    }

                    return layer;
                });

        matchings.bindContent(
            propertyPathStore
                .from(applicationContext.currentLegacyMissionProperty())
                .selectReadOnlyList(Mission::matchingsProperty));

        currentMatching.bind(
            PropertyPath.from(applicationContext.currentLegacyMissionProperty())
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
                        PropertyHelper.setValueSafe(layer.enabledProperty(), showCurrent);
                    } else {
                        PropertyHelper.setValueSafe(layer.enabledProperty(), showOther);
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
