/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenCredit;

import java.awt.*;
import java.util.Arrays;

/**
 * Container for holding multiple {@link MScreenCreditAnnotation},
 * doesn't actually do anything.
 *
 * Used here: {@link eu.mavinci.desktop.gui.wwext.MMercatorTiledImageLayer#draw(gov.nasa.worldwind.render.DrawContext)}
 */
public class ListOfMScreenCredits implements ScreenCredit {
    /* package */ public final ScreenCredit[] credits;

    public ListOfMScreenCredits(ScreenCredit... credits) {
        if (credits.length < 1) {
            throw new IllegalArgumentException("Give me some credit...");
        }
        this.credits = Arrays.copyOf(credits, credits.length);
    }

    @Override
    public void setViewport(Rectangle viewport) {

    }

    @Override
    public Rectangle getViewport() {
        return null;
    }

    @Override
    public void setOpacity(double opacity) {

    }

    @Override
    public double getOpacity() {
        return 0;
    }

    @Override
    public void setLink(String link) {

    }

    @Override
    public String getLink() {
        return null;
    }

    @Override
    public void pick(DrawContext drawContext, Point point) {

    }

    @Override
    public void render(DrawContext drawContext) {

    }
}
