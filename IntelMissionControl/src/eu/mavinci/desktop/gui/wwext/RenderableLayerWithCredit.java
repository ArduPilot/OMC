/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;

public class RenderableLayerWithCredit extends RenderableLayer {

    @Override
    protected void doRender(DrawContext dc) {
        super.doRender(dc);
        dc.addScreenCredit(this.getScreenCredit());
    }

}
