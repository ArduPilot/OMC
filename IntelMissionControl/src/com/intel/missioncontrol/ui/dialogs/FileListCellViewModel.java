/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.awt.Desktop;
import java.io.File;

/** Created by akorotenko on 8/8/17. */
public class FileListCellViewModel implements ViewModel {

    public static final String CLOSE_FILE_EVENT = "CLOSE_FILE_EVENT";

    private final StringProperty fileName = new SimpleStringProperty("Some file name");
    private final ObjectProperty<File> file = new SimpleObjectProperty<>();

    private final Command closeFileCommand =
        new DelegateCommand(
            () ->
                new Action() {
                    @Override
                    protected void action() throws Exception {
                        MvvmFX.getNotificationCenter().publish(CLOSE_FILE_EVENT, getFile());
                    }
                });

    private final Command openFileCommand =
        new DelegateCommand(
            () ->
                new Action() {
                    @Override
                    protected void action() throws Exception {
                        Desktop.getDesktop().open(file.getValue());
                    }
                });

    public FileListCellViewModel(File file) {
        this.fileName.set(file.getName());
        this.file.setValue(file);
    }

    public void initialize() {}

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public Command getCloseFileCommand() {
        return closeFileCommand;
    }

    public File getFile() {
        return file.get();
    }

    public ObjectProperty<File> fileProperty() {
        return file;
    }

    public Command getOpenFileCommand() {
        return openFileCommand;
    }
}
