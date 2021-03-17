/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import com.jogamp.opengl.GL;

@Deprecated
public class GlobeBaloonAnnotation extends GlobeAnnotation {
    public GlobeBaloonAnnotation(String text, Position position) {
        super(text, position);
    }

    @Override
    protected void doRenderNow(DrawContext dc) {
        GL gl = dc.getGL();
        if (gl.glIsEnabled(GL.GL_DEPTH_TEST)) {
            gl.glDisable(GL.GL_DEPTH_TEST);
            superRender(dc);
            gl.glEnable(GL.GL_DEPTH_TEST);
        } else {
            superRender(dc);
        }
    }

    private void superRender(DrawContext dc) {
        try {
            super.doRenderNow(dc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
