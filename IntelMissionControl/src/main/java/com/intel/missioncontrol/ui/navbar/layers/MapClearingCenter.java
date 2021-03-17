/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.google.inject.Inject;
import com.intel.missioncontrol.settings.GeneralSettings;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import java.time.Duration;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;

public class MapClearingCenter implements IMapClearingCenter {

    private final GeneralSettings generalSettings;
    private final WeakListenerList<IMapClearingCenterListener> listeners =
        new WeakListenerList<>("mapClearCenterListeners");

    private CancellationSource cancelAutoClear;

    @Inject
    public MapClearingCenter(GeneralSettings generalSettings) {
        this.generalSettings = generalSettings;
        generalSettings.autoClearingIntervallProperty().addListener((observable, oldValue, newValue) -> timerChanged());
        timerChanged();
    }

    private void timerChanged() {
        if (cancelAutoClear != null) {
            cancelAutoClear.cancel();
        }

        long interval = generalSettings.getAutoClearingIntervallInMS();
        if (interval <= 0) {
            return;
        }

        Duration duration = Duration.ofMillis(interval);
        Dispatcher.platform()
            .runLaterAsync(this::clearOldTrackCache, duration, duration, cancelAutoClear = new CancellationSource());
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
