/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.sun;

import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.jogamp.opengl.GL2;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

public class WWSunModel implements Renderable {

    private static final double distanceInEarthRadius = 2;

    private static final Globe globe = StaticInjector.getInstance(IWWGlobes.class).getDefaultGlobe();;
    private static final double distance = globe.getDiameter() / 2 * distanceInEarthRadius;

    @Override
    public void render(DrawContext dc) {
        // System.out.println("globe diameter" + distance);
        Vec4 sun = sunDirection.multiply3(distance);
        GL2 gl = dc.getGL().getGL2();

        gl.glPushAttrib(
            GL2.GL_TEXTURE_BIT
                | GL2.GL_COLOR_BUFFER_BIT
                | GL2.GL_DEPTH_BUFFER_BIT
                | GL2.GL_HINT_BIT
                | GL2.GL_POLYGON_BIT
                | GL2.GL_ENABLE_BIT
                | GL2.GL_CURRENT_BIT
                | GL2.GL_LIGHTING_BIT
                | GL2.GL_TRANSFORM_BIT);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();

        float[] result = new float[1];
        gl.glGetFloatv(GL2.GL_POINT_SIZE, result, 0);
        float oldSize = result[0];
        gl.glPointSize(10.f); // 0.5-10 allowed

        gl.glDisable(GL2.GL_CULL_FACE);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_ALWAYS); // GL_LEQUAL);

        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);

        // float[] res = new float[2];
        // gl.glGetFloatv(GL2.GL_SMOOTH_POINT_SIZE_RANGE, res,0);
        // System.out.println("GL_SMOOTH_POINT_SIZE_RANGE: "+res[0]+"-"+res[1]);
        // TODO

        // gl.glRotated(sunPos.longitude.degrees, 0,1,0);
        // gl.glRotated(-sunPos.latitude.degrees, 1,0,0);
        // dc.getView().pushReferenceCenter(dc, dc.getGlobe().getCenter());
        gl.glEnable(GL2.GL_COLOR);

        gl.glBegin(GL2.GL_POINTS);
        gl.glColor3f(1, 1, 0);
        gl.glVertex3d(sun.x, sun.y, sun.z);
        gl.glEnd();
        // dc.getView().popReferenceCenter(dc);

        gl.glDisable(GL2.GL_POINT_SMOOTH);
        gl.glPointSize(oldSize);

        gl.glMatrixMode(GL_MODELVIEW);
        gl.glPopMatrix();

        gl.glPopAttrib();
    }

    Vec4 sunDirection = Vec4.UNIT_X;

    public void setSunDirection(Vec4 direction) {
        this.sunDirection = direction;
    }

}
