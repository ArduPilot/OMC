/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.airTraffic;

import com.intel.missioncontrol.airtraffic.dto.AirtrafficObject;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.FlightplanLayer;
import eu.mavinci.desktop.gui.wwext.UserFacingIconOriented;
import eu.mavinci.desktop.gui.wwext.UserFacingIconWithUserData;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

/**
 * Represents the airtraffic disting from the controlled UAV. For example the traffic
 */
public class AircraftRenderable implements Renderable {

    public static FlarmAircraftType[] types = {FlarmAircraftType.unknown,
            FlarmAircraftType.glider_motorGlider,
            FlarmAircraftType.glider_motorGlider,
            FlarmAircraftType.poweredAircraft,
            FlarmAircraftType.poweredAircraft,
            FlarmAircraftType.poweredAircraft, // 5
            FlarmAircraftType.jetAircraft,//6
            FlarmAircraftType.undefined,
            FlarmAircraftType.undefined,
            FlarmAircraftType.undefined,
            FlarmAircraftType.helicopter_rotorcraft, //10
            FlarmAircraftType.paraGliderSoft,
            FlarmAircraftType.balloon,
            FlarmAircraftType.balloon,
            FlarmAircraftType.uav, // 13
            FlarmAircraftType.flyingSaucerUFO,
            FlarmAircraftType.paraGliderSoft,
            FlarmAircraftType.skydiverDropZone, //16
            FlarmAircraftType.undefined,
            FlarmAircraftType.undefined,
            FlarmAircraftType.undefined,
            FlarmAircraftType.flyingSaucerUFO,
            FlarmAircraftType.flyingSaucerUFO,
            FlarmAircraftType.balloon,
            FlarmAircraftType.staticObject,
            FlarmAircraftType.staticObject
    };
    UserFacingIconOriented icon;

    public AircraftRenderable(AirtrafficObject ato, IElevationModel elevationModel, IEgmModel egmModel) {

        int typeNr = ato.getProperties().getType();
        var type = types[typeNr];


        Position pos = Position.fromDegrees(ato.geometry.coordinates.get(1),
                ato.geometry.coordinates.get(0),
                ato.getProperties().getWgs84Altitude());

        pos = elevationModel.getPositionOverGround(pos);
        pos = new Position(pos.latitude, pos.longitude, pos.getAltitude() + 10);

        icon = new UserFacingIconOriented(type.getBufferedImageFull(), pos, ato);

        icon.setSize(UserFacingIconWithUserData.d32);
        icon.setYaw(ato.getProperties().getCourseOverGround());
        icon.setSelectable(true);
        icon.setDraggable(false);
        icon.setHighlightScale(FlightplanLayer.HIGHLIGHT_SCALE);
        icon.setToolTipText(ato.getProperties().getIdentifier() + "  @barometric: " + ato.getProperties().getBaroAltitude());
        icon.setToolTipTextColor(java.awt.Color.YELLOW);
        icon.setHasTooltip(true);
        icon.setAlwaysOnTop(true);


    }

    @Override
    public void render(DrawContext dc) {
        if (icon != null) {
            icon.render(dc);
        }
    }

}
