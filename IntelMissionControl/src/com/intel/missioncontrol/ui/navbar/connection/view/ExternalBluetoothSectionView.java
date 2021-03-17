/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.common.BindingUtils;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.ExternalBluetoothSectionViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.desktop.bluetooth.BTService;
import eu.mavinci.desktop.bluetooth.BTdevice;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.IOException;
import java.io.InputStream;
import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/** @author Vladimir Iordanov */
public class ExternalBluetoothSectionView extends ViewBase<ExternalBluetoothSectionViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    private VBox rtkConfigurationView;

    @FXML
    private Button refreshDevicesButton;

    @FXML
    private VBox bluetoothSettings;

    @FXML
    private ComboBox<BTdevice> deviceCombo;

    @FXML
    private ComboBox<BTService> serviceCombo;

    @InjectViewModel
    private ExternalBluetoothSectionViewModel viewModel;

    private static final double REFRESH_ICON_SIZE = ScaleHelper.emsToPixels(1.3);

    @Override
    public void initializeView() {
        super.initializeView();

        deviceCombo.itemsProperty().bind(viewModel.devicesListProperty());
        deviceCombo.valueProperty().bindBidirectional(viewModel.selectedDeviceProperty());
        deviceCombo.disableProperty().bind(viewModel.getRefreshDevicesCommand().notExecutableProperty());

        serviceCombo.itemsProperty().bind(viewModel.servicesListProperty());
        serviceCombo.valueProperty().bindBidirectional(viewModel.selectedServiceProperty());
        serviceCombo.disableProperty().bind(viewModel.getRefreshDevicesCommand().notExecutableProperty());

        bluetoothSettings.disableProperty().bind(viewModel.isConnectedProperty());

        try (InputStream svgInput =
            Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(
                    "/com/intel/missioncontrol/icons/icon_refresh(fill=theme-button-text-color).svg")) {
            Image iconImage = new Image(svgInput, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
            refreshDevicesButton.setGraphic(new ImageView(iconImage));
        } catch (IOException e) {
            Debug.getLog().log(Debug.WARNING, "ExternalBluetoothSectionView icon_refresh.svg:", e);
        }

        refreshDevicesButton.setOnAction(event -> refreshDevices());

        Animation buttonProgressAnimation =
            Animations.forButtonGraphicRotation(refreshDevicesButton, Animations.ROTATION_CLOCK_WISE);

        viewModel
            .getRefreshDevicesCommand()
            .executableProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        buttonProgressAnimation.stop();
                    } else {
                        buttonProgressAnimation.playFromStart();
                    }
                });

        // Show Configuration section only when is connected
        BindingUtils.bindVisibility(rtkConfigurationView, viewModel.isConnectedProperty().not());
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public ExternalBluetoothSectionViewModel getViewModel() {
        return viewModel;
    }

    private void refreshDevices() {
        viewModel.getRefreshDevicesCommand().execute();
    }

}
