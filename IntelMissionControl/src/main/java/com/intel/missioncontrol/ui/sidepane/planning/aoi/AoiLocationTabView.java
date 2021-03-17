/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.AreaOfInterestCorner;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.FillMode;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.controls.QuantitySpinnerTableCell;
import com.intel.missioncontrol.ui.sidepane.planning.EditWaypointsView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

public class AoiLocationTabView extends ViewBase<AoiLocationTabViewModel> {

    public static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

    @FXML
    private Pane rootPane;

    @FXML
    private Hyperlink addVertex;

    @FXML
    private Hyperlink addVertexFromUAV;

    @FXML
    private Hyperlink optimizeAoiLink;

    @FXML
    private TableColumn<AreaOfInterestCorner, Integer> vertexCountColumn;

    @FXML
    private TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> isRefPoint;

    @FXML
    private TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> latColumn;

    @FXML
    private TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> lonColumn;

    @FXML
    private TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> altAboveR;

    @FXML
    private TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> altAboveTakeoff;

    @FXML
    private TableColumn<AreaOfInterestCorner, AreaOfInterestCorner> deleteColumn;

    @FXML
    private TableView<AreaOfInterestCorner> vertexTable;

    @InjectViewModel
    private AoiLocationTabViewModel viewModel;

    private final IQuantityStyleProvider quantityStyleProvider;
    private final ILanguageHelper languageHelper;

    @Inject
    public AoiLocationTabView(ISettingsManager settingsManager, ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
        quantityStyleProvider = settingsManager.getSection(GeneralSettings.class);
        QuantityFormat quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        quantityFormat.setMaximumFractionDigits(6);
        quantityFormat.setSignificantDigits(8);
    }

    @Override
    public void initializeView() {
        super.initializeView();

        addVertex.disableProperty().bind(viewModel.getAddVertexCommand().notExecutableProperty());
        addVertex.setOnAction((a) -> viewModel.getAddVertexCommand().execute());

        addVertexFromUAV.disableProperty().bind(viewModel.getAddVertexFromUavCommand().notExecutableProperty());
        addVertexFromUAV.setOnAction((a) -> viewModel.getAddVertexFromUavCommand().execute());

        optimizeAoiLink.disableProperty().bind(viewModel.getMaximizeAoiCommand().notExecutableProperty());
        optimizeAoiLink.setOnAction((a) -> viewModel.getMaximizeAoiCommand().execute());
        optimizeAoiLink.visibleProperty().bind(viewModel.maximizeAoiVisibleProperty());

        vertexCountColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue().indexProperty().get()));

        isRefPoint.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        isRefPoint.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(AreaOfInterestCorner item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            if (item.isReferencePoint()) {
                                final Button btn = new Button();
                                btn.getStyleClass().addAll("flat-icon-button", "icon-refpoint");
                                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                                setGraphic(btn);
                                setAlignment(Pos.CENTER_LEFT);
                                btn.setTooltip(
                                    new Tooltip(
                                        languageHelper.getString(
                                            "com.intel.missioncontrol.ui.planning.settings.GeneralSettingsSectionView.referencePoint")));
                            }
                        }
                    }
                });
        latColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        latColumn.setCellFactory(
            callback ->
                new QuantitySpinnerTableCell<>(
                    quantityStyleProvider,
                    AreaOfInterestCorner::latProperty,
                    Quantity.of(-180, Unit.DEGREE),
                    Quantity.of(180, Unit.DEGREE),
                    8,
                    6,
                    FillMode.ZERO_FILL,
                    EditWaypointsView.LAT_LON_STEP,
                    true));

        lonColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        lonColumn.setCellFactory(
            callback ->
                new QuantitySpinnerTableCell<>(
                    quantityStyleProvider,
                    AreaOfInterestCorner::lonProperty,
                    Quantity.of(-180, Unit.DEGREE),
                    Quantity.of(180, Unit.DEGREE),
                    8,
                    6,
                    FillMode.ZERO_FILL,
                    EditWaypointsView.LAT_LON_STEP,
                    true));

        altAboveR.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        altAboveR.setCellFactory(
            callback ->
                new QuantitySpinnerTableCell<>(
                    quantityStyleProvider,
                    (e) -> e.altAboveRefPointProperty(),
                    Quantity.of(-300, Unit.METER),
                    Quantity.of(1000, Unit.METER),
                    6,
                    2,
                    FillMode.NO_FILL,
                    EditWaypointsView.ALT_STEP,
                    true));
        altAboveR.setEditable(false);
        altAboveTakeoff.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        altAboveTakeoff.setCellFactory(
            callback ->
                new QuantitySpinnerTableCell<>(
                    quantityStyleProvider,
                    (e) -> e.altAboveTakeoffProperty(),
                    Quantity.of(-300, Unit.METER),
                    Quantity.of(1000, Unit.METER),
                    6,
                    2,
                    FillMode.NO_FILL,
                    EditWaypointsView.ALT_STEP,
                    true));
        altAboveTakeoff.setEditable(false);
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
                            btn.getStyleClass().addAll("transparent-icon-button", "icon-trash", "destructive");
                            btn.disableProperty().bind(viewModel.getDeleteCommand().notExecutableProperty());
                            btn.setOnAction((e) -> viewModel.getDeleteCommand().execute(item));
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            setGraphic(btn);
                            setAlignment(Pos.CENTER_LEFT);
                        }
                    }
                });

        // bidirectional binding is unfortunately not offered, so we need two listeners
        vertexTable
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> viewModel.selectionProperty().set(newValue));
        viewModel
            .selectionProperty()
            .addListener((observable, oldValue, newValue) -> vertexTable.getSelectionModel().select(newValue));

        vertexTable.setRowFactory(
            tv -> {
                TableRow<AreaOfInterestCorner> row = new TableRow<>();

                row.setOnDragDetected(
                    event -> {
                        if (!row.isEmpty()) {
                            Integer index = row.getIndex();
                            Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                            db.setDragView(row.snapshot(null, null));
                            ClipboardContent cc = new ClipboardContent();
                            cc.put(SERIALIZED_MIME_TYPE, index);
                            db.setContent(cc);
                            event.consume();
                        }
                    });

                row.setOnDragOver(
                    event -> {
                        Dragboard db = event.getDragboard();
                        if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                            if (row.getIndex() != ((Integer)db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                                event.acceptTransferModes(TransferMode.MOVE);
                                event.consume();
                            }
                        }
                    });

                row.setOnDragDropped(
                    event -> {
                        Dragboard db = event.getDragboard();
                        if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                            int draggedIndex = (Integer)db.getContent(SERIALIZED_MIME_TYPE);
                            int dropIndex;

                            if (row.isEmpty()) {
                                dropIndex = vertexTable.getItems().size() - 1;
                            } else {
                                dropIndex = row.getIndex();
                            }

                            viewModel.dragVertex(draggedIndex, dropIndex);
                            event.setDropCompleted(true);
                            vertexTable.getSelectionModel().select(dropIndex);
                            event.consume();
                        }
                    });

                return row;
            });

        vertexTable.itemsProperty().bind(viewModel.cornersProperty());
        vertexTable.setEditable(true);
    }

    @Override
    protected Parent getRootNode() {
        return rootPane;
    }

    @Override
    protected AoiLocationTabViewModel getViewModel() {
        return viewModel;
    }

}
