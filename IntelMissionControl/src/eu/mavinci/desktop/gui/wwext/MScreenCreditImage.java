/*
 * This Source is licenced under the NASA OPEN SOURCE AGREEMENT VERSION 1.3
 *
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 *
 * Modifications by MAVinci GmbH, Germany (C) 2009-2016:
 * adapted from nasa sources to support own credit types
 */
package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.PublishSource;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenCreditImage;

import java.awt.Color;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class MScreenCreditImage extends ScreenCreditImage implements ICreditIndivSize {
    private static final double DIMMED_OPACITY = 0.8;
    private static final int HIGHLIGHTED_OPACITY = 1;

    MScreenCreditImage(String name, Object imageSource) {
        super(name, imageSource);
    }

    public int getImageWidth(DrawContext dc) {
        return (int)Math.round(width * this.getViewport().getHeight() / height);
    }

    public int getImageHeight(DrawContext dc) {
        return (int)this.getViewport().getHeight();
    }

    public int getPreferredHeight() {
        return height;
    }

    public int getPreferredWidth() {
        return width;
    }

    static HashMap<String, WeakReference<MScreenCreditImage>> hashMap =
        new HashMap<String, WeakReference<MScreenCreditImage>>(2000);
    static ReferenceQueue<MScreenCreditImage> queue = new ReferenceQueue<MScreenCreditImage>();

    /**
     * use caching of ScreenCredits this delivers the same object if it is called a second time with the same name this
     * will enable WWJ to show identical credits only once, even if multiple data sources (e.g. different geoTiffs) like
     * to show the same credit
     *
     * @param text
     * @param link
     * @return
     */
    public static MScreenCreditImage create(String name, Object imageSource) {
        // cleanup cache
        Reference<? extends MScreenCreditImage> oldRef = queue.poll();
        while (oldRef != null) {
            hashMap.remove(oldRef);
            oldRef = queue.poll();
        }

        WeakReference<MScreenCreditImage> ref = hashMap.get(name);

        MScreenCreditImage screenCredit = null;
        if (ref != null) {
            screenCredit = ref.get();
        }
        // System.out.println("get img for from cache:" + f);
        if (screenCredit == null) {
            // System.out.println(" cache miss!");
            screenCredit = new MScreenCreditImage(name, imageSource);
            hashMap.put(name, new WeakReference<MScreenCreditImage>(screenCredit, queue));
        }

        return screenCredit;
    }
    public void highlight() {
        setOpacity(HIGHLIGHTED_OPACITY);
    }

    public void dim() {
        setOpacity(DIMMED_OPACITY);
    }

    @Override
    protected void draw(DrawContext dc) {
        super.draw(dc);
    }
}
