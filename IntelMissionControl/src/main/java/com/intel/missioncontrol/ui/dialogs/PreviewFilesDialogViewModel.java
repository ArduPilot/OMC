/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import eu.mavinci.desktop.helper.FileHelper;
import java.io.File;
import java.io.IOException;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewFilesDialogViewModel extends DialogViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreviewFilesDialogViewModel.class);

    @InjectScope
    private SendSupportDialogScope sendSupportDialogScope;

    private final ParameterizedCommand<FileListCellViewModel> openFileCommand =
        new ParameterizedDelegateCommand<FileListCellViewModel>(
            parameter -> {
                File file = parameter.getFile();
                try {
                    FileHelper.openFile(file);
                    LOGGER.info("Opened File: " + file.getAbsolutePath());
                } catch (IOException e) {
                    LOGGER.warn("Error opening File: " + e);
                }
            });

    public ReadOnlyStringProperty estimatedSizeTextProperty() {
        return sendSupportDialogScope.estimatedSizeTextProperty();
    }

    public ReadOnlyIntegerProperty filesCountProperty() {
        return sendSupportDialogScope.filesCountProperty();
    }

    public ObservableList<FileListCellViewModel> getFiles() {
        return sendSupportDialogScope.getFiles();
    }

    public ParameterizedCommand<FileListCellViewModel> getOpenFileCommand() {
        return openFileCommand;
    }

}
