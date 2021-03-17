/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.api.support.CouldNotSendReportException;
import com.intel.missioncontrol.api.support.ErrorCategory;
import com.intel.missioncontrol.api.support.ISupportManager;
import com.intel.missioncontrol.api.support.Priority;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.navbar.connection.DataTransferFtpService;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Singleton
public class ProgressTaskFactory implements IProgressTaskFactory {

    private final ISupportManager supportManager;
    private final ILanguageHelper languageHelper;
    private final IDialogService dialogService;

    @Inject
    public ProgressTaskFactory(
            ISupportManager supportManager,
            ILanguageHelper languageHelper,
            IDialogService dialogService) {
        this.supportManager = supportManager;
        this.languageHelper = languageHelper;
        this.dialogService = dialogService;
    }

    @Override
    public ProgressTask getForSendSupport(
            ErrorCategory category,
            Priority priority,
            String problemDescription,
            Map<String, Boolean> options,
            List<File> files,
            List<String> recipients,
            List<MatchingsTableRowData> matchingsUsage,
            long size,
            String fullName,
            String country,
            String ticketIdOld) {

        // Gives us a nice name if it works and the key (earlier behavior) if looking up the name does not work
        String processName =
                languageHelper.getString(
                        "com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory.send.support.request.title");

        return new ProgressTask(processName, dialogService, size) {
            @Override
            protected Void call() throws Exception {
                try {
                    supportManager.sendFilesToServer(
                            category,
                            priority,
                            problemDescription,
                            options,
                            matchingsUsage,
                            files,
                            recipients,
                            this,
                            fullName,
                            country,
                            ticketIdOld);

                } catch (CouldNotSendReportException e) {
                    Debug.getLog().log(Level.WARNING, "could not send support request", e);
                    openCannotReachServerDialog(e.getReportFolder());
                }

                return null;
            }
        };
    }

    @Override
    public ProgressTask getForResendSupport(File reportFolder, long size) {
        String processName =
                languageHelper.getString(
                        "com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory.upload.support.request.title");

        return new ProgressTask(processName, dialogService, size) {

            @Override
            protected Void call() throws Exception {
                try {
                    supportManager.doUpload(reportFolder, this);
                } catch (CouldNotSendReportException e) {
                    Debug.getLog().log(Level.WARNING, "could not send support request", e);
                    openCannotReachServerDialog(e.getReportFolder());
                }

                return null;
            }
        };
    }

    @Override
    public ProgressTask getForTicketDownload(String ticketId, IApplicationContext applicationContext) {
        String processName =
                languageHelper.getString("com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory.download.ticket.title");
        return new ProgressTask(processName, dialogService, 0) {

            @Override
            protected Void call() throws Exception {
                supportManager.doDownload(ticketId, this, applicationContext);
                return null;
            }
        };
    }

    public ProgressTask getForPhotologDownload(IAirplane airplane) {
        String processName =
                languageHelper.getString(
                        "com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory.download.photo.log.title");
        return new ProgressTask(processName, dialogService, 0) {
            @Override
            protected Void call() throws Exception {
                DataTransferFtpService.downloadLog(
                        airplane, dialogService, this, DataTransferFtpService.PHOTO_LOG_FILE_EXTENSION);
                return null;
            }
        };
    }

    public ProgressTask getForFlightplanDownload(IAirplane airplane) {
        String processName =
                languageHelper.getString(
                        "com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory.download.flightplan.title");
        return new ProgressTask(processName, dialogService, 0) {
            @Override
            protected Void call() throws Exception {
                DataTransferFtpService.downloadLog(
                        airplane,
                        dialogService,
                        this,
                        DataTransferFtpService.PHOTO_LOG_FILE_EXTENSION,
                        DataTransferFtpService.FLIGHT_LOG_FILE_EXTENSION);
                return null;
            }
        };
    }

    public ProgressTask getForGpsRawDataDownload(IAirplane airplane) {
        String processName =
                languageHelper.getString(
                        "com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory.download.gps.raw.data.title");
        return new ProgressTask(processName, dialogService, 0) {

            @Override
            protected Void call() throws Exception {
                DataTransferFtpService.downloadGpsRawData(airplane, dialogService, this);
                return null;
            }
        };
    }

    public ProgressTask getForGpsDebuggingDownload(IAirplane airplane) {
        return new ProgressTask(
                languageHelper.getString(
                        "com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory.download.gps.debug.data.title"),
                dialogService,
                0) {

            @Override
            protected Void call() throws Exception {
                DataTransferFtpService.downloadGpsDebuggingData(airplane, dialogService, this);
                return null;
            }
        };
    }
}
