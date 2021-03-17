/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.worldwind;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.render.Balloon;
import gov.nasa.worldwind.render.BalloonAttributes;
import gov.nasa.worldwind.render.BasicBalloonAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.ScreenAnnotationBalloon;
import gov.nasa.worldwind.render.Size;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;

public class MBalloon {

    private RenderableLayer layer;
    Balloon balloon;

    public void calculatingFlightPlanBalloon(WorldWindow worldWindow) {
        int toolbarPanelVgap = 10; // ToolBarPanel.TOOLBAR_VGAP;
        Vec4 v = worldWindow.getView().project(worldWindow.getModel().getGlobe().getCenter());
        balloon =
            new ScreenAnnotationBalloon(
                "Calculating Flight Plan...",
                new Point((int)v.x - (toolbarPanelVgap / 2), (int)v.y + ((int)v.y * 95 / 100)));

        BalloonAttributes attrs = new BasicBalloonAttributes();
        attrs.setSize(Size.fromPixels(300, 40));
        attrs.setOffset(new Offset(0d, 0d, AVKey.PIXELS, AVKey.PIXELS));
        attrs.setInsets(new Insets(10, 10, 10, 10));
        attrs.setLeaderShape(AVKey.SHAPE_RECTANGLE);
        attrs.setTextColor(Color.WHITE);
        attrs.setInteriorMaterial(Material.BLUE);
        // attrs.setInteriorOpacity(0.6);
        attrs.setOutlineMaterial(Material.BLUE);
        attrs.setBalloonShape(AVKey.SHAPE_RECTANGLE);

        balloon.setAttributes(attrs);

        this.layer = new RenderableLayer();
        this.layer.setName("MBalloon");
        this.layer.addRenderable(balloon);
        insertBeforePlacenames(worldWindow, this.layer);
    }

    public static void insertBeforePlacenames(WorldWindow wwd, Layer layer) {
        // Insert the layer into the layer list just before the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof PlaceNameLayer) {
                compassPosition = layers.indexOf(l);
            }
        }

        layers.add(compassPosition, layer);
    }

    public void removeCalculatingFlightPlanBalloon() {
        this.layer.removeRenderable(balloon);
    }

}
