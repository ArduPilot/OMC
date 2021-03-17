/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.CPicArea;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HeightWidgetView extends VBox implements FxmlView<HeightWidgetViewModel>, Initializable {

    private static final double HEIGHT_MIN = CPicArea.MIN_CROP_HEIGHT_METER;
    private static final double HEIGHT_MAX = CPicArea.MAX_CROP_HEIGHT_METER;
    private static final double HEIGHT_STEP = 1.0;
    private static final int HEIGHT_FRACTION_DIGITS = 1;

    @InjectViewModel
    private HeightWidgetViewModel viewModel;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> spinnerHeight;

    @FXML
    private Label spinnerHeightLabel;

    @Inject
    private ISettingsManager settingsManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ViewHelper.initAutoCommitSpinner(
            spinnerHeight,
            viewModel.heightQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            HEIGHT_FRACTION_DIGITS,
            HEIGHT_MIN,
            HEIGHT_MAX,
            HEIGHT_STEP,
            false);
    }
}
