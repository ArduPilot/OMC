/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.layers.RenderableLayer;

public class AndroidModelLayer extends RenderableLayer {

    public AndroidModelLayer(IAirplane plane) {
        setName("AndroidPosLayerName");
        setPickEnabled(false);
        new WWModel3DAndroid(plane, this);
    }
}
