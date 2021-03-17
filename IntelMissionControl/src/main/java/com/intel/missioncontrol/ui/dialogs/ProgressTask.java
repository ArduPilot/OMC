/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.utils.IBackgroundTaskManager.BackgroundTask;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import java.io.File;

public abstract class ProgressTask extends BackgroundTask implements IMProgressMonitor {

    private final IDialogService dialogService;
    private long size;

    protected ProgressTask(String name, IDialogService dialogService, long size) {
        super(name);
        this.dialogService = dialogService;
        this.size = size / 1000;
        updateTitle(name);
    }

    public void openCannotReachServerDialog(File reportFolder) {
        dialogService.requestDialogAndWait(
            WindowHelper.getPrimaryViewModel(), SendSupportRetryViewModel.class, () -> reportFolder);
    }

    @Override
    public void setProgressNote(String note, int progress) {
        updateProgress(progress, size);
        updateMessage(note);
    }

    @Override
    public void setNote(String note) {
        updateMessage(note);
    }

    @Override
    public void setProgress(int nv) {
        updateProgress(nv, size);
    }

    @Override
    public void close() {}

    @Override
    public boolean isCanceled() {
        return isCancelled();
    }

    @Override
    public void setMaximum(int m) {
        size = m;
    }

    @Override
    public String getName() {
        return getTitle();
    }

}
