/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

public interface ILayerFactory {

    <T extends ILayer> T newLayer(Class<T> layerClass);

}
