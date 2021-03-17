/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.flightplan.PhotoLogLineType;
import eu.mavinci.core.flightplan.visitors.ExtractPicAreasVisitor;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDebug;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFlightPlanXML;
import eu.mavinci.core.plane.listeners.IAirplaneListenerOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPhoto;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPositionOrientation;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.desktop.gui.wwext.IWWRenderableWithUserData;
import eu.mavinci.desktop.gui.wwext.SurfacePolygonWithUserDataSlave;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PhotoLogLine;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public class AerialPinholeCurrentView extends AerialPinholeImage
        implements Renderable,
            PreRenderable,
            IAirplaneListenerPosition,
            IAirplaneListenerOrientation,
            IAirplaneListenerPositionOrientation,
            IAirplaneListenerPhoto,
            IAirplaneListenerDebug,
            IAirplaneListenerFlightPlanXML,
            ComputeCornerData.IAerialPinholeImageContext {

    SurfacePolygonWithUserDataSlave renderable = null;
    Color color = AerialPinholeImageLayer.IMG_LAYER_DEF_COL;
    private Polygon frustumPolygon;
    double projectionDistance = 1000;

    public void setColor(Color color) {
        if (this.color.equals(color)) {
            return;
        }

        this.color = color;
        changeLine();
    }

    public Color getColor() {
        return color;
    }

    private IAirplane plane;
    private RenderableLayer layer = new RenderableLayer();

    public AerialPinholeCurrentView(IAirplane plane) {
        super(new PhotoLogLine(new PhotoData(), plane.getHardwareConfiguration()));
        this.context = this;
        this.plane = plane;
        PhotoLogLine line = (PhotoLogLine)getPhotoLogLine();
        line.type = PhotoLogLineType.FLASH;
        line.cameraRollRate = 0;
        line.cameraPitchRate = 0;
        line.cameraYawRate = 0;
        line.groundSpeed_cms = 0;
        line.cameraRoll = 0;
        line.cameraPitch = 0;
        line.cameraYaw = 0;
        line.lat = 0;
        line.lon = 0;
        line.alt = -100000;
        line.fixType = GPSFixType.gpsFix;
        setUserData(plane);

        plane.addListener(this);
        layer.setPickEnabled(false);
        layer.addRenderable(this);
    }

    @Override
    public Double getStartingElevationOverWgs84() {
        try {
            // System.out.println("req. start Elev");
            return plane.getAirplaneCache().getStartElevOverWGS84();
        } catch (AirplaneCacheEmptyException e) {
            // this is by design ok after startup since cache will be maybe empty on first pic
            // System.out.println("no stating elev");
            return null;
        }
    }

    @Override
    public LatLon getStartingPosition() {
        try {
            // System.out.println("req. start Elev");
            return plane.getAirplaneCache().getStartPosBaro();
        } catch (AirplaneCacheEmptyException e) {
            // this is by design ok after startup since cache will be maybe empty on first pic
            // System.out.println("no stating elev");
            return null;
        }
    }

    @SuppressWarnings("checkstyle:abbreviationaswordinname")
    public RenderableLayer getWWLayer() {
        return layer;
    }

    @Override
    protected void setCorners(ComputeCornerData computeCornerData) {
        super.setCorners(computeCornerData);
        changeLine();
        resetFrustum();
    }

    private void changeLine() {
        ComputeCornerData computeCornerData = this.getComputeCornerData();
        renderable = null; // layer.removeAllRenderables();
        if (computeCornerData == null || computeCornerData.getGroundProjectedCorners() == null) {
            return;
        }
        // if (posList == null) return;
        // layer.addRenderable(makeLine(corners,getColor(),this));
        renderable = makeLine(computeCornerData.getGroundProjectedCorners(), getColor(), this);
        renderable.setHighlighted(isHighlighted);
        resetMarker();
    }

    @Override
    public void render(DrawContext dc) {
        if (shouldRender()) {
            if (renderable != null) {
                renderable.render(dc);
            }

            if (frustumPolygon != null) {
                frustumPolygon.render(dc);
            }
        }

        isRendered = true;
    }

    @Override
    public void preRenderDetail(DrawContext dc) {
        if (renderable != null) {
            renderable.preRender(dc);
        }

        if (frustumPolygon != null) {
            frustumPolygon.preRender(dc);
        }
    }

    private void resetFrustum() {
        ComputeCornerData computeCornerData = getComputeCornerData();
        if (computeCornerData == null) {
            frustumPolygon = null;
            return;
        }

        ArrayList<Position> idealCorners = computeCornerData.getIdealCorners();
        Position shiftedPosOnLevel = computeCornerData.getShiftedPosOnLevel();
        if (idealCorners == null || idealCorners.size() != 4 || shiftedPosOnLevel == null) {
            frustumPolygon = null;
            return;
        }

        frustumPolygon =
            new Polygon(
                Arrays.asList(
                    idealCorners.get(0),
                    shiftedPosOnLevel,
                    idealCorners.get(1),
                    shiftedPosOnLevel,
                    idealCorners.get(2),
                    shiftedPosOnLevel,
                    idealCorners.get(3),
                    idealCorners.get(0),
                    idealCorners.get(1),
                    idealCorners.get(2),
                    idealCorners.get(3)));

        ShapeAttributes attr = new BasicShapeAttributes();
        attr.setDrawInterior(false);
        attr.setDrawOutline(true);
        attr.setOutlineWidth(1.0);
        attr.setOutlineMaterial(new Material(Color.WHITE));
        frustumPolygon.setAttributes(attr);
    }

    public static SurfacePolygonWithUserDataSlave makeLine(
            Iterable<? extends LatLon> pos, Color col, IWWRenderableWithUserData master) {
        return makeLine(pos, col, 2.5, master);
    }

    public static SurfacePolygonWithUserDataSlave makeLine(
            Iterable<? extends LatLon> pos, Color col, double width, IWWRenderableWithUserData master) {
        // line.setFollowTerrain(true);
        // line.setClosed(true);
        // line.setPathType(Polyline.RHUMB_LINE);
        // line.setLineWidth(15);
        // line.setM(getColor());
        ShapeAttributes atr = new BasicShapeAttributes();
        // atr.setDrawInterior(true);
        atr.setInteriorMaterial(new Material(col));
        atr.setInteriorOpacity(col.getAlpha() / 255.);
        atr.setDrawInterior(true);
        atr.setDrawOutline(true);
        atr.setOutlineWidth(width);
        atr.setOutlineMaterial(new Material(col));
        // atr.setOutlineOpacity(0.5);
        // atr.set
        SurfacePolygonWithUserDataSlave line = new SurfacePolygonWithUserDataSlave(pos, master);
        line.setAttributes(atr);
        return line;
    }

    private boolean isHighlighted = false;
    private boolean isRendered = false;

    @Override
    public synchronized void recv_positionOrientation(PositionOrientationData po) {
        if (isHighlighted && !isRendered) {
            return;
        }

        CPhotoLogLine line = getPhotoLogLine();
        line.alt = Math.round(po.altitude);
        line.lat = po.lat;
        line.lon = po.lon;
        line.cameraRoll = po.cameraRoll;
        line.cameraPitch = po.cameraPitch;
        line.cameraYaw = po.cameraYaw;
        isHighlighted = false;

        invalidatePhotoLogLine();
    }

    @Override
    public synchronized void recv_orientation(OrientationData o) {
        if (isHighlighted && !isRendered) {
            return;
        }

        if (o.synthezided) {
            return;
        }

        CPhotoLogLine line = getPhotoLogLine();
        line.cameraRoll = o.roll;
        line.cameraPitch = o.pitch;
        line.cameraYaw = o.yaw;
        isHighlighted = false;

        invalidatePhotoLogLine();
    }

    @Override
    public synchronized void recv_position(PositionData p) {
        if (isHighlighted && !isRendered) {
            return;
        }

        if (p.synthezided) {
            return;
        }

        CPhotoLogLine line = getPhotoLogLine();
        line.lat = p.lat;
        line.lon = p.lon;
        line.alt = p.altitude;
        isHighlighted = false;

        invalidatePhotoLogLine();
    }

    @Override
    protected void photoLogLineInvalidated() {
        isRendered = false;
        layer.firePropertyChange(AVKey.LAYER, null, layer);
    }

    @Override
    public synchronized void recv_photo(PhotoData photo) {
        CPhotoLogLine line = getPhotoLogLine();
        line.refreshFromPhotoData(photo);
        isHighlighted = true;
        invalidatePhotoLogLine();
    }

    @Override
    public void recv_debug(DebugData d) {
        if (isHighlighted && !isRendered) {
            return;
        }

        CPhotoLogLine line = getPhotoLogLine();
        line.gps_ellipsoid_cm = d.gps_ellipsoid;
        line.gps_altitude_cm = d.gpsAltitude;
        isHighlighted = false;

        invalidatePhotoLogLine();
    }

    @Override
    public void recv_setFlightPlanXML(String plan, Boolean reentry, Boolean succeed) {
        Flightplan flightPlan = plane.getFPmanager().getOnAirFlightplan();
        ExtractPicAreasVisitor vis = new ExtractPicAreasVisitor();
        vis.startVisit(flightPlan);
        MinMaxPair pair = new MinMaxPair();
        for (CPicArea p : vis.picAreas) {
            pair.update(p.getAlt());
        }

        if (pair.isValid()) {
            projectionDistance = pair.mean();
            // TODO trigger corner recompute
        }
    }

    public double getProjectionDistance() {
        return projectionDistance;
    }

    public IHardwareConfiguration getHardwareConfiguration() {
        return plane.getHardwareConfiguration();
    }

    public gov.nasa.worldwind.geom.Vec4 getRtkOffset() {
        return null;
    }

    @Override
    public double getStartingElevationOverWgs84WithOffset() {
        Double d = getStartingElevationOverWgs84();
        return d == null ? 0 : d.doubleValue();
    }

    @Override
    public double getElevationOffset() {
        return 0;
    }

}
