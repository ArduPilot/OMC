/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import com.google.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.layout.VBox;

public class WidthWidgetView extends VBox implements FxmlView<WidthWidgetViewModel>, Initializable {

    private static final double WIDTH_MIN = 1.0;
    private static final double WIDTH_MAX = 1000.0;
    private static final double WIDTH_STEP = 1.0;
    private static final int WIDTH_FRACTION_DIGITS = 1;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> spinnerWidth;

    @InjectViewModel
    private WidthWidgetViewModel viewModel;

    @Inject
    private ISettingsManager settingsManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ViewHelper.initAutoCommitSpinner(
            spinnerWidth,
            viewModel.widthQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            WIDTH_FRACTION_DIGITS,
            WIDTH_MIN,
            WIDTH_MAX,
            WIDTH_STEP,
            false);
    }

}
