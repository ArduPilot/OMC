/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import javafx.event.Event;
import javafx.event.EventType;

public class MapInputEvent extends Event {

    public static final EventType<MapInputEvent> MAP_SELECT = new EventType<>("MAP_SELECT");

    public static final EventType<MapInputEvent> MAP_ACTION = new EventType<>("MAP_ACTION");

    private final Object mapObject;
    private final double x;
    private final double y;

    public MapInputEvent(Object source, EventType<? extends Event> eventType, Object mapObject, double x, double y) {
        super(source, null, eventType);
        this.mapObject = mapObject;
        this.x = x;
        this.y = y;
    }

    public Object getMapObject() {
        return mapObject;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
