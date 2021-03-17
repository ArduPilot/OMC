/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.geom.BilinearInterpolator;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FBOTexture;
import gov.nasa.worldwind.render.WWTexture;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.jogamp.opengl.GL2;
import java.util.List;

public class ProjectiveTexture extends FBOTexture {

    public ProjectiveTexture(WWTexture imageSource, Sector sector, List<LatLon> corners) {
        super(imageSource, sector, corners);
    }

    protected void drawQuad(DrawContext dc, Matrix geoToCartesian, int slices, int stacks) {
        Vec4 ll = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(0));
        Vec4 lr = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(1));
        Vec4 ur = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(2));
        Vec4 ul = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(3));
        BilinearInterpolator interp = new BilinearInterpolator(ll, lr, ur, ul);

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2
        // compatibility.

        gl.glBegin(GL2.GL_TRIANGLE_STRIP);
        try {
            this.drawQuad(dc, interp, slices, stacks);
        } finally {
            gl.glEnd();
        }
    }

    /*
     * See this page for the explanation http://www.reedbeta.com/blog/2012/05/26/quadrilateral-interpolation-part- 1/
     */
    protected double[] quad2Qcoord(double[] c1, double[] c2, double[] c3, double[] c4) {
        Vector2D vec1 = new Vector2D(c1[0], c1[1]);

        Vector2D vec2 = new Vector2D(c2[0], c2[1]);

        Vector2D vec3 = new Vector2D(c3[0], c3[1]);

        Vector2D vec4 = new Vector2D(c4[0], c4[1]);

        Line diag1 = new Line(vec1, vec4);
        Line diag2 = new Line(vec2, vec3);

        Vector2D center = diag1.intersection(diag2);

        double d1 = vec1.distance(center);
        double d2 = vec2.distance(center);
        double d3 = vec3.distance(center);
        double d4 = vec4.distance(center);

        double q1 = (d1 + d4) / d4;
        double q2 = (d2 + d3) / d3;
        double q3 = (d1 + d4) / d1;
        double q4 = (d3 + d2) / d2;

        return new double[] {q1, q2, q4, q3};
    }

    protected void drawQuad(DrawContext dc, BilinearInterpolator interp, int slices, int stacks) {
        double[] c1 = new double[4];
        double[] c2 = new double[4];
        double[] c3 = new double[4];
        double[] c4 = new double[4];

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2
        // compatibility.

        interp.interpolate(0, 0, c1);
        interp.interpolate(0, 1, c2);
        interp.interpolate(1, 0, c3);
        interp.interpolate(1, 1, c4);

        double[] qs = quad2Qcoord(c1, c2, c3, c4);

        gl.glTexCoord4d(0, 0, 0, qs[0]);
        gl.glVertex3dv(c1, 0);

        gl.glTexCoord4d(0, qs[1], 0, qs[1]);
        gl.glVertex3dv(c2, 0);

        gl.glTexCoord4d(qs[2], 0, 0, qs[2]);
        gl.glVertex3dv(c3, 0);

        gl.glTexCoord4d(qs[3], qs[3], 0, qs[3]);
        gl.glVertex3dv(c4, 0);
    }

}
