/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.plane.dronekit.model.DroneKitConnector;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.navbar.connection.model.ConnectionItem;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.UavConnectViewModel;
import com.intel.missioncontrol.utils.IntegerValidator;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.plane.IAirplane;
import java.nio.file.Files;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class UavConnectView extends ViewBase<UavConnectViewModel> {

    private static final String ICON_REMOVE = "/com/intel/missioncontrol/icons/icon_remove.svg";
    private static final String ICON_DOT = "/com/intel/missioncontrol/icons/icon_dot(fill=theme-green).svg";

    private static final int ICON_SIZE = (int)ScaleHelper.emsToPixels(1.3);

    @InjectContext
    private Context context;

    @FXML
    private VBox root;

    //    @FXML
    //    private TitledPane uavConnectorInfo;
    //
    //    @FXML
    //    private Node shortUavInfo;

    @FXML
    private Text connectStatusText;

    @FXML
    private ToggleSwitch autoUpdateSwitch;

    @FXML
    private Button autoConnectButton;

    @FXML
    private Spinner<Integer> portSpinner;

    @FXML
    private TableView<ConnectionItem> tableView;

    @FXML
    private TableColumn<ConnectionItem, ConnectionItem> selectColumn;

    @FXML
    private TableColumn<ConnectionItem, ConnectionItem> nameColumn;

    @FXML
    private TableColumn<ConnectionItem, ConnectionItem> typeColumn;

    @FXML
    private TableColumn<ConnectionItem, ConnectionItem> transportColumn;

    @FXML
    private TableColumn<ConnectionItem, ConnectionItem> hostportColumn;

    @FXML
    private TableColumn<ConnectionItem, ConnectionItem> trashColumn;

    @FXML
    private Button addConnectionButton;

    @FXML
    private Button connectButton;

    @FXML
    private VBox autoConnectRoot;

    @FXML
    private MavLinkConnectionWidget autoConnect;

    @InjectViewModel
    private UavConnectViewModel viewModel;

    private Property<Integer> portIntegerProperty;
    private final ILanguageHelper languageHelper;
    private IDialogContextProvider dialogContextProvider;
    private Image connectedIcon;
    private Image trashIcon;

    @Inject
    public UavConnectView(ILanguageHelper languageHelper, IDialogContextProvider dialogContextProvider) {
        this.languageHelper = languageHelper;
        this.dialogContextProvider = dialogContextProvider;
    }

    AutoConnectModel connectModel;

    // this should probably go somewhere else, but leaving it hear for now because it's easy
    private class AutoConnectModel {
        private MavLinkConnectWidgetController connectController = null;
        private DroneKitConnector connector = null;

        private void registerMissionWithController(Mission newMission)  {
            IAirplane airplane = newMission != null ? newMission.getLegacyPlane() : null;
            System.out.println(">>>> setting up connection for mission " + newMission + " plane=" +airplane);

            // shutdown existing connection;
            try {
                if (connector != null) {
                    connector.getConnectionManager().disconnect();
                    connector = null;
                }
                // do this after so that view is reset;
                connectController.teardown();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (airplane == null) {
                // nothing we can do mission must be closed
                return;
            }

            // create new Connection!
            try {
                connector = new DroneKitConnector(airplane, () -> Files.createTempDirectory("ZZZ").toFile());
                connectController.setup(connector);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // call me with valid view model please
        AutoConnectModel() {
            connectController = new MavLinkConnectWidgetController(autoConnect);

            viewModel.currentMissionProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("UavConnectView.currentMissionProperty changed:" + oldValue +  "->"+ newValue);
                Platform.runLater(() -> {
                    registerMissionWithController(newValue);
                });
            });
        }

    }


    @Override
    protected void initializeView() {
        super.initializeView();
        dialogContextProvider.setContext(viewModel, context);

        if (connectModel != null) {
            System.err.println("UavConnectView.initializeView being called again, connectModel probably can't handle this ");
        }

        connectModel = new AutoConnectModel();

        autoConnect.onUavConnectViewInit(this, viewModel);

        connectStatusText
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> {
                        switch (viewModel.connectionStatusObjectProperty().get()) {
                        case LISTENING:
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.listening");
                        case CONNECTED:
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.connectedTo",
                                viewModel.selectedConnectionItemProperty().get().getName(),
                                viewModel.selectedConnectionItemProperty().get().getConnectionDeviceType().toString());
                        case CONNECTING:
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.connecting");
                        case DISCONNECTED:
                        default:
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.notConnected");
                        }
                    },
                    viewModel.connectionStatusObjectProperty()));

        //autoUpdateSwitch.selectedProperty().bindBidirectional(viewModel.acceptIncommingConnectionProperty());

        IntegerValidator portSpinnerValueFactory = new IntegerValidator(0, 0, 99999, 1, 5);
        portSpinner.setValueFactory(portSpinnerValueFactory.getValueFactory());
        portIntegerProperty = viewModel.receivingPortProperty().asObject();
        portSpinner.getValueFactory().valueProperty().bindBidirectional(portIntegerProperty);

        configureTable(
            languageHelper,
            tableView,
            selectColumn,
            nameColumn,
            typeColumn,
            transportColumn,
            hostportColumn,
            trashColumn);
        tableView.itemsProperty().bindBidirectional(viewModel.connectionItemListProperty());
        viewModel.selectedConnectionItemProperty().bind(tableView.getSelectionModel().selectedItemProperty());
        connectButton.setOnAction(
            event -> {
                if (connectButton
                        .getText()
                        .equals(
                            languageHelper.getString(
                                "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.connect"))) {
                    viewModel.getConnectCommand(true).execute();
                } else {
                    viewModel.getDisconnectCommand().execute();
                }
            });
        connectButton.disableProperty().bind(viewModel.connectButtonDisableCheck().or(viewModel.acceptIncommingConnectionProperty()));
        addConnectionButton.disableProperty().bind(viewModel.acceptIncommingConnectionProperty());
        tableView.disableProperty().bind(viewModel.acceptIncommingConnectionProperty());
        portSpinner.disableProperty().bind(viewModel.acceptIncommingConnectionProperty().not());

        connectButton
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> {
                        if (viewModel.isSelectedItemConnected().get()) {
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.disconnect");
                        } else {
                            return languageHelper.getString(
                                "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.connect");
                        }
                    },
                    viewModel.isSelectedItemConnected()));
    }

    //    @Override
    //    public void initialize(URL location, ResourceBundle resources) {
    //        dialogContextProvider.setContext(viewModel, context);
    ////        uavConnectorInfo.visibleProperty().bind(viewModel.usbConnectorInfoVisibleProperty());
    ////        uavConnectorInfo.managedProperty().bind(viewModel.usbConnectorInfoManagedProperty());
    ////        uavConnectorInfo.expandedProperty().bindBidirectional(viewModel.usbConnectorInfoExpandedProperty());
    ////
    ////        shortUavInfo.visibleProperty().bind(viewModel.shortUavInfoVisibleProperty());
    ////        shortUavInfo.managedProperty().bind(viewModel.shortUavInfoManagedProperty());
    //

    //    }

    public void OnAddConnectionButtonClicked(ActionEvent actionEvent) {
        viewModel.getAddConnectionCommand().execute();
    }

    @Override
    protected Parent getRootNode() {
        return root;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    private void configureTable(
            ILanguageHelper languageHelper,
            TableView<ConnectionItem> tableView,
            TableColumn<ConnectionItem, ConnectionItem> selectColumn,
            TableColumn<ConnectionItem, ConnectionItem> nameColumn,
            TableColumn<ConnectionItem, ConnectionItem> typeColumn,
            TableColumn<ConnectionItem, ConnectionItem> transportColumn,
            TableColumn<ConnectionItem, ConnectionItem> hostportColumn,
            TableColumn<ConnectionItem, ConnectionItem> trashColumn) {

        // for each column, set the cellvaluefactory
        selectColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        selectColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(ConnectionItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                            setTooltip(null);
                            graphicProperty().unbind();
                            setGraphic(null);
                        } else {
                            graphicProperty()
                                .bind(
                                    Bindings.createObjectBinding(
                                        () -> {
                                            if (item.selectedProperty().get()) {
                                                return new ImageView(getConnectedIcon());
                                            } else {
                                                return null;
                                            }
                                        },
                                        item.selectedProperty()));
                        }
                    }
                });

        nameColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        nameColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(ConnectionItem item, boolean empty) {
                        super.updateItem(item, empty);
                        setPrefWidth(90);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                            setWrapText(true);
                            setTooltip(null);
                            graphicProperty().unbind();
                            setGraphic(null);
                        } else {
                            textProperty().bind(item.nameProperty());
                            Tooltip tooltip = new Tooltip();
                            setWrapText(true);
                            tooltip.textProperty().bind(textProperty());
                            setTooltip(tooltip);
                        }
                    }
                });

        typeColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        typeColumn.setCellFactory(
            callback -> {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(ConnectionItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                            setTooltip(null);
                            graphicProperty().unbind();
                            setGraphic(null);
                        } else {
                            textProperty()
                                .bind(
                                    Bindings.createStringBinding(
                                        () -> {
                                            switch (item.connectionDeviceTypeProperty().get()) {
                                            case COPTER:
                                                return languageHelper.getString(
                                                    "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.uavType.copter");
                                            case PLANE:
                                                return languageHelper.getString(
                                                    "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.uavType.plane");
                                            case SIMULATOR:
                                            default:
                                                return languageHelper.getString(
                                                    "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.uavType.simulation");
                                            }
                                        },
                                        item.connectionDeviceTypeProperty()));
                            Tooltip tooltip = new Tooltip();
                            tooltip.textProperty().bind(textProperty());
                            setTooltip(tooltip);
                        }
                    }
                };
            });

        transportColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        transportColumn.setCellFactory(
            callback -> {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(ConnectionItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                            setTooltip(null);
                            graphicProperty().unbind();
                            setGraphic(null);
                        } else {
                            textProperty()
                                .bind(
                                    Bindings.createStringBinding(
                                        () -> {
                                            switch (item.connectionTransportTypeProperty().get()) {
                                            case UDP:
                                                return languageHelper.getString(
                                                    "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.transport.udp");
                                            case TCP:
                                            default:
                                                return languageHelper.getString(
                                                    "com.intel.missioncontrol.ui.navbar.connection.view.uavConnectView.transport.tcp");
                                            }
                                        },
                                        item.connectionTransportTypeProperty()));
                            Tooltip tooltip = new Tooltip();
                            tooltip.textProperty().bind(textProperty());
                            setTooltip(tooltip);
                        }
                    }
                };
            });

        hostportColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        hostportColumn.setCellFactory(
            callback -> {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(ConnectionItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                            setTooltip(null);
                            graphicProperty().unbind();
                            setGraphic(null);
                        } else {
                            textProperty()
                                .bind(
                                    Bindings.createStringBinding(
                                        () -> {
                                            return item.hostProperty().get() + " : " + item.portProperty().get();
                                        },
                                        item.hostProperty(),
                                        item.portProperty()));
                            Tooltip tooltip = new Tooltip();
                            tooltip.textProperty().bind(textProperty());
                            setTooltip(tooltip);
                        }
                    }
                };
            });
        hostportColumn.setSortable(false);

        trashColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        trashColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(ConnectionItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            Button trashButton = new Button();
                            trashButton.setGraphic(new ImageView(getTrashIcon()));
                            setGraphic(trashButton);
                            disableProperty().bind(item.selectedProperty());
                            trashButton.setOnAction(
                                event -> {
                                    // TODO: delete item from list
                                    viewModel.removeConnectionItemFromList(item);
                                });
                        }
                    }
                });
        trashColumn.setSortable(false);

        tableView.setColumnResizePolicy((p) -> true);
    }

    private Image getConnectedIcon() {
        if (connectedIcon == null) {
            connectedIcon = new Image(ICON_DOT);
        }

        return connectedIcon;
    }

    private Image getTrashIcon() {
        if (trashIcon == null) {
            trashIcon = new Image(ICON_REMOVE,
                    ICON_SIZE,
                    ICON_SIZE,
                    true,
                    false);
        }

        return trashIcon;
    }
}
