/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.intel.missioncontrol.beans.property.ReadOnlyAsyncObjectProperty;
import gov.nasa.worldwind.geom.Position;

public interface IMapController {

    ReadOnlyAsyncObjectProperty<Position> pointerPositionProperty();

    default Position getPointerPosition() {
        return pointerPositionProperty().get();
    }

    boolean popLastMouseMode();

    boolean tryCancelMouseModes(InputMode... modes);

    ReadOnlyAsyncObjectProperty<InputMode> mouseModeProperty();

    InputMode getMouseMode();

    void setMouseMode(InputMode mouseMode);

    boolean isSelecting();

    boolean isEditing();

}
