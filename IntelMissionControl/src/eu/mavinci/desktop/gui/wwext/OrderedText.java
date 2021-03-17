/*
 * This Source is licenced under the NASA OPEN SOURCE AGREEMENT VERSION 1.3
 *
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 *
 * Modifications by MAVinci GmbH, Germany (C) 2009-2016:
 * added tooltips to ordered texts
 */
package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.PublishSource;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.ToolTipRenderer;

import java.awt.Color;
import java.awt.Font;

@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class OrderedText implements OrderedRenderable {
    protected Font font;
    protected String text;
    protected int x, y;
    protected double eyeDistance;
    protected java.awt.Point pickPoint;
    protected Layer layer;
    protected java.awt.Color colorText;
    protected java.awt.Color colorOutline;
    protected java.awt.Color colorInterior;

    public OrderedText(String text, Font font, int x, int y, java.awt.Color color, double eyeDistance) {
        this.text = text;
        this.font = font;
        this.x = x;
        this.y = y;
        this.eyeDistance = eyeDistance;
        this.colorText = color;
        this.colorOutline = color;
        this.colorInterior = ToolTipRenderer.getContrastingColor(color);
    }

    public void setColorText(Color colorText) {
        this.colorText = colorText;
    }

    public void setColorOutline(Color colorOutline) {
        this.colorOutline = colorOutline;
    }

    public void setColorInterior(Color colorInterior) {
        this.colorInterior = colorInterior;
    }

    public OrderedText(
            String text, Font font, int x, int y, java.awt.Point pickPoint, Layer layer, double eyeDistance) {
        this.text = text;
        this.font = font;
        this.x = x;
        this.y = y;
        this.eyeDistance = eyeDistance;
        this.pickPoint = pickPoint;
        this.layer = layer;
    }

    public double getDistanceFromEye() {
        return this.eyeDistance;
    }

    public void render(DrawContext dc) {
        ToolTipRenderer toolTipRenderer = this.getToolTipRenderer(dc);
        toolTipRenderer.render(dc, this.text, x, y);
    }

    public void pick(DrawContext dc, java.awt.Point pickPoint) {}

    protected ToolTipRenderer getToolTipRenderer(DrawContext dc) {
        ToolTipRenderer tr = (this.font != null) ? new ToolTipRenderer(this.font) : new ToolTipRenderer();

        if (colorInterior != null && colorOutline != null && colorText != null) {
            tr.setTextColor(colorText);
            tr.setOutlineColor(colorOutline);
            tr.setInteriorColor(colorInterior);
        } else {
            tr.setUseSystemLookAndFeel(true);
        }

        return tr;
    }
}
