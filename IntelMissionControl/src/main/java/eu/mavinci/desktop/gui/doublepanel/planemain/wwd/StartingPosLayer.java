/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.map.IMapDragManager;
import com.intel.missioncontrol.map.IMapModel;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.listeners.IAirplaneListenerStartPos;
import eu.mavinci.desktop.gui.wwext.IconLayerCentered;
import eu.mavinci.desktop.gui.wwext.UserFacingIconWithUserData;
import eu.mavinci.geo.ILatLonReferenced;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

public class StartingPosLayer extends IconLayerCentered implements IAirplaneListenerStartPos, ILatLonReferenced {

    IAirplane plane;
    IMapDragManager dragger;

    public StartingPosLayer(IAirplane plane, IMapModel mapModel) {
        setName("StartingPosLayerName");
        setPickEnabled(true);
        setAlwaysUseAbsoluteElevation(false);
        setRenderAlwaysOverGround(true);
        setEnabled(false);

        this.plane = plane;
        dragger = mapModel.getDragManager();

        plane.addListener(this);
    }

    private void recontructLayer() {
        if (!dragger.isDragging()) {

            // drawing starting position
            LatLon startPos;
            try {
                startPos = plane.getAirplaneCache().getStartPosBaro();
            } catch (AirplaneCacheEmptyException e) {
                return;
            }
            // PlaneStartingPoint simStartPos = new PlaneStartingPoint(plane);
            // System.out.println("elev startPoint" + elevationStartPoint);
            UserFacingIconWithUserData icon =
                UserFacingIconWithUserData.getOnTerrainRelativeHeightInLayer(
                    "com/intel/missioncontrol/gfx/map_takeoff.svg", startPos, this);
            icon.setSelectable(true);
            icon.setDraggable(isChangeable());
            icon.setHighlightScale(FlightplanLayer.HIGHLIGHT_SCALE);
            icon.setToolTipText("Start Position " + startPos.toString());
            icon.setToolTipTextColor(java.awt.Color.YELLOW);

            removeAllIcons();
            addIcon(icon);

            firePropertyChange(AVKey.LAYER, null, this);
        }
    }

    public boolean isChangeable() {
        return isChangeable(plane);
    }

    public static boolean isChangeable(IAirplane plane) {
        try {
            if (plane.getAirplaneCache().isSimulation()) {
                return true;
            } else {
                return !plane.isReadable();
            }
        } catch (AirplaneCacheEmptyException e) {
            return true;
        }
    }

    @Override
    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        recontructLayer();
    }

    public void setStartingPoint(Position pos) {
        if (!isChangeable()) {
            return;
        }

        plane.setStartpos(pos.getLongitude().degrees, pos.getLatitude().degrees);
    }

    public Position getPosition() {
        try {
            return new Position(
                plane.getAirplaneCache().getStartPosBaro(), plane.getAirplaneCache().getStartElevOverWGS84());
        } catch (AirplaneCacheEmptyException e) {
            return null;
        }
    }

    @Override
    public LatLon getLatLon() {
        try {
            return plane.getAirplaneCache().getStartPosBaro();
        } catch (AirplaneCacheEmptyException e) {
            return null;
        }
    }
}
