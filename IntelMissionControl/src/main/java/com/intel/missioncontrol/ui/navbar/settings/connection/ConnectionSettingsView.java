/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.connection;

import com.google.inject.Inject;
import com.intel.missioncontrol.drone.connection.TcpIpTransportType;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ActivityButton;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import java.util.function.Function;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.asyncfx.beans.binding.ConversionBindings;
import org.asyncfx.beans.binding.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionSettingsView extends ViewBase<ConnectionSettingsViewModel> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionSettingsView.class);

    private final GeneralSettings generalSettings;
    private static final String ICON_COMPLETE = "/com/intel/missioncontrol/icons/icon_complete(fill=theme-green).svg";
    private static final String ICON_ALERT =
        "/com/intel/missioncontrol/icons/icon_warning(fill=theme-warning-color).svg";
    private static final String ICON_LOADING = "/com/intel/missioncontrol/icons/icon_progress.svg";
    private final IDialogContextProvider dialogContextProvider;
    private Image completeIcon;
    private Image alertIcon;
    private Image loadingIcon;
    private ILanguageHelper languageHelper;

    @InjectContext
    private Context context;

    @FXML
    private VBox root;

    @FXML
    private ToggleSwitch enableConnectionListenerSwitch;

    @FXML
    private Spinner<Integer> connectionListenerPortSpinner;

    @FXML
    private VBox connectionAuto;

    @FXML
    private ImageView listenerImageStatus;

    @FXML
    private Label listenerStatus;

    @FXML
    private Label connectMessage;

    @FXML
    private TableView<ConnectionSettingsTableItem> tableView;

    @FXML
    private TableColumn<ConnectionSettingsTableItem, ConnectionSettingsTableItem> nameColumn;

    @FXML
    private TableColumn<ConnectionSettingsTableItem, ConnectionSettingsTableItem> modelColumn;

    @FXML
    private TableColumn<ConnectionSettingsTableItem, ConnectionSettingsTableItem> connectionDetailsColumn;

    @FXML
    private TableColumn<ConnectionSettingsTableItem, ConnectionSettingsTableItem> idColumn;

    @FXML
    private TableColumn<ConnectionSettingsTableItem, ConnectionSettingsTableItem> deleteColumn;

    @FXML
    private Button addConnectionButton;

    @FXML
    private Button editConnectionButton;

    @FXML
    private ActivityButton connectToDroneButton;

    @InjectViewModel
    private ConnectionSettingsViewModel viewModel;

    private RotateTransition anim = null;

    @Inject
    public ConnectionSettingsView(
            IDialogContextProvider dialogContextProvider,
            ISettingsManager settingsManager,
            ILanguageHelper languageHelper) {
        this.dialogContextProvider = dialogContextProvider;
        this.generalSettings = settingsManager.getSection(GeneralSettings.class);
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        dialogContextProvider.setContext(viewModel, context);

        enableConnectionListenerSwitch
            .selectedProperty()
            .bindBidirectional(viewModel.acceptIncomingConnectionsProperty());

        IntegerValidator portSpinnerValidator = new IntegerValidator(0, 0, 65535, 1, 5);
        connectionListenerPortSpinner.setValueFactory(portSpinnerValidator.getValueFactory());

        ConversionBindings.bindBidirectional(
            connectionListenerPortSpinner.getValueFactory().valueProperty(),
            viewModel.listeningPortProperty(),
            Converters.numberToInt());

        connectionListenerPortSpinner
            .disableProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () -> !viewModel.acceptIncomingConnectionsProperty().getValue(),
                    viewModel.acceptIncomingConnectionsProperty()));

        listenerImageStatus
            .imageProperty()
            .bind(
                Bindings.createObjectBinding(
                    () -> {
                        switch (viewModel.listenerImageTypeProperty().get()) {
                        case WARNING:
                            // TODO need a better way to turn off animation
                            if (anim != null) {
                                anim.jumpTo(Duration.ZERO);
                                anim.stop();
                            }

                            return getAlertIcon();
                        case LOADING:
                            anim = Animations.spinAlmostForever(listenerImageStatus);
                            return getLoadingIcon();
                        case COMPLETED:
                            // TODO need a better way to turn off animation
                            if (anim != null) {
                                anim.jumpTo(Duration.ZERO);
                                anim.stop();
                            }

                            return getCompleteIcon();
                        case NONE:
                        default:
                            return null;
                        }
                    },
                    viewModel.listenerImageTypeProperty()));

        listenerStatus.textProperty().bind(viewModel.listenerStatusProperty());
        connectMessage
            .visibleProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () -> viewModel.currentMissionProperty().get() == null, viewModel.currentMissionProperty()));

        configureTable(
            tableView, languageHelper, nameColumn, modelColumn, connectionDetailsColumn, idColumn, deleteColumn);

        tableView.itemsProperty().bind(viewModel.tableItemsProperty());

        tableView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener(
                (observable, oldValue, newValue) ->
                    viewModel
                        .selectedTableItemProperty()
                        .setValue(tableView.getSelectionModel().selectedItemProperty().get()));

        viewModel
            .selectedTableItemProperty()
            .addListener((observable, oldValue, newValue) -> updateTableSelection(newValue));

        updateTableSelection(viewModel.selectedTableItemProperty().getValue());

        connectToDroneButton.disableProperty().bind(viewModel.getConnectToDroneCommand().notExecutableProperty());
        connectToDroneButton.isBusyProperty().bind(viewModel.getConnectToDroneCommand().runningProperty());
        addConnectionButton.setOnAction(event -> viewModel.getAddConnectionCommand().execute());
        addConnectionButton.disableProperty().bind(viewModel.getAddConnectionCommand().notExecutableProperty());
        editConnectionButton.setOnAction(event -> viewModel.getEditConnectionCommand().execute());
        editConnectionButton.disableProperty().bind(viewModel.getEditConnectionCommand().notExecutableProperty());

        connectionAuto.visibleProperty().bind(generalSettings.operationLevelProperty().isEqualTo(OperationLevel.DEBUG));
    }

    private void updateTableSelection(ConnectionSettingsTableItem connectionItem) {
        int rowIndex = tableView.getItems().indexOf(connectionItem);
        if (rowIndex == -1) {
            tableView.getSelectionModel().clearSelection();
        } else {
            tableView.getSelectionModel().clearAndSelect(rowIndex);
        }
    }

    public void OnConnectToDrone() {
        viewModel.getConnectToDroneCommand().executeAsync();
    }

    public void OnAddConnectionButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getAddConnectionCommand().execute();
    }

    public void OnEditConnectionButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getEditConnectionCommand().execute();
    }

    private Image getCompleteIcon() {
        if (completeIcon == null) {
            completeIcon = new Image(ICON_COMPLETE);
        }

        return completeIcon;
    }

    private Image getAlertIcon() {
        if (alertIcon == null) {
            alertIcon = new Image(ICON_ALERT);
        }

        return alertIcon;
    }

    private Image getLoadingIcon() {
        if (loadingIcon == null) {
            loadingIcon = new Image(ICON_LOADING);
        }

        return loadingIcon;
    }

    @Override
    protected Parent getRootNode() {
        return root;
    }

    @Override
    public ConnectionSettingsViewModel getViewModel() {
        return viewModel;
    }

    private void configureTable(
            TableView<ConnectionSettingsTableItem> tableView,
            ILanguageHelper languageHelper,
            TableColumn<ConnectionSettingsTableItem, ConnectionSettingsTableItem> nameColumn,
            TableColumn<ConnectionSettingsTableItem, ConnectionSettingsTableItem> modelColumn,
            TableColumn<ConnectionSettingsTableItem, ConnectionSettingsTableItem> connectionDetailsColumn,
            TableColumn<ConnectionSettingsTableItem, ConnectionSettingsTableItem> systemIdColumn,
            TableColumn<ConnectionSettingsTableItem, ConnectionSettingsTableItem> deleteColumn) {

        // for each column, set the setCellValueFactory
        setCellFactoryForColumn(
            nameColumn,
            item ->
                Bindings.createStringBinding(
                    () -> {
                        if (item.isOnlineProperty().getValue()) {
                            return item.nameProperty().getValue()
                                + " "
                                + languageHelper.getString(ConnectionSettingsView.class, "online");
                        } else {
                            return item.nameProperty().getValue();
                        }
                    },
                    item.nameProperty(),
                    item.isOnlineProperty()),
            null);

        setCellFactoryForColumn(
            modelColumn,
            ConnectionSettingsTableItem::modelNameProperty,
            item ->
                Bindings.createObjectBinding(
                    () -> {
                        if (item.modelNameProperty().getValue() != null) {
                            return null;
                        }

                        ImageView res = new ImageView(getAlertIcon());
                        res.setFitWidth(16);
                        res.setFitHeight(16);
                        return res;
                    },
                    item.modelNameProperty()));

        setCellFactoryForColumn(
            connectionDetailsColumn,
            item ->
                Bindings.createStringBinding(
                    () -> {
                        String host = item.hostProperty().getValue();
                        int port = item.portProperty().getValue().intValue();
                        TcpIpTransportType transportType = item.connectionTransportTypeProperty().getValue();

                        if (host != null && port > 0 && transportType != null) {
                            return host + " : " + port + " (" + transportType + ")";
                        }

                        LOGGER.warn("Invalid connection settings for " + item.nameProperty().getValue());
                        return "";
                    },
                    item.hostProperty(),
                    item.portProperty(),
                    item.connectionTransportTypeProperty()),
            null);

        setCellFactoryForColumn(
            systemIdColumn,
            item ->
                Bindings.createStringBinding(
                    () -> Integer.toString(item.systemIdProperty().getValue().intValue()), item.systemIdProperty()),
            null);

        setCellFactoryForColumn(
            deleteColumn,
            null,
            item ->
                Bindings.createObjectBinding(
                    () -> {
                        if (item.isOnlineProperty().getValue()) {
                            return null;
                        }

                        Button trashButton = new Button();
                        trashButton.getStyleClass().addAll("icon-trash", "transparent-icon-button");
                        trashButton.setOnAction(event -> viewModel.getRemoveConnectionCommand().execute(item));

                        return trashButton;
                    },
                    item.isOnlineProperty()));

        deleteColumn.setSortable(false);
        deleteColumn.setResizable(false);
    }

    private <TRow> void setCellFactoryForColumn(
            TableColumn<TRow, TRow> column,
            Function<TRow, ObservableValue<String>> textBinding,
            Function<TRow, ObservableValue<Node>> graphicBinding) {
        column.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        column.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(TRow item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            graphicProperty().unbind();
                            setText(null);
                            setGraphic(null);
                        } else {
                            if (textBinding != null) {
                                textProperty().bind(textBinding.apply(item));

                                setWrapText(true);
                                tooltipProperty()
                                    .bind(
                                        Bindings.createObjectBinding(
                                            () -> {
                                                String text = textProperty().get();
                                                if (text != null && !text.isEmpty()) {
                                                    return new Tooltip(text);
                                                }

                                                return null;
                                            },
                                            textProperty()));
                            }

                            if (graphicBinding != null) {
                                graphicProperty().bind(graphicBinding.apply(item));
                            }
                        }
                    }
                });
        column.setSortable(false);
    }
}
