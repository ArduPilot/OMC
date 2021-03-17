/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.intel.missioncontrol.networking.MapTileDownloadStatusSubscriber;
import com.intel.missioncontrol.networking.MapTileDownloadStatusNotifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DownloadStatusNotifierTest {
    private MapTileDownloadStatusSubscriber subscriber;
    private MapTileDownloadStatusNotifier downloadStatusNotifier;

    private final String[] downloadStateHolder = {"placeholder_started", "placeholder_stopped"};
    private final int STARTED = 0;
    private final int STOPPED = 1;

    @BeforeAll
    public void setUp() throws Exception {
        subscriber = new MapTileDownloadStatusSubscriber() {
            public void downloadStarted(String downloadSessionId) {
                downloadStateHolder[STARTED] = downloadSessionId;
            }

            public void downloadFinished(String downloadSessionId) {
                downloadStateHolder[STOPPED] = downloadSessionId;
            }
        };
        downloadStatusNotifier = new MapTileDownloadStatusNotifier();
    }

    @Test
    public void notifierShouldNotifySubscribers() throws Exception {
        downloadStatusNotifier.subscribe(subscriber);

        String downloadSessionId = downloadStatusNotifier.downloadStarted();
        // pretend we have some downloading process here
        downloadStatusNotifier.downloadFinished(downloadSessionId);

        assertNotEquals(downloadStateHolder[STARTED], "placeholder_started");
        assertNotEquals(downloadStateHolder[STOPPED], "placeholder_stopped");
    }

    @Test
    public void downloadSessionIds_areSame_forOneDownloadSession() throws Exception {
        downloadStatusNotifier.subscribe(subscriber);

        String downloadSessionId = downloadStatusNotifier.downloadStarted();
        // pretend we have some downloading process here
        downloadStatusNotifier.downloadFinished(downloadSessionId);

        assertEquals(downloadStateHolder[STARTED], downloadStateHolder[STOPPED]);
    }

    @Test
    public void downloadSessionIds_areDifferent_forOtherDownloadSessions() throws Exception {
        downloadStatusNotifier.subscribe(subscriber);

        String downloadSessionId = downloadStatusNotifier.downloadStarted();
        downloadStatusNotifier.downloadFinished(downloadSessionId);

        String anotherDownloadSessionId = downloadStatusNotifier.downloadStarted();
        downloadStatusNotifier.downloadFinished(anotherDownloadSessionId);

        assertNotEquals(downloadSessionId, anotherDownloadSessionId);
    }
}
