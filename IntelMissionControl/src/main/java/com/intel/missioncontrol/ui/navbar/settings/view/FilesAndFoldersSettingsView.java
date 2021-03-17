/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.view;

import com.intel.missioncontrol.export.ExternalApplication;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.settings.viewmodel.FilesAndFoldersSettingsViewModel;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class FilesAndFoldersSettingsView extends ViewBase<FilesAndFoldersSettingsViewModel> {

    @InjectViewModel
    private FilesAndFoldersSettingsViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private TextField projectFolderTextField;

    @FXML
    private Button openProjectFolderButton;

    @FXML
    private TextField photoScanTextField;

    @FXML
    private Button openPhotoScanButton;

    @FXML
    private TextField contextCaptureTextField;

    @FXML
    private Button openContextCaptureFolderButton;

    @FXML
    private TextField pix4TextField;

    @FXML
    private Button openPix4DFolderButton;

    @Override
    public void initializeView() {
        super.initializeView();

        projectFolderTextField.textProperty().bindBidirectional(viewModel.projectFolderProperty());
        photoScanTextField.textProperty().bindBidirectional(viewModel.agiSoftPhotoScanPathProperty());
        contextCaptureTextField.textProperty().bindBidirectional(viewModel.contextCapturePathProperty());
        pix4TextField.textProperty().bindBidirectional(viewModel.pix4DesktopPathProperty());

        openProjectFolderButton.disableProperty().bind(viewModel.projectFolderProperty().isEmpty());
        openPhotoScanButton.disableProperty().bind(viewModel.agiSoftPhotoScanPathProperty().isEmpty());
        openContextCaptureFolderButton.disableProperty().bind(viewModel.contextCapturePathProperty().isEmpty());
        openPix4DFolderButton.disableProperty().bind(viewModel.pix4DesktopPathProperty().isEmpty());

        openProjectFolderButton.setOnAction(event -> viewModel.getOpenFolderCommand().execute());
        openPhotoScanButton.setOnAction(
            event -> viewModel.getOpenFolderCommand().execute(ExternalApplication.AGI_SOFT_PHOTO_SCAN));
        openContextCaptureFolderButton.setOnAction(
            event -> viewModel.getOpenFolderCommand().execute(ExternalApplication.CONTEXT_CAPTURE));
        openPix4DFolderButton.setOnAction(
            event -> viewModel.getOpenFolderCommand().execute(ExternalApplication.PIX_4_DESKTOP));
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    protected FilesAndFoldersSettingsViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void setFolderForSessionBasedFolder() {
        viewModel.getBrowseCommand().execute();
    }

    @FXML
    public void setFolderForAgiSoftPhotoScanProPath() {
        viewModel.getBrowseCommand().execute(ExternalApplication.AGI_SOFT_PHOTO_SCAN);
    }

    @FXML
    public void setFolderForContextCaptureMasterDesktopPath() {
        viewModel.getBrowseCommand().execute(ExternalApplication.CONTEXT_CAPTURE);
    }

    @FXML
    public void setFolderForPix4DDesktopPath() {
        viewModel.getBrowseCommand().execute(ExternalApplication.PIX_4_DESKTOP);
    }

}
