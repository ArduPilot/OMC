/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces;

import com.intel.missioncontrol.networking.MapTileDownloadStatusSubscriber;
import com.intel.missioncontrol.networking.MapTileDownloadStatusNotifier;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DownloadStatusNotifierTest {
    private MapTileDownloadStatusSubscriber subscriber;
    private MapTileDownloadStatusNotifier downloadStatusNotifier;

    private final String[] downloadStateHolder = {"placeholder_started", "placeholder_stopped"};
    private final int STARTED = 0;
    private final int STOPPED = 1;

    @Before
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

        assertThat(downloadStateHolder[STARTED], isChangedFor("placeholder_started"));
        assertThat(downloadStateHolder[STOPPED], isChangedFor("placeholder_stopped"));
    }

    private Matcher<String> isChangedFor(String placeholder) {
        return is(not(placeholder));
    }

    @Test
    public void downloadSessionIds_areSame_forOneDownloadSession() throws Exception {
        downloadStatusNotifier.subscribe(subscriber);

        String downloadSessionId = downloadStatusNotifier.downloadStarted();
        // pretend we have some downloading process here
        downloadStatusNotifier.downloadFinished(downloadSessionId);

        assertThat(downloadStateHolder[STARTED], is(downloadStateHolder[STOPPED]));
    }

    @Test
    public void downloadSessionIds_areDifferent_forOtherDownloadSessions() throws Exception {
        downloadStatusNotifier.subscribe(subscriber);

        String downloadSessionId = downloadStatusNotifier.downloadStarted();
        downloadStatusNotifier.downloadFinished(downloadSessionId);

        String anotherDownloadSessionId = downloadStatusNotifier.downloadStarted();
        downloadStatusNotifier.downloadFinished(anotherDownloadSessionId);

        assertThat(downloadSessionId, is(not(anotherDownloadSessionId)));
    }
}
