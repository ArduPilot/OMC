/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import eu.mavinci.desktop.gui.wwext.LazilyLoadedCompressingTexture.SourceWithCorners;
import eu.mavinci.desktop.helper.ImageMask;
import eu.mavinci.desktop.main.debug.profiling.requests.RenderRequest;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;

public class SurfaceImageCompressing extends ProjectiveImage implements IWWRenderableWithUserData {

    SourceWithCorners newDelayed;
    boolean newDelayedData; // ATTENTION: THIS HAS TO BE ABOVE the constructor, and is only allowed to be default
    // initialized, otherwise the
    // true value from inside the parent constructor call will be overwritten

    boolean wasBind;
    boolean doingBind;

    Iterable<? extends LatLon> cornersSource;

    public SurfaceImageCompressing() {
        super();
    }

    public SurfaceImageCompressing(Object imageSource, Iterable<? extends LatLon> corners) {
        super(imageSource, corners);
    }

    public SurfaceImageCompressing(Object imageSource, Sector sector) {
        super(imageSource, sector);
    }

    protected void initializeSourceTexture(DrawContext dc) {

        // System.out.println("new texture: " + this.getImageSource());
        this.sourceTexture = new LazilyLoadedCompressingTexture(this.getImageSource(), true);
    }

    public synchronized void setImageSource(Object imageSource, Iterable<? extends LatLon> corners, ImageMask mask) {
        // delaying this stuff, to do it thread save in rendering context once before prerender
        // so we can avoid concurrency bugs
        newDelayed = new SourceWithCorners(imageSource, corners, mask);
        // System.out.println("next Data:" +newDelayed );
        newDelayedData = true;
    }

    @Override
    public synchronized void setImageSource(Object imageSource, Iterable<? extends LatLon> corners) {
        setImageSource(imageSource, corners, null);
    }

    @Override
    public synchronized void preRender(DrawContext dc) {
        if (newDelayedData) {
            // System.out.println("processing Delayed" + newDelayed);
            newDelayedData = false;

            if (this.generatedTexture != null) {
                this.previousGeneratedTexture = this.generatedTexture;
            }

            if (imageSource == null || !Sector.isSector(newDelayed.corners)) {
                // on first loading, I need to add the corners directly, or if the image has to be transformed to sector
                // shape
                // System.out.println("fist / shaping");
                initializeGeometry(newDelayed.corners);
            }

            this.imageSource = newDelayed;
            this.sourceTexture = null;
            this.generatedTextureExpired = true;
        }

        // boolean needReBind = (this.generatedTexture == null || this.generatedTextureExpired);
        super.preRender(dc);
        // boolean isReBind = (this.generatedTexture == null || this.generatedTextureExpired);
        // System.out.println("need " + needReBind + " -> " + isReBind);
        // if (!needReBind && isReBind){
        if (wasBind) {
            Iterable<? extends LatLon> cornersNext = (newDelayed != null ? newDelayed.corners : null);
            if (sourceTexture instanceof LazilyLoadedCompressingTexture) {
                LazilyLoadedCompressingTexture tex = (LazilyLoadedCompressingTexture)sourceTexture;
                cornersNext = tex.getCorners();
                // } else{
                // System.out.println("something wrong: " + sourceTexture);
            }
            // System.out.println("wasbind: " + wasBind + " corn:"+cornersNext);
            if (cornersNext != null) {
                initializeGeometry(cornersNext);
            }
        }

        // Debug.profiler.requestFinished(request);
    }

    @Override
    protected void initializeGeometry(Iterable<? extends LatLon> corners) {
        if (corners != cornersSource) {
            cornersSource = corners;
            // System.out.println("reinit geom:" + corners);
            super.initializeGeometry(corners);
        }
    }

    @Override
    public void render(DrawContext dc) {
        super.render(dc);
    }

    public synchronized boolean bind(final DrawContext dc) {
        // System.out.println("bind:" + imageSource.hashCode());
        wasBind = SurfaceImageCompressing.super.bind(dc);
        return wasBind;
    }

    public boolean isRendering() {
        return wasBind;
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
