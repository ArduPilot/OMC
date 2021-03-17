/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

public interface ITransformationProvider {

    Vec4 transformToLocal(LatLon latLon);

    Vec4 transformToLocalInclAlt(Position latLon);

    Position transformToGlobe(Vec4 vec);

    Vec4 compensateCamCentrency(Vec4 vec, boolean isForward, boolean isRot90);

    double compensateCamCentrency(double parallelCoordinate, boolean isForward);

    Vec4 transformToGlobalNorthing(Vec4 vec);

    Vec4 transformToLocal(Vec4 vec);

    Angle transformYawToLocal(Angle yaw);

    Angle transformYawToGlobal(Angle yaw);

    double getHeightOffsetToGlobal();

}
