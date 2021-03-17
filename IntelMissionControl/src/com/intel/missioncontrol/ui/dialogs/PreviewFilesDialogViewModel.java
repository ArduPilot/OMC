/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.intel.missioncontrol.ui.commands.IParameterizedCommand;
import com.intel.missioncontrol.ui.commands.ParameterizedDelegateCommand;
import de.saxsys.mvvmfx.InjectScope;
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

    private final IParameterizedCommand<FileListCellViewModel> openFileCommand =
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

    public IParameterizedCommand<FileListCellViewModel> getOpenFileCommand() {
        return openFileCommand;
    }

}
