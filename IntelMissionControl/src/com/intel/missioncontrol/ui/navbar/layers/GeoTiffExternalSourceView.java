/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.google.inject.Inject;
import com.intel.missioncontrol.beans.binding.QuantityBindings;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.ElevationModelShiftWrapper;
import com.intel.missioncontrol.map.geotiff.GeoTiffEntry;
import com.intel.missioncontrol.map.geotiff.GeoTiffType;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.intel.missioncontrol.ui.controls.QuantitySpinnerValueFactory;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class GeoTiffExternalSourceView extends DialogView<GeoTiffExternalSourceViewModel> {

    private static final DateTimeFormatter DATETIME_FORMATTER =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());

    private static Image activityIndicator =
        new Image(
            GeoTiffExternalSourceView.class
                .getResource("/com/intel/missioncontrol/icons/icon_progress.svg")
                .toExternalForm());

    private final ILanguageHelper languageHelper;
    private final ISettingsManager settingsManager;

    @InjectViewModel
    private GeoTiffExternalSourceViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private TableView<GeoTiffEntry> tableView;

    @FXML
    private TableColumn<GeoTiffEntry, GeoTiffEntry> enabledColumn;

    @FXML
    private TableColumn<GeoTiffEntry, GeoTiffEntry> nameColumn;

    @FXML
    private TableColumn<GeoTiffEntry, GeoTiffEntry> typeColumn;

    @FXML
    private TableColumn<GeoTiffEntry, GeoTiffEntry> diskUsageColumn;

    @FXML
    private TableColumn<GeoTiffEntry, GeoTiffEntry> elevationColumn;

    @FXML
    private TableColumn<GeoTiffEntry, GeoTiffEntry> modifiedColumn;

    @FXML
    private TableColumn<GeoTiffEntry, GeoTiffEntry> deleteColumn;

    @Inject
    public GeoTiffExternalSourceView(ILanguageHelper languageHelper, ISettingsManager settingsManager) {
        this.languageHelper = languageHelper;
        this.settingsManager = settingsManager;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        configureTable(
            languageHelper,
            tableView,
            enabledColumn,
            nameColumn,
            typeColumn,
            diskUsageColumn,
            elevationColumn,
            modifiedColumn,
            deleteColumn);

        tableView.itemsProperty().bind(viewModel.geoTiffFilesProperty());

        viewModel.selectedGeoTiffProperty().bind(tableView.getSelectionModel().selectedItemProperty());
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    protected GeoTiffExternalSourceViewModel getViewModel() {
        return viewModel;
    }

    private void configureTable(
            ILanguageHelper languageHelper,
            TableView<GeoTiffEntry> tableView,
            TableColumn<GeoTiffEntry, GeoTiffEntry> enabledColumn,
            TableColumn<GeoTiffEntry, GeoTiffEntry> nameColumn,
            TableColumn<GeoTiffEntry, GeoTiffEntry> typeColumn,
            TableColumn<GeoTiffEntry, GeoTiffEntry> diskUsageColumn,
            TableColumn<GeoTiffEntry, GeoTiffEntry> elevationColumn,
            TableColumn<GeoTiffEntry, GeoTiffEntry> modifiedColumn,
            TableColumn<GeoTiffEntry, GeoTiffEntry> deleteColumn) {

        // for each column, set the cellvaluefactory
        enabledColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        enabledColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(GeoTiffEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            CheckBox checkBox = new CheckBox();
                            checkBox.selectedProperty().bindBidirectional(item.enabledProperty());
                            checkBox.selectedProperty()
                                .addListener(
                                    (observable, oldValue, newValue) -> {
                                        if (newValue) {
                                            item.zoomToItem();
                                        }
                                    });
                            setGraphic(checkBox);
                        }
                    }
                });
        enabledColumn.setComparator(
            (o1, o2) -> {
                return Boolean.compare(o1.isEnabled(), o2.isEnabled());
            });

        nameColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        nameColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(GeoTiffEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                            setTooltip(null);
                            graphicProperty().unbind();
                            setGraphic(null);
                        } else {
                            textProperty().bind(item.nameProperty());
                            Tooltip tooltip = new Tooltip();
                            tooltip.textProperty().bind(textProperty());
                            setTooltip(tooltip);
                        }
                    }
                });
        nameColumn.setComparator(Comparator.comparing(GeoTiffEntry::getName));

        typeColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        typeColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(GeoTiffEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            graphicProperty().unbind();
                            textProperty().unbind();
                            setGraphic(null);
                            setText(null);
                            setTooltip(null);
                        } else {
                            textProperty()
                                .bind(
                                    Bindings.createStringBinding(
                                        () -> languageHelper.toFriendlyName(item.typeProperty().get()),
                                        item.typeProperty(),
                                        item.importProgressProperty()));
                            Tooltip tooltip = new Tooltip();
                            tooltip.textProperty().bind(textProperty());
                            setTooltip(tooltip);
                        }
                    }
                });
        typeColumn.setComparator(Comparator.comparingInt(g -> g.getType().ordinal()));

        diskUsageColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        diskUsageColumn.setCellFactory(
            callback -> new DiskUsageCell(settingsManager.getSection(GeneralSettings.class)));
        diskUsageColumn.setComparator(Comparator.comparingInt(g -> g.diskUsageProperty().get().getValue().intValue()));

        modifiedColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        modifiedColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(GeoTiffEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                            setTooltip(null);
                        } else {
                            Tooltip tooltip = new Tooltip();
                            tooltip.textProperty().bind(textProperty());
                            setTooltip(tooltip);
                            textProperty()
                                .bind(
                                    Bindings.createStringBinding(
                                        () ->
                                            item.sourceModifyedDateProperty().get() == null
                                                ? ""
                                                : DATETIME_FORMATTER.format(item.sourceModifyedDateProperty().get()),
                                        item.sourceModifyedDateProperty()));
                        }
                    }
                });
        modifiedColumn.setComparator(
            Comparator.comparingLong(g -> g.sourceModifyedDateProperty().get().getEpochSecond()));

        elevationColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        elevationColumn.setCellFactory(
            callback ->
                new ElevationCell(languageHelper, settingsManager.getSection(GeneralSettings.class), viewModel));
        elevationColumn.setSortable(false);

        deleteColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        deleteColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(GeoTiffEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            // Todo: make this a payload button with the x/bin icon
                            final Button btn = new Button();
                            btn.getStyleClass().addAll("flat-icon-button", "icon-trash", "destructive");
                            btn.disableProperty().bind(viewModel.getUnloadCommand().notExecutableProperty());
                            btn.setOnAction((e) -> viewModel.getUnloadCommand().execute(item));
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            setGraphic(btn);
                        }
                    }
                });
        deleteColumn.setSortable(false);

        tableView.setColumnResizePolicy((p) -> true);
    }

    @FXML
    public void addGeotiffFile(ActionEvent actionEvent) {
        viewModel.addGeotiffFile();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString("com.intel.missioncontrol.ui.layers.ManageExternalSourceView.title"));
    }

    private static class ElevationCell extends TableCell<GeoTiffEntry, GeoTiffEntry> {
        private final ILanguageHelper languageHelper;
        private final IQuantityStyleProvider quantityStyleProvider;
        private final GeoTiffExternalSourceViewModel viewModel;
        private final QuantityFormat quantityFormatter;

        ElevationCell(
                ILanguageHelper languageHelper,
                IQuantityStyleProvider quantityStyleProvider,
                GeoTiffExternalSourceViewModel viewModel) {
            this.languageHelper = languageHelper;
            this.quantityStyleProvider = quantityStyleProvider;
            this.viewModel = viewModel;
            quantityFormatter = new AdaptiveQuantityFormat(quantityStyleProvider);
        }

        @Override
        protected void updateItem(GeoTiffEntry item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                graphicProperty().unbind();
                textProperty().unbind();
                setGraphic(null);
                setText(null);
            } else {
                graphicProperty().unbind();
                textProperty().unbind();
                setText(null);

                var autoSelectBestAltitudeButton = new Button();
                autoSelectBestAltitudeButton.getStyleClass().addAll("transparent-icon-button", "icon-bulb");
                autoSelectBestAltitudeButton.setAlignment(Pos.CENTER);
                autoSelectBestAltitudeButton.setTooltip(
                    new Tooltip(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.layers.ManageExternalSourceView.tabs.geotiff.table.elevation.tooltip")));

                var elevationModeComboBox = new ComboBox<ElevationModelShiftWrapper.ShiftType>();
                elevationModeComboBox.setPadding(new Insets(3));
                elevationModeComboBox.getItems().addAll(Arrays.asList(ElevationModelShiftWrapper.ShiftType.values()));

                var additionalManualAltitudeSpinner = new AutoCommitSpinner<Quantity<Dimension.Length>>();
                additionalManualAltitudeSpinner.setPadding(new Insets(3));

                var evalationDetailsEditor = new HBox();
                evalationDetailsEditor.setPadding(new Insets(3, 0, 3, 0));
                evalationDetailsEditor.setSpacing(3);
                evalationDetailsEditor.setAlignment(Pos.CENTER);
                evalationDetailsEditor.getChildren().add(elevationModeComboBox);
                evalationDetailsEditor.getChildren().add(additionalManualAltitudeSpinner);
                evalationDetailsEditor.getChildren().add(autoSelectBestAltitudeButton);
                StackPane.setAlignment(evalationDetailsEditor, Pos.CENTER_LEFT);

                // on selection show editors, otherwise just a label
                evalationDetailsEditor
                    .visibleProperty()
                    .bind(viewModel.selectedGeoTiffProperty().isEqualTo(item).and(item.isLoadingFileProperty().not()));
                evalationDetailsEditor.managedProperty().bind(evalationDetailsEditor.visibleProperty());

                var evalationDetailsLabel = new Label();
                evalationDetailsLabel
                    .visibleProperty()
                    .bind(
                        viewModel.selectedGeoTiffProperty().isNotEqualTo(item).and(item.isLoadingFileProperty().not()));
                evalationDetailsLabel.managedProperty().bind(evalationDetailsLabel.visibleProperty());
                StackPane.setAlignment(evalationDetailsLabel, Pos.CENTER_LEFT);

                var loadingProgress = new HBox();
                loadingProgress.setPadding(new Insets(3, 0, 3, 0));
                loadingProgress.setSpacing(3);
                loadingProgress.setAlignment(Pos.CENTER_LEFT);
                StackPane.setAlignment(loadingProgress, Pos.CENTER_LEFT);

                ImageView loadingProgressIcon = new ImageView(activityIndicator);
                loadingProgressIcon.setFitHeight(16);
                loadingProgressIcon.setFitWidth(16);
                Animations.spinForever(loadingProgressIcon);
                var progressLabel = new Label();
                progressLabel
                    .textProperty()
                    .bind(
                        Bindings.createStringBinding(
                            () ->
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.layers.ManageExternalSourceView.tabs.geotiff.table.import",
                                    Math.round(item.importProgressProperty().get() * 1000) / 10.),
                            item.importProgressProperty()));

                loadingProgress.getChildren().add(loadingProgressIcon);
                loadingProgress.getChildren().add(progressLabel);
                loadingProgress.visibleProperty().bind(item.isLoadingFileProperty());
                loadingProgress.managedProperty().bind(loadingProgress.visibleProperty());

                var rootPane = new StackPane();
                rootPane.getChildren().add(evalationDetailsEditor);
                rootPane.getChildren().add(evalationDetailsLabel);
                rootPane.getChildren().add(loadingProgress);

                // the entire elevation cell is only visible if this data is elevation.
                // use binding to react to type change (after loading) during runtime
                rootPane.visibleProperty()
                    .bind(item.typeProperty().isEqualTo(GeoTiffType.ELEVATION).or(item.isLoadingFileProperty()));

                // setup editor
                elevationModeComboBox.valueProperty().bindBidirectional(item.elevationModelShiftTypeProperty());
                elevationModeComboBox.setConverter(
                    new EnumConverter<>(languageHelper, ElevationModelShiftWrapper.ShiftType.class));
                autoSelectBestAltitudeButton
                    .disableProperty()
                    .bind(viewModel.getAutoDetectManualOffsetCommand().notExecutableProperty());
                autoSelectBestAltitudeButton.setOnAction(
                    event -> viewModel.getAutoDetectManualOffsetCommand().execute(item));

                QuantitySpinnerValueFactory<Dimension.Length> factory =
                    new QuantitySpinnerValueFactory<>(
                        quantityStyleProvider,
                        item.shiftProperty().getUnitInfo(),
                        2,
                        Quantity.of(ElevationModelShiftWrapper.SHIFT_MIN, Unit.METER),
                        Quantity.of(ElevationModelShiftWrapper.SHIFT_MAX, Unit.METER));

                factory.valueProperty().bindBidirectional(item.shiftProperty());
                additionalManualAltitudeSpinner.setValueFactory(factory);

                // TODO FIXME additionalManualAltitudeSpinner is not editable by keyboart, only by clicking
                // on spinners
                additionalManualAltitudeSpinner.setEditable(true);

                additionalManualAltitudeSpinner
                    .visibleProperty()
                    .bind(
                        item.elevationModelShiftTypeProperty().isEqualTo(ElevationModelShiftWrapper.ShiftType.MANUAL));
                autoSelectBestAltitudeButton.visibleProperty().bind(additionalManualAltitudeSpinner.visibleProperty());

                // setup alternative label
                evalationDetailsLabel
                    .textProperty()
                    .bind(
                        Bindings.createStringBinding(
                            () -> {
                                if (item.elevationModelShiftTypeProperty().get() != null
                                        && item.shiftProperty().get() != null) {
                                    String tmp = languageHelper.toFriendlyName(item.getElevationModelShiftType());
                                    if (item.elevationModelShiftTypeProperty().get()
                                            == ElevationModelShiftWrapper.ShiftType.MANUAL) {
                                        tmp += " ";
                                        tmp +=
                                            quantityFormatter.format(
                                                item.shiftProperty().get(), item.shiftProperty().getUnitInfo());
                                    }

                                    return tmp;
                                } else {
                                    return "";
                                }
                            },
                            item.elevationModelShiftTypeProperty(),
                            item.shiftProperty(),
                            item.isLoadingFileProperty()));

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(rootPane);
                setAlignment(Pos.CENTER_LEFT);
            }
        }

    }

    private static class DiskUsageCell extends TableCell<GeoTiffEntry, GeoTiffEntry> {
        private final QuantityFormat quantityFormat;

        DiskUsageCell(IQuantityStyleProvider quantityStyleProvider) {
            quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
            quantityFormat.setMaximumFractionDigits(1);
            quantityFormat.setSignificantDigits(4);
            setStyle("-fx-alignment: CENTER-RIGHT;");
        }

        @Override
        protected void updateItem(GeoTiffEntry item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                textProperty().unbind();
                visibleProperty().unbind();
                setText(null);
            } else {
                visibleProperty().bind(item.isLoadingFileProperty().not());
                textProperty().bind(QuantityBindings.createStringBinding(item.diskUsageProperty(), quantityFormat));
            }
        }
    }

    public void closeWindow(ActionEvent event) {
        viewModel.getCloseCommand().execute();
    }
}
