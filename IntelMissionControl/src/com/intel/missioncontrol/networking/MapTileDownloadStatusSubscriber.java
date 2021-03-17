/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

public interface MapTileDownloadStatusSubscriber {
    void downloadStarted(String downloadSessionId);
    void downloadFinished(String downloadSessionId);
}
