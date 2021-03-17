/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.intel.missioncontrol.ui.ViewModelBase;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.desktop.helper.FileFilter;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.asyncfx.concurrent.Future;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface IDialogService {

    String GET_WINDOW_REQUEST = "IDialogSupport.GetWindowRequest";

    class GetWindowRequest {
        private Window window;

        public void setWindow(Window window) {
            this.window = window;
        }

        public @Nullable Window getWindow() {
            return window;
        }
    }

    /**
     * Shows the specified dialog and returns immediately. The dialog will be owned by the view that is associated with
     * the specified view model. If the associated view has registered a MvvmFX context for the view model using
     * IDialogContextProvider, the dialog will be in the scope hierarchy of the view model.
     */
    <ViewModelType extends DialogViewModel> Future<ViewModelType> requestDialogAsync(
            ViewModel ownerViewModel, Class<ViewModelType> dialogViewModelClass);

    /**
     * Shows the specified dialog and returns immediately. The dialog will be owned by the view that is associated with
     * the specified view model. If the associated view has registered a MvvmFX context for the view model using
     * IDialogContextProvider, the dialog will be in the scope hierarchy of the view model.
     */
    <ViewModelType extends DialogViewModel> Future<ViewModelType> requestDialogAsync(
            ViewModel ownerViewModel, Class<ViewModelType> dialogViewModelClass, boolean modal);

    /**
     * Shows the specified dialog and returns immediately. The dialog will be owned by the view that is associated with
     * the specified view model. If the associated view has registered a MvvmFX context for the view model using
     * IDialogContextProvider, the dialog will be in the scope hierarchy of the view model.
     */
    <ViewModelType extends DialogViewModel, PayloadType> Future<ViewModelType> requestDialogAsync(
            ViewModel ownerViewModel, Class<ViewModelType> dialogViewModelClass, Supplier<PayloadType> payloadSupplier);

    /**
     * Shows the specified dialog and returns immediately. The dialog will be owned by the view that is associated with
     * the specified view model. If the associated view has registered a MvvmFX context for the view model using
     * IDialogContextProvider, the dialog will be in the scope hierarchy of the view model.
     */
    <ViewModelType extends DialogViewModel, PayloadType> Future<ViewModelType> requestDialogAsync(
            ViewModel ownerViewModel,
            Class<ViewModelType> dialogViewModelClass,
            Supplier<PayloadType> payloadSupplier,
            boolean modal);

    /**
     * Shows the specified dialog and waits until the dialog was closed. The dialog will be owned by the view that is
     * associated with the specified view model. If the associated view has registered a MvvmFX context for the view
     * model using IDialogContextProvider, the dialog will be in the scope hierarchy of the view model.
     */
    <ViewModelType extends DialogViewModel> ViewModelType requestDialogAndWait(
            ViewModel ownerViewModel, Class<ViewModelType> dialogViewModelClass);

    /**
     * Shows the specified dialog and waits until the dialog was closed. The dialog will be owned by the view that is
     * associated with the specified view model. If the associated view has registered a MvvmFX context for the view
     * model using IDialogContextProvider, the dialog will be in the scope hierarchy of the view model.
     */
    <ViewModelType extends DialogViewModel, PayloadType> ViewModelType requestDialogAndWait(
            ViewModel ownerViewModel, Class<ViewModelType> dialogViewModelClass, Supplier<PayloadType> payloadSupplier);

    <ViewModelType extends ViewModelBase> Future<ViewModelType> requestPopoverDialogAsync(
            ViewModel ownerViewModel, Class<ViewModelType> popoverDialogViewModelClass, Point2D location);

    <T> void requestProgressDialog(Task<T> task, String title, String header);

    <T> void requestProgressDialog(ViewModel ownerViewModel, Task<T> task, String title, String header);

    <T> void requestProgressDialogAndWait(Task<T> task, String title, String header);

    <T> void requestProgressDialogAndWait(ViewModel ownerViewModel, Task<T> task, String title, String header);

    Path requestFileOpenDialog(ViewModel ownerViewModel);

    Path requestFileOpenDialog(ViewModel ownerViewModel, String title);

    Path requestFileOpenDialog(ViewModel ownerViewModel, String title, Path initialFolder);

    Path requestFileOpenDialog(
            ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder);

    Path requestFileOpenDialog(
            ViewModel ownerViewModel, String title, Path initialFolder, FileFilter... extensionFilter);

    Path requestFileOpenDialog(
            ViewModel ownerViewModel,
            String title,
            Object folderPersistenceToken,
            Path initialFolder,
            FileFilter... extensionFilter);

    Path[] requestMultiFileOpenDialog(ViewModel ownerViewModel);

    Path[] requestMultiFileOpenDialog(ViewModel ownerViewModel, String title);

    Path[] requestMultiFileOpenDialog(ViewModel ownerViewModel, String title, Path initialFolder);

    Path[] requestMultiFileOpenDialog(
            ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder);

    Path[] requestMultiFileOpenDialog(
            ViewModel ownerViewModel, String title, Path initialFolder, FileFilter... extensionFilter);

    Path[] requestMultiFileOpenDialog(
            ViewModel ownerViewModel,
            String title,
            Object folderPersistenceToken,
            Path initialFolder,
            FileFilter... extensionFilter);

    Path requestFileSaveDialog(ViewModel ownerViewModel);

    Path requestFileSaveDialog(ViewModel ownerViewModel, String title);

    Path requestFileSaveDialog(ViewModel ownerViewModel, String title, Path initialFolder);

    Path requestFileSaveDialog(
            ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder);

    Path requestFileSaveDialog(
            ViewModel ownerViewModel, String title, Path initialFolder, FileFilter... extensionFilter);

    Path requestFileSaveDialog(
            ViewModel ownerViewModel,
            String title,
            Object folderPersistenceToken,
            Path initialFolder,
            FileFilter... extensionFilter);

    Path requestDirectoryChooser(ViewModel ownerViewModel);

    Path requestDirectoryChooser(ViewModel ownerViewModel, String title);

    Path requestDirectoryChooser(ViewModel ownerViewModel, String title, Path initialFolder);

    Path requestDirectoryChooser(
            ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder);

    <T> Future<T> requestInputDialogAsync(
            ViewModel ownerViewModel,
            @Nullable String title,
            @Nullable String message,
            StringConverter<T> converter,
            boolean allowEmptyValue);

    String requestInputDialogAndWait(
            ViewModel ownerViewModel, @Nullable String title, @Nullable String message, boolean allowEmptyValue);

    <T> T requestInputDialogAndWait(
            ViewModel ownerViewModel,
            @Nullable String title,
            @Nullable String message,
            @Nullable String defaultInput,
            StringConverter<T> converter,
            boolean allowEmptyValue);

    @Deprecated
    DialogResult requestCancelableConfirmation(String title, String message);

    @Deprecated
    public DialogResult requestCancelableConfirmation(String title, String message, ButtonType[] buttons);

    <T> boolean requestConfirmation(
            ViewModel ownerViewModel, @Nullable String title, @Nullable String message, StringConverter<T> converter);

    <T> boolean requestConfirmation(ViewModel ownerViewModel, @Nullable String title, @Nullable String message);

    <T> boolean requestConfirmation(@Nullable String title, @Nullable String message);

    void showInfoMessage(String title, String message);

    void showWarningMessage(String title, String message);

    void showErrorMessage(String title, String message);

    @Deprecated
    void openDirectory(String path);

    @Deprecated
    String showChoicesDialog(String header, String okButtonText, List<String> samples);

}
