/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.intel.missioncontrol.beans.property.ReadOnlyAsyncListProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.concurrent.FluentFuture;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AltitudeModes;

public interface IMapModel {

    ReadOnlyAsyncListProperty<ILayer> layersProperty();

    IMapDragManager getDragManager();

    double getAltitudeForNewNodesWithinM();

    AltitudeModes getAltitudeModeForNewNodes();

    void addNodeFromGui(double lat, double lon);

    FluentFuture<Void> deleteSelectionAsync();

    AreaOfInterest addAreaOfInterest(Mission mission, PlanType aoiId);

    void setShowWireframeInterior(boolean visible);

}
