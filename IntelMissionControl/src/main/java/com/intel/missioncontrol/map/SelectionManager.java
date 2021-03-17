/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.intel.missioncontrol.mission.WayPoint;
import org.asyncfx.beans.AsyncObservable;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectionManager implements ISelectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionManager.class);

    private final AsyncObjectProperty<Object> currentSelection = new SimpleAsyncObjectProperty<>(this);

    private final AsyncListProperty<WayPoint> highlighted =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<WayPoint>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(wp -> new AsyncObservable[] {wp.selectedProperty()}))
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
        PropertyHelper.setValueSafe(currentSelection, newSelection);
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
