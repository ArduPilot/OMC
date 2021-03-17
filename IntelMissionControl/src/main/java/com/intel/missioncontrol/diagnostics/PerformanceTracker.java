/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

import gov.nasa.worldwind.javafx.WWGLNode;
import org.asyncfx.AsyncFX;

public class PerformanceTracker {

    private static com.sun.javafx.perf.PerformanceTracker sceneTracker;
    private static WWGLNode wwNode;

    public static void setSceneTracker(com.sun.javafx.perf.PerformanceTracker sceneTracker) {
        PerformanceTracker.sceneTracker = sceneTracker;
    }

    public static void setWWNode(WWGLNode node) {
        PerformanceTracker.wwNode = node;
    }

    public static WWGLNode getWWNode() {
        return wwNode;
    }

    public static void setBreakAfterMillis(int millis) {
        AsyncFX.setBreakAfterMillis(millis);
        AsyncFX.setBreakRequestHandler(millis >= 0 ? PerformanceTracker::breakRequest : null);
    }

    static com.sun.javafx.perf.PerformanceTracker getSceneTracker() {
        return sceneTracker;
    }

    // Since we can't break programmatically, feel free to place a breakpoint in this method!
    private static void breakRequest() {
        System.out.println("Break request.");
    }

}
