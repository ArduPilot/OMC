/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers;

import com.intel.missioncontrol.helper.FontHelper;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import eu.mavinci.core.plane.CAirplaneCache;
import eu.mavinci.desktop.gui.wwext.UserFacingTextLayer;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.UserFacingText;
import gov.nasa.worldwind.util.RayCastingSupport;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

public class DistanceRenderable implements Renderable, MouseListener, MouseMotionListener, PreRenderable {

    private static final Color col = Color.ORANGE;
    private static final Font f = FontHelper.getBaseFont(1.7, Font.BOLD);
    private static final Font fB = FontHelper.getBaseFont(2.5, Font.BOLD);

    private final RenderableLayer parentLayer;
    private final RenderableLayer layer = new RenderableLayer();
    private final UserFacingTextLayer textLayer = new UserFacingTextLayer();
    private final LinkedList<Position> corners = new LinkedList<Position>();
    private final IElevationModel elevationModel;
    private final IWWGlobes globes;
    private final IWWMapView mapView;
    private final WorldWindow wwd;
    private final QuantityFormat quantityFormat;

    boolean isActive = false;

    DistanceRenderable(
            RenderableLayer parentLayer,
            IElevationModel elevationModel,
            IWWGlobes globes,
            IWWMapView mapView,
            IMapController mapController,
            WorldWindow wwd,
            IQuantityStyleProvider quantityStyleProvider) {
        this.quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        quantityFormat.setSignificantDigits(3);
        this.parentLayer = parentLayer;
        this.globes = globes;
        this.mapView = mapView;
        this.elevationModel = elevationModel;
        this.wwd = wwd;

        wwd.getInputHandler().addMouseListener(this);
        wwd.getInputHandler().addMouseMotionListener(this);

        layer.setPickEnabled(false);
        textLayer.setPickEnabled(false);
        mapController.mouseModeProperty().addListener((observable, oldValue, newValue) -> mouseModeChanges(newValue));

        mouseModeChanges(mapController.getMouseMode());

        textLayer.getTextRenderer().setAlwaysOnTop(true);
        textLayer.getTextRenderer().setEffect(AVKey.TEXT_EFFECT_SHADOW);
        parentLayer.addRenderable(this);
    }

    public class OverlayText extends UserFacingText {
        public OverlayText(CharSequence text, Position textPosition) {
            super(text, fixTextElevation(textPosition));
            setPriority(1e16);
            setFont(f);
            setBackgroundColor(Color.BLACK);
        }

        @Override
        public void setPosition(Position position) {
            super.setPosition(fixTextElevation(position));
        }

    }

    @Override
    public void render(DrawContext dc) {
        if (!isActive) {
            return;
        }

        layer.render(dc);
        textLayer.render(dc);
    }

    @Override
    public void preRender(DrawContext dc) {
        if (!isActive) {
            return;
        }

        layer.preRender(dc);
        textLayer.preRender(dc);
    }

    private void mouseModeChanges(InputMode newMouseMode) {
        boolean willBeActive = (newMouseMode == InputMode.ADD_MEASURMENT_POINTS);
        if (isActive == willBeActive) {
            return;
        }

        isActive = willBeActive;
        if (!isActive) {
            layer.removeAllRenderables();
            textLayer.clear();
            parentLayer.firePropertyChange(AVKey.LAYER, null, parentLayer);
            corners.clear();
        }
    }

    public Position fixTextElevation(Position p) {
        return new Position(p, IElevationModel.MIN_LEVEL_OVER_GROUND);
    }

    protected void mouse(MouseEvent e) {
        if (!isActive) {
            return;
        }

        double x = e.getX();
        double y = e.getY();

        Globe globe = globes.getActiveGlobe();
        Line ray = mapView.computeRayFromScreenPoint(x, y);
        Position p = null;
        if (mapView.isFlatEarth()) {
            p = mapView.computePositionFromScreenPoint(x, y);
        }

        if (wwd != null && p == null && mapView.getEyePosition().getElevation() < globe.getMaxElevation() * 10) {
            // Use ray casting below some altitude
            // Try ray intersection with current terrain geometry
            Intersection[] intersections = wwd.getSceneController().getTerrain().intersect(ray);
            if (intersections != null && intersections.length > 0) {
                p = globe.computePositionFromPoint(intersections[0].getIntersectionPoint());
            } else {
                // Fallback on raycasting using elevation data
                p = RayCastingSupport.intersectRayWithTerrain(globe, ray.getOrigin(), ray.getDirection(), 1000, 2);
            }
        }

        // fallback for groundsticker, or directly for fix altitude dragger
        if (p == null) {
            // Use intersection with sphere at reference altitude.
            Intersection[] inters = globe.intersect(ray, 0);
            if (inters != null) {
                p = globe.computePositionFromPoint(inters[0].getIntersectionPoint());
            }
        }

        if (p == null) {
            return; // dont know where in the world we are
        }

        e.consume();

        // System.out.println("event:"+event.getAllTopPickedObjects().get(0)+ " p="+p);
        if (SwingUtilities.isRightMouseButton(e)) {
            corners.clear();
        }

        if (!SwingUtilities.isLeftMouseButton(e)) {
            if (!corners.isEmpty()) {
                corners.removeLast();
            }
        } else {
            if (corners.isEmpty()) {
                corners.add(p); // dirty hack, to get it initially rendered
            }
        }

        layer.removeAllRenderables();
        textLayer.clear();

        if (!corners.isEmpty()) {
            corners.add(p);

            Polyline line = new Polyline();
            line.setFollowTerrain(true);
            line.setPositions(corners);
            line.setPathType(Polyline.LINEAR);
            line.setLineWidth(5);
            line.setColor(col);
            layer.addRenderable(line);

            if (corners.size() > 2) {
                SurfacePolygon pol = new SurfacePolygon();
                pol.setLocations(corners);
                ShapeAttributes atr = new BasicShapeAttributes();
                atr.setDrawInterior(true);
                atr.setInteriorMaterial(new Material(col));
                atr.setInteriorOpacity(0.25);
                atr.setDrawOutline(false);
                pol.setAttributes(atr);
                pol.setHighlightAttributes(atr);
                layer.addRenderable(pol);

                LatLon latLon = Sector.boundingSector(corners).getCentroid();
                String string =
                    quantityFormat.format(Quantity.of(pol.getArea(globe), Unit.SQUARE_METER), UnitInfo.LOCALIZED_AREA);
                string = string.replaceAll(Pattern.quote("\u202F"), " ");
                OverlayText txtC = new OverlayText(string, elevationModel.getPositionOverGround(latLon));
                textLayer.add(txtC);
            }

            Iterator<Position> it = corners.iterator();
            Position last = null;
            Position next = it.next();
            double sum = 0;
            double dist = 0;
            while (next != null && it.hasNext()) {
                last = next;
                next = it.next();
                dist =
                    CAirplaneCache.distanceMeters(
                        last.latitude.degrees, last.longitude.degrees, next.latitude.degrees, next.longitude.degrees);
                sum += dist;
                if (dist <= 0.5) {
                    continue;
                }

                String string = quantityFormat.format(Quantity.of(dist, Unit.METER), UnitInfo.LOCALIZED_LENGTH);
                string = string.replaceAll(Pattern.quote("\u202F"), " ");
                OverlayText txt =
                    new OverlayText(
                        string, elevationModel.getPositionOverGround(Position.interpolate(0.5, last, next)));
                textLayer.add(txt);
            }

            if (last != null) {
                LinkedList<Position> circPos = new LinkedList<Position>();
                Angle distA = Angle.fromRadians(dist / elevationModel.getRadiusAt(last));
                for (int r = 0; r <= 360; r += 10) {
                    Position pc = new Position(LatLon.greatCircleEndPosition(last, Angle.fromDegrees(r), distA), 0);
                    pc = elevationModel.getPositionOnGround(pc);
                    circPos.add(pc);
                }

                Polyline circ = new Polyline();
                circ.setFollowTerrain(true);
                circ.setPositions(circPos);
                circ.setPathType(Polyline.LINEAR);
                circ.setLineWidth(5);
                circ.setStippleFactor(5);
                circ.setStipplePattern((short)0xAAAA);
                circ.setColor(col);

                layer.addRenderable(circ);
            }

            String string = quantityFormat.format(Quantity.of(sum, Unit.METER), UnitInfo.LOCALIZED_LENGTH);
            string = string.replaceAll(Pattern.quote("\u202F"), " ");
            OverlayText txt = new OverlayText(string, elevationModel.getPositionOverGround(corners.getLast()));
            txt.setFont(fB);
            textLayer.add(txt);
        }

        parentLayer.firePropertyChange(AVKey.LAYER, null, parentLayer);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mouse(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mouse(e);
    }

}
