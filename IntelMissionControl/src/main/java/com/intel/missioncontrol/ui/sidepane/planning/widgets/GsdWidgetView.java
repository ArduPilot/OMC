/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.intel.missioncontrol.ui.controls.Button;
import com.intel.missioncontrol.ui.controls.skins.AppendixSpinnerSkin;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.CPicArea;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GsdWidgetView extends VBox implements FxmlView<GsdWidgetViewModel>, Initializable {

    public static final double ALT_MIN = 0.1;
    public static final double ALT_MAX = 1000.0;
    public static final double SPINNER_STEP = 1.0;
    public static final double GSD_SPINNER_STEP = 0.01;

    public static final int GSD_FRACTION_DIGITS = 2;
    public static final int ALT_FRACTION_DIGITS = 2;

    @FXML
    public Label gsdWarningLabel;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> gsdInput;

    @FXML
    private AutoCommitSpinner<Quantity<Length>> altDistanceInput;

    @FXML
    private Label labelGsd;

    @FXML
    private Button setGsd;

    @FXML
    private Button setDistance;


    @InjectViewModel
    private GsdWidgetViewModel viewModel;

    @Inject
    private ISettingsManager settingsManager;

    @Inject
    private ILanguageHelper languageHelper;

    private StringBinding upperStr;
    private StringBinding lowerStr;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ViewHelper.initAutoCommitSpinner(
                gsdInput,
                viewModel.gsdQuantityProperty(),
                Unit.METER,
                settingsManager.getSection(GeneralSettings.class),
                GSD_FRACTION_DIGITS,
                CPicArea.MIN_GSD,
                CPicArea.MAX_GSD,
                GSD_SPINNER_STEP,
                false);

        ViewHelper.initAutoCommitSpinner(
                altDistanceInput,
                viewModel.altitudeQuantityProperty(),
                Unit.METER,
                settingsManager.getSection(GeneralSettings.class),
                ALT_FRACTION_DIGITS,
                ALT_MIN,
                ALT_MAX,
                SPINNER_STEP,
                false);

        gsdInput.setSkin(new AppendixSpinnerSkin<>(gsdInput, "/pixel", 1));

        labelGsd.textProperty().bind(viewModel.lblGsdProperty());

        var quantityStyleProvider = settingsManager.getSection(GeneralSettings.class);
        QuantityFormat quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        quantityFormat.setMaximumFractionDigits(2);
        quantityFormat.setSignificantDigits(3);

        lowerStr = QuantityBindings.createStringBinding(viewModel.gsdLowerEndOfRangeProperty(), quantityFormat);
        upperStr = QuantityBindings.createStringBinding(viewModel.gsdUpperEndOfRangeProperty(), quantityFormat);

        gsdWarningLabel
                .textProperty()
                .bind(
                        Bindings.format(
                                languageHelper.getString("com.intel.missioncontrol.ui.sidepane.planning.widgets.gsdWarningLabel"),
                                lowerStr,
                                upperStr));
        gsdWarningLabel.managedProperty().bind(viewModel.gsdOutSideToleranceProperty());
        gsdWarningLabel.visibleProperty().bind(viewModel.gsdOutSideToleranceProperty());

        gsdInput.disableProperty().bind(setGsd.visibleProperty());
        altDistanceInput.disableProperty().bind(setDistance.visibleProperty());

        setGsd.managedProperty().bind(setGsd.visibleProperty());
        setDistance.managedProperty().bind(setDistance.visibleProperty());

    }

    @FXML
    private void hideMe() {
        setGsd.visibleProperty().setValue(!setGsd.visibleProperty().getValue());
        setDistance.visibleProperty().setValue(!setDistance.visibleProperty().getValue());
        if (setDistance.visibleProperty().getValue()) {
            gsdInput.requestFocus();
        } else {
            altDistanceInput.requestFocus();
        }
    }
}
