/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.impl;

import com.intel.missioncontrol.map.MapInputEvent;
import eu.mavinci.desktop.gui.wwext.IWWRenderableWithUserData;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.javafx.WWNode;
import java.awt.Point;
import javafx.application.Platform;
import javafx.event.EventType;

public class EventDispatcherSelectListener implements SelectListener {

    private final WWNode node;

    public EventDispatcherSelectListener(WWNode node) {
        this.node = node;
    }

    @Override
    public void selected(SelectEvent event) {
        if (!event.isLeftClick() && !event.isLeftDoubleClick()) {
            return;
        }

        Point point = event.getPickPoint();
        final double x = point != null ? point.x : -1;
        final double y = point != null ? point.y : -1;
        Object pickedObjectCandidate = PickingHelper.getPickedObject(event, false, false, node);
        if (pickedObjectCandidate instanceof IWWRenderableWithUserData) {
            IWWRenderableWithUserData renderable = (IWWRenderableWithUserData)pickedObjectCandidate;
            if (renderable.isSelectable()) {
                pickedObjectCandidate = renderable.getUserData();
            }
        } else {
            pickedObjectCandidate = null;
        }

        final Object pickedObject = pickedObjectCandidate;
        if (pickedObject != null) {
            final EventType<MapInputEvent> eventType;
            if (event.isLeftClick()) {
                eventType = MapInputEvent.MAP_SELECT;
            } else if (event.isLeftDoubleClick()) {
                eventType = MapInputEvent.MAP_ACTION;
            } else {
                eventType = null;
            }

            if (eventType != null) {
                Platform.runLater(() -> node.fireEvent(new MapInputEvent(node, eventType, pickedObject, x, y)));
            }
        }
    }

}
