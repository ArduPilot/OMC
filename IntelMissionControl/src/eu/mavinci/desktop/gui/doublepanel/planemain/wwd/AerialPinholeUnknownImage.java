/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.desktop.gui.wwext.IWWRenderableWithUserData;
import eu.mavinci.desktop.gui.wwext.SurfacePolygonWithUserDataSlave;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.awt.Color;
import java.util.ArrayList;

public class AerialPinholeUnknownImage extends AerialPinholeImage implements Renderable, PreRenderable {

    SurfacePolygonWithUserDataSlave renderable = null;
    // SurfacePolygonWithUserDataSlave renderablePoint = null;

    Color color = AerialPinholeImageLayer.IMG_LAYER_DEF_COL;

    public long timestamp = System.currentTimeMillis();

    public void setColor(Color color) {
        if (this.color.equals(color)) {
            return;
        }

        this.color = color;
        changeLine();
        // fireImageLayerChanged(); //TODO Fire change
    }

    public Color getColor() {
        return color;
    }

    String name;

    public AerialPinholeUnknownImage(
            PhotoData photo, ComputeCornerData.IAerialPinholeImageContext context) { // / , double startPosAltitude) {
        super(photo, context);
        name = "" + photo.number;
    }

    // ArrayList<Position> posList;

    @Override
    protected void setCorners(ComputeCornerData computeCornerData) {
        super.setCorners(computeCornerData);
        changeLine();
    }

    private void changeLine() {
        renderable = null; // layer.removeAllRenderables();
        ComputeCornerData computeCornerData = this.getComputeCornerData();
        if (computeCornerData == null) {
            return;
        }

        ArrayList<LatLon> groundProjectedCorners = computeCornerData.getGroundProjectedCorners();

        if (groundProjectedCorners == null) {
            return;
        }
        // if (posList == null) return;
        // layer.addRenderable(makeLine(corners,getColor(),this));
        renderable = makeLine(groundProjectedCorners, getColor(), this);
        resetMarker();
    }

    @Override
    public void render(DrawContext dc) {
        if (shouldRender()) {
            // System.out.println("isVis:"+isVisible + " isInit:"+isInit + " AREA:" +
            if (renderable != null) {
                renderable.render(dc);
            }

            if (markerLayer != null) {
                markerLayer.render(dc);
            }
        }
    }

    @Override
    public void preRenderDetail(DrawContext dc) {
        if (shouldRender()) {
            if (renderable != null) {
                renderable.preRender(dc);
            }

            if (markerLayer != null) {
                markerLayer.preRender(dc);
            }
        }
    }

    // private final Color border = new Color(0,0,255,64);
    // private final ShapeAttributes inside = new BasicShapeAttributes();

    public static SurfacePolygonWithUserDataSlave makeLine(
            Iterable<? extends LatLon> pos, Color col, IWWRenderableWithUserData master) {
        return makeLine(pos, col, 2.5, master);
    }

    public static SurfacePolygonWithUserDataSlave makeLine(
            Iterable<? extends LatLon> pos, Color col, double width, IWWRenderableWithUserData master) {
        SurfacePolygonWithUserDataSlave line = new SurfacePolygonWithUserDataSlave(pos, master);
        line.setSelectable(false);
        // line.setFollowTerrain(true);
        // line.setClosed(true);
        // line.setPathType(Polyline.RHUMB_LINE);
        // line.setLineWidth(15);
        // line.setM(getColor());
        ShapeAttributes atr = new BasicShapeAttributes();
        // atr.setDrawInterior(true);
        // atr.setInteriorMaterial(new Material(getColor()));
        // atr.setInteriorOpacity(0.15);
        atr.setDrawInterior(false);
        atr.setDrawOutline(true);
        atr.setOutlineWidth(width);
        atr.setOutlineMaterial(new Material(col));
        // atr.setOutlineOpacity(1);
        // atr.set
        line.setAttributes(atr);
        return line;
    }

    @Override
    public String getName() {
        // return Integer.toString(photo.number) + super.getName();
        return name + super.getName();
    }

}
