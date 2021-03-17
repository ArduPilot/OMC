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
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.UnmannedAerialVehicle;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.DetectedUavViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.animation.Animation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DetectedUavView extends ViewBase<DetectedUavViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    private ComboBox<UnmannedAerialVehicle> selectedUav;

    @FXML
    private Button refreshButton;

    @FXML
    private Label uavPinLabel;

    @FXML
    private ComboBox<String> uavPinsList;

    @FXML
    private Button connectButton;

    @FXML
    private Button disconnectButton;

    @FXML
    private CheckBox userDisconnectCheck;

    @FXML
    private HBox connectingBlock;

    @FXML
    private ImageView iconProgress;

    @FXML
    private VBox uavPinsListBlock;

    @InjectViewModel
    private DetectedUavViewModel viewModel;

    private DelegateCommand refreshCommand;
    private DelegateCommand connectCommand;

    private static final double REFRESH_ICON_SIZE = ScaleHelper.emsToPixels(1.3);
    private Animation refreshButtonAnimation;

    @Override
    public void initializeView() {
        super.initializeView();

        uavPinLabel.visibleProperty().bind(viewModel.uavPinLabelVisibleProperty());
        uavPinLabel.managedProperty().bind(viewModel.uavPinLabelVisibleProperty());

        uavPinsList.visibleProperty().bind(viewModel.uavPinsListVisibleProperty());
        uavPinsList.managedProperty().bind(viewModel.uavPinsListVisibleProperty());
        uavPinsList.disableProperty().bind(viewModel.uavPinsListDisableProperty());
        uavPinsList.valueProperty().bindBidirectional(viewModel.uavPinValueProperty());
        uavPinsList
            .itemsProperty()
            .set(FXCollections.observableList(new ArrayList<>(viewModel.uavPinsHistoricalValuesProperty().getValue())));

        viewModel
            .uavPinsHistoricalValuesProperty()
            .addListener(
                (observable, oldValue, newValue) ->
                    uavPinsList.itemsProperty().set(FXCollections.observableList(new ArrayList<>(newValue))));

        Image iconImage =
            new Image(
                "/com/intel/missioncontrol/icons/icon_refresh(fill=theme-button-text-color).svg",
                REFRESH_ICON_SIZE,
                REFRESH_ICON_SIZE,
                true,
                false);
        refreshButton.setGraphic(new ImageView(iconImage));

        refreshButton
            .disableProperty()
            .bind(
                viewModel
                    .connectionStateProperty()
                    .isNotEqualTo(ConnectionState.NOT_CONNECTED)
                    .or(viewModel.refreshInProgressProperty()));

        viewModel
            .refreshInProgressProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        if (refreshButtonAnimation == null) {
                            refreshButtonAnimation =
                                Animations.forButtonGraphicRotation(refreshButton, Animations.ROTATION_CLOCK_WISE);
                        }

                        refreshButtonAnimation.playFromStart();
                    } else {
                        refreshButtonAnimation.stop();
                        refreshButtonAnimation = null;
                    }
                });

        selectedUav.itemsProperty().bind(viewModel.availableUavsProperty());
        selectedUav.setCellFactory(
            view ->
                new ListCell<UnmannedAerialVehicle>() {
                    @Override
                    protected void updateItem(UnmannedAerialVehicle item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.name);
                            setId(item.info.serialNumber);
                        } else {
                            setId("prompt");
                        }
                    }
                });
        selectedUav.valueProperty().bindBidirectional(viewModel.selectedUavProperty());
        selectedUav.disableProperty().bind(viewModel.selectedUavDisableProperty());

        connectButton.disableProperty().bind(viewModel.connectButtonDisableProperty());
        connectButton
            .visibleProperty()
            .bind(viewModel.connectionStateProperty().isEqualTo(ConnectionState.NOT_CONNECTED));
        connectButton
            .managedProperty()
            .bind(viewModel.connectionStateProperty().isEqualTo(ConnectionState.NOT_CONNECTED));

        BindingUtils.bindVisibility(
            connectingBlock, viewModel.connectionStateProperty().isEqualTo(ConnectionState.CONNECTING));
        Animation progressAnimation = Animations.forImageRotation(iconProgress, Animations.ROTATION_CLOCK_WISE);
        viewModel
            .connectionInProgressProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        progressAnimation.play();
                    } else {
                        progressAnimation.stop();
                    }
                });

        disconnectButton.disableProperty().bind(viewModel.disconnectButtonDisableProperty());
        disconnectButton
            .visibleProperty()
            .bind(viewModel.connectionStateProperty().isEqualTo(ConnectionState.CONNECTED));
        disconnectButton
            .managedProperty()
            .bind(viewModel.connectionStateProperty().isEqualTo(ConnectionState.CONNECTED));
        disconnectButton.addEventHandler(MouseEvent.MOUSE_CLICKED, viewModel::disconnectButtonOnMouseClick);

        userDisconnectCheck.visibleProperty().bind(viewModel.userDisconnectCheckVisibleProperty());
        userDisconnectCheck.selectedProperty().bindBidirectional(viewModel.userDisconnectCheckMarkedProperty());

        refreshCommand = viewModel.getRefreshCommand(true);
        connectCommand = viewModel.getConnectCommand(true);

        refreshAction();
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public DetectedUavViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    protected void refreshAction() {
        refreshCommand.execute();
    }

    @FXML
    public void connectAction() {
        connectCommand.execute();
    }

    @FXML
    public void onConnectingCancelClicked() {
        connectCommand.cancel();
    }
}
