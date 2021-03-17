/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.Parity;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.navbar.settings.viewmodel.AirTrafficMonitorSettingsViewModel;
import com.intel.missioncontrol.ui.navbar.settings.viewmodel.AirspacesProvidersSettingsViewModel;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

public class AirTrafficMonitorSettingsView extends ViewBase<AirTrafficMonitorSettingsViewModel> {

    @InjectViewModel
    private AirTrafficMonitorSettingsViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private ComboBox<Number> portComboBox;

    @FXML
    private ComboBox<Number> dataBitsComboBox;

    @FXML
    private ComboBox<Number> stopBitsComboBox;

    @FXML
    private ComboBox<Parity> parityComboBox;

    @FXML
    private ComboBox<Number> baudRateComboBox;

    @FXML
    private Spinner<Quantity<Length>> minHorizontalDistanceSpinner;

    @FXML
    private Spinner<Quantity<Length>> minVerticalDistanceSpinner;

    private final IQuantityStyleProvider quantityStyleProvider;

    @Inject
    public AirTrafficMonitorSettingsView(IQuantityStyleProvider quantityStyleProvider) {
        this.quantityStyleProvider = quantityStyleProvider;
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    public AirTrafficMonitorSettingsViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void initializeView() {
        super.initializeView();

        this.portComboBox.itemsProperty().bind(this.viewModel.availableComPortsProperty());
        this.portComboBox.valueProperty().bindBidirectional(viewModel.comPortProperty());
        this.portComboBox.setConverter(
            new StringConverter<>() {
                @Override
                public Integer fromString(String arg0) {
                    return Integer.valueOf(arg0.substring(3));
                }

                @Override
                public String toString(Number arg0) {
                    return "COM" + arg0;
                }
            });

        this.dataBitsComboBox.itemsProperty().bind(viewModel.availableDataBitsProperty());
        this.dataBitsComboBox.valueProperty().bindBidirectional(viewModel.dataBitsProperty());

        this.stopBitsComboBox.itemsProperty().bind(viewModel.availableStopBitsProperty());
        this.stopBitsComboBox.valueProperty().bindBidirectional(viewModel.stopBitsProperty());

        this.parityComboBox.itemsProperty().bind(viewModel.availableParitiesProperty());
        this.parityComboBox.valueProperty().bindBidirectional(viewModel.parityProperty());

        this.baudRateComboBox.itemsProperty().bind(viewModel.availableBaudRatesProperty());
        this.baudRateComboBox.valueProperty().bindBidirectional(viewModel.baudRateProperty());

        ViewHelper.initAutoCommitSpinnerWithQuantity(
            minHorizontalDistanceSpinner,
            viewModel.minimumHorizontalDistanceProperty(),
            Unit.METER,
            quantityStyleProvider,
            2,
            AirspacesProvidersSettingsViewModel.MIN_HORIZONTAL_DISTANCE_LOWER,
            AirspacesProvidersSettingsViewModel.MIN_HORIZONTAL_DISTANCE_UPPER,
            1.0,
            false);

        ViewHelper.initAutoCommitSpinnerWithQuantity(
            minVerticalDistanceSpinner,
            viewModel.minimumVerticalDistanceProperty(),
            Unit.METER,
            quantityStyleProvider,
            2,
            AirspacesProvidersSettingsViewModel.MIN_VERTICAL_DISTANCE_LOWER,
            AirspacesProvidersSettingsViewModel.MIN_VERTICAL_DISTANCE_UPPER,
            1.0,
            false);
    }

}
