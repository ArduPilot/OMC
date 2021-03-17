/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.ILayerFactory;

public class WWLayerFactory implements ILayerFactory {

    private final Injector injector;

    @Inject
    public WWLayerFactory(Injector injector) {
        this.injector = injector;
    }

    @Override
    public <T extends ILayer> T newLayer(Class<T> layerClass) {
        return injector.getInstance(layerClass);
    }

}
