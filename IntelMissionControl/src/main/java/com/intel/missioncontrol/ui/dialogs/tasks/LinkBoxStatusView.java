/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.tasks;

import com.intel.missioncontrol.linkbox.BatteryAlertLevel;
import com.intel.missioncontrol.linkbox.DataConnectionStatus;
import com.intel.missioncontrol.linkbox.DroneConnectionQuality;
import com.intel.missioncontrol.linkbox.LinkBoxAlertLevel;
import com.intel.missioncontrol.linkbox.LinkBoxGnssState;
import com.intel.missioncontrol.linkbox.WifiConnectionQuality;
import com.intel.missioncontrol.ui.RootView;
import com.intel.missioncontrol.ui.common.CssStyleRotation;
import com.intel.missioncontrol.ui.controls.Button;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;

public class LinkBoxStatusView extends RootView<LinkBoxStatusViewModel> {

    private final UIAsyncObjectProperty<BatteryAlertLevel> batteryInfo = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<WifiConnectionQuality> linkboxConnectionQuality =
        new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<DataConnectionStatus> cloudConnectionInfo = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<LinkBoxGnssState> gnssInfo = new UIAsyncObjectProperty<>(this);
    private final UIAsyncIntegerProperty numberOfSatellitesProperty = new UIAsyncIntegerProperty(this);
    private final UIAsyncObjectProperty<DroneConnectionQuality> droneConnectionQuality =
        new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<LinkBoxAlertLevel> linkBoxWarningInfo = new UIAsyncObjectProperty<>(this);
    private final UIAsyncStringProperty linkBoxMessage = new UIAsyncStringProperty(this);

    @InjectViewModel
    private LinkBoxStatusViewModel viewModel;

    @FXML
    private VBox layoutRoot;

    @FXML
    private Label linkboxname;

    @FXML
    private Label message;

    @FXML
    private Button batteryStatus;

    @FXML
    private Button linkBoxConnectivity;

    @FXML
    private Button cloudConnectivity;

    @FXML
    private Button gnssState;

    @FXML
    private Button droneConnectivity;

    @FXML
    private Button linkboxwarning;

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        linkboxname.textProperty().bind(viewModel.linkBoxNameProperty());

        initializeBatteryInfo();
        initializeLinkBoxConnectionQuality();
        initializeCloudConnectivity();
        initializeGnssinfo();
        initializeDroneConnectionQuality();
        initializeLinkBoxWarningInfo();
        initializeMessage();
    }

    private void initializeMessage() {
        message.setVisible(false);
        message.setManaged(false);

        linkBoxMessage.addListener((observable, oldValue, newValue) -> updateMessageField(newValue));
        linkBoxMessage.bind(viewModel.messageProperty());
    }

    private void updateMessageField(String newValue) {
        if (newValue != null && !newValue.isEmpty()) {
            message.setVisible(true);
            message.setManaged(true);
            message.setText(newValue);
        } else {
            message.setVisible(false);
            message.setManaged(false);
        }
    }

    private void initializeBatteryInfo() {
        batteryInfo.addListener((observable, oldValue, newValue) -> updateBatteryStatus(newValue));
        batteryInfo.bind(viewModel.batteryAlertLevelProperty());
    }

    private void updateBatteryStatus(BatteryAlertLevel newValue) {
        batteryStatus
            .getStyleClass()
            .removeAll(
                "icon-linkbox-battery-good",
                "icon-linkbox-battery-ok",
                "icon-linkbox-battery-low",
                "icon-linkbox-battery-warning",
                "icon-linkbox-battery-emergency",
                "icon-linkbox-battery-charging",
                "icon-linkbox-battery-undefined",
                "icon-linkbox-battery-unknown");
        switch (newValue) {
        case FULL:
            CssStyleRotation.stop(batteryStatus);
            batteryStatus.getStyleClass().add("icon-linkbox-battery-good");
            break;
        case OK:
            CssStyleRotation.stop(batteryStatus);
            batteryStatus.getStyleClass().add("icon-linkbox-battery-ok");
            break;
        case LOW:
            CssStyleRotation.stop(batteryStatus);
            batteryStatus.getStyleClass().add("icon-linkbox-battery-low");
            break;
        case CRITICAL:
            CssStyleRotation.stop(batteryStatus);
            batteryStatus.getStyleClass().add("prewarn");
            batteryStatus.getStyleClass().add("icon-linkbox-battery-warning");
            break;
        case EMERGENCY:
        case FAILED:
        case UNHEALTHY:
            CssStyleRotation.setCritical(batteryStatus);
            batteryStatus.getStyleClass().add("icon-linkbox-battery-emergency");
            break;
        case CHARGING:
            CssStyleRotation.stop(batteryStatus);
            batteryStatus.getStyleClass().add("icon-linkbox-battery-charging");
            break;
        case UNDEFINED:
            CssStyleRotation.setCritical(batteryStatus);
            batteryStatus.getStyleClass().add("icon-linkbox-battery-undefined");
            break;
        case PLUGGEDIN:
            CssStyleRotation.stop(batteryStatus);
            batteryStatus.getStyleClass().add("icon-linkbox-battery-unknown");
            break;
        }
    }

    private void initializeLinkBoxConnectionQuality() {
        linkboxConnectionQuality.addListener(
            (observable, oldValue, newValue) -> updateLinkBoxConnectionQuality(newValue));
        linkboxConnectionQuality.bind(viewModel.linkboxConnectionQualityProperty());
    }

    private void updateLinkBoxConnectionQuality(WifiConnectionQuality newValue) {
        linkBoxConnectivity.getStyleClass().removeAll("icon-linkbox-hotspot-ok", "icon-linkbox-hotspot-off");
        switch (newValue) {
        case CONFIGURED:
            CssStyleRotation.stop(linkBoxConnectivity);
            linkBoxConnectivity.getStyleClass().add("icon-linkbox-hotspot-ok");
            break;
        case SCANNING:
            CssStyleRotation.setRotation(
                linkBoxConnectivity, "icon-linkbox-hotspot-warning_1", "icon-linkbox-hotspot-warning_2");
            break;
        case OFFLINE:
        case AVAILABLE:
        case FIRMWAREUPGRADE:
        default:
            CssStyleRotation.setCritical(linkBoxConnectivity);
            linkBoxConnectivity.getStyleClass().add("icon-linkbox-hotspot-off");
            break;
        }
    }

    private void initializeCloudConnectivity() {
        cloudConnectionInfo.addListener((observable, oldValue, newValue) -> updateCloudConnectionInfo(newValue));
        cloudConnectionInfo.bind(viewModel.cloudDataConnectionStatusProperty());
    }

    private void updateCloudConnectionInfo(DataConnectionStatus newValue) {
        cloudConnectivity
            .getStyleClass()
            .removeAll(
                "icon-linkbox-dataconnection-connected",
                "icon-linkbox-dataconnection-downloading",
                "icon-linkbox-dataconnection-uploading",
                "icon-linkbox-dataconnection-invalid");
        switch (newValue) {
        case AVAILABLE:
            cloudConnectivity.getStyleClass().add("icon-linkbox-dataconnection-connected");
            break;
        case DOWNLOADING:
            cloudConnectivity.getStyleClass().add("icon-linkbox-dataconnection-downloading");
            break;
        case UPLOADING:
            cloudConnectivity.getStyleClass().add("icon-linkbox-dataconnection-uploading");
            break;
        case UNKNOWN:
        case NOTAVAILABLE:
        case ERROR:
            cloudConnectivity.getStyleClass().add("prewarn");
            cloudConnectivity.getStyleClass().add("icon-linkbox-dataconnection-invalid");
            break;
        }
    }

    private void initializeGnssinfo() {
        gnssInfo.addListener((observable, oldValue, newValue) -> updateGnssInfo(newValue));
        gnssInfo.bind(viewModel.gnssStateProperty());
        numberOfSatellitesProperty.bind(viewModel.numberOfSatellitesProperty());
    }

    private void updateGnssInfo(LinkBoxGnssState newValue) {
        gnssState
            .getStyleClass()
            .removeAll("icon-linkbox-gnss-streaming", "icon-linkbox-gnss-available", "icon-linkbox-gnss-notavailable");
        switch (newValue) {
        case STREAMING:
            CssStyleRotation.stop(gnssState);
            gnssState.getStyleClass().add("icon-linkbox-gnss-streaming");
            break;
        case AVAILABLE:
            CssStyleRotation.stop(gnssState);
            gnssState.getStyleClass().add("icon-linkbox-gnss-available");
            break;
        case NOTAVAILABLE:
        case UNDEFINED:
        default:
            CssStyleRotation.setCritical(gnssState);
            gnssState.getStyleClass().add("icon-linkbox-gnss-notavailable");
            break;
        }
    }

    private void initializeDroneConnectionQuality() {
        droneConnectionQuality.addListener((observable, oldValue, newValue) -> updateDroneConnectionQuality(newValue));
        droneConnectionQuality.bind(viewModel.droneConnectionQualityProperty());
    }

    private void updateDroneConnectionQuality(DroneConnectionQuality newValue) {
        droneConnectivity
            .getStyleClass()
            .removeAll(
                "icon-linkbox-droneconnection-ok",
                "icon-linkbox-droneconnection-warning",
                "icon-linkbox-droneconnection-error");
        switch (newValue) {
        case OK:
            CssStyleRotation.stop(droneConnectivity);
            droneConnectivity.getStyleClass().add("icon-linkbox-droneconnection-ok");
            break;
        case PARTIAL:
            CssStyleRotation.stop(droneConnectivity);
            droneConnectivity.getStyleClass().add("prewarn");
            droneConnectivity.getStyleClass().add("icon-linkbox-droneconnection-warning");
            break;
        case DOWN:
        default:
            CssStyleRotation.setCritical(droneConnectivity);
            droneConnectivity.getStyleClass().add("icon-linkbox-droneconnection-error");
            break;
        }
    }

    private void initializeLinkBoxWarningInfo() {
        linkBoxWarningInfo.addListener((observable, oldValue, newValue) -> updateLinkBoxWarningInfo(newValue));
        linkBoxWarningInfo.bind(viewModel.warningLevelProperty());
    }

    private void updateLinkBoxWarningInfo(LinkBoxAlertLevel newValue) {
        linkboxwarning.getStyleClass().removeAll("icon-linkbox-status-ok", "icon-linkbox-status-error");
        switch (newValue) {
        case ERROR:
        case WARNING:
            CssStyleRotation.setCritical(linkboxwarning);
            linkboxwarning.getStyleClass().add("icon-linkbox-status-error");
            break;
        case INFO:
        default:
            CssStyleRotation.stop(linkboxwarning);
            linkboxwarning.getStyleClass().add("icon-linkbox-status-ok");
            linkboxwarning.setVisible(false);
            break;
        }
    }

    public void configureRTK(ActionEvent actionEvent) {
        viewModel.getShowRTKConfigurationDialogCommand().execute();
    }
}
