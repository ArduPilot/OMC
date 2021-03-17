/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.CPicArea;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;

import com.google.inject.Inject;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class BladesWidgetView extends VBox implements Initializable, FxmlView<BladesWidgetViewModel> {

    private static final double RADIUS_MIN = 1.0;
    private static final double RADIUS_MAX = 10000.0;
    private static final double RADIUS_STEP = 1.0;

    private static final double HEIGHT_MIN = CPicArea.MIN_CROP_HEIGHT_METER;
    private static final double HEIGHT_MAX = CPicArea.MAX_CROP_HEIGHT_METER;
    private static final double HEIGHT_STEP = 1.0;

    private static final int RADIUS_FRACTION_DIGITS = 1;
    private static final int HEIGHT_FRACTION_DIGITS = 1;

    private static final double DIRECTION_MIN = 0.0;
    private static final double DIRECTION_MAX = 360.0;
    private static final double DIRECTION_STEP = 1.0;

    private static final double BLADE_COUNT_MIN = 1;
    private static final double BLADE_COUNT_MAX = 6;
    private static final double BLADE_COUNT_STEP = 1;

    @FXML
    public Label spinnerBladeDiameterLabel;

    @FXML
    public Label spinnerBladeLengthLabel;

    @FXML
    public Label spinnerBladeThinRadiusLabel;

    @FXML
    public Label spinnerBladeCoverLengthLabel;

    @FXML
    public Label spinnerBladePitchLabel;

    @FXML
    public Label spinnerBladeStartRotationLabel;

    @FXML
    public Label spinnerNumberOfBladesLabel;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> spinnerBladeDiameter;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> spinnerBladeLength;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> spinnerBladeThinRadius;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> spinnerBladeCoverLength;

    @FXML
    private AutoCommitSpinner<Quantity<Angle>> spinnerBladePitch;

    @FXML
    private AutoCommitSpinner<Quantity<Angle>> spinnerBladeStartRotation;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> spinnerNumberOfBlades;

    @InjectViewModel
    private BladesWidgetViewModel viewModel;

    @Inject
    public ILanguageHelper languageHelper;

    @Inject
    private ISettingsManager settingsManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ViewHelper.initAutoCommitSpinner(
                spinnerBladeDiameter,
                viewModel.bladeDiameterQuantityProperty(),
                Unit.METER,
                settingsManager.getSection(GeneralSettings.class),
                RADIUS_FRACTION_DIGITS,
                RADIUS_MIN,
                RADIUS_MAX,
                RADIUS_STEP,
                false);

        ViewHelper.initAutoCommitSpinner(
                spinnerBladeLength,
                viewModel.bladeLengthQuantityProperty(),
                Unit.METER,
                settingsManager.getSection(GeneralSettings.class),
                HEIGHT_FRACTION_DIGITS,
                HEIGHT_MIN,
                HEIGHT_MAX,
                HEIGHT_STEP,
                false);

        ViewHelper.initAutoCommitSpinner(
                spinnerBladeThinRadius,
                viewModel.bladeThinRadiusQuantityProperty(),
                Unit.METER,
                settingsManager.getSection(GeneralSettings.class),
                HEIGHT_FRACTION_DIGITS,
                HEIGHT_MIN,
                HEIGHT_MAX,
                HEIGHT_STEP,
                false);

        ViewHelper.initAutoCommitSpinner(
                spinnerBladeCoverLength,
                viewModel.bladeCoverLengthQuantityProperty(),
                Unit.METER,
                settingsManager.getSection(GeneralSettings.class),
                HEIGHT_FRACTION_DIGITS,
                HEIGHT_MIN,
                HEIGHT_MAX,
                HEIGHT_STEP,
                false);

        ViewHelper.initAutoCommitSpinner(
                spinnerBladePitch,
                viewModel.bladePitchQuantityProperty(),
                Unit.DEGREE,
                settingsManager.getSection(GeneralSettings.class),
                0,
                DIRECTION_MIN,
                DIRECTION_MAX,
                DIRECTION_STEP,
                true);

        ViewHelper.initAutoCommitSpinner(
                spinnerBladeStartRotation,
                viewModel.bladeStartRotationQuantityProperty(),
                Unit.DEGREE,
                settingsManager.getSection(GeneralSettings.class),
                0,
                DIRECTION_MIN,
                DIRECTION_MAX,
                DIRECTION_STEP,
                true);

        ViewHelper.initAutoCommitSpinner(
                spinnerNumberOfBlades,
                viewModel.numberOfBladesQuantityProperty(),
                Unit.METER,
                settingsManager.getSection(GeneralSettings.class),
                0,
                BLADE_COUNT_MIN,
                BLADE_COUNT_MAX,
                BLADE_COUNT_STEP,
                false);
    }
}
