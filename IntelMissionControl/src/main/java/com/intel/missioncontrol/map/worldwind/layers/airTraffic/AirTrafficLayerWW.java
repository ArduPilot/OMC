/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.airTraffic;

import com.google.inject.Inject;
import com.intel.missioncontrol.airtraffic.IAirTrafficManager;
import com.intel.missioncontrol.airtraffic.dto.AirtrafficObject;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwindx.examples.ShapeEditingExtension;
import java.awt.Color;
import org.asyncfx.collections.LockedList;

public class AirTrafficLayerWW extends RenderableLayer {

    private final IAirTrafficManager atm;
    private final IEgmModel egmModel;
    private final IElevationModel elevationModel;
    /**
     * when displaying aircrafttraffic an arrow is added. the length of the arrow is speed * updateInterval;
     */
    private final double updateInterval = 10;

    @Inject
    public AirTrafficLayerWW(IAirTrafficManager atm, IElevationModel elevationModel, IEgmModel egmModel) {
        this.atm = atm;
        setPickEnabled(true);
        setEnabled(true);
        setPickEnabled(false);
        this.egmModel = egmModel;
        this.elevationModel = elevationModel;
        atm.relevantTrafficProperty().addListener((observable, oldValue, newValue, subChange) -> reconstruct());
    }

    /**
     * Given some lat/lon position and a bearing, add distance in the direction of bearing
     *
     * @param pos      initial position
     * @param bearing  bearing in which to add distance
     * @param distance
     * @return
     */
    public static LatLon offset(LatLon pos, double bearing, double distance) {
        double R = 6378.1;
        double brng = bearing * (2 * Math.PI / 360.);


        double lat1 = pos.latitude.radians;
        double lon1 = pos.longitude.radians;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / R) +
                Math.cos(lat1) * Math.sin(distance / R) * Math.cos(brng));

        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(distance / R) * Math.cos(lat1),
                Math.cos(distance / R) - Math.sin(lat1) * Math.sin(lat2));
        return LatLon.fromRadians(lat2, lon2);

    }

    private void reconstruct() {

        removeAllRenderables();

        try (LockedList<AirtrafficObject> airtrafficObjects = atm.relevantTrafficProperty().lock()) {
            for (AirtrafficObject airtrafficObject : airtrafficObjects) {
                addRenderable(new AircraftRenderable(airtrafficObject, elevationModel, egmModel));


                Position pos = Position.fromDegrees(airtrafficObject.geometry.coordinates.get(1),
                        airtrafficObject.geometry.coordinates.get(0),
                        airtrafficObject.getProperties().getWgs84Altitude());
                pos = elevationModel.getPositionOverGround(pos);
                var pos1 = new Position(pos.latitude, pos.longitude, pos.getAltitude() + 10);

                var lat1 = new LatLon(pos1);

                var arrowLength = airtrafficObject.getProperties().getSpeedOverGround() * updateInterval;
                var projectedAltDifference = airtrafficObject.getProperties().getVerticalSpeed() * updateInterval;
                var lat2 = offset(lat1, airtrafficObject.getProperties().getCourseOverGround(), arrowLength / 1000.);
                var pos2 = new Position(lat2, pos.getAltitude() + 10 + projectedAltDifference);
                var arrow = new ShapeEditingExtension.Arrow(pos1, pos2, pos.getAltitude() + 10);

                ShapeAttributes attrs = new BasicShapeAttributes();
                attrs.setOutlineMaterial(new Material(new Color(0x00ffff)));
                attrs.setOutlineWidth(3);
                attrs.setEnableAntialiasing(true);
                arrow.setAttributes(attrs);


                addRenderable(arrow);


            }
        }
        firePropertyChange(AVKey.LAYER, null, this);

    }
}
