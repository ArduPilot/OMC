/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.measure.AngleStyle;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.LocationFormat;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionFormatter.class);

    private final LocationFormat locationFormat = new LocationFormat();
    private final IElevationModel elevationModel;
    private final ISrsManager srsManager;

    private Position lastPosition;
    private String[] lastResult;

    public PositionFormatter(IElevationModel elevationModel, ISrsManager srsManager) {
        this.elevationModel = elevationModel;
        this.srsManager = srsManager;
    }

    public String[] formatPosition(Position position, AngleStyle angleStyle, MSpatialReference srs) {
        if (lastPosition != null && lastPosition.equals(position)) {
            return lastResult;
        }

        lastPosition = position;
        if (position == null) {
            return lastResult = new String[] {"", "", ""};
        }

        String x;
        String y;
        String z;
        String elev;
        String lx;
        String ly;
        try {
            // the following line had the intention to always sample the best possible ground resolution
            // unfortunately this caused major issues with downloading of elevation data all the time in the
            // background since its forcing the elevation model to have a certain precision (1m), which is insane if
            // you are scrolling over the globe on a more highlevel scale
            // newPos = EarthElevationModel.setOnGround(newPos);

            position =
                new Position(
                    position.getLatitude(),
                    position.getLongitude(),
                    elevationModel.getElevationAsGoodAsPossible(position.getLatitude(), position.getLongitude()));

            Vec4 v = srs.fromWgs84(position);
            if (srs.isGeographic()) {
                locationFormat.setAngleStyle(angleStyle);
                return lastResult = locationFormat.splitFormat(srs.toLocation(Position.fromDegrees(v.y, v.x, v.z)));
            }

            QuantityFormat format = new QuantityFormat();
            format.setSignificantDigits(8);
            format.setMaximumFractionDigits(8);

            Unit<?> xyUnit = Unit.parseSymbol(srs.getXyUnit(), Dimension.Length.class, Dimension.Angle.class)[0];
            Unit<Dimension.Length> zUnit = Unit.parseSymbol(srs.getZUnit(), Dimension.Length.class);
            Quantity<?> vx = Quantity.of(v.x, xyUnit);
            Quantity<?> vy = Quantity.of(v.y, xyUnit);
            Quantity<Dimension.Length> vz = Quantity.of(v.z, zUnit);

            lx = srs.getYLabel() + ": " + format.format(vy);
            ly = srs.getXLabel() + ": " + format.format(vx);
            elev = srs.getZLabel() + ": " + format.format(vz);
        } catch (Exception e) {
            return lastResult = new String[] {"", "", ""};
        }

        if (elev == null) {
            locationFormat.setAngleStyle(angleStyle);
            return lastResult = locationFormat.splitFormat(srsManager.getDefault().toLocation(position));
        }

        x = lx;
        y = ly;
        z = elev;
        return lastResult = new String[] {x, y, z};
    }

}
