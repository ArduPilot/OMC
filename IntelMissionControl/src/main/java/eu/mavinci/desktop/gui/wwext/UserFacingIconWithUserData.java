/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.helper.ScaleHelper;
import eu.mavinci.desktop.main.core.Application;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.UserFacingIcon;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class UserFacingIconWithUserData extends UserFacingIcon implements IWWRenderableWithUserData {

    public static final Dimension d8 = ScaleHelper.scaleDimension(new Dimension(8, 8));
    public static final Dimension d16 = ScaleHelper.scaleDimension(new Dimension(16, 16));
    public static final Dimension d24 = ScaleHelper.scaleDimension(new Dimension(24, 24));
    public static final Dimension d32 = ScaleHelper.scaleDimension(new Dimension(32, 32));
    public static final Dimension d48 = ScaleHelper.scaleDimension(new Dimension(48, 48));
    public static final Dimension d64 = ScaleHelper.scaleDimension(new Dimension(64, 64));
    public static final Dimension d96 = ScaleHelper.scaleDimension(new Dimension(96, 96));
    public static final Dimension d128 = ScaleHelper.scaleDimension(new Dimension(128, 128));

    UserFacingIconWithUserData(Object icon, Position pos, Object userDataObject) {
        super(icon, pos);
        setSize(d24);
        o = userDataObject;
    }

    UserFacingIconWithUserData(Object icon, LatLon latLon, Object userDataObject) {
        super(icon, new Position(latLon, 0));
        setSize(d24);
        o = userDataObject;
    }

    public UserFacingIconWithUserData(BufferedImage icon, Position pos, Object userDataObject) {
        this((Object)icon, pos, userDataObject);
    }

    public UserFacingIconWithUserData(String iconPath, Position pos, Object userDataObject) {
        this(Application.getBufferedImageFromResource(iconPath), pos, userDataObject);
    }

    public UserFacingIconWithUserData(String iconPath, Dimension iconSize, Position pos, Object userDataObject) {
        this(Application.getBufferedImageFromResource(iconPath, iconSize.width, iconSize.height), pos, userDataObject);
    }

    public static UserFacingIconWithUserData getOnTerrainRelativeHeightInLayer(
            String iconPath, LatLon pos, Object userDataObject) {
        return new UserFacingIconWithUserData(Application.getBufferedImageFromResource(iconPath), pos, userDataObject);
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

    public void setImageFromResource(String resourcePath) {
        setImageSource(Application.getBufferedImageFromResource(resourcePath));
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
