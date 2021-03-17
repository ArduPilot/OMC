/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.logicstyle.samplr.Request;
import eu.mavinci.core.desktop.main.debug.IProfilingListener;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.util.Pair;
import org.asyncfx.concurrent.Dispatcher;

public class PerformanceMonitorViewModel extends DialogViewModel<Void, Void> {

    private static final int UPDATE_INTERVAL = 10;

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);
    private final DoubleProperty frameRate = new SimpleDoubleProperty();
    private final IntegerProperty drawCalls = new SimpleIntegerProperty();
    private final IntegerProperty eventHandlerTimeouts = new SimpleIntegerProperty();
    private final ListProperty<FrameInfo> frames = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<Pair<String, String>> frameData =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<StatisticsInfo> totalStats = new SimpleObjectProperty<>(new StatisticsInfo());
    private final ObjectProperty<StatisticsInfo> lastSegmentStats = new SimpleObjectProperty<>(new StatisticsInfo());

    private final IntegerProperty selectedBarIndex =
        new SimpleIntegerProperty(-1) {
            @Override
            protected void invalidated() {
                super.invalidated();

                frameData.clear();

                int index = get();
                if (index >= 0) {
                    var frame = frames.get(index);
                    frameData.add(new Pair<>("glDraw*", Integer.toString(frame.getDrawCalls())));
                    frameData.add(new Pair<>("glReadPixels", Integer.toString(frame.getReadPixelsCount())));
                    frameData.addAll(frame.getAdditionalData());
                }
            }
        };

    private final WorldWindProfiler profiler = WorldWindProfiler.getProfilers().stream().findFirst().orElseThrow();
    private final InvalidationListener framesChangedListener =
        observable -> Dispatcher.platform().run(this::framesChanged);
    private final IProfilingListener profilingListener;
    private final List<Pair<String, String>> additionalFrameData = new ArrayList<>();

    private int updateCounter;

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public ReadOnlyDoubleProperty frameRateProperty() {
        return frameRate;
    }

    public ReadOnlyIntegerProperty drawCallsProperty() {
        return drawCalls;
    }

    public ReadOnlyIntegerProperty eventHandlerTimeoutsProperty() {
        return eventHandlerTimeouts;
    }

    public ReadOnlyListProperty<FrameInfo> framesProperty() {
        return frames;
    }

    public ReadOnlyListProperty<Pair<String, String>> frameDataProperty() {
        return frameData;
    }

    public ReadOnlyObjectProperty<StatisticsInfo> totalStatsProperty() {
        return totalStats;
    }

    public ReadOnlyObjectProperty<StatisticsInfo> lastSegmentStatsProperty() {
        return lastSegmentStats;
    }

    public IntegerProperty selectedBarIndexProperty() {
        return selectedBarIndex;
    }

    @Inject
    public PerformanceMonitorViewModel(IProfilingManager profilingManager) {
        profilingListener =
            new IProfilingListener() {
                @Override
                public void requestStarted(Request request) {}

                @Override
                public void requestFinished(Request request) {
                    synchronized (additionalFrameData) {
                        additionalFrameData.add(
                            new Pair<>(request.getClass().getSimpleName(), "id=" + request.getId()));
                    }
                }
            };

        profilingManager.addListener(profilingListener);
        profiler.getFrames().addListener(new WeakInvalidationListener(framesChangedListener));

        // eventHandlerTimeouts.bind(PropertyHelper.totalTimeoutsProperty());
    }

    private void framesChanged() {
        if (!enabled.get()) {
            return;
        }

        var lastFrame = profiler.getFrames().get(profiler.getFrames().size() - 1);
        if (lastFrame.isSegmentStart()) {
            lastSegmentStats.get().reset();
        }

        synchronized (additionalFrameData) {
            lastFrame.getAdditionalData().addAll(additionalFrameData);
            additionalFrameData.clear();
        }

        totalStats.get().update(lastFrame);
        lastSegmentStats.get().update(lastFrame);
        frameRate.set(lastFrame.getFrameRate());
        drawCalls.set(lastFrame.getDrawCalls());

        if (updateCounter % UPDATE_INTERVAL == 0) {
            updateCounter = 1;
            frames.setAll(profiler.getFrames());
        } else {
            ++updateCounter;
        }
    }

}
