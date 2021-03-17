/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.update;

import com.intel.missioncontrol.ui.MainViewModel;
import eu.mavinci.core.update.EnumUpdateTargets;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import javafx.beans.property.SimpleObjectProperty;

/** Created by bulatnikov on 7/19/17. */
public interface IUpdateManager {

    String getCurrentMajor();

    String getApplicationName();

    String getBuildNumber();

    String getBuildCommitId();

    boolean isAnyUpdateAvailable();

    boolean isUpdateAvailable(EnumUpdateTargets target);

    Map<EnumUpdateTargets, AvailableUpdate> getAvailableUpdatesMap();

    String getCurrentFullVersion();

    void runUpdate(EnumUpdateTargets target);

    void stopDownloads();

    void installUpdates();

    void showDialog();

    void showDialogNow();

    void skipVersion(EnumUpdateTargets target);

    void showRevertDialog();

    LocalDateTime getLastCheckedDateTime();

    SimpleObjectProperty<LocalDateTime> lastCheckedDateTimeProperty();

    void setMainViewModel(MainViewModel mainViewModel);

    String getCurrentGitBranch();

    boolean updateAndCloseApp() throws IOException;
}
