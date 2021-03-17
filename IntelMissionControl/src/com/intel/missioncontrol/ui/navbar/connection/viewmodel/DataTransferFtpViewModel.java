/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IProgressTaskFactory;
import com.intel.missioncontrol.ui.dialogs.ProgressTask;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.InjectScope;
import eu.mavinci.plane.FTPManager;
import eu.mavinci.plane.IAirplane;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DataTransferFtpViewModel extends ViewModelBase {

    public static final String DEFAULT_CONNECTION_URL = "ftp://ConnectorUrl/";
    public static final String DEFAULT_UAV_URL = "ftp://UAVUrl/";

    private StringProperty connectorUrl = new SimpleStringProperty("ftp://ConnectorUrl/");
    private StringProperty uavUrl = new SimpleStringProperty("ftp://UAVUrl/");

    private final IApplicationContext applicationContext;
    private final IProgressTaskFactory progressTaskFactory;
    private final IBackgroundTaskManager backgroundTaskManager;
    private final GeneralSettings generalSettings;

    @InjectScope
    private UavConnectionScope uavConnectionScope;

    @InjectScope
    private MainScope mainScope;

    @Inject
    public DataTransferFtpViewModel(
            IApplicationContext applicationContext,
            ISettingsManager settingsManager,
            IProgressTaskFactory progressTaskFactory,
            IBackgroundTaskManager backgroundTaskManager) {
        this.applicationContext = applicationContext;
        this.generalSettings = settingsManager.getSection(GeneralSettings.class);
        this.progressTaskFactory = progressTaskFactory;
        this.backgroundTaskManager = backgroundTaskManager;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        uavConnectionScope
            .connectionStateProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == ConnectionState.CONNECTED) {
                        FTPManager ftpManager =
                            applicationContext.getCurrentMission().uavProperty().get().getLegacyPlane().getFTPManager();
                        connectorUrl.set(ftpManager.getFTPurl(true));
                        uavUrl.set(ftpManager.getFTPurl(false));
                    } else {
                        connectorUrl.set(DEFAULT_CONNECTION_URL);
                        uavUrl.set(DEFAULT_UAV_URL);
                    }
                });
    }

    public void showUrlInBrowser(String urlStr) {
        try {
            Desktop.getDesktop().browse(new URI(urlStr));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public BooleanBinding connectorUrlVisibleProperty() {
        return Bindings.createBooleanBinding(
            () ->
                generalSettings.getOperationLevel() == OperationLevel.TECHNICIAN
                    || generalSettings.getOperationLevel() == OperationLevel.DEBUG,
            generalSettings.operationLevelProperty());
    }

    public BooleanBinding uavUrlVisibleProperty() {
        return connectorUrlVisibleProperty();
    }

    public StringProperty connectorUrlProperty() {
        return connectorUrl;
    }

    public StringProperty uavUrlProperty() {
        return uavUrl;
    }

    public ProgressTask downloadPhotolog() {
        ProgressTask task = progressTaskFactory.getForPhotologDownload(getCurrentPlane());
        backgroundTaskManager.submitTask(task);
        return task;
    }

    public ProgressTask downloadFlightlog() {
        ProgressTask task = progressTaskFactory.getForFlightplanDownload(getCurrentPlane());
        backgroundTaskManager.submitTask(task);
        return task;
    }

    public ProgressTask downloadGpsRawData() {
        ProgressTask task = progressTaskFactory.getForGpsRawDataDownload(getCurrentPlane());
        backgroundTaskManager.submitTask(task);
        return task;
    }

    public ProgressTask downloadGpsDebugging() {
        ProgressTask task = progressTaskFactory.getForGpsDebuggingDownload(getCurrentPlane());
        backgroundTaskManager.submitTask(task);
        return task;
    }

    private IAirplane getCurrentPlane() {
        if (uavConnectionScope.connectionStateProperty().getValue() == ConnectionState.CONNECTED) {
            return applicationContext.getCurrentMission().getLegacyPlane();
        }

        return null;
    }
}
