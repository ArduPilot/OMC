/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.airspaces.AirspaceProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Time;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.navbar.settings.viewmodel.AirspacesProvidersSettingsViewModel;
import com.intel.missioncontrol.ui.validation.LabelValidationVisualizer;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.validation.visualization.ValidationVisualizer;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class AirspacesProvidersSettingsView extends ViewBase<AirspacesProvidersSettingsViewModel> {

    private static final String TOGGLE_YES = "com.intel.missioncontrol.ui.controls.skins.ToggleSwitchSkin.yes";
    private static final String TOGGLE_NO = "com.intel.missioncontrol.ui.controls.skins.ToggleSwitchSkin.no";

    @InjectViewModel
    private AirspacesProvidersSettingsViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    private Pane layoutRoot;

    @FXML
    private Spinner<Quantity<Length>> maxAltitudeAboveGroundSpinner;

    @FXML
    private Spinner<Quantity<Length>> maxAltitudeAboveSeaLevelSpinner;

    @FXML
    private Spinner<Quantity<Time>> minimumTimeLandingSpinner;

    @FXML
    private Spinner<Quantity<Length>> minHorizontalDistanceSpinner;

    @FXML
    private Spinner<Quantity<Length>> minVerticalDistanceSpinner;

    @FXML
    private Label maxAltitudeAboveGroundErrorLabel;

    @FXML
    private Label maxAltitudeAboveSeaLevelErrorLabel;

    @FXML
    private Label minimumTimeLandingErrorLabel;

    @FXML
    private Label minHorizontalDistanceErrorLabel;

    @FXML
    private Label minVerticalDistanceErrorLabel;

    @FXML
    private ToggleGroup airspaceProvider;

    @FXML
    private Label providerChangedHintLabel;

    @FXML
    public ToggleSwitch useSurfaceDataForPlanningSwitch;

    @FXML
    public VBox surfaceDataGroup;

    @FXML
    public VBox useAirspaceDataForPlanning;

    @FXML
    public VBox minimumTimeBetweenLandingAndSunset;

    @FXML
    public VBox minHorizontalDistance;

    @FXML
    public VBox minVerticalDistance;

    @FXML
    public CheckBox useDefaultElevationModelCheckBox;

    @FXML
    public CheckBox useGeoTIFFCheckBox;

    @FXML
    public ToggleSwitch useAirspaceDataForPlanningSwitch;

    @FXML
    private VBox airspaceProviderBox;

    private final ISettingsManager settingsManager;
    private final ILanguageHelper languageHelper;
    private final IDialogContextProvider dialogContextProvider;
    private final List<ValidationVisualizer> validationVisualizers = new ArrayList<>();

    @Inject
    public AirspacesProvidersSettingsView(
            ISettingsManager settingsManager,
            ILanguageHelper languageHelper,
            IDialogContextProvider dialogContextProvider) {
        this.settingsManager = settingsManager;
        this.languageHelper = languageHelper;
        this.dialogContextProvider = dialogContextProvider;
    }

    @Override
    protected AirspacesProvidersSettingsViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    public void initializeView() {
        super.initializeView();
        dialogContextProvider.setContext(viewModel, context);
        initSpinners();
        initAirspacesProvidersToggle();
        initPlanningToggles();

        airspaceProviderBox
            .visibleProperty()
            .bind(
                settingsManager
                    .getSection(GeneralSettings.class)
                    .operationLevelProperty()
                    .isEqualTo(OperationLevel.DEBUG));
        airspaceProviderBox.managedProperty().bind(airspaceProviderBox.visibleProperty());

        minimumTimeBetweenLandingAndSunset
            .visibleProperty()
            .bind(
                settingsManager
                    .getSection(GeneralSettings.class)
                    .operationLevelProperty()
                    .isEqualTo(OperationLevel.DEBUG));
        minimumTimeBetweenLandingAndSunset.managedProperty().bind(minimumTimeBetweenLandingAndSunset.visibleProperty());
    }

    private void initPlanningToggles() {
        surfaceDataGroup.disableProperty().bind(useSurfaceDataForPlanningSwitch.selectedProperty().not());
        useSurfaceDataForPlanningSwitch
            .selectedProperty()
            .bindBidirectional(viewModel.useSurfaceDataForPlanningProperty());
        useDefaultElevationModelCheckBox
            .selectedProperty()
            .bindBidirectional(viewModel.useDefaultElevationModelProperty());
        useGeoTIFFCheckBox.selectedProperty().bindBidirectional(viewModel.useGeoTIFFProperty());
        useAirspaceDataForPlanningSwitch
            .selectedProperty()
            .bindBidirectional(viewModel.useAirspaceDataForPlanningProperty());
    }

    private void initSpinners() {
        LabelValidationVisualizer visualizer;

        ViewHelper.initAutoCommitSpinnerWithQuantity(
            maxAltitudeAboveGroundSpinner,
            viewModel.maxAltitudeAboveGroundProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            2,
            AirspacesProvidersSettingsViewModel.MAX_ALTITUDE_ABOVE_GROUND_LOWER,
            AirspacesProvidersSettingsViewModel.MAX_ALTITUDE_ABOVE_GROUND_UPPER,
            1.0,
            false);

        visualizer = new LabelValidationVisualizer();
        visualizer.initVisualization(
            viewModel.getMaxAltitudeAboveGroundValidationStatus(), maxAltitudeAboveGroundErrorLabel);
        validationVisualizers.add(visualizer);

        ViewHelper.initAutoCommitSpinnerWithQuantity(
            maxAltitudeAboveSeaLevelSpinner,
            viewModel.maxAltitudeAboveSeaLevelProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            4,
            AirspacesProvidersSettingsViewModel.MAX_ALTITUDE_ABOVE_SEA_LEVEL_LOWER,
            AirspacesProvidersSettingsViewModel.MAX_ALTITUDE_ABOVE_SEA_LEVEL_UPPER,
            10.0,
            false);

        visualizer = new LabelValidationVisualizer();
        visualizer.initVisualization(
            viewModel.getMaxAltitudeAboveSeaLevelValidationStatus(), maxAltitudeAboveSeaLevelErrorLabel);
        validationVisualizers.add(visualizer);

        ViewHelper.initAutoCommitSpinnerWithQuantity(
            minimumTimeLandingSpinner,
            viewModel.minTimeBetweenLandingAndSunsetProperty(),
            Unit.MINUTE,
            settingsManager.getSection(GeneralSettings.class),
            2,
            AirspacesProvidersSettingsViewModel.MIN_TIME_BETWEEN_LANDING_AND_SUNSET_LOWER,
            AirspacesProvidersSettingsViewModel.MIN_TIME_BETWEEN_LANDING_AND_SUNSET_UPPER,
            1.0,
            false);

        visualizer = new LabelValidationVisualizer();
        visualizer.initVisualization(
            viewModel.getMinTimeBetweenLandingAndSunsetValidationStatus(), minimumTimeLandingErrorLabel);
        validationVisualizers.add(visualizer);


        ViewHelper.initAutoCommitSpinnerWithQuantity(
                minHorizontalDistanceSpinner,
                viewModel.minHorizontalDistanceProperty(),
                Unit.METER,
                settingsManager.getSection(GeneralSettings.class),
                0,
                AirspacesProvidersSettingsViewModel.MIN_HORIZONTAL_DISTANCE_LOWER,
                AirspacesProvidersSettingsViewModel.MIN_HORIZONTAL_DISTANCE_UPPER,
                1.0,
                false);

        visualizer = new LabelValidationVisualizer();
        visualizer.initVisualization(
                viewModel.getMinHorizontalDistanceValidationStatus(), minHorizontalDistanceErrorLabel);
        validationVisualizers.add(visualizer);


        ViewHelper.initAutoCommitSpinnerWithQuantity(
                minVerticalDistanceSpinner,
                viewModel.minVerticalDistanceProperty(),
                Unit.METER,
                settingsManager.getSection(GeneralSettings.class),
                0,
                AirspacesProvidersSettingsViewModel.MIN_VERTICAL_DISTANCE_LOWER,
                AirspacesProvidersSettingsViewModel.MIN_VERTICAL_DISTANCE_UPPER,
                1.0,
                false);

        visualizer = new LabelValidationVisualizer();
        visualizer.initVisualization(
                viewModel.getMinHorizontalDistanceValidationStatus(), minVerticalDistanceErrorLabel);
        validationVisualizers.add(visualizer);
    }

    private void initAirspacesProvidersToggle() {
        airspaceProvider
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    viewModel.scheduleAirspaceProviderToChange((String)newValue.getUserData());
                    if (oldValue != null) {
                        handleHintLabelVisibility();
                    }
                });

        viewModel
            .airspaceProviderProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    selectAirspaceProvidersToggle(newValue);
                });

        selectAirspaceProvidersToggle(viewModel.getCurrentAirspaceProvider());
    }

    private void handleHintLabelVisibility() {
        providerChangedHintLabel.setVisible(isAirspaceProviderScheduledForChange());
        providerChangedHintLabel.setManaged(isAirspaceProviderScheduledForChange());
    }

    private boolean isAirspaceProviderScheduledForChange() {
        return viewModel.getCurrentAirspaceProvider() != viewModel.getScheduledForChangeAirspaceProvider();
    }

    private void selectAirspaceProvidersToggle(AirspaceProvider provider) {
        airspaceProvider
            .getToggles()
            .stream()
            .filter(t -> t.getUserData().equals(provider.toString()))
            .findAny()
            .ifPresent(t -> t.setSelected(true));
    }

    @FXML
    public void manageExternalDataSourcesClicked() {
        viewModel.getShowDataSourcesCommand().execute();
    }
}
