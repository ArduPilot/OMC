/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.connection.dialog;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.asyncfx.beans.binding.ConversionBindings;
import org.asyncfx.beans.binding.Converters;
import com.intel.missioncontrol.drone.connection.TcpIpTransportType;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.common.hardware.PlatformItem;
import com.intel.missioncontrol.ui.common.hardware.PlatformItemType;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.validation.ValidationMessage;
import de.saxsys.mvvmfx.utils.validation.visualization.ControlsFxVisualizer;
import de.saxsys.mvvmfx.utils.validation.visualization.ValidationVisualizer;
import java.util.Map;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class ConnectionDialogView extends DialogView<ConnectionDialogViewModel> {

    private static final String AIRPLANE_IMAGE_PATH = "/com/intel/missioncontrol/gfx/uav/";
    private static final String AIRPLANE_IMAGE_FILE_DEFAULT = "default.png";

    private static final Map<PlatformItemType, String> PLATFORM_CAPTION_MAP =
        ImmutableMap.<PlatformItemType, String>builder()
            .put(
                PlatformItemType.FIXED_WING,
                "com.intel.missioncontrol.ui.common.hardware.HardwareSelectionView.fixedWings")
            .put(
                PlatformItemType.MULTICOPTER,
                "com.intel.missioncontrol.ui.common.hardware.HardwareSelectionView.multicopters")
            .build();
    // TODO same as HardwareSelectionView?
    private static final double UAV_IMAGE_FIT_HEIGHT = ScaleHelper.emsToPixels(2.67);

    @FXML
    private Pane root;

    @FXML
    private TextField connectionName;

    @FXML
    private ToggleGroup transportToggleGroup;

    @FXML
    private RadioButton udpRadio;

    @FXML
    private RadioButton tcpRadio;

    @FXML
    private ComboBox<PlatformItem> uavComboBox;

    @FXML
    private Spinner<Integer> portSpinner;

    @FXML
    private Spinner<Integer> systemIdSpinner;

    @FXML
    private TextField hostTextField;

    @FXML
    private Button commitButton;

    @FXML
    private Label validationErrorLabel;

    @InjectViewModel
    private ConnectionDialogViewModel viewModel;

    private ILanguageHelper languageHelper;

    @Inject
    public ConnectionDialogView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    protected Parent getRootNode() {
        return root;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        connectionName.textProperty().bindBidirectional(viewModel.connectionNameProperty());

        tcpRadio.setUserData(TcpIpTransportType.TCP);
        udpRadio.setUserData(TcpIpTransportType.UDP);
        setTransportToggle(viewModel.transportTypeProperty().getValue());
        viewModel.transportTypeProperty().addListener((observable, oldValue, newValue) -> setTransportToggle(newValue));

        tcpRadio.disableProperty().bind(viewModel.fixedConnectionProperty());
        udpRadio.disableProperty().bind(viewModel.fixedConnectionProperty());

        transportToggleGroup
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) ->
                    viewModel.transportTypeProperty().setValue((TcpIpTransportType)newValue.getUserData()));

        uavComboBox.disableProperty().bind(viewModel.fixedPlatformItemProperty());
        uavComboBox.itemsProperty().bind(viewModel.availablePlatformItemsProperty());
        uavComboBox.valueProperty().bindBidirectional(viewModel.selectedPlatformItemProperty());
        uavComboBox.setCellFactory(
            param ->
                new ListCell<>() {
                    @Override
                    protected void updateItem(PlatformItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null) {
                            return;
                        }

                        if (item.isCaption()) {
                            setDisable(true);
                            getStyleClass().add("title-menu-item");
                            setText(languageHelper.getString(PLATFORM_CAPTION_MAP.get(item.getItemType())));
                            setGraphic(null);
                        } else {
                            setDisable(false);
                            getStyleClass().remove("title-menu-item");

                            if (item.getDescription() != null) {
                                setText(item.getDescription().getName());
                            }

                            ImageView imageView = new ImageView();
                            imageView.setImage(createImageFromImageFile(item.getDescription().getImageFile()));
                            imageView.setFitHeight(UAV_IMAGE_FIT_HEIGHT);
                            imageView.setPreserveRatio(true);
                            setGraphic(imageView);
                        }
                    }
                });

        hostTextField.textProperty().bindBidirectional(viewModel.hostProperty());
        hostTextField.disableProperty().bind(viewModel.fixedConnectionProperty());

        IntegerValidator portSpinnerValueFactory = new IntegerValidator(5760, 1, 99999, 1, 5);
        portSpinner.setValueFactory(portSpinnerValueFactory.getValueFactory());
        ConversionBindings.bindBidirectional(
            portSpinner.getValueFactory().valueProperty(), viewModel.portProperty(), Converters.numberToInt());
        portSpinner.disableProperty().bind(viewModel.fixedConnectionProperty());

        IntegerValidator systemIdSpinnerValueFactory = new IntegerValidator(1, 0, 99999, 1, 5);
        systemIdSpinner.setValueFactory(systemIdSpinnerValueFactory.getValueFactory());
        ConversionBindings.bindBidirectional(
            systemIdSpinner.getValueFactory().valueProperty(), viewModel.systemIdProperty(), Converters.numberToInt());
        systemIdSpinner.disableProperty().bind(viewModel.fixedConnectionProperty());

        commitButton.disableProperty().bind(viewModel.getFormValidationStatus().validProperty().not());
        commitButton.textProperty().bind(viewModel.commitTextProperty());

        ValidationVisualizer visualizer = new ControlsFxVisualizer();
        visualizer.initVisualization(viewModel.getFormValidationStatus(), commitButton);

        validationErrorLabel.visibleProperty().bind(viewModel.getFormValidationStatus().validProperty().not());
        validationErrorLabel
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> {
                        if (viewModel.getFormValidationStatus().isValid()) {
                            return "";
                        }

                        Optional<ValidationMessage> msg = viewModel.getFormValidationStatus().getHighestMessage();
                        return msg.isPresent() ? msg.get().getMessage() : "";
                    },
                    viewModel.getFormValidationStatus().validProperty()));
    }

    private void setTransportToggle(TcpIpTransportType transportType) {
        transportToggleGroup
            .getToggles()
            .stream()
            .filter(t -> t.getUserData().equals(transportType))
            .findAny()
            .ifPresent(t -> t.setSelected(true));
    }

    public void OnCommitButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getCommitCommand().execute();
    }

    public void OnCancelButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
    }

    private Image createImageFromImageFile(String imageFile) {
        String fileName =
            (imageFile != null && !imageFile.isEmpty())
                ? imageFile.replaceAll("[^.0-9A-Za-z_-]+", "_").replaceAll("\\.\\.", ".")
                : AIRPLANE_IMAGE_FILE_DEFAULT;

        Image res;
        try {
            res = new Image(AIRPLANE_IMAGE_PATH + fileName);
        } catch (java.lang.IllegalArgumentException ex) {
            res = new Image(AIRPLANE_IMAGE_PATH + AIRPLANE_IMAGE_FILE_DEFAULT);
        }

        return res;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return viewModel.dialogTitleProperty();
    }
}
