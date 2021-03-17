/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.intel.missioncontrol.ui.controls.RadioButton;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.PlanType;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class NoFlyWidgetView extends GridPane implements Initializable, FxmlView<NoFlyWidgetViewModel> {

    @FXML
    private Label geofenceTypelabel;

    @FXML
    private CheckBox enableFloor;

    @FXML
    private AutoCommitSpinner<Quantity<Dimension.Length>> floorLevel;

    // commented out until "above ground" is implemented properly

    // @FXML
    // private Label floorAboveLbl;

    // @FXML
    // private ComboBox<CPicArea.HeightReferenceTypes> floorReference;
    @FXML
    private Label egmOffset;

    @FXML
    private CheckBox enableCeiling;

    @FXML
    private AutoCommitSpinner<Quantity<Dimension.Length>> ceilingLevel;

    @FXML
    private Label ceilingAboveLbl;

    @FXML
    private ComboBox<CPicArea.RestrictedAreaHeightReferenceTypes> ceilingReference;

/*
    @FXML
    private ToggleGroup areaShapeToggleGroup;
*/

    @FXML
    private RadioButton polygonBtn;

    @FXML
    private RadioButton circleBtn;

    @FXML
    private VBox radiusBox;

    @FXML
    private AutoCommitSpinner<Quantity<Dimension.Length>> spinnerRadius;

    @InjectViewModel
    private NoFlyWidgetViewModel viewModel;

    private final ISettingsManager settingsManager;

    private final ILanguageHelper languageHelper;

    private final QuantityFormat quantityFormat;

    @Inject
    public NoFlyWidgetView(ISettingsManager settingsManager, ILanguageHelper languageHelper) {
        this.settingsManager = settingsManager;
        this.languageHelper = languageHelper;
        quantityFormat = new AdaptiveQuantityFormat(settingsManager.getSection(GeneralSettings.class));
        quantityFormat.setSignificantDigits(1);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ViewHelper.initAutoCommitSpinner(
            floorLevel,
            viewModel.floorQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            2,
            CPicArea.RESTRICTION_MIN,
            CPicArea.RESTRICTION_MAX,
            5.,
            false);
        ViewHelper.initAutoCommitSpinner(
            ceilingLevel,
            viewModel.ceilingQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            2,
            CPicArea.RESTRICTION_MIN,
            CPicArea.RESTRICTION_MAX,
            5.,
            false);
        enableFloor.selectedProperty().bindBidirectional(viewModel.floorEnabledProperty());
        enableCeiling.selectedProperty().bindBidirectional(viewModel.ceilingEnabledProperty());

        egmOffset
            .textProperty()
            .bind(QuantityBindings.createStringBinding(viewModel.egmOffsetProperty(), quantityFormat));
        // commented out until "above ground" is implemented properly
        // floorReference.getItems().addAll(CPicArea.HeightReferenceTypes.values());
        // floorReference.setConverter(new EnumConverter<>(languageHelper, CPicArea.HeightReferenceTypes.class));
        // floorReference.valueProperty().bindBidirectional(viewModel.floorReferenceProperty());

        ceilingReference.getItems().addAll(CPicArea.RestrictedAreaHeightReferenceTypes.values());
        ceilingReference.setConverter(
            new EnumConverter<>(languageHelper, CPicArea.RestrictedAreaHeightReferenceTypes.class));
        ceilingReference.valueProperty().bindBidirectional(viewModel.ceilingReferenceProperty());

        geofenceTypelabel
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () ->
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.planning.widgets.NoFlyWidgetView.firstLabel."
                                + viewModel.aoiTypeProperty().get()),
                    viewModel.aoiTypeProperty()));

        floorLevel.disableProperty().bind(viewModel.floorEnabledProperty().not());
        ceilingLevel.disableProperty().bind(viewModel.ceilingEnabledProperty().not());
        ceilingAboveLbl.disableProperty().bind(viewModel.ceilingEnabledProperty().not());
        ceilingReference.setDisable(true);

        radiusBox.visibleProperty().bind(viewModel.needRadiusProperty());
        radiusBox.managedProperty().bind(radiusBox.visibleProperty());
/*

        areaShapeToggleGroup
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (viewModel.aoiTypeProperty().get().isNoFlyZone()) {
                        if (newValue.getUserData().equals("POLYGON")) {
                            viewModel.aoiTypeProperty().set(PlanType.NO_FLY_ZONE_POLY);
                        } else {
                            viewModel.aoiTypeProperty().set(PlanType.NO_FLY_ZONE_CIRC);
                        }
                    } else {
                        if (newValue.getUserData().equals("POLYGON")) {
                            viewModel.aoiTypeProperty().set(PlanType.GEOFENCE_POLY);
                        } else {
                            viewModel.aoiTypeProperty().set(PlanType.GEOFENCE_CIRC);
                        }
                    }
                });
*/

        viewModel.aoiTypeProperty().addListener((observable, oldValue, newValue) -> setAoiType(newValue));
        setAoiType(viewModel.aoiTypeProperty().get());

        ViewHelper.initAutoCommitSpinner(
            spinnerRadius,
            viewModel.radiusQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            RadiusWidgetView.RADIUS_FRACTION_DIGITS,
            RadiusWidgetView.RADIUS_MIN,
            RadiusWidgetView.RADIUS_MAX,
            RadiusWidgetView.RADIUS_STEP,
            false);
    }

    private void setAoiType(PlanType planType) {
        if (planType == null) {
            return;
        }

        if (planType.isCircular()) {
            circleBtn.setSelected(true);
        } else {
            polygonBtn.setSelected(true);
        }
    }
}
