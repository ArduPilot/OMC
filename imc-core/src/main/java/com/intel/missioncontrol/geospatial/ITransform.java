/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

public interface ITransform<T> {

    T transform(Position pos);

    Position transform(T vec);

}
