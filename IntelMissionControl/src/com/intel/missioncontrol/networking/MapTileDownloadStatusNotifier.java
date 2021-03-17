/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MapTileDownloadStatusNotifier {
    private List<MapTileDownloadStatusSubscriber> subscribers = new ArrayList<>();

    public void subscribe(MapTileDownloadStatusSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public String downloadStarted() {
        String downloadSessionId = UUID.randomUUID().toString();
        subscribers.forEach(s -> s.downloadStarted(downloadSessionId));
        return downloadSessionId;
    }

    public void downloadFinished(String downloadSessionId) {
        subscribers.forEach(s -> s.downloadFinished(downloadSessionId));
    }
}
