/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Position.PositionList;
import gov.nasa.worldwind.render.ExtrudedPolygon;

public class ExtrudedPolygonWithUserData extends ExtrudedPolygon implements IWWRenderableWithUserData {

    public static final double MIN_RENDERING_HEIGHT = 1;

    public ExtrudedPolygonWithUserData() {
        super();
    }

    public ExtrudedPolygonWithUserData(Double height) {
        super(height);
    }

    public ExtrudedPolygonWithUserData(Iterable<? extends LatLon> corners, double height, Iterable<?> imageSources) {
        super(corners, height, imageSources);
    }

    public ExtrudedPolygonWithUserData(Iterable<? extends LatLon> corners, Double height) {
        super(corners, height < MIN_RENDERING_HEIGHT ? MIN_RENDERING_HEIGHT : height);
    }

    public ExtrudedPolygonWithUserData(Iterable<? extends Position> corners, Iterable<?> imageSources) {
        super(corners, imageSources);
    }

    public ExtrudedPolygonWithUserData(Iterable<? extends Position> corners) {
        super(corners);
    }

    public ExtrudedPolygonWithUserData(PositionList corners) {
        super(corners);
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

    protected boolean isSelectable = true;

    @Override
    public boolean isSelectable() {
        return isSelectable;
    }

    @Override
    public void setSelectable(boolean isSelectable) {
        this.isSelectable = isSelectable;
    }

    protected boolean isPopupTriggering = false;

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
