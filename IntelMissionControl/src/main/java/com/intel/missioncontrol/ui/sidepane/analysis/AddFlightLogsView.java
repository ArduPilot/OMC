/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.DurationFormatter;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class AddFlightLogsView extends DialogView<AddFlightLogsViewModel> {

    private static final DateTimeFormatter DATETIME_FORMATTER =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());

    private static final DurationFormatter DURATION_FORMATTER = new DurationFormatter(false, true, true, true);

    private static Image activityIndicator =
        new Image(
            AddFlightLogsView.class.getResource("/com/intel/missioncontrol/icons/icon_progress.svg").toExternalForm());

    @InjectViewModel
    private AddFlightLogsViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private TableView<FlightLogEntry> tableView;

    @FXML
    private TableColumn<FlightLogEntry, FlightLogEntry> checkedColumn;

    @FXML
    private TableColumn<FlightLogEntry, FlightLogEntry> nameColumn;

    @FXML
    private TableColumn<FlightLogEntry, FlightLogEntry> dateColumn;

    @FXML
    private TableColumn<FlightLogEntry, FlightLogEntry> durationColumn;

    @FXML
    private TableColumn<FlightLogEntry, FlightLogEntry> imageCountColumn;

    @FXML
    private CheckBox selectionCheckBox;

    @FXML
    private TextField originPathTextField;

    @FXML
    private Button browseButton;

    @FXML
    private Button openFolderButton;

    @FXML
    private Button copySelectedButton;

    @FXML
    private Button cancelButton;

    @FXML
    private CheckBox eraseFilesCheckbox;

    private final ILanguageHelper languageHelper;

    @Inject
    public AddFlightLogsView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(AddFlightLogsView.class.getName() + ".title"));
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        configureTable(
            languageHelper, tableView, checkedColumn, nameColumn, dateColumn, durationColumn, imageCountColumn);

        tableView.itemsProperty().bind(viewModel.flightLogsProperty());

        originPathTextField.textProperty().bindBidirectional(viewModel.originPathProperty());

        browseButton.disableProperty().bind(viewModel.getBrowseCommand().notExecutableProperty());
        browseButton.setOnAction(event -> viewModel.getBrowseCommand().execute());

        openFolderButton.disableProperty().bind(viewModel.getOpenFolderCommand().notExecutableProperty());
        openFolderButton.setOnAction(event -> viewModel.getOpenFolderCommand().execute());

        copySelectedButton.disableProperty().bind(viewModel.getCopySelectedCommand().notExecutableProperty());
        copySelectedButton.setOnAction(event -> viewModel.getCopySelectedCommand().execute());

        cancelButton.disableProperty().bind(viewModel.getCloseCommand().notExecutableProperty());
        cancelButton.setOnAction(event -> viewModel.getCloseCommand().execute());

        eraseFilesCheckbox.selectedProperty().bindBidirectional(viewModel.eraseLogsProperty());

        selectionCheckBox.selectedProperty().bindBidirectional(viewModel.selectionCheckBoxProperty());
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    static void configureTable(
            ILanguageHelper languageHelper,
            TableView<FlightLogEntry> tableView,
            TableColumn<FlightLogEntry, FlightLogEntry> checkedColumn,
            TableColumn<FlightLogEntry, FlightLogEntry> nameColumn,
            TableColumn<FlightLogEntry, FlightLogEntry> dateColumn,
            TableColumn<FlightLogEntry, FlightLogEntry> durationColumn,
            TableColumn<FlightLogEntry, FlightLogEntry> imageCountColumn) {
        checkedColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        checkedColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(FlightLogEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            CheckBox checkBox = new CheckBox();
                            checkBox.selectedProperty().bindBidirectional(item.selectedProperty());
                            checkBox.disableProperty().bind(item.readingLogFileProperty());
                            setGraphic(checkBox);
                        }
                    }
                });

        dateColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        dateColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(FlightLogEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            graphicProperty().unbind();
                            textProperty().unbind();
                            setGraphic(null);
                            setText(null);
                            setTooltip(null);
                        } else {
                            ObjectBinding<ImageView> binding =
                                Bindings.createObjectBinding(
                                    () -> {
                                        if (!item.isReadingLogFile()) {
                                            return null;
                                        }

                                        ImageView imageView = new ImageView(activityIndicator);
                                        imageView.setFitHeight(16);
                                        imageView.setFitWidth(16);
                                        Animations.spinForever(imageView);
                                        return imageView;
                                    },
                                    item.readingLogFileProperty());

                            graphicProperty().bind(binding);

                            textProperty()
                                .bind(
                                    Bindings.createStringBinding(
                                        () ->
                                            item.getImageCount() == 0
                                                ? ""
                                                : (item.getDate() == null
                                                    ? languageHelper.getString(
                                                        AddFlightLogsView.class.getName() + ".readingLogFile")
                                                    : DATETIME_FORMATTER.format(item.getDate())),
                                        item.dateProperty(),
                                        item.readingLogFileProperty()));

                            Tooltip tooltip = new Tooltip();
                            tooltip.textProperty().bind(textProperty());
                            setTooltip(tooltip);
                        }
                    }
                });

        nameColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        nameColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(FlightLogEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                            setTooltip(null);
                        } else {
                            textProperty().bind(item.nameProperty());
                            Tooltip tooltip = new Tooltip();
                            tooltip.textProperty().bind(textProperty());
                            setTooltip(tooltip);
                        }
                    }
                });

        durationColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        durationColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(FlightLogEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                            setTooltip(null);
                        } else {
                            textProperty()
                                .bind(
                                    Bindings.createStringBinding(
                                        () ->
                                            item.getDuration() == null
                                                ? null
                                                : DURATION_FORMATTER.format(item.getDuration()),
                                        item.durationProperty()));

                            Tooltip tooltip = new Tooltip();
                            tooltip.textProperty().bind(textProperty());
                            setTooltip(tooltip);
                        }
                    }
                });

        imageCountColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        imageCountColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(FlightLogEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                            setTooltip(null);
                        } else {
                            textProperty()
                                .bind(
                                    Bindings.createStringBinding(
                                        () -> item.isReadingLogFile() ? null : Integer.toString(item.getImageCount()),
                                        item.readingLogFileProperty()));

                            Tooltip tooltip = new Tooltip();
                            tooltip.textProperty().bind(textProperty());
                            setTooltip(tooltip);
                        }
                    }
                });

        tableView.setSelectionModel(
            new TableView.TableViewSelectionModel<>(tableView) {
                @Override
                public ObservableList<TablePosition> getSelectedCells() {
                    return FXCollections.emptyObservableList();
                }

                @Override
                public boolean isSelected(int i, TableColumn<FlightLogEntry, ?> tableColumn) {
                    return false;
                }

                @Override
                public void select(int i, TableColumn<FlightLogEntry, ?> tableColumn) {}

                @Override
                public void clearAndSelect(int i, TableColumn<FlightLogEntry, ?> tableColumn) {}

                @Override
                public void clearSelection(int i, TableColumn<FlightLogEntry, ?> tableColumn) {}

                @Override
                public void selectLeftCell() {}

                @Override
                public void selectRightCell() {}

                @Override
                public void selectAboveCell() {}

                @Override
                public void selectBelowCell() {}

            });

    }

}
