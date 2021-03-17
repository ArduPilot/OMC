/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.intel.missioncontrol.concurrent.FluentFuture;
import com.intel.missioncontrol.ui.ViewModelBase;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.desktop.helper.FileFilter;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonType;
import javafx.util.StringConverter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MockDialogService implements IDialogService {
    @Override
    public <ViewModelType extends DialogViewModel> FluentFuture<ViewModelType> requestDialog(
            ViewModel ownerViewModel, Class<ViewModelType> dialogViewModelClass) {
        return null;
    }

    @Override
    public <ViewModelType extends DialogViewModel> FluentFuture<ViewModelType> requestDialog(
            ViewModel ownerViewModel, Class<ViewModelType> dialogViewModelClass, boolean modal) {
        return null;
    }

    @Override
    public <ViewModelType extends DialogViewModel, PayloadType> FluentFuture<ViewModelType> requestDialog(
            ViewModel ownerViewModel,
            Class<ViewModelType> dialogViewModelClass,
            Supplier<PayloadType> payloadSupplier) {
        return null;
    }

    @Override
    public <ViewModelType extends DialogViewModel, PayloadType> FluentFuture<ViewModelType> requestDialog(
            ViewModel ownerViewModel,
            Class<ViewModelType> dialogViewModelClass,
            Supplier<PayloadType> payloadSupplier,
            boolean modal) {
        return null;
    }

    @Override
    public <ViewModelType extends DialogViewModel> ViewModelType requestDialogAndWait(
            ViewModel ownerViewModel, Class<ViewModelType> dialogViewModelClass) {
        return null;
    }

    @Override
    public <ViewModelType extends DialogViewModel, PayloadType> ViewModelType requestDialogAndWait(
            ViewModel ownerViewModel,
            Class<ViewModelType> dialogViewModelClass,
            Supplier<PayloadType> payloadSupplier) {
        return null;
    }

    @Override
    public <ViewModelType extends ViewModelBase> FluentFuture<ViewModelType> requestPopoverDialog(
            ViewModel ownerViewModel, Class<ViewModelType> popoverDialogViewModelClass, Point2D location) {
        return null;
    }

    @Override
    public <T> void requestProgressDialog(Task<T> task, String title, String header) {}

    @Override
    public <T> void requestProgressDialog(ViewModel ownerViewModel, Task<T> task, String title, String header) {}

    @Override
    public <T> void requestProgressDialogAndWait(Task<T> task, String title, String header) {}

    @Override
    public <T> void requestProgressDialogAndWait(ViewModel ownerViewModel, Task<T> task, String title, String header) {}

    @Override
    public Path requestFileOpenDialog(ViewModel ownerViewModel) {
        return null;
    }

    @Override
    public Path requestFileOpenDialog(ViewModel ownerViewModel, String title) {
        return null;
    }

    @Override
    public Path requestFileOpenDialog(ViewModel ownerViewModel, String title, Path initialFolder) {
        return null;
    }

    @Override
    public Path requestFileOpenDialog(
            ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder) {
        return null;
    }

    @Override
    public Path requestFileOpenDialog(
            ViewModel ownerViewModel, String title, Path initialFolder, FileFilter... extensionFilter) {
        return null;
    }

    @Override
    public Path requestFileOpenDialog(
            ViewModel ownerViewModel,
            String title,
            Object folderPersistenceToken,
            Path initialFolder,
            FileFilter... extensionFilter) {
        return null;
    }

    @Override
    public Path[] requestMultiFileOpenDialog(ViewModel ownerViewModel) {
        return new Path[0];
    }

    @Override
    public Path[] requestMultiFileOpenDialog(ViewModel ownerViewModel, String title) {
        return new Path[0];
    }

    @Override
    public Path[] requestMultiFileOpenDialog(ViewModel ownerViewModel, String title, Path initialFolder) {
        return new Path[0];
    }

    @Override
    public Path[] requestMultiFileOpenDialog(
            ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder) {
        return new Path[0];
    }

    @Override
    public Path[] requestMultiFileOpenDialog(
            ViewModel ownerViewModel, String title, Path initialFolder, FileFilter... extensionFilter) {
        return new Path[0];
    }

    @Override
    public Path[] requestMultiFileOpenDialog(
            ViewModel ownerViewModel,
            String title,
            Object folderPersistenceToken,
            Path initialFolder,
            FileFilter... extensionFilter) {
        return new Path[0];
    }

    @Override
    public Path requestFileSaveDialog(ViewModel ownerViewModel) {
        return null;
    }

    @Override
    public Path requestFileSaveDialog(ViewModel ownerViewModel, String title) {
        return null;
    }

    @Override
    public Path requestFileSaveDialog(ViewModel ownerViewModel, String title, Path initialFolder) {
        return null;
    }

    @Override
    public Path requestFileSaveDialog(
            ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder) {
        return null;
    }

    @Override
    public Path requestFileSaveDialog(
            ViewModel ownerViewModel, String title, Path initialFolder, FileFilter... extensionFilter) {
        return null;
    }

    @Override
    public Path requestFileSaveDialog(
            ViewModel ownerViewModel,
            String title,
            Object folderPersistenceToken,
            Path initialFolder,
            FileFilter... extensionFilter) {
        return null;
    }

    @Override
    public Path requestDirectoryChooser(ViewModel ownerViewModel) {
        return null;
    }

    @Override
    public Path requestDirectoryChooser(ViewModel ownerViewModel, String title) {
        return null;
    }

    @Override
    public Path requestDirectoryChooser(ViewModel ownerViewModel, String title, Path initialFolder) {
        return null;
    }

    @Override
    public Path requestDirectoryChooser(
            ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder) {
        return null;
    }

    @Override
    public FluentFuture<String> requestInputDialog(
            ViewModel ownerViewModel, @Nullable String title, @Nullable String message, boolean allowEmptyValue) {
        return null;
    }

    @Override
    public <T> FluentFuture<T> requestInputDialog(
            ViewModel ownerViewModel,
            @Nullable String title,
            @Nullable String message,
            StringConverter<T> converter,
            boolean allowEmptyValue) {
        return null;
    }

    @Override
    public String requestInputDialogAndWait(
            ViewModel ownerViewModel, @Nullable String title, @Nullable String message, boolean allowEmptyValue) {
        return null;
    }

    @Override
    public <T> T requestInputDialogAndWait(
            ViewModel ownerViewModel,
            @Nullable String title,
            @Nullable String message,
            @Nullable String defaultInput,
            StringConverter<T> converter,
            boolean allowEmptyValue) {
        return null;
    }

    @Override
    public boolean requestConfirmation(String title, String message) {
        return false;
    }

    @Override
    public DialogResult requestCancelableConfirmation(String title, String message) {
        return null;
    }

    @Override
    public DialogResult requestCancelableConfirmation(String title, String message, ButtonType[] buttons) {
        return null;
    }

    @Override
    public void showInfoMessage(String title, String message) {}

    @Override
    public void showWarningMessage(String title, String message) {}

    @Override
    public void showErrorMessage(String title, String message) {}

    @Override
    public boolean requestFileOverwriteConfirmation(String file) {
        return false;
    }

    @Override
    public void openDirectory(String path) {}

    @Override
    public String showChoicesDialog(String header, String okButtonText, List<String> samples) {
        return null;
    }

}
