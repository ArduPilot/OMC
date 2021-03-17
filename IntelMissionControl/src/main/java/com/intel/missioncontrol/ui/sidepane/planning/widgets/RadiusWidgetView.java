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
import javafx.scene.layout.VBox;

public class RadiusWidgetView extends VBox implements Initializable, FxmlView<RadiusWidgetViewModel> {

    public static final double RADIUS_MIN = CPicArea.MIN_CORRIDOR_WIDTH_METER;
    public static final double RADIUS_MAX = CPicArea.MAX_CORRIDOR_WIDTH_METER;
    public static final double RADIUS_STEP = 1.0;
    public static final int RADIUS_FRACTION_DIGITS = 1;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> spinnerRadius;

    @InjectViewModel
    private RadiusWidgetViewModel viewModel;

    @Inject
    private ISettingsManager settingsManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ViewHelper.initAutoCommitSpinner(
            spinnerRadius,
            viewModel.radiusQuantityProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            RADIUS_FRACTION_DIGITS,
            RADIUS_MIN,
            RADIUS_MAX,
            RADIUS_STEP,
            false);
    }

}
