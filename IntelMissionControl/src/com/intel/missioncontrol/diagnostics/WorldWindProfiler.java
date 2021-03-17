/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

import com.intel.missioncontrol.collections.RingQueue;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.util.PerformanceStatistic;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;

public class WorldWindProfiler implements RenderingListener {

    private static final List<WeakReference<WorldWindProfiler>> profilers = new ArrayList<>();

    private final ObservableList<FrameInfo> frames = new ObservableQueue<>(new RingQueue<>(200));
    private FrameInfo.Builder frameInfoBuilder = new FrameInfo.Builder();
    private boolean firstFrame = true;

    public WorldWindProfiler() {
        synchronized (profilers) {
            profilers.add(new WeakReference<>(this));
        }
    }

    public static List<WorldWindProfiler> getProfilers() {
        synchronized (profilers) {
            return profilers.stream().map(Reference::get).filter(Objects::nonNull).collect(Collectors.toList());
        }
    }

    public ObservableList<FrameInfo> getFrames() {
        return frames;
    }

    WorldWindow worldWindow;

    public void bindToWWD(WorldWindow worldWindow) {
        this.worldWindow = worldWindow;
    }

    @Override
    public void stageChanged(RenderingEvent event) {
        // can use identity comparison here
        if (event.getStage() == RenderingEvent.BEFORE_RENDERING) {
            if (firstFrame) {
                firstFrame = false;
            } else {
                frames.add(frameInfoBuilder.getFrameInfo());
            }

            frameInfoBuilder.startRendering();
        } else if (event.getStage() == RenderingEvent.BEFORE_BUFFER_SWAP) {
            frameInfoBuilder.copyPixels();
        } else if (event.getStage() == RenderingEvent.AFTER_BUFFER_SWAP) {
            frameInfoBuilder.endRendering();
            WorldWindow worldWindow = this.worldWindow;
            if (worldWindow == null) {
                return;
            }

            frameInfoBuilder.setFrameRate((double)worldWindow.getValue(PerformanceStatistic.FRAME_RATE));
            frameInfoBuilder.setDrawCalls((int)worldWindow.getValue(PerformanceStatistic.DRAW_CALLS));
            frameInfoBuilder.setReadPixelsCount((int)worldWindow.getValue(PerformanceStatistic.READ_PIXELS_COUNT));
        }
    }
}
