/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;

public class VisibilityTracker implements RenderingListener {

    private final AsyncBooleanProperty coverageLegendVisible = new SimpleAsyncBooleanProperty(this);
    private final Object renderToken = new Object();

    private boolean coverageLayerWasRenderedThisFrame;

    public void flagCoverageLayerWasRendered() {
        coverageLayerWasRenderedThisFrame = true;
    }

    @Override
    public void stageChanged(RenderingEvent event) {
        synchronized (renderToken) {
            // identity comparison
            if (event.getStage() == RenderingEvent.AFTER_BUFFER_SWAP) {
                final boolean coverageLayerWasRenderedThisFrameFinal = coverageLayerWasRenderedThisFrame;
                coverageLegendVisible.setValue(coverageLayerWasRenderedThisFrameFinal);
                coverageLayerWasRenderedThisFrame = false;
            }
        }
    }

    public AsyncBooleanProperty coverageLegendVisibleProperty() {
        return coverageLegendVisible;
    }

}
