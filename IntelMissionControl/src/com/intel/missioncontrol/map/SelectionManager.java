/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.intel.missioncontrol.beans.AsyncObservable;
import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.concurrent.SynchronizationContext;
import com.intel.missioncontrol.mission.WayPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectionManager implements ISelectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionManager.class);

    private final AsyncObjectProperty<Object> currentSelection =
        new SimpleAsyncObjectProperty<>(
            this,
            new PropertyMetadata.Builder<>()
                .synchronizationContext(SynchronizationContext.getCurrent())
                .create());

    private final AsyncListProperty<WayPoint> highlighted =
            new SimpleAsyncListProperty<>(
                    this,
                    new PropertyMetadata.Builder<AsyncObservableList<WayPoint>>()
                            .initialValue(
                                    FXAsyncCollections.observableArrayList(
                                            wp -> new AsyncObservable[] {wp.selectedProperty()}))
                            .create());

    private Object targetSelection = null;

    @Override
    public synchronized void setSelection(Object newSelection) {
        if (targetSelection != null) {
            LOGGER.warn(
                "Selection manager already has a target: "
                    + targetSelection
                    + " cannot set selection on: "
                    + newSelection,
                new Exception());
            // seems that this line is actually not needed, if I remove the comment, then when I select a corner in the
            // advanced parameters table, AOI is selected instead
            // Dispatcher.postToUI(() -> setSelectionAsync(newSelection));
            return;
        }

        targetSelection = newSelection;
        currentSelection.setAsync(newSelection);
        targetSelection = null;
    }

    @Override
    public Object getSelection() {
        return currentSelection.get();
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Object> currentSelectionProperty() {
        return currentSelection;
    }

    @Override
    public AsyncListProperty<WayPoint> getHighlighted() {
        return highlighted;
    }

}
