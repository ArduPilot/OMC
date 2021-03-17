/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.google.inject.Inject;
import com.intel.missioncontrol.concurrent.CancellationTokenSource;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.settings.GeneralSettings;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import javafx.util.Duration;

public class MapClearingCenter implements IMapClearingCenter {

    private final CancellationTokenSource cancelAutoClear = new CancellationTokenSource();
    private final GeneralSettings generalSettings;
    private final WeakListenerList<IMapClearingCenterListener> listeners =
        new WeakListenerList<>("mapClearCenterListeners");

    @Inject
    public MapClearingCenter(GeneralSettings generalSettings) {
        this.generalSettings = generalSettings;
        generalSettings.autoClearingIntervallProperty().addListener((observable, oldValue, newValue) -> timerChanged());
        timerChanged();
    }

    private void timerChanged() {
        cancelAutoClear.close();
        long interval = generalSettings.getAutoClearingIntervallInMS();
        if (interval <= 0) {
            return;
        }

        Duration duration = Duration.millis(interval);
        Dispatcher.scheduleOnUI(this::clearOldTrackCache, duration, duration, cancelAutoClear.createToken());
    }

    @Override
    public void clearUavImageCache() {
        for (IMapClearingCenterListener listener : listeners) {
            listener.clearUavImageCache();
        }
    }

    @Override
    public void clearTrackLog() {
        for (IMapClearingCenterListener listener : listeners) {
            listener.clearTrackLog();
        }
    }

    @Override
    public void clearOldTrackCache() {
        for (IMapClearingCenterListener listener : listeners) {
            listener.clearOldTrackCache();
        }
    }

    @Override
    public void addWeakListener(IMapClearingCenterListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeWeakListener(IMapClearingCenterListener listener) {
        this.listeners.remove(listener);
    }

}
