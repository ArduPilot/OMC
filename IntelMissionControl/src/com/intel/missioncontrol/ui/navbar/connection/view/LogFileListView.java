/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.connection.LogFileListItem;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.LogFileListViewModel;
import com.intel.missioncontrol.utils.FileUtils;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.scene.layout.Pane;

public class LogFileListView extends ViewBase<LogFileListViewModel> {

    @InjectViewModel
    private LogFileListViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private HBox boxLogFileTable;

    @FXML
    private Button loadLogFile;

    @FXML
    private Hyperlink showFilteredFiles;

    @FXML
    private TableView<LogFileListItem> logFileTable;

    @FXML
    private TableColumn<LogFileListItem, String> columnFileName;

    @FXML
    private TableColumn<LogFileListItem, LogFileSize> columnFileSize;

    @FXML
    private TableColumn<LogFileListItem, LogFileDate> columnFileDate;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private FileUtils fileUtils;

    @Override
    public void initializeView() {
        super.initializeView();

        logFileTable.setItems(viewModel.getLogFileListProperty());
        logFileTable.setOnMouseClicked(
            event -> {
                if (event.getClickCount() == 2 && !loadLogFile.isDisabled()) {
                    onLoadLogFile();
                }
            });

        columnFileName.setCellValueFactory(
            dataItem -> {
                String fileName = fileUtils.extractFilename(dataItem.getValue().getQualifiedFileName());
                return new SimpleStringProperty(fileName);
            });
        columnFileSize.setCellValueFactory(
            dataItem -> {
                long formattedSize = Math.round(((double)dataItem.getValue().getSize()) / 1024);
                LogFileSize fileSize =
                    new LogFileSize(
                        formattedSize,
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.connection.view.LogFileListView.FileSizeFormat"));
                return new SimpleObjectProperty<>(fileSize);
            });
        columnFileDate.setCellValueFactory(
            dataItem -> {
                SimpleDateFormat formatter =
                    new SimpleDateFormat(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.connection.view.LogFileListView.FileDateFormat"));
                LogFileDate date = new LogFileDate(dataItem.getValue().getDate(), formatter);
                return new SimpleObjectProperty<>(date);
            });

        columnFileName.prefWidthProperty().bind(logFileTable.widthProperty().divide(7).multiply(3));
        columnFileSize.prefWidthProperty().bind(logFileTable.widthProperty().divide(7));
        columnFileDate.prefWidthProperty().bind(logFileTable.widthProperty().divide(7).multiply(3));

        loadLogFile
            .disableProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () -> logFileTable.getSelectionModel().getSelectedItems().size() != 1,
                    logFileTable.getSelectionModel().getSelectedItems()));

        viewModel
            .getIsFilteredFilesShowedProperty()
            .addListener((observable, oldValue, newValue) -> setShowFilteredName(newValue));
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public LogFileListViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    private void onBrowse() {
        viewModel.onBrowse();
    }

    @FXML
    private void onLoadLogFile() {
        viewModel.onLoadLogFile(logFileTable.getSelectionModel().getSelectedItem().getQualifiedFileName());
    }

    @FXML
    private void onMouseClickedShowFilteredFiles() {
        viewModel.getIsFilteredFilesShowedProperty().set(!viewModel.getIsFilteredFilesShowedProperty().get());
    }

    private void setShowFilteredName(boolean isFiltered) {
        String linkText =
            languageHelper.getString(
                isFiltered
                    ? "com.intel.missioncontrol.ui.connection.view.LogFileListView.showAll"
                    : "com.intel.missioncontrol.ui.connection.view.LogFileListView.showFiltered");

        showFilteredFiles.textProperty().setValue(linkText);
    }

    private class LogFileDate implements Comparable {
        private Date date;
        private SimpleDateFormat dateFormat;

        public LogFileDate(Date date, SimpleDateFormat dateFormat) {
            this.date = date;
            this.dateFormat = dateFormat;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return dateFormat.format(date);
        }

        @Override
        public int compareTo(Object o) {
            LogFileDate that = (LogFileDate)o;
            return this.date.compareTo(that.getDate());
        }
    }

    private class LogFileSize implements Comparable {

        private Long size;
        private String units;

        public LogFileSize(Long size, String units) {
            this.size = size;
            this.units = units;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        @Override
        public int compareTo(Object o) {
            LogFileSize that = (LogFileSize)o;
            return this.size.compareTo(that.getSize());
        }

        @Override
        public String toString() {
            return String.format(units, size);
        }
    }
}
