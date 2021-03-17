/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.sun;

import gov.nasa.worldwind.layers.RenderableLayer;

public class SunModelLayer extends RenderableLayer {

    WWSunModel sunModel = new WWSunModel();

    public SunModelLayer() {
        setName("Sun_LAYER_NAME");

        addRenderable(sunModel);
        setEnabled(true);
        setPickEnabled(false);
        setMaxActiveAltitude(Float.MAX_VALUE);
    }
}
