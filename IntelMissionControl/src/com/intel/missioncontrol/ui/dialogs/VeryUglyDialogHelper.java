/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.api.support.ErrorCategory;
import com.intel.missioncontrol.api.support.Priority;
import com.intel.missioncontrol.api.support.SupportManager;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import com.intel.missioncontrol.utils.IVersionProvider;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import java.io.File;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.controlsfx.dialog.ProgressDialog;

@Deprecated
public class VeryUglyDialogHelper implements IVeryUglyDialogHelper {
    public static final String PROGRESS_DIALOG_TITLE = "org.controlsfx.dialog.ProgressDialog.sendSupportTitle";
    public static final String PROGRESS_DIALOG_HEADER = "org.controlsfx.dialog.ProgressDialog.sendSupportHeader";
    public static final String DIALOG_TICKET_TITLE = "org.controlsfx.dialog.ProgressDialog.downloadTicketTitle";
    public static final String DIALOG_TICKET_HEADER = "org.controlsfx.dialog.ProgressDialog.downloadTicketHeader";

    @Inject
    private IDialogService dialogService;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private IPathProvider pathProvider;

    @Inject
    private IProgressTaskFactory progressTaskFactory;

    @Inject
    private IBackgroundTaskManager backgroundTaskManager;

    public ProgressDialog createProgressDialogForResendSupport(File reportFolder, long filesSize) {
        ProgressTask task = progressTaskFactory.getForResendSupport(reportFolder, filesSize);
        return createProgressDialogFromTask(
            task, languageHelper.getString(PROGRESS_DIALOG_TITLE), languageHelper.getString(PROGRESS_DIALOG_HEADER));
    }

    public ProgressDialog createProgressDialogForSendSupport(
            ErrorCategory category,
            Priority priority,
            String problemDescription,
            Map<String, Boolean> options,
            List<File> files,
            long filesSize,
            List<String> recipients,
            List<MatchingsTableRowData> matchingsUsage,
            String fullName,
            String country,
            String ticketIdOld) {
        ProgressTask task =
            progressTaskFactory.getForSendSupport(
                category,
                priority,
                problemDescription,
                options,
                files,
                recipients,
                matchingsUsage,
                filesSize,
                fullName,
                country,
                ticketIdOld);
        return createProgressDialogFromTask(
            task, languageHelper.getString(PROGRESS_DIALOG_TITLE), languageHelper.getString(PROGRESS_DIALOG_HEADER));
    }

    public ProgressDialog createProgressDialogForTicketDownload(
            String ticketId, IApplicationContext applicationContext) {
        File targetFolder = SupportManager.getTicketFolder(ticketId, pathProvider);
        if (targetFolder.exists() && !dialogService.requestFileOverwriteConfirmation(targetFolder.getName())) {
            return null;
        }

        ProgressTask task = progressTaskFactory.getForTicketDownload(ticketId, applicationContext);
        return createProgressDialogFromTask(
            task, languageHelper.getString(DIALOG_TICKET_TITLE), languageHelper.getString(DIALOG_TICKET_HEADER));
    }

    public ProgressDialog createProgressDialogFromTask(ProgressTask task, String title, String header) {
        final ProgressDialog dialog = new ProgressDialog(task);

        dialog.initStyle(StageStyle.DECORATED);

        dialog.setTitle(title);
        dialog.setHeaderText(header);
        setGenericStyle(dialog);
        initModalityOrPutToFront(dialog);

        // TODO: WWJFX - removed
        /*if (appWindow != null) {
            // Deactivate AppWindow during task run
            EventHandler<WorkerStateEvent> startedHandler = event -> appWindow.setActive(false);
            EventHandler<WorkerStateEvent> finishedHandler = event -> appWindow.setActive(true);
            task.setOnScheduled(startedHandler);
            task.setOnSucceeded(finishedHandler);
            task.setOnCancelled(finishedHandler);
            task.setOnFailed(finishedHandler);
        }*/

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
        dialog.getDialogPane()
            .lookupButton(ButtonType.CANCEL)
            .setOnMousePressed(
                event -> {
                    task.cancel();
                });
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add("secondary-button");
        backgroundTaskManager.submitTask(task);

        return dialog;
    }

    public static void initModalityOrPutToFront(Dialog dialog) {
        initModalityOrPutToFront(dialog, null);
    }

    public static void initModalityOrPutToFront(Dialog dialog, Window ownerWindow) {
        if (DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).getSystem().isWindows()) {
            Stage owner = WindowHelper.getPrimaryStage();
            dialog.initOwner((ownerWindow == null) ? (owner.getScene().getWindow()) : (ownerWindow));
        } else {
            Stage stage = (Stage)dialog.getDialogPane().getScene().getWindow();
            stage.initOwner(ownerWindow);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setAlwaysOnTop(true);
            stage.toFront();
        }
    }

    public static void setGenericStyle(Dialog dialog) {
        dialog.initStyle(StageStyle.UTILITY);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().clear();
        dialogPane
            .getStylesheets()
            .add(ProgressDialog.class.getResource("/com/intel/missioncontrol/styles/controls.css").toExternalForm());
        dialogPane
            .getStylesheets()
            .add(
                ProgressDialog.class
                    .getResource("/com/intel/missioncontrol/styles/themes/colors-light.css")
                    .toExternalForm());
        dialogPane
            .getStylesheets()
            .add(ProgressDialog.class.getResource("/com/intel/missioncontrol/styles/dialog.css").toExternalForm());
        dialogPane.getStyleClass().add("st-dialog");

        for (ButtonType type : dialogPane.getButtonTypes()) {
            Button btnStyles = (Button)dialogPane.lookupButton(type);
            if (type.getButtonData().isDefaultButton()) {
                btnStyles.getStyleClass().add("primary-button");
            } else {
                btnStyles.getStyleClass().add("secondary-button");
            }
        }
    }
}
