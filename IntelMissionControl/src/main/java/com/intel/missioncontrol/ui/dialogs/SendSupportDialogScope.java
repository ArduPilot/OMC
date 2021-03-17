/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SendSupportDialogScope implements Scope {

    public static final String ESTIMATED_SIZE =
            "com.intel.missioncontrol.ui.dialogs.PreviewFilesDialogView.estimatedSizeLabel";

    @Inject
    private ILanguageHelper languageHelper;

    private final StringProperty estimatedSizeText = new SimpleStringProperty();
    private final IntegerProperty filesCount = new SimpleIntegerProperty();
    private final ObservableList<FileListCellViewModel> reportFiles = FXCollections.observableArrayList();

    public ObservableList<FileListCellViewModel> getFiles() {
        return reportFiles;
    }

    public void setFiles(ObservableList<FileListCellViewModel> files) {
        reportFiles.clear();
        long filesSize =
            files.stream()
                .mapToLong(
                    model -> {
                        File file = model.getFile();
                        if (file.isDirectory()) {
                            try {
                                return Files.walk(Paths.get(file.toURI()))
                                    .filter(path -> path.toFile().isFile())
                                    .mapToLong(
                                        path -> {
                                            reportFiles.add(new FileListCellViewModel(path.toFile()));
                                            return path.toFile().length();
                                        })
                                    .sum();
                            } catch (IOException e) {
                                // something happened
                            }
                        } else {
                            reportFiles.add(model);
                        }

                        return file.length();
                    })
                .sum();

        estimatedSizeText.set(
            languageHelper.getString(ESTIMATED_SIZE, filesSize / FileUtils.ONE_MB, reportFiles.size()));
    }

    public ReadOnlyIntegerProperty filesCountProperty() {
        return filesCount;
    }

    public ReadOnlyStringProperty estimatedSizeTextProperty() {
        return estimatedSizeText;
    }
}
