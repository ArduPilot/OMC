/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.RtkType;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.RtkBaseStationViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.EnumSet;
import java.util.ResourceBundle;
import javafx.scene.layout.Pane;

import static com.intel.missioncontrol.ui.common.BindingUtils.bindVisibility;

public class RtkBaseStationView extends ViewBase<RtkBaseStationViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    private Label header;

    @FXML
    private Label helpPopupLabel;

    @FXML
    private Button internalConnectButton;

    @FXML
    private Button internalDisconnectButton;

    @FXML
    private Button internalCrossCheckButton;

    @FXML
    private Node internalBaseStationConnectionSettings;

    @FXML
    private Node externalBaseStationConnectionSettings;

    @FXML
    private Node ntripConnectionSettings;

    @FXML
    private ComboBox<RtkType> rtkSourceCombo;

    @InjectViewModel
    private RtkBaseStationViewModel viewModel;

    @Inject
    private ILanguageHelper languageHelper;

    private Image externalRtkStation;
    private Image internalRtkStation;
    private Image ntripRtkStation;

    @Override
    public void initializeView() {
        super.initializeView();

        externalRtkStation = new Image("/com/intel/missioncontrol/gfx/gfx_external_base_station.svg");
        internalRtkStation = new Image("/com/intel/missioncontrol/gfx/gfx_internal_base_station.svg");
        ntripRtkStation = new Image("/com/intel/missioncontrol/gfx/gfx_ntrip_base_station.svg");

        rtkSourceCombo
            .valueProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    switch (newValue) {
                    case NTRIP:
                        header.setGraphic(new ImageView(ntripRtkStation));
                        break;
                    case EXTERNAL_BASE_STATION:
                        header.setGraphic(new ImageView(externalRtkStation));
                        break;
                    case INTERNAL_BASE_STATION:
                        header.setGraphic(new ImageView(internalRtkStation));
                        break;
                    default:
                        header.setGraphic(null);
                        break;
                    }
                });

        initCombobox();
        initButtons();
        initHelpPopup();

        bindVisibility(ntripConnectionSettings, rtkSourceCombo.valueProperty().isEqualTo(RtkType.NTRIP));
        bindVisibility(
            externalBaseStationConnectionSettings,
            rtkSourceCombo.valueProperty().isEqualTo(RtkType.EXTERNAL_BASE_STATION));
        bindVisibility(
            internalBaseStationConnectionSettings,
            rtkSourceCombo.valueProperty().isEqualTo(RtkType.INTERNAL_BASE_STATION));
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    public RtkBaseStationViewModel getViewModel() {
        return viewModel;
    }

    private void initCombobox() {
        rtkSourceCombo.setCellFactory(
            view ->
                new ListCell<RtkType>() {
                    @Override
                    protected void updateItem(RtkType item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setId(item.name().toLowerCase());
                            setText(item.toString());
                        }
                    }
                });
        rtkSourceCombo.itemsProperty().set(FXCollections.observableArrayList(EnumSet.allOf(RtkType.class)));
        rtkSourceCombo.setPromptText(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.connection.view.RtkBaseStationView.sourceComboPlaceholder"));
        rtkSourceCombo.disableProperty().bind(viewModel.isConnectedBinding());
        rtkSourceCombo.valueProperty().bindBidirectional(viewModel.rtkSourceProperty());
    }

    private void initButtons() {
        internalConnectButton.setOnAction(event -> viewModel.getConnectCommand().execute());
        bindVisibility(
            internalConnectButton, viewModel.connectedStateProperty().isEqualTo(ConnectionState.NOT_CONNECTED));
        internalConnectButton
            .disableProperty()
            .bind(
                rtkSourceCombo
                    .getSelectionModel()
                    .selectedItemProperty()
                    .isNull()
                    .or(rtkSourceCombo.getSelectionModel().selectedItemProperty().isEqualTo(RtkType.NONE)));

        internalDisconnectButton.setOnAction(event -> viewModel.getDisconnectCommand().execute());

        bindVisibility(
            internalDisconnectButton, viewModel.connectedStateProperty().isEqualTo(ConnectionState.CONNECTED));

        internalCrossCheckButton.setOnAction(event -> viewModel.getCrossCheckCommand().execute());
        // Temporary hide the button
        internalCrossCheckButton.getParent().setVisible(false);
        internalCrossCheckButton.getParent().setManaged(false);
        //        bindVisibility(internalCrossCheckButton.getParent(),
        // viewModel.getCrossCheckCommand().executableProperty());
    }

    private void initHelpPopup() {
        Tooltip tooltip =
            new Tooltip(
                languageHelper.getString("com.intel.missioncontrol.ui.connection.view.RtkBaseStationView.popupText"));

        tooltip.setWrapText(true);
        tooltip.setAutoHide(true);

        helpPopupLabel.setOnMouseClicked(
            event -> {
                Bounds bounds = helpPopupLabel.localToScreen(helpPopupLabel.getBoundsInLocal());
                double x = bounds.getMaxX();
                double y = bounds.getMaxY();
                double width = helpPopupLabel.getParent().getBoundsInLocal().getWidth();
                tooltip.setMaxWidth(width);
                tooltip.show(helpPopupLabel, x - width / 2, y);
            });
    }
}
