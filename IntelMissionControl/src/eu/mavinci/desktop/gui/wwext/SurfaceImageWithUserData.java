/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SurfaceImageWithUserData extends SurfaceImageCompressing implements IWWRenderableWithUserData {

    public static final Color transparencyColor = new Color(0, 0, 0, 0);

    public SurfaceImageWithUserData() {
        super();
        setPickEnabled(true);
    }

    public SurfaceImageWithUserData(File path, List<? extends LatLon> corners) throws IOException {
        super(path.toURI(), corners);
        setPickEnabled(true);
    }

    public SurfaceImageWithUserData(BufferedImage tileImage, List<? extends LatLon> corners) {
        super(tileImage, corners);
        setPickEnabled(true);
    }

    /**
     * @param bufferedImage width and heigh of image have to be a power of 2, images needs 4bit color deepness
     * @param sector
     */
    public SurfaceImageWithUserData(BufferedImage bufferedImage, Sector sector) {
        super(bufferedImage, sector);
        setPickEnabled(true);
    }

    public SurfaceImageWithUserData(File file, Sector sec) {
        super(file.toURI(), sec);
        setPickEnabled(true);
    }

    Object userData = null;

    int count = 1;

    public synchronized void increment() {
        count++;
    }

    public synchronized boolean decrement() {
        count--;
        return count == 0;
    }

    @Override
    public Object getUserData() {
        return userData;
    }

    @Override
    public void setUserData(Object o) {
        // System.out.println("new Surface Image with this userdata:" + o);
        userData = o;
    }

    boolean isDraggable = false;

    @Override
    public boolean isDraggable() {
        return isDraggable;
    }

    @Override
    public void setDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    boolean isSelectable = true;

    @Override
    public boolean isSelectable() {
        return isSelectable;
    }

    @Override
    public void setSelectable(boolean isSelectable) {
        this.isSelectable = isSelectable;
    }

    boolean isPopupTriggering = true;

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
