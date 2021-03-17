/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.layers.IconLayer;

public class IconLayerCentered extends IconLayer {

    public IconLayerCentered() {
        iconRenderer = new IconRendererCentered();
    }

    public void setRenderAlwaysOverGround(boolean renderAlwaysOverGround) {
        ((IconRendererCentered)iconRenderer).setRenderAlwaysOverGround(renderAlwaysOverGround);
    }

    public void setReferencePoint(IconRendererCentered.ReferencePoint referencePointIsCenter) {
        ((IconRendererCentered)iconRenderer).setReferencePoint(referencePointIsCenter);
    }
}
