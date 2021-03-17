/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.wwjextensions;

import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.util.Random;

import net.java.joglutils.model.examples.DisplayListRenderer;

public class Test3D extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

		private static final long serialVersionUID = -6958319546011216570L;

		public AppFrame() {
            super(true, true, false);
            try {
                RenderableLayer layer = new RenderableLayer();
                layer.setName("movable 3D object");
                Random generator = new Random();
                for (int i = 0; i < 100; i++) {
                    layer.addRenderable(
                            new Movable3DModel(DisplayListRenderer.getInstance(), "3dmodels/spaceship.3ds",
                            new Position(Angle.fromDegrees(generator.nextInt() % 90),
                            Angle.fromDegrees(generator.nextInt() % 180), 200000),
                            300000));
                }
                this.getWwd().addSelectListener(new GenericDraggerAdjuster((WorldWindowGLCanvas) this.getWwd()));
                insertBeforeCompass(this.getWwd(), layer);
                this.getLayerPanel().update(this.getWwd());                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ApplicationTemplate.start("Movable 3D Model Layer Test", AppFrame.class);
    }
}

