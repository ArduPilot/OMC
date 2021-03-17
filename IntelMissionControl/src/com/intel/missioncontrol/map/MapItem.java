/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import gov.nasa.worldwind.geom.Position;

public final class MapItem {

    private final ILayer layer;
    private final Object object;
    private final Position position;

    public MapItem(ILayer layer, Object object, Position position) {
        this.layer = layer;
        this.object = object;
        this.position = position;
    }

    public ILayer getLayer() {
        return layer;
    }

    public Object getObject() {
        return object;
    }

    public Position getPosition() {
        return position;
    }

}
