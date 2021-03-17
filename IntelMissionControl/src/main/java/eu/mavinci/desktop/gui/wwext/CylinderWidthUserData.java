/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Cylinder;

public class CylinderWidthUserData extends Cylinder implements IWWRenderableWithUserData {

    LatLon pos;
    double lowerWgs84;
    double upperWgs84;
    double radius;

    public CylinderWidthUserData(
            LatLon pos, double lowerWgs84, double upperWgs84, double radius, Object userDataObject) {
        o = userDataObject;
        updatePos(pos, lowerWgs84, upperWgs84, radius);
    }

    public void updatePos(LatLon pos, double lowerWgs84, double upperWgs84, double radius) {
        this.pos = pos;
        this.lowerWgs84 = lowerWgs84;
        this.upperWgs84 = upperWgs84;
        this.radius = radius;
        setAltitudeMode(WorldWind.ABSOLUTE);
        setCenterPosition(new Position(pos, lowerWgs84));
        setVerticalRadius((upperWgs84 - lowerWgs84) / 2);
        setNorthSouthRadius(radius);
        setEastWestRadius(radius);
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

    boolean isSelectableWhileAddNewPoints = true;

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
