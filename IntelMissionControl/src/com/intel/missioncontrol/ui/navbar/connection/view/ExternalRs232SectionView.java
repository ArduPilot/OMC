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
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.ExternalRs232SectionViewModel;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.rs232.Rs232Params;
import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/** @author Vladimir Iordanov */
public class ExternalRs232SectionView extends ViewBase<ExternalRs232SectionViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    public VBox rtkConfigurationView;

    @FXML
    private Button refreshPortsButton;

    @FXML
    private ComboBox<String> portsCombobox;

    @FXML
    private Spinner<Integer> baud;

    @FXML
    private ComboBox<Rs232Params.DataBits> dataBits;

    @FXML
    private ComboBox<Rs232Params.StopBit> stopBits;

    @FXML
    private ComboBox<Rs232Params.Parity> parity;

    @FXML
    private VBox portSettings;

    @InjectViewModel
    private ExternalRs232SectionViewModel viewModel;

    private static final double REFRESH_ICON_SIZE = ScaleHelper.emsToPixels(1.3);

    @Override
    public void initializeView() {
        super.initializeView();
        portSettings.disableProperty().bind(viewModel.isConnectedProperty());

        initBitRateControl();

        try (var svgInput =
            Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(
                    "/com/intel/missioncontrol/icons/icon_refresh(fill=theme-button-text-color).svg")) {
            Image iconImage = new Image(svgInput, REFRESH_ICON_SIZE, REFRESH_ICON_SIZE, true, false);
            refreshPortsButton.setGraphic(new ImageView(iconImage));
        } catch (Exception e) {
            Debug.getLog().log(Debug.WARNING, "ExternalRs232SectionView icon_refresh.svg:", e);
        }

        refreshPortsButton.setOnAction(event -> viewModel.getRefreshPortsCommand().execute());

        Animation buttonProgressAnimation =
            Animations.forButtonGraphicRotation(refreshPortsButton, Animations.ROTATION_CLOCK_WISE);

        viewModel
            .getRefreshPortsCommand()
            .executableProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        buttonProgressAnimation.stop();
                    } else {
                        buttonProgressAnimation.playFromStart();
                    }
                });

        portsCombobox.itemsProperty().bind(viewModel.portsListProperty());
        portsCombobox.valueProperty().bindBidirectional(viewModel.portProperty());
        portsCombobox.disableProperty().bind(viewModel.getRefreshPortsCommand().notExecutableProperty());

        dataBits.itemsProperty().bind(viewModel.dataBitsListProperty());
        dataBits.valueProperty().bindBidirectional(viewModel.dataBitsProperty());
        dataBits.setCellFactory(
            param ->
                new ListCell<Rs232Params.DataBits>() {
                    @Override
                    protected void updateItem(Rs232Params.DataBits item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setId(String.valueOf(item.getValue()));
                            setText(item.getLabel());
                        }
                    }
                });

        stopBits.itemsProperty().bind(viewModel.stopBitsListProperty());
        stopBits.valueProperty().bindBidirectional(viewModel.stopBitsProperty());
        stopBits.setCellFactory(
            param ->
                new ListCell<Rs232Params.StopBit>() {
                    @Override
                    protected void updateItem(Rs232Params.StopBit item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setId(String.valueOf(item.getValue()));
                            setText(item.getLabel());
                        }
                    }
                });

        parity.itemsProperty().bind(viewModel.parityListProperty());
        parity.valueProperty().bindBidirectional(viewModel.parityProperty());
        parity.setCellFactory(
            param ->
                new ListCell<Rs232Params.Parity>() {
                    @Override
                    protected void updateItem(Rs232Params.Parity item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setId(String.valueOf(item.getValue()));
                            setText(item.getLabel());
                        }
                    }
                });

        // Show Configuration section only when is connected
        BindingUtils.bindVisibility(rtkConfigurationView, viewModel.isConnectedProperty().not());
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    public ExternalRs232SectionViewModel getViewModel() {
        return viewModel;
    }

    private void initBitRateControl() {
        final int amountToStepBy = 2;
        final int minDigits = String.valueOf(Rs232Params.MIN_BIT_RATE).length();
        final int maxDigits = String.valueOf(Rs232Params.MAX_BIT_RATE).length();

        IntegerValidator validator =
            new IntegerValidator(
                viewModel.getBitRate(),
                Rs232Params.MIN_BIT_RATE,
                Rs232Params.MAX_BIT_RATE,
                minDigits,
                maxDigits,
                amountToStepBy);

        SpinnerValueFactory<Integer> valueFactory = validator.getValueFactory();

        baud.setValueFactory(valueFactory);
        baud.getEditor().setTextFormatter(validator.getTextFormatter());

        valueFactory.valueProperty().bindBidirectional(viewModel.bitRateProperty());
    }

}
