/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.intel.missioncontrol.map.IMapView;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewLimits;

public interface IWWMapView extends OrbitView, IMapView {

    boolean isDetectCollisions();

    void setDetectCollisions(boolean detectCollisions);

    boolean hadCollisions();

    Angle getHeading();

    Position getEyePosition();

    Position getCenterPosition();

    void setCenterPosition(Position center);

    double getZoom();

    void setZoom(double zoom);

    OrbitViewLimits getOrbitViewLimits();

    void setOrbitViewLimits(OrbitViewLimits limits);

    boolean canFocusOnViewportCenter();

    void focusOnViewportCenter();

    void stopMovementOnCenter();

    DrawContext getDC();

}
