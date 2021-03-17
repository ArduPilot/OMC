/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.layers.RenderableLayer;

public class PlaneModelLayer extends RenderableLayer {

    public PlaneModelLayer(IAirplane plane) {
        setName("CurrentPosLayerName");
        setPickEnabled(false);
        new WWModel3DAirplane(plane, this);
    }

}
