/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.tasks;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.analysis.AddFlightLogsViewModel;
import com.intel.missioncontrol.ui.sidepane.analysis.DataImportViewModel;
import com.intel.missioncontrol.ui.sidepane.analysis.FlightLogEntry;
import com.intel.missioncontrol.ui.sidepane.analysis.LogFileHelper;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import eu.mavinci.desktop.helper.FileHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyLogsTask extends IBackgroundTaskManager.BackgroundTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CopyLogsTask.class);

    private final List<File> copyTargets = new ArrayList<>();
    private final List<FlightLogEntry> selectedLogs;
    private final boolean eraseLogs;
    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final IDialogService dialogService;
    private final Mission mission;

    public CopyLogsTask(
            List<FlightLogEntry> selectedLogs,
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            boolean eraseLogs,
            AsyncBooleanProperty showHints) {
        super(languageHelper.getString(AddFlightLogsViewModel.class.getName() + ".copyLogs.title"));
        this.selectedLogs = selectedLogs;
        this.applicationContext = applicationContext;
        this.eraseLogs = eraseLogs;
        this.languageHelper = languageHelper;
        this.dialogService = dialogService;
        this.mission = applicationContext.getCurrentMission();

        addEventHandler(
            WorkerStateEvent.ANY,
            event -> {
                if (hasFinished()) {
                    if (showHints.get()) {
                        Toast toast =
                            Toast.of(ToastType.INFO)
                                .setShowIcon(true)
                                .setText(
                                    languageHelper.getString(DataImportViewModel.class.getName() + ".hintInsertPicsSD"))
                                .setCloseable(true)
                                .setAction(
                                    languageHelper.getString(
                                        "com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.hintGotIt"),
                                    true,
                                    true,
                                    () -> showHints.set(false),
                                    Platform::runLater)
                                .create();
                        applicationContext.addToast(toast);
                    }
                }
            });
    }

    public List<File> getCopyTargets() {
        return copyTargets;
    }

    @Override
    protected Void call() throws Exception {
        File ftpFolder =
            MissionConstants.getFlightLogsFolder(applicationContext.getCurrentMission().getDirectoryFile());
        int i = 0;
        for (FlightLogEntry flightLogEntry : selectedLogs) {
            updateMessage(
                String.format(
                    AddFlightLogsViewModel.class.getName() + ".copyLogs.msg",
                    i,
                    selectedLogs.size(),
                    flightLogEntry.getName()));
            File file = flightLogEntry.getPath();
            updateProgress(i, selectedLogs.size());
            try {
                File target = new File(ftpFolder, file.getName());
                target = FileHelper.getNextFreeFilename(target);
                FileHelper.copyDirectorySynchron(file, target);
                copyTargets.add(target);
                mission.addFlightLog(target);

                LogFileHelper.setLogsIdentical(file, target);
                if (eraseLogs && !target.equals(file)) {
                    FileHelper.deleteDir(languageHelper, file, true);
                }
            } catch (IOException e) {
                dialogService.showErrorMessage(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisView.errorCopyLogfile.title", file),
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisView.errorCopyLogfile.msg", file));
                LOGGER.error("Could not copy log" + file, e);
            }
        }

        return null;
    }
}
