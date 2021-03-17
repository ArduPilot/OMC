/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;

public class SurfacePolygonWithUserDataSlave extends SurfacePolygon implements IWWRenderableWithUserData {

    IWWRenderableWithUserData master;

    public SurfacePolygonWithUserDataSlave(IWWRenderableWithUserData master) {
        super();
        this.master = master;
    }

    public SurfacePolygonWithUserDataSlave(Iterable<? extends LatLon> iterable, IWWRenderableWithUserData master) {
        super(iterable);
        this.master = master;
    }

    public SurfacePolygonWithUserDataSlave(
            ShapeAttributes attributes, Iterable<? extends LatLon> iterable, IWWRenderableWithUserData master) {
        super(attributes, iterable);
        this.master = master;
    }

    public SurfacePolygonWithUserDataSlave(ShapeAttributes attributes, IWWRenderableWithUserData master) {
        super(attributes);
        this.master = master;
    }

    @Override
    public Object getUserData() {
        return master.getUserData();
    }

    @Override
    public boolean isDraggable() {
        return master.isDraggable();
    }

    @Override
    public boolean isSelectable() {
        return master.isSelectable();
    }

    @Override
    public boolean isPopupTriggering() {
        return master.isPopupTriggering();
    }

    @Override
    public void setDraggable(boolean isDraggable) {}

    @Override
    public void setPopupTriggering(boolean isPopupTriggering) {}

    @Override
    public void setSelectable(boolean isSelectable) {}

    @Override
    public void setUserData(Object o) {}

    boolean hasTooltip = true;

    @Override
    public boolean hasTooltip() {
        return hasTooltip;
    }

    @Override
    public void setHasTooltip(boolean hasTooltip) {
        this.hasTooltip = hasTooltip;
    }

    @Override
    public boolean isSelectableWhileAddNewPoints() {
        return master.isSelectableWhileAddNewPoints();
    }

    @Override
    public void setSelectableWhileAddNewPoints(boolean isSelectableWhileAddNewPoints) {
        master.setSelectable(isSelectableWhileAddNewPoints);
    }

    @Override
    public boolean isHighlightableEvenWithoutSelectability() {
        return master.isHighlightableEvenWithoutSelectability();
    }

    @Override
    public void setHighlightableEvenWithoutSelectability(boolean isHighlightable) {
        master.setHighlightableEvenWithoutSelectability(isHighlightable);
    }
}
