/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

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
import org.asyncfx.collections.RingQueue;

public class WorldWindProfiler implements RenderingListener {

    private static final List<WeakReference<WorldWindProfiler>> profilers = new ArrayList<>();

    private final ObservableList<FrameInfo> frames = new ObservableQueue<>(new RingQueue<>(200));
    private final List<FrameInfo> lastFrames = new ArrayList<>();
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

    public synchronized List<FrameInfo> getAndClearFrames() {
        var list = new ArrayList<>(lastFrames);
        lastFrames.clear();
        return list;
    }

    private WorldWindow worldWindow;

    public void bindToWWD(WorldWindow worldWindow) {
        this.worldWindow = worldWindow;
    }

    @Override
    @SuppressWarnings("StringEquality")
    public synchronized void stageChanged(RenderingEvent event) {
        // can use identity comparison here
        if (event.getStage() == RenderingEvent.BEFORE_RENDERING) {
            if (firstFrame) {
                firstFrame = false;
            } else {
                FrameInfo frameInfo = frameInfoBuilder.getFrameInfo();
                frames.add(frameInfo);
                lastFrames.add(frameInfo);
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

            for (var stats : worldWindow.getPerFrameStatistics()) {
                switch (stats.getKey()) {
                case PerformanceStatistic.FRAME_RATE:
                    frameInfoBuilder.setFrameRate((double)stats.getValue());
                    break;
                case PerformanceStatistic.DRAW_CALLS:
                    frameInfoBuilder.setDrawCalls((int)stats.getValue());
                    break;
                case PerformanceStatistic.READ_PIXELS_COUNT:
                    frameInfoBuilder.setReadPixelsCount((int)stats.getValue());
                    break;
                case PerformanceStatistic.FRAMES_RENDERED:
                    frameInfoBuilder.setRenderedFrames((int)stats.getValue());
                    break;
                }
            }
        }
    }
}
