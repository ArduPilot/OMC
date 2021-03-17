/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.update;

public interface UpdateItemListener {

    void startDownload();

    void updateProgress(long currentFileSize, long contentLength);

    void onDownloadComplete();
}
