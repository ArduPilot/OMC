/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

public interface IConformalTransformFactory {

    /** Returns a conformal transformation that is centered on the specified point. */
    ITransform<ProjectedPosition> createFromReference(LatLon reference);

}
