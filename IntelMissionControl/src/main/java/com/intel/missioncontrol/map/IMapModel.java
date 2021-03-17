/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AltitudeModes;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.concurrent.Future;

public interface IMapModel {

    ReadOnlyAsyncListProperty<ILayer> layersProperty();

    IMapDragManager getDragManager();

    double getAltitudeForNewNodesWithinM();

    AltitudeModes getAltitudeModeForNewNodes();

    void addNodeFromGui(double lat, double lon);

    Future<Void> deleteSelectionAsync();

    AreaOfInterest addAreaOfInterest(Mission mission, PlanType aoiId);

    void setShowWireframeInterior(boolean visible);

}
