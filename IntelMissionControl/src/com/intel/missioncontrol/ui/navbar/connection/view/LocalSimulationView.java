/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.model.LocalSimulationItem;
import com.intel.missioncontrol.ui.navbar.connection.view.widget.AirplaneSpeedComponent;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.LocalSimulationViewModel;
import com.intel.missioncontrol.ui.navigation.ConnectionPage;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class LocalSimulationView extends ViewBase<LocalSimulationViewModel> {

    public static final String START_SIMULATION =
        "com.intel.missioncontrol.ui.connection.view.LocalSimulationView.actionButton.start";
    public static final String STOP_SIMULATION =
        "com.intel.missioncontrol.ui.connection.view.LocalSimulationView.actionButton.stop";
    public static final double SET_START_POSITION_BUTTON_FIT_HEIGHT = ScaleHelper.emsToPixels(1.35);

    @InjectViewModel
    private LocalSimulationViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private ComboBox<LocalSimulationItem> simulatorsCombo;

    @FXML
    private Button actionButton;

    @FXML
    private Separator actionSectionSeparator;

    @FXML
    private Pane propertiesPanel;

    @FXML
    private AutoCommitSpinner<VariantQuantity> latitudeSpinner;

    @FXML
    private AutoCommitSpinner<VariantQuantity> longitudeSpinner;

    @FXML
    private ImageView setStartPositionImage;

    @FXML
    private VBox fullyConnectedVBox;

    @FXML
    private Label labelFullyConnected;

    @FXML
    private AirplaneSpeedComponent airplaneSpeedComponent;

    @Inject
    private ISettingsManager settingsManager;

    private final ILanguageHelper languageHelper;

    @FXML
    private ToggleButton setStartPositionButton;

    @Inject
    public LocalSimulationView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    public LocalSimulationViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void initializeView() {
        super.initializeView();

        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        simulatorsCombo.itemsProperty().bind(viewModel.simulatorsProperty());
        simulatorsCombo.setCellFactory(
            view ->
                new ListCell<LocalSimulationItem>() {
                    @Override
                    protected void updateItem(LocalSimulationItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getLabel());
                            setId(item.getLabel().replaceAll(" ", "_").toUpperCase());
                        }
                    }
                });
        simulatorsCombo.valueProperty().bindBidirectional(viewModel.selectedSimulatorProperty());

        setStartPositionImage.setFitHeight(SET_START_POSITION_BUTTON_FIT_HEIGHT);

        ViewHelper.initCoordinateSpinner(latitudeSpinner, viewModel.latitudeQuantityProperty(), generalSettings, true);
        ViewHelper.initCoordinateSpinner(
            longitudeSpinner, viewModel.longitudeQuantityProperty(), generalSettings, false);

        viewModel
            .isSimulationStartedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    boolean isStarted = newValue;
                    viewModel.connectedPageProperty();
                    // Change label of the button
                    String text = isStarted ? STOP_SIMULATION : START_SIMULATION;
                    actionButton.setText(languageHelper.getString(text));

                    ConnectionPage connectionPage = isStarted ? ConnectionPage.LOCAL_SIMULATION : null;
                    ConnectionState connectionState =
                        isStarted ? ConnectionState.CONNECTED : ConnectionState.NOT_CONNECTED;
                    viewModel.connectedPageProperty().setValue(connectionPage);
                    viewModel.connectionStateProperty().setValue(connectionState);
                });

        // airplaneSpeedComponent.simulationSpeedProperty().bindBidirectional(viewModel.simulationSpeedProperty());

        simulatorsCombo.disableProperty().bind(viewModel.isSimulationStartedProperty());
        actionButton.disableProperty().bind(viewModel.getActionButtonCommand().notExecutableProperty());
        propertiesPanel.visibleProperty().bind(viewModel.getActionButtonCommand().executableProperty());
        propertiesPanel.managedProperty().bind(propertiesPanel.visibleProperty());
        actionSectionSeparator.visibleProperty().bind(propertiesPanel.visibleProperty());
        labelFullyConnected.visibleProperty().bind(viewModel.isSimulationStartedProperty());
        fullyConnectedVBox.managedProperty().bind(viewModel.isSimulationStartedProperty());
        setStartPositionButton.selectedProperty().bindBidirectional(viewModel.takeoffButtonPressedProperty());


    }

    @FXML
    public void actionButtonPressed() {
        viewModel.getActionButtonCommand().execute();
    }

    @FXML
    public void startPositionButtonPressed() {
        viewModel.getSetStartPositionButtonCommand().execute();
    }
}
