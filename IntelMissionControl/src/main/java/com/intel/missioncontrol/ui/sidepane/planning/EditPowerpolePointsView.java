/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.AreaOfInterestCorner;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.FillMode;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.controls.AutoCommitTextFieldTableCell;
import com.intel.missioncontrol.ui.controls.QuantitySpinnerTableCell;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.controls.VariantQuantitySpinnerValueFactory;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.planning.aoi.AoiLocationTabView;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;
import de.saxsys.mvvmfx.utils.notifications.WeakNotificationObserver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

public class EditPowerpolePointsView extends DialogView<EditPowerpolePointsViewModel> {

    public static final double LAT_LON_STEP = 0.00001;
    public static final double ALT_STEP = 0.00001;
    private final NotificationObserver getWindowObserver =
        (key, payload) -> {
            if (payload.length > 0 && payload[0] instanceof IDialogService.GetWindowRequest) {
                IDialogService.GetWindowRequest getWindowRequest = (IDialogService.GetWindowRequest)payload[0];
                getWindowRequest.setWindow(WindowHelper.getPrimaryStage());
            }
        };
    private final IDialogContextProvider dialogContextProvider;
    private final ILanguageHelper languageHelper;
    private final ISettingsManager settingsManager;

    @FXML
    public CheckBox selectionCheckBox;

    @FXML
    public ToggleSwitch localOrGlobalSwitch;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> targetColumn;

    @FXML
    public Button bulkEditBtn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> checkedColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> numberInFlightColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> latColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> lonColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> altitudeAboveRColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> pitchColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> yawColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> triggerColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> resolutionColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> distanceColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> frameDiagColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> noteColumn;

    @FXML
    public TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> deleteColumn;

    @FXML
    public SplitMenuButton addPointMenu;

    @FXML
    public MenuItem duplicateSelectedMenuItem;

    @FXML
    public MenuItem getFromUAVMenuItem;

    @FXML
    public Button importBtn;

    @FXML
    public Button moveUpBtn;

    @FXML
    public Button moveDownBtn;

    @FXML
    public Button editSelectedBtn;

    @FXML
    public Button closeBtn;

    @FXML
    public ToggleSwitch optimizeWayPointsSwitch;

    private boolean updatingSelection;

    @FXML
    private TableView<AreaOfInterestCorner> tableView;

    @InjectViewModel
    private EditPowerpolePointsViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    private Pane layoutRoot;

    private IDialogService dialogService;

    @Inject
    public EditPowerpolePointsView(
            ILanguageHelper languageHelper,
            ISettingsManager settingsManager,
            IDialogService dialogService,
            IDialogContextProvider dialogContextProvider) {
        this.languageHelper = languageHelper;
        this.settingsManager = settingsManager;
        this.dialogService = dialogService;
        this.dialogContextProvider = dialogContextProvider;
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(EditPowerpolePointsView.class.getName() + ".title"));
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        optimizeWayPointsSwitch.selectedProperty().bindBidirectional(viewModel.optimizeWayPointsProperty());

        bulkEditBtn.disableProperty().bind(viewModel.getOpenBulkEditingDialogCommand().executableProperty().not());
        dialogContextProvider.setContext(viewModel, context);
        viewModel.subscribe(IDialogService.GET_WINDOW_REQUEST, new WeakNotificationObserver(getWindowObserver));

        selectionCheckBox.setOnAction(
            event -> selectionCheckBox.getContextMenu().show(selectionCheckBox, Side.BOTTOM, 0, 0));

        // Todo: not implemented yet, needs live feedback from drone
        getFromUAVMenuItem.disableProperty().setValue(true);

        final int maxAngleFractionDigits = 6;
        final int significantAngleDigits = 8;
        final var minYaw = Quantity.of(0, Unit.DEGREE);
        final var maxYaw = Quantity.of(360, Unit.DEGREE);

        IQuantityStyleProvider quantityStyleProvider = settingsManager.getSection(GeneralSettings.class);
        QuantityFormat quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        quantityFormat.setMaximumFractionDigits(maxAngleFractionDigits);
        quantityFormat.setSignificantDigits(significantAngleDigits);

        var angleSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Dimension.Angle.class,
                Quantity.of(-360, Unit.DEGREE),
                Quantity.of(360, Unit.DEGREE),
                1,
                true,
                significantAngleDigits,
                maxAngleFractionDigits);

        var lengthSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Dimension.Length.class,
                Quantity.of(-Double.MAX_VALUE, Unit.METER),
                Quantity.of(Double.MAX_VALUE, Unit.METER),
                1,
                true,
                8,
                5);

        numberInFlightColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        numberInFlightColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(AreaOfInterestCorner item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                        } else {
                            textProperty().bind(item.indexProperty().asString());
                        }
                    }
                });
        numberInFlightColumn.setComparator(Comparator.comparingInt(wp -> wp.indexProperty().get()));

        checkedColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        checkedColumn.setCellFactory(
            callback ->
                new TableCell<>() {

                    CheckBox checkBox;
                    boolean filtersAdded = false;

                    @Override
                    protected void updateItem(AreaOfInterestCorner item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            checkBox = null;
                        } else {
                            checkBox = new CheckBox();
                            checkBox.selectedProperty().bindBidirectional(item.selectedProperty());
                            checkBox.mouseTransparentProperty().set(true);

                            if (!filtersAdded) {
                                addEventFilter(
                                    MouseEvent.MOUSE_PRESSED,
                                    new EventHandler<MouseEvent>() {
                                        @Override
                                        public void handle(MouseEvent event) {
                                            if (!event.isShiftDown()) {
                                                event.consume();
                                            } else {
                                                // nop
                                            }
                                        }
                                    });
                                addEventFilter(
                                    MouseEvent.MOUSE_CLICKED,
                                    new EventHandler<MouseEvent>() {
                                        @Override
                                        public void handle(MouseEvent event) {
                                            if (!event.isShiftDown()) {
                                                checkBox.selectedProperty().set(!checkBox.selectedProperty().get());
                                                event.consume();
                                            } else {
                                                // nop
                                            }

                                            updateSelection();
                                        }
                                    });
                                filtersAdded = true;
                            }

                            setGraphic(checkBox);
                        }
                    }
                });

        latColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        latColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<AreaOfInterestCorner, AreaOfInterestCorner, Dimension.Angle>(
                        quantityStyleProvider,
                        AreaOfInterestCorner::latProperty,
                        Quantity.of(-180, Unit.DEGREE),
                        Quantity.of(180, Unit.DEGREE),
                        8,
                        6,
                        FillMode.ZERO_FILL,
                        LAT_LON_STEP,
                        true);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        latColumn.setComparator(
            Comparator.comparingDouble(wp -> wp.latProperty().get().convertTo(Unit.DEGREE).getValue().doubleValue()));

        lonColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        lonColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<AreaOfInterestCorner, AreaOfInterestCorner, Dimension.Angle>(
                        quantityStyleProvider,
                        AreaOfInterestCorner::lonProperty,
                        Quantity.of(-180, Unit.DEGREE),
                        Quantity.of(180, Unit.DEGREE),
                        8,
                        6,
                        FillMode.ZERO_FILL,
                        LAT_LON_STEP,
                        true);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        lonColumn.setComparator(
            Comparator.comparingDouble(wp -> wp.lonProperty().get().convertTo(Unit.DEGREE).getValue().doubleValue()));

        altitudeAboveRColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        altitudeAboveRColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<AreaOfInterestCorner, AreaOfInterestCorner, Dimension.Length>(
                        quantityStyleProvider,
                        AreaOfInterestCorner::altAboveRefPointProperty,
                        Quantity.of(-Double.MAX_VALUE, Unit.METER),
                        Quantity.of(Double.MAX_VALUE, Unit.METER),
                        4,
                        2,
                        FillMode.NO_FILL,
                        1,
                        false);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        altitudeAboveRColumn.setComparator(
            Comparator.comparingDouble(
                wp -> wp.altAboveRefPointProperty().get().convertTo(Unit.METER).getValue().intValue()));

        pitchColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        pitchColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<AreaOfInterestCorner, AreaOfInterestCorner, Dimension.Angle>(
                        quantityStyleProvider,
                        AreaOfInterestCorner::pitchProperty,
                        viewModel.getMinPitch(),
                        viewModel.getMaxPitch(),
                        4,
                        1,
                        FillMode.NO_FILL,
                        1,
                        false);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        pitchColumn.setComparator(
            Comparator.comparingDouble(wp -> wp.getPitch().convertTo(Unit.DEGREE).getValue().doubleValue()));

        yawColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        yawColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<AreaOfInterestCorner, AreaOfInterestCorner, Dimension.Angle>(
                        quantityStyleProvider,
                        AreaOfInterestCorner::yawProperty,
                        minYaw,
                        maxYaw,
                        4,
                        1,
                        FillMode.NO_FILL,
                        1,
                        true);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        yawColumn.setComparator(
            Comparator.comparingDouble(wp -> wp.getYaw().convertTo(Unit.DEGREE).getValue().doubleValue()));

        triggerColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        triggerColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(AreaOfInterestCorner item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            var triggerSwitch = new CheckBox();
                            triggerSwitch.selectedProperty().bindBidirectional(item.triggerImageProperty());
                            triggerSwitch.disableProperty().bind(viewModel.editableProperty().not());
                            setGraphic(triggerSwitch);
                        }
                    }
                });
        triggerColumn.setComparator((o1, o2) -> Boolean.compare(o1.isTriggerImage(), o2.isTriggerImage()));

        targetColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        targetColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(AreaOfInterestCorner item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            var targetSwitch = new CheckBox();
                            targetSwitch.selectedProperty().bindBidirectional(item.targetProperty());
                            targetSwitch.disableProperty().bind(viewModel.editableProperty().not());
                            setGraphic(targetSwitch);
                        }
                    }
                });
        targetColumn.setComparator((o1, o2) -> Boolean.compare(o1.isTarget(), o2.isTarget()));

        resolutionColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        resolutionColumn.setCellFactory(
            callback ->
                new QuantitySpinnerTableCell<>(
                    quantityStyleProvider,
                    AreaOfInterestCorner::resolutionProperty,
                    Quantity.of(0, Unit.METER),
                    Quantity.of(1e6, Unit.METER),
                    4,
                    2,
                    FillMode.NO_FILL,
                    1,
                    false));
        resolutionColumn.setComparator(
            Comparator.comparingDouble(
                wp ->
                    wp.resolutionProperty().get() == null
                        ? Double.POSITIVE_INFINITY
                        : wp.resolutionProperty().get().getValue().doubleValue()));

        distanceColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        distanceColumn.setCellFactory(
            callback ->
                new QuantitySpinnerTableCell<>(
                    quantityStyleProvider,
                    AreaOfInterestCorner::distanceProperty,
                    Quantity.of(0, Unit.METER),
                    Quantity.of(1e6, Unit.METER),
                    4,
                    2,
                    FillMode.NO_FILL,
                    1,
                    false));
        distanceColumn.setComparator(
            Comparator.comparingDouble(
                wp ->
                    wp.distanceProperty().get() == null
                        ? Double.POSITIVE_INFINITY
                        : wp.distanceProperty().get().getValue().doubleValue()));

        frameDiagColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        frameDiagColumn.setCellFactory(
            callback ->
                new QuantitySpinnerTableCell<>(
                    quantityStyleProvider,
                    AreaOfInterestCorner::frameDiagProperty,
                    Quantity.of(0, Unit.METER),
                    Quantity.of(1e6, Unit.METER),
                    4,
                    2,
                    FillMode.NO_FILL,
                    1,
                    false));
        frameDiagColumn.setComparator(
            Comparator.comparingDouble(
                wp ->
                    wp.frameDiagProperty().get() == null
                        ? Double.POSITIVE_INFINITY
                        : wp.frameDiagProperty().get().getValue().doubleValue()));

        noteColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        noteColumn.setCellFactory(
            callback ->
                new AutoCommitTextFieldTableCell<AreaOfInterestCorner, AreaOfInterestCorner>(
                    AreaOfInterestCorner::noteProperty));

        deleteColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        deleteColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(AreaOfInterestCorner item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            final Button btn = new Button();
                            btn.getStyleClass().addAll("flat-icon-button", "icon-trash");
                            btn.disableProperty()
                                .bind(
                                    viewModel.getDeleteSelectedAreaOfInterestCornersCommand().notExecutableProperty());
                            btn.setOnAction(
                                e -> {
                                    viewModel.getDeleteSelectedAreaOfInterestCornersCommand().execute(item);
                                });
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            setGraphic(btn);
                            setAlignment(Pos.CENTER_LEFT);
                        }
                    }
                });

        viewModel
            .areaOfInterestCornersProperty()
            .addListener(
                (ListChangeListener<AreaOfInterestCorner>)
                    change -> {
                        if (updatingSelection) {
                            return;
                        }

                        updatingSelection = true;

                        int selectedCount = 0;
                        tableView.getSelectionModel().clearSelection();
                        for (AreaOfInterestCorner wp : viewModel.areaOfInterestCornersProperty()) {
                            if (wp.isSelected()) {
                                ++selectedCount;
                                int rowIndex = tableView.getItems().indexOf(wp);
                                tableView.getSelectionModel().select(rowIndex);
                            }
                        }

                        updateSelectionCheckBoxState(selectedCount);
                        updatingSelection = false;
                    });

        tableView
            .getSelectionModel()
            .getSelectedItems()
            .addListener(
                (ListChangeListener<AreaOfInterestCorner>)
                    change -> {
                        if (updatingSelection) {
                            return;
                        }

                        updatingSelection = true;

                        for (AreaOfInterestCorner wp : viewModel.areaOfInterestCornersProperty()) {
                            wp.setSelected(false);
                        }

                        for (AreaOfInterestCorner wp : tableView.getSelectionModel().getSelectedItems()) {
                            wp.setSelected(true);
                        }

                        updateSelectionCheckBoxState(tableView.getSelectionModel().getSelectedItems().size());
                        updatingSelection = false;
                    });

        viewModel.selectedAreaOfInterestCornerProperty().bind(tableView.getSelectionModel().selectedItemProperty());
        tableView.editableProperty().bind(viewModel.editableProperty());
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getItems().setAll(viewModel.areaOfInterestCornersProperty());

        // useSimpleDragAndDropSelection();

        tableView.itemsProperty().bind(viewModel.areaOfInterestCornersProperty());

        addPointMenu.disableProperty().bind(viewModel.getAddCornerCommand().notExecutableProperty());
        addPointMenu.setOnAction((a) -> viewModel.getAddCornerCommand().execute());

        closeBtn.disableProperty().bind(viewModel.getCloseCommand().notExecutableProperty());
    }

    private void updateSelectionCheckBoxState(int selectedCount) {
        if (selectedCount == 0) {
            selectionCheckBox.setSelected(false);
            selectionCheckBox.setIndeterminate(false);
        } else if (selectedCount < viewModel.areaOfInterestCornersProperty().size()) {
            selectionCheckBox.setSelected(true);
            selectionCheckBox.setIndeterminate(true);
        } else {
            selectionCheckBox.setSelected(true);
            selectionCheckBox.setIndeterminate(false);
        }
    }

    private void useSimpleDragAndDropSelection() {
        tableView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> viewModel.selectionProperty().set(newValue));
        viewModel
            .selectionProperty()
            .addListener((observable, oldValue, newValue) -> tableView.getSelectionModel().select(newValue));

        tableView.setRowFactory(
            tv -> {
                TableRow<AreaOfInterestCorner> row = new TableRow<>();

                row.setOnDragDetected(
                    event -> {
                        if (!row.isEmpty()) {
                            Integer index = row.getIndex();
                            Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                            db.setDragView(row.snapshot(null, null));
                            ClipboardContent cc = new ClipboardContent();
                            cc.put(AoiLocationTabView.SERIALIZED_MIME_TYPE, index);
                            db.setContent(cc);
                            event.consume();
                        }
                    });

                row.setOnDragOver(
                    event -> {
                        Dragboard db = event.getDragboard();
                        if (db.hasContent(AoiLocationTabView.SERIALIZED_MIME_TYPE)) {
                            if (row.getIndex()
                                    != ((Integer)db.getContent(AoiLocationTabView.SERIALIZED_MIME_TYPE)).intValue()) {
                                event.acceptTransferModes(TransferMode.MOVE);
                                event.consume();
                            }
                        }
                    });

                row.setOnDragDropped(
                    event -> {
                        Dragboard db = event.getDragboard();
                        if (db.hasContent(AoiLocationTabView.SERIALIZED_MIME_TYPE)) {
                            int draggedIndex = (Integer)db.getContent(AoiLocationTabView.SERIALIZED_MIME_TYPE);
                            int dropIndex;

                            if (row.isEmpty()) {
                                dropIndex = tableView.getItems().size() - 1;
                            } else {
                                dropIndex = row.getIndex();
                            }

                            viewModel.dragVertex(draggedIndex, dropIndex);
                            event.setDropCompleted(true);
                            tableView.getSelectionModel().select(dropIndex);
                            event.consume();
                        }
                    });

                return row;
            });
    }

    @FXML
    private void selectAllClicked() {
        viewModel.getSelectCommand().execute(EditPowerpolePointsViewModel.SelectionFilter.ANY);
        updateSelection();
    }

    @FXML
    private void deselectAllClicked() {
        viewModel.getDeselectCommand().execute(EditPowerpolePointsViewModel.SelectionFilter.ANY);
        updateSelection();
    }

    @FXML
    private void invertSelectionClicked() {
        viewModel.getInvertSelectionCommand().execute();
        updateSelection();
    }

    @FXML
    private void selectNoImgClicked() {
        viewModel.getSelectCommand().execute(EditPowerpolePointsViewModel.SelectionFilter.NO_IMAGE);
        updateSelection();
    }

    @FXML
    private void deselectNoImgClicked() {
        viewModel.getDeselectCommand().execute(EditPowerpolePointsViewModel.SelectionFilter.NO_IMAGE);
        updateSelection();
    }

    public void duplicateSelected(ActionEvent actionEvent) {
        viewModel.getDuplicateSelectedCommand().execute();
    }

    public void getFromUAV(ActionEvent actionEvent) {}

    public void importAction(ActionEvent actionEvent) {
        viewModel.importCSV();
    }

    public void moveUpAction(ActionEvent actionEvent) {
        var selectedItems = tableView.getSelectionModel().getSelectedItems();
        var selectedIndices = tableView.getSelectionModel().getSelectedIndices();
        var selectedIndicesCopy = FXCollections.observableArrayList(selectedIndices);

        for (var selectedIndex : selectedIndicesCopy) {
            viewModel.getMoveUpCommand().execute(viewModel.areaOfInterestCornersProperty().get(selectedIndex));
        }
    }

    public void moveDownAction(ActionEvent actionEvent) {
        var selectedIndices = tableView.getSelectionModel().getSelectedIndices();
        var selectedIndicesCopy = FXCollections.observableArrayList(selectedIndices);

        ListIterator<Integer> li = selectedIndicesCopy.listIterator(selectedIndicesCopy.size());
        while (li.hasPrevious()) {
            var selectedIndex = li.previous();
            viewModel.getMoveDownCommand().execute(viewModel.areaOfInterestCornersProperty().get(selectedIndex));
        }
    }

    public void editSelectedAction(ActionEvent actionEvent) {}

    public void closeAction(ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
    }

    public void addCornerAction(ActionEvent actionEvent) {}

    public void openBulkEditDialog(ActionEvent actionEvent) {
        viewModel.getOpenBulkEditingDialogCommand().execute(viewModel.getAreaOfInterest());
    }

    public void updateSelection() {
        tableView.getSelectionModel().clearSelection();
        var selectedIndices = new ArrayList<Integer>();
        int i = 0;
        for (AreaOfInterestCorner wp : viewModel.areaOfInterestCornersProperty()) {
            if (wp.isSelected()) {
                selectedIndices.add(i);
            }

            i++;
        }

        for (var rowIndex : selectedIndices) {
            tableView.getSelectionModel().select(rowIndex);
        }
    }
}
