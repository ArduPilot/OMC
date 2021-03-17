/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.common.BindingUtils;
import com.intel.missioncontrol.ui.navbar.NavBarMenuView;
import com.intel.missioncontrol.ui.navigation.ConnectionPage;
import de.saxsys.mvvmfx.InjectViewModel;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ConnectionView extends NavBarMenuView<ConnectionViewModel, ConnectionPage> {

    private static final Set<String> STATUS_CLASSES =
        EnumSet.allOf(ButtonCssStyles.class).stream().map(ButtonCssStyles::getCssClassName).collect(Collectors.toSet());

    private static final List<ConnectionPage> ALWAYS_ENABLED_PAGES =
        ImmutableList.<ConnectionPage>builder()
            .add(ConnectionPage.RTK_BASE_STATION)
            .add(ConnectionPage.JOYSTICK)
            .build();

    private final ImmutableMap<ConnectionState, Consumer<RadioButton>> onConnectionStateChanged =
        ImmutableMap.<ConnectionState, Consumer<RadioButton>>builder()
            .put(
                ConnectionState.CONNECTED,
                rb -> {
                    disablePossibilityToConnect(rb);
                    propagateConnectionStatus(rb, ButtonCssStyles.CONNECTED);
                })
            .put(
                ConnectionState.CONNECTED_WARNING,
                rb -> {
                    disablePossibilityToConnect(rb);
                    propagateConnectionStatus(rb, ButtonCssStyles.CONNECTED, ButtonCssStyles.CONNECTED_WARNING);
                })
            .put(ConnectionState.NOT_CONNECTED, this::enablePossibilityToConnect)
            .build();

    @InjectViewModel @MonotonicNonNull
    private ConnectionViewModel viewModel;

    @FXML @MonotonicNonNull
    private Pane layoutRoot;

    @FXML @MonotonicNonNull
    private Parent transformRoot;

    @FXML @MonotonicNonNull
    private ToggleGroup menuToggleGroup;

    @FXML @MonotonicNonNull
    private Pane contentPane;

    @FXML @MonotonicNonNull
    private Region localSimulationView;

    @FXML @MonotonicNonNull
    private Region uavConnectView;

    @FXML @MonotonicNonNull
    private Region logFileView;

    @FXML @MonotonicNonNull
    private Region rtkBaseStationView;

    @FXML @MonotonicNonNull
    private Region joystickView;

    @FXML @MonotonicNonNull
    private Region dataTransferFtpView;

    @FXML @MonotonicNonNull
    private RadioButton localSimulationButton;

    @FXML @MonotonicNonNull
    private RadioButton uavConnectButton;

    @FXML @MonotonicNonNull
    private RadioButton rtkBaseStationButton;

    @FXML @MonotonicNonNull
    private RadioButton joystickButton;

    @FXML @MonotonicNonNull
    private RadioButton logFileButton;

    @FXML @MonotonicNonNull
    private RadioButton dataTransferFtpButton;

    private final ILanguageHelper languageHelper;

    @Inject
    public ConnectionView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        Expect.notNull(
            localSimulationView, "localSimulationView",
            uavConnectView, "uavConnectView",
            logFileView, "logFileView",
            rtkBaseStationView, "rtkBaseStationView",
            joystickView, "joystickView",
            dataTransferFtpView, "dataTransferFtpView",
            localSimulationButton, "localSimulationButton",
            uavConnectButton, "uavConnectButton",
            logFileButton, "logFileButton",
            dataTransferFtpButton, "dataTransferFtpButton");

        initializeButtons();

        localSimulationButton.disableProperty().bind(connectedButNotTo(ConnectionPage.LOCAL_SIMULATION));
        localSimulationButton.selectedProperty().bindBidirectional(viewModel.localSimulationButtonSelectedProperty());
        BindingUtils.bindVisibility(localSimulationButton, viewModel.isAdvancedOperationLevelBinding());
        localSimulationView.visibleProperty().bindBidirectional(viewModel.localSimulationViewVisibleProperty());

        uavConnectButton.disableProperty().bind(connectedButNotTo(ConnectionPage.UAV_CONNECT));
        uavConnectButton.selectedProperty().bindBidirectional(viewModel.uavConnectButtonSelectedProperty());
        uavConnectView.visibleProperty().bindBidirectional(viewModel.uavConnectViewVisibleProperty());

        logFileButton.disableProperty().bind(connectedButNotTo(ConnectionPage.LOG_FILE));
        logFileButton.selectedProperty().bindBidirectional(viewModel.logFileButtonSelectedProperty());
        logFileView.visibleProperty().bindBidirectional(viewModel.logFileViewVisibleProperty());

        rtkBaseStationButton.selectedProperty().bindBidirectional(viewModel.rtkBaseStationButtonSelectedProperty());
        rtkBaseStationButton.disableProperty().bind(viewModel.isMissionChosen().not());
        rtkBaseStationView.visibleProperty().bindBidirectional(viewModel.rtkBaseStationViewVisibleProperty());

        // hide for now until phase 3
        joystickButton.setDisable(true);
        joystickButton.setVisible(false);
        joystickButton.setManaged(false);
        joystickView.setVisible(false);
        // joystickButton.selectedProperty().bindBidirectional(viewModel.joystickButtonSelectedProperty());
        // joystickView.visibleProperty().bindBidirectional(viewModel.joystickViewVisibleProperty());

        dataTransferFtpView.visibleProperty().bindBidirectional(viewModel.dataTransferFtpViewVisibleProperty());
        dataTransferFtpButton.selectedProperty().bindBidirectional(viewModel.dataTransferFtpSelectedProperty());
        dataTransferFtpButton.disableProperty().bind(viewModel.dataTransferFtpViewDisableProperty());

        final Map<ConnectionPage, Region> map =
            ImmutableMap.<ConnectionPage, Region>builder()
                .put(ConnectionPage.LOCAL_SIMULATION, localSimulationView)
                .put(ConnectionPage.UAV_CONNECT, uavConnectView)
                .put(ConnectionPage.LOG_FILE, logFileView)
                .put(ConnectionPage.RTK_BASE_STATION, rtkBaseStationView)
                .put(ConnectionPage.JOYSTICK, joystickView)
                .put(ConnectionPage.DATA_TRANSFER_FTP, dataTransferFtpView)
                .build();

        // When the current page changes programmatically, we need to manually toggle the corresponding radio button.
        //
        viewModel
            .currentPageProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectRadioButton(newValue);
                        currentTabChanged(map, newValue);
                    }
                });

        // When the user clicks on a radio button and selects it, we need to update the current page property.
        //
        getMenuToggleGroup()
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (viewModel != null && newValue != null && newValue.getUserData() != null) {
                        viewModel.currentPageProperty().set(ConnectionPage.valueOf((String)newValue.getUserData()));
                    }
                });

        localSimulationView
            .prefWidthProperty()
            .addListener((observable, oldValue, newValue) -> getContentPane().setPrefWidth(newValue.doubleValue()));

        selectRadioButton(viewModel.currentPageProperty().get());

        viewModel
            .rtkConnectionState()
            .addListener(
                (o, prev, status) -> {
                    onConnectionStateChanged.get(status).accept(rtkBaseStationButton);
                    updateRtkMenuText();
                });
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    public Parent getTransformRoot() {
        return transformRoot;
    }

    @Override
    public ToggleGroup getMenuToggleGroup() {
        return menuToggleGroup;
    }

    @Nullable
    @Override
    public Pane getContentPane() {
        return contentPane;
    }

    @Override
    protected ConnectionViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void onBackgroundClicked() {
        viewModel.getCloseCommand().execute();
    }

    @FXML
    public void onCloseClicked() {
        viewModel.getCloseCommand().execute();
    }

    private void updateRtkMenuText() {
        StringBuffer text =
            new StringBuffer(
                languageHelper.getString("com.intel.missioncontrol.ui.connection.ConnectionView.rtkBaseStation"));
        if (viewModel.rtkConnectionState().get() == ConnectionState.CONNECTED
                || viewModel.rtkConnectionState().get() == ConnectionState.CONNECTED_WARNING) {
            text.append(" (").append(viewModel.rtkBaseConnectionType().get().getMenuText()).append(")");
        }

        rtkBaseStationButton.setText(text.toString());
    }

    private BooleanBinding connectedButNotTo(ConnectionPage localSimulation) {
        return connected().and(notConnectedTo(localSimulation));
    }

    private BooleanBinding connected() {
        return viewModel.connectedPageProperty().isNotNull();
    }

    private BooleanBinding notConnectedTo(ConnectionPage connectionPage) {
        return viewModel.connectedPageProperty().isNotEqualTo(connectionPage);
    }

    private void initializeButtons() {
        ImmutableMap<ConnectionPage, RadioButton> buttons =
            ImmutableMap.<ConnectionPage, RadioButton>builder()
                .put(ConnectionPage.LOCAL_SIMULATION, localSimulationButton)
                .put(ConnectionPage.UAV_CONNECT, uavConnectButton)
                .put(ConnectionPage.LOG_FILE, logFileButton)
                .put(ConnectionPage.RTK_BASE_STATION, rtkBaseStationButton)
                //            .put(ConnectionPage.JOYSTICK, joystickButton) // hide for now until phase 3
                .put(ConnectionPage.DATA_TRANSFER_FTP, dataTransferFtpButton)
                .build();

        viewModel
            .connectedPageProperty()
            .addListener(
                (observable, previous, current) -> {
                    if (isConnectionProcedure(current)) {
                        propagateConnectionStatus(buttons.get(current), ButtonCssStyles.CONNECTED);
                        buttons.entrySet()
                            .stream()
                            .filter(e -> e.getKey() != current)
                            .filter(e -> !ALWAYS_ENABLED_PAGES.contains(e.getKey()))
                            .map(Map.Entry::getValue)
                            .forEach(this::disablePossibilityToConnect);
                    } else {
                        buttons.entrySet().stream().map(Map.Entry::getValue).forEach(this::enablePossibilityToConnect);
                    }
                });
    }

    private void enablePossibilityToConnect(RadioButton button) {
        replaceInCssStyleList(button.getStyleClass());
    }

    private void disablePossibilityToConnect(RadioButton button) {
        replaceInCssStyleList(button.getStyleClass());
    }

    private void propagateConnectionStatus(RadioButton button, ButtonCssStyles... cssStyles) {
        replaceInCssStyleList(button.getStyleClass(), cssStyles);
    }

    private void replaceInCssStyleList(ObservableList<String> cssClasses, ButtonCssStyles... replacement) {
        cssClasses.removeAll(STATUS_CLASSES);
        if (replacement != null) {
            cssClasses.addAll(
                Arrays.stream(replacement).map(ButtonCssStyles::getCssClassName).collect(Collectors.toList()));
        }
    }

    private boolean isConnectionProcedure(ConnectionPage page) {
        return page != null;
    }

    private enum ButtonCssStyles {
        CONNECTED("connection-menu-button-connected"),
        CONNECTED_WARNING("connection-menu-button-connected-warning");

        private final String cssClassName;

        ButtonCssStyles(String cssClassName) {
            this.cssClassName = cssClassName;
        }

        public String getCssClassName() {
            return cssClassName;
        }
    }

}
