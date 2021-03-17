/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.mapmanager.airspaces.layers;

import eu.mavinci.airspace.EAirspaceManager;
import eu.mavinci.airspace.GolfUpperBoundAirspace;
import eu.mavinci.airspace.IAirspace;
import eu.mavinci.airspace.IAirspaceListener;
import eu.mavinci.desktop.gui.wwext.SurfacePolygonWithUserData;
import eu.mavinci.desktop.helper.ColorHelper;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirspacesLayer extends AbstractLayer implements IAirspaceListener {
    private static final Logger LOG = LoggerFactory.getLogger(AirspacesLayer.class);

    private enum AirspaceTypeAttributes {
        RESTRICTED(new Color(221, 0, 33), new Color(255, 95, 95), 0.15),
        WARNING(new Color(255, 163, 0), new Color(243, 213, 78), 0.2),
        ALLOWED(new Color(181, 211, 52), new Color(126, 211, 33), 0.15);

        private final Color fillColor;
        private final Color outlineColor;
        private final double opacity;

        AirspaceTypeAttributes(Color fillColor, Color outlineColor, double opacity) {
            this.fillColor = fillColor;
            this.outlineColor = outlineColor;
            this.opacity = opacity > 1.0 ? 1.0 : opacity;
        }
    }

    private static final double OUTLINE_OPACITY = 1.0;
    private static final int OUTLINE_WIDTH_IN_PX = 2;

    private static final int EXPIRED_AIRSPACES_REMOVAL_ATTEMPT_DELAY_IN_MINUTES = 15;
    private static final int NETWORK_AVAILABILITY_CHECK_DELAY_IN_SECONDS =
        EXPIRED_AIRSPACES_REMOVAL_ATTEMPT_DELAY_IN_MINUTES * 60 - 30;

    private RenderableLayer airspacesSurfaceLayer = new RenderableLayer();

    public AirspacesLayer() {
        setPickEnabled(true);
        setName(getName());
        airspacesSurfaceLayer.setMaxActiveAltitude(3000000);
        airspacesSurfaceLayer.setPickEnabled(true);

        EAirspaceManager.instance().addAirspaceListListener(this);

        reconstruct();
    }

    @Override
    protected void doRender(DrawContext dc) {
        airspacesSurfaceLayer.render(dc);
    }

    @Override
    protected void doPreRender(DrawContext dc) {
        super.doPreRender(dc);
        airspacesSurfaceLayer.preRender(dc);
    }

    @Override
    protected void doPick(DrawContext dc, Point point) {
        super.doPick(dc, point);
        airspacesSurfaceLayer.pick(dc, point);
    }

    protected void reconstruct() {
        airspacesSurfaceLayer.removeAllRenderables();
        List<IAirspace> allAirspaces = EAirspaceManager.instance().getAllAirspacesInCache();
        long renderedAirspacesCount =
            allAirspaces
                .stream()
                .filter(this::acceptedForRendering)
                .map(this::toRenderableSurface)
                .peek(this::addRenderable)
                .count();

        LOG.debug("{}:Airspaces layer has been rendered {} new airspaces", getName(), renderedAirspacesCount);
        firePropertyChange(AVKey.LAYER, null, this);
    }

    private void addRenderable(AirspaceSurfacePolygon surfacePolygon) {
        airspacesSurfaceLayer.addRenderable(surfacePolygon);
    }

    @Override
    public void airspacesChanged() {
        reconstruct();
    }

    protected boolean acceptedForRendering(IAirspace a) {
        return !(a instanceof GolfUpperBoundAirspace);
    }

    private AirspaceSurfacePolygon toRenderableSurface(IAirspace airspace) {
        List<Position> positions = new ArrayList<>(airspace.getPolygon().size());
        for (LatLon latLon : airspace.getPolygon()) {
            positions.add(
                new Position(
                    Position.fromDegrees(latLon.getLatitude().getDegrees(), latLon.getLongitude().getDegrees()),
                    airspace.floorMeters(latLon)));
        }

        AirspaceSurfacePolygon surfacePolygon = new AirspaceSurfacePolygon(positions);
        surfacePolygon.setHasTooltip(false);
        surfacePolygon.setUserData(airspace);

        AirspaceTypeAttributes typeAttributes = getAirspaceTypeAttribute(airspace);
        Material fillMaterial = new Material(typeAttributes.fillColor);
        Material outlineMaterial = new Material(typeAttributes.outlineColor);

        ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setDrawInterior(true);
        attributes.setInteriorMaterial(fillMaterial);
        attributes.setInteriorOpacity(typeAttributes.opacity);
        attributes.setDrawOutline(true);
        attributes.setOutlineWidth(OUTLINE_WIDTH_IN_PX);
        attributes.setOutlineMaterial(outlineMaterial);
        attributes.setOutlineOpacity(OUTLINE_OPACITY);
        surfacePolygon.setAttributes(attributes);

        attributes = attributes.copy();
        attributes.setInteriorMaterial(new Material(ColorHelper.invertColor(typeAttributes.fillColor)));
        attributes.setInteriorOpacity(typeAttributes.opacity);
        surfacePolygon.setHighlightAttributes(attributes);

        return surfacePolygon;
    }

    private AirspaceTypeAttributes getAirspaceTypeAttribute(IAirspace airspace) {
        double restrictRatio = 0;
        if (!airspace.getType().isMAVAllowed()) {
            restrictRatio =
                (1.
                    - Math.min(
                        1,
                        airspace.getFloorReferenceGroundOrSeaLevel()
                            / GolfUpperBoundAirspace.getGOLF_FLOOR_METERS_REL()));
        }

        if (restrictRatio == 1) {
            return AirspaceTypeAttributes.RESTRICTED;
        } else if (restrictRatio == 0) {
            return AirspaceTypeAttributes.ALLOWED;
        }

        return AirspaceTypeAttributes.WARNING;
    }

    private static class AirspaceSurfacePolygon extends SurfacePolygonWithUserData {
        private IAirspace airspace;

        AirspaceSurfacePolygon(List<Position> positions) {
            super(positions);
        }

        public IAirspace getUserData() {
            return airspace;
        }

        public void setUserData(IAirspace airspace) {
            this.airspace = airspace;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AirspaceSurfacePolygon that = (AirspaceSurfacePolygon)o;

            return airspace != null ? airspace.equals(that.airspace) : that.airspace == null;
        }

        @Override
        public int hashCode() {
            return airspace != null ? airspace.hashCode() : 0;
        }

        public boolean isSelectable() {
            return false;
        }

        public boolean isHighlightableEvenWithoutSelectability() {
            return true;
        }
    }

}
