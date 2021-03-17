/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.intel.missioncontrol.beans.property.ReadOnlyAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncObjectProperty;
import gov.nasa.worldwind.geom.Position;
import java.awt.Point;

public interface IMapDragManager {
    ReadOnlyAsyncBooleanProperty isDraggingProperty();

    ReadOnlyAsyncObjectProperty dragObjectUserDataProperty();

    ReadOnlyAsyncObjectProperty<Position> lastDraggingPositionProperty();

    default boolean isDragging() {
        return isDraggingProperty().get();
    }

    default Object getDragObjectUserData() {
        return dragObjectUserDataProperty().get();
    }

    default Position getLastDraggingPosition() {
        return lastDraggingPositionProperty().get();
    }

    void stopDragging();

    boolean startDragging(Object pickedObject, Point pickPoint, double startAlt, Position position);

    void move(Point point);
}
