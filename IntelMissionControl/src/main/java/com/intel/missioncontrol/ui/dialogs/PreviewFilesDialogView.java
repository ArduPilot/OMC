/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/** Created by akorotenko on 8/8/17. */
public class PreviewFilesDialogView extends DialogView<PreviewFilesDialogViewModel> {

    private static final String PREVIEW_DIALOG_VIEW_TITLE =
        "com.intel.missioncontrol.ui.dialogs.PreviewFilesDialogView.title";

    @InjectViewModel
    private PreviewFilesDialogViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private TableView<FileListCellViewModel> filesTable;

    @FXML
    private Label estimatedSizeLabel;

    @FXML
    private TableColumn<FileListCellViewModel, String> columnFileName;

    @FXML
    private TableColumn<FileListCellViewModel, String> columnFileFolder;

    @FXML
    private TableColumn<FileListCellViewModel, String> columnFileSize;

    @Inject
    private ILanguageHelper languageHelper;

    @Override
    public void initializeView() {
        super.initializeView();
        estimatedSizeLabel.textProperty().bind(viewModel.estimatedSizeTextProperty());
        filesTable.setItems(viewModel.getFiles());
        columnFileName.setCellValueFactory(dataItem -> new SimpleStringProperty(dataItem.getValue().getFileName()));

        columnFileFolder.setCellValueFactory(
            dataItem -> {
                String folder = dataItem.getValue().getFile().getParent();
                return new SimpleStringProperty(folder);
            });

        columnFileFolder.setCellFactory(
            (TableColumn<FileListCellViewModel, String> param) -> new RightEllipsedTableCell());

        columnFileSize.setCellValueFactory(
            dataItem -> {
                long size = dataItem.getValue().getFile().length();
                return new SimpleStringProperty(String.format("%d KB", (size / 1024)));
            });

        columnFileName.setMaxWidth(1f * Integer.MAX_VALUE * 30); // 30% width
        columnFileFolder.setMaxWidth(1f * Integer.MAX_VALUE * 60); // 50% width
        columnFileSize.setMaxWidth(1f * Integer.MAX_VALUE * 10); // 20% width
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected PreviewFilesDialogViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(PREVIEW_DIALOG_VIEW_TITLE));
    }

    public void okButtonClicked() {
        viewModel.getCloseCommand().execute();
    }

    public void onTableClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() > 1) {
            if (!(mouseEvent.getTarget() instanceof TableColumnHeader)) {
                FileListCellViewModel selectedItem = filesTable.getSelectionModel().getSelectedItem();
                viewModel.getOpenFileCommand().execute(selectedItem);
            }
        }
    }

    static class RightEllipsedTableCell extends TableCell<FileListCellViewModel, String> {
        RightEllipsedTableCell() {
            this(null);
        }

        RightEllipsedTableCell(String ellipsisString) {
            super();
            setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
            if (ellipsisString != null) {
                setEllipsisString(ellipsisString);
            }
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? "" : item);
        }
    }

}
