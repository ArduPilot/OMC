/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.landing;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityArithmetic;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AdornerSplitView;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.controls.VariantQuantitySpinnerValueFactory;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.LandingModes;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

public class LandingView extends ViewBase<LandingViewModel> {
    @InjectViewModel
    private LandingViewModel viewModel;

    @FXML
    private AdornerSplitView rootNode;

    @FXML
    private Spinner<VariantQuantity> landingLatitudeSpinner;

    @FXML
    private Spinner<VariantQuantity> landingLongitudeSpinner;

    @FXML
    private ToggleButton chooseLandingPositionButton;

    @FXML
    private VBox locationEditBox;

    private final IQuantityStyleProvider quantityStyleProvider;

    private final ILanguageHelper languageHelper;

    @FXML
    private ComboBox<LandingModes> landingModeCombobox;

    @FXML
    private ToggleSwitch autoLandingSwitch;

    @FXML
    private VBox elevationBox;

    @FXML
    private Spinner<Quantity<Dimension.Length>> elevationSpinner;

    @Inject
    public LandingView(ISettingsManager settingsManager, ILanguageHelper languageHelper) {
        this.quantityStyleProvider = settingsManager.getSection(GeneralSettings.class);
        this.languageHelper = languageHelper;
    }

    @Override
    public void initializeView() {
        super.initializeView();

        autoLandingSwitch.selectedProperty().bindBidirectional(viewModel.autolandingProperty());

        EnumConverter<LandingModes> landingModesEnumConverter = new EnumConverter<>(languageHelper, LandingModes.class);
        landingModeCombobox.getItems().addAll(LandingModes.values());
        landingModeCombobox.setConverter(landingModesEnumConverter);
        landingModeCombobox.valueProperty().bindBidirectional(viewModel.selectedLandingModeProperty());

        locationEditBox.disableProperty().bind(viewModel.landingLocationEditableProperty().not());
        locationEditBox.visibleProperty().bind(viewModel.landingLocationVisibleProperty());
        locationEditBox.managedProperty().bind(viewModel.landingLocationVisibleProperty());

        elevationBox.visibleProperty().bind(viewModel.showHoverAltitudeProperty());
        elevationBox.managedProperty().bind(viewModel.showHoverAltitudeProperty());

        ViewHelper.initAutoCommitSpinner(
            elevationSpinner,
            viewModel.hoverElevationProperty(),
            Unit.METER,
            quantityStyleProvider,
            5,
            0.0,
            1000.0,
            1.0,
            false);

        int maxAngleFractionDigits = 6;
        int significantAngleDigits = 8;

        var latAngleSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Dimension.Angle.class,
                Quantity.of(-90, Unit.DEGREE),
                Quantity.of(90, Unit.DEGREE),
                0.00000899823,
                true,
                significantAngleDigits,
                maxAngleFractionDigits);

        var lonAngleSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Dimension.Angle.class,
                Quantity.of(-180, Unit.DEGREE),
                Quantity.of(180, Unit.DEGREE),
                0.00000899823,
                true,
                significantAngleDigits,
                maxAngleFractionDigits);

        var lengthSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Dimension.Length.class,
                Quantity.of(-Double.MAX_VALUE, Unit.METER),
                Quantity.of(Double.MAX_VALUE, Unit.METER),
                1,
                true,
                8,
                5);

        var valueFactory =
            new VariantQuantitySpinnerValueFactory(
                quantityStyleProvider,
                QuantityArithmetic.LATITUDE,
                viewModel.landingLatitudeProperty(),
                latAngleSettings,
                lengthSettings);

        landingLatitudeSpinner.setValueFactory(valueFactory);
        landingLatitudeSpinner.setEditable(true);

        var lonValueFactory =
            new VariantQuantitySpinnerValueFactory(
                quantityStyleProvider,
                QuantityArithmetic.LONGITUDE,
                viewModel.landingLongitudeProperty(),
                lonAngleSettings,
                lengthSettings);

        landingLongitudeSpinner.setValueFactory(lonValueFactory);
        landingLongitudeSpinner.setEditable(true);

        chooseLandingPositionButton
            .disableProperty()
            .bind(viewModel.getToggleChooseLandingPositionCommand().notExecutableProperty());

        chooseLandingPositionButton.selectedProperty().bindBidirectional(viewModel.landingButtonPressedProperty());
        chooseLandingPositionButton.getStyleClass().remove("toggle-button");
    }

    @Override
    public AdornerSplitView getRootNode() {
        return rootNode;
    }

    @Override
    public LandingViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void onToggleChooseLandingPositionClicked() {
        viewModel.getToggleChooseLandingPositionCommand().execute();
    }

}
