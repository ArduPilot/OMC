/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.google.common.collect.ImmutableList;
import eu.mavinci.desktop.helper.gdal.ISRSreferenced;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import eu.mavinci.flightplan.ReferencePoint;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AbstractGraticuleLayer;
import gov.nasa.worldwind.layers.GraticuleRenderingParams;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.UserFacingText;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import javafx.beans.property.ObjectProperty;

public final class LocalGraticuleLayer extends AbstractGraticuleLayer {

    private static final String GRATICULE_LEVEL_0 = "Graticule.LocalLevel0";
    private static final String GRATICULE_LEVEL_1 = "Graticule.LocalLevel1";

    private ObjectProperty<MSpatialReference> srs;
    private ReferencePoint lastOrigin;
    private Object[] axisLines;
    private Object[] gridLines;
    private GeographicText[] labels;
    private DecimalFormat decimalFormat = new DecimalFormat("#.#");

    final int axisLength = 10;
    final int subdivisions = 10;

    public LocalGraticuleLayer(ObjectProperty<MSpatialReference> srs) {
        this.srs = srs;
        setName("localGraticule");
        setPickEnabled(false);
        initRenderingParams();
    }

    private void initRenderingParams() {
        GraticuleRenderingParams params;
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, Color.GREEN);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, Color.WHITE);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, Font.decode("Arial-Bold-16"));
        setRenderingParams(GRATICULE_LEVEL_0, params);

        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0.75f, 0.75f, 0.75f));
        setRenderingParams(GRATICULE_LEVEL_1, params);
    }

    @Override
    public void selectRenderables(DrawContext dc) {
        ReferencePoint currentOrigin = this.srs.get().getOrigin();
        if (this.lastOrigin != currentOrigin) {
            this.lastOrigin = currentOrigin;
            updateGrid(currentOrigin);
        }

        for (Object line : axisLines) {
            addRenderable(line, GRATICULE_LEVEL_0);
        }

        for (Object line : gridLines) {
            addRenderable(line, GRATICULE_LEVEL_1);
        }

        for (GeographicText label : labels) {
            addRenderable(label, GRATICULE_LEVEL_0);
        }
    }

    @Override
    protected void clear(DrawContext dc) {
        super.clear(dc);
        this.applyTerrainConformance();
    }

    private void applyTerrainConformance() {
        getRenderingParams(GRATICULE_LEVEL_0)
            .setValue(GraticuleRenderingParams.KEY_LINE_CONFORMANCE, this.terrainConformance);
        getRenderingParams(GRATICULE_LEVEL_1)
            .setValue(GraticuleRenderingParams.KEY_LINE_CONFORMANCE, this.terrainConformance);
    }

    private void updateGrid(ReferencePoint origin) {
        final double subdivisionLength = (double)axisLength / subdivisions;

        if (axisLines == null || axisLines.length != 2) {
            axisLines = new Object[2];
        }

        if (gridLines == null || gridLines.length != subdivisions * 4) {
            gridLines = new Object[subdivisions * 4];
        }

        if (labels == null || labels.length != subdivisions * 4) {
            labels = new GeographicText[subdivisions * 4];
        }

        Position originPos = Position.fromDegrees(origin.getLat(), origin.getLon());
        Angle rotation = Angle.fromDegrees(-origin.getYaw());
        Angle rotationPlus90 = rotation.add(Angle.NEG90);

        axisLines[0] =
            createLineRenderable(
                ImmutableList.of(
                    translatePosition(originPos, rotation, -axisLength),
                    translatePosition(originPos, rotation, axisLength)),
                AVKey.LINEAR);

        axisLines[1] =
            createLineRenderable(
                ImmutableList.of(
                    translatePosition(originPos, rotationPlus90, -axisLength),
                    translatePosition(originPos, rotationPlus90, axisLength)),
                AVKey.LINEAR);

        int count = 0;
        for (int i = -subdivisions; i <= subdivisions; ++i) {
            if (i == 0) {
                continue;
            }

            Position pos = translatePosition(originPos, rotationPlus90, i * subdivisionLength);
            gridLines[count] =
                createLineRenderable(
                    ImmutableList.of(
                        translatePosition(pos, rotation, -axisLength), translatePosition(pos, rotation, axisLength)),
                    AVKey.LINEAR);

            labels[count] = new UserFacingText(decimalFormat.format(i * subdivisionLength), pos);
            ++count;
        }

        for (int i = -subdivisions; i <= subdivisions; ++i) {
            if (i == 0) {
                continue;
            }

            Position pos = translatePosition(originPos, rotation, i * subdivisionLength);
            gridLines[count] =
                createLineRenderable(
                    ImmutableList.of(
                        translatePosition(pos, rotationPlus90, -axisLength),
                        translatePosition(pos, rotationPlus90, axisLength)),
                    AVKey.LINEAR);

            labels[count] = new UserFacingText(decimalFormat.format(i * subdivisionLength), pos);
            ++count;
        }
    }

    private static Position translatePosition(Position position, Angle angle, double distanceInMeters) {
        double distanceRad = distanceInMeters / (1852 * (180 * 60 / Math.PI));
        double lat =
            Math.asin(
                Math.sin(position.latitude.radians) * Math.cos(distanceRad)
                    + Math.cos(position.latitude.radians) * Math.sin(distanceRad) * Math.cos(angle.radians));

        double lon;
        if (Math.abs(Math.cos(lat)) < 0.0000001) {
            lon = position.longitude.radians;
        } else {
            double dlon =
                Math.atan2(
                    Math.sin(angle.radians) * Math.sin(distanceRad) * Math.cos(position.latitude.radians),
                    Math.cos(distanceRad) - Math.sin(position.latitude.radians) * Math.sin(lat));
            double x = position.longitude.radians - dlon + Math.PI;
            double y = 2.0 * Math.PI;
            lon = x - y * Math.floor(x / y) - Math.PI;
        }

        return Position.fromRadians(lat, lon, position.elevation);
    }
}
