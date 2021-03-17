/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.layers.IconLayer;

public class MIconLayer extends IconLayer {

    public MIconLayer() {
        iconRenderer = new MIconRenderer();
    }
}
