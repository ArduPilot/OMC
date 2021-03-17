/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Position.PositionList;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.WWTexture;

import java.util.List;

public class PolygonWithUserData extends Polygon implements IWWRenderableWithUserData {

    public PolygonWithUserData() {
        super();
    }

    public PolygonWithUserData(Iterable<? extends Position> corners) {
        super(corners);
    }

    public PolygonWithUserData(PositionList corners) {
        super(corners);
    }

    @Override
    protected WWTexture makeTexture(Object imageSource) {
        return new LazilyLoadedCompressingTexture(imageSource, true);
    }

    // The Polygon.reset method computes the winding order of the boundary locations, and reverses
    // them some of the time. We don't need this feature, since our winding order is determined
    // by the order of locations in the position list.
    // We should probably stop using the Polygon class to render simple quads...
    //
    @Override
    protected void reset() {
        for (List<? extends Position> boundary : this.boundaries) {
            if (boundary == null || boundary.size() < 3) {
                continue;
            }
        }

        this.numPositions = this.countPositions();

        this.previousIntersectionShapeData = null;
        this.previousIntersectionTerrain = null;
        this.previousIntersectionGlobeStateKey = null;

        this.shapeDataCache.removeAllEntries();
        this.sector = null;
        this.surfaceShape = null;
    }

    private Object o = null;

    @Override
    public Object getUserData() {
        return o;
    }

    @Override
    public void setUserData(Object o) {
        this.o = o;
    }

    private boolean isDraggable = false;

    @Override
    public boolean isDraggable() {
        return isDraggable;
    }

    @Override
    public void setDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    protected boolean isSelectable = false;

    @Override
    public boolean isSelectable() {
        return isSelectable;
    }

    @Override
    public void setSelectable(boolean isSelectable) {
        this.isSelectable = isSelectable;
    }

    protected boolean isPopupTriggering = true;

    @Override
    public boolean isPopupTriggering() {
        return isPopupTriggering;
    }

    @Override
    public void setPopupTriggering(boolean isPopupTriggering) {
        this.isPopupTriggering = isPopupTriggering;
    }

    boolean hasTooltip = true;

    @Override
    public boolean hasTooltip() {
        return hasTooltip;
    }

    @Override
    public void setHasTooltip(boolean hasTooltip) {
        this.hasTooltip = hasTooltip;
    }

    boolean isSelectableWhileAddNewPoints = false;

    @Override
    public boolean isSelectableWhileAddNewPoints() {
        return isSelectableWhileAddNewPoints;
    }

    @Override
    public void setSelectableWhileAddNewPoints(boolean isSelectableWhileAddNewPoints) {
        this.isSelectableWhileAddNewPoints = isSelectableWhileAddNewPoints;
    }


    boolean isHighlightable = false;

    @Override
    public boolean isHighlightableEvenWithoutSelectability() {
        return isHighlightable;
    }

    @Override
    public void setHighlightableEvenWithoutSelectability(boolean isHighlightable) {
        this.isHighlightable = isHighlightable;
    }
}
