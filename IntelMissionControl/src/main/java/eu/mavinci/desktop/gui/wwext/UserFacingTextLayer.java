/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicText;

import java.util.ArrayList;

public class UserFacingTextLayer extends AbstractLayer {

    private final ArrayList<GeographicText> labels = new ArrayList<GeographicText>();
    protected final GeographicTextRendererForgroundAble textRenderer = new GeographicTextRendererForgroundAble();

    public GeographicTextRendererForgroundAble getTextRenderer() {
        return textRenderer;
    }

    public UserFacingTextLayer() {
        setPickEnabled(false);
    }

    public void doRender(DrawContext dc) {
        textRenderer.render(dc, this.labels);
    }

    public void add(GeographicText text) {
        labels.add(text);
        fireLayerChanged();
    }

    public void clear() {
        if (labels.size() == 0) {
            return;
        }

        labels.clear();
        fireLayerChanged();
    }

    public void fireLayerChanged() {
        firePropertyChange(AVKey.LAYER, null, this);
    }

    public ArrayList<GeographicText> getTexts() {
        return labels;
    }

}
