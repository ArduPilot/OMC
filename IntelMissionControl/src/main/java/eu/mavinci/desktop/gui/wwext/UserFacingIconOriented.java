/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;
import gov.nasa.worldwind.util.OGLUtil;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import net.java.joglutils.model.geometry.Model;

public class UserFacingIconOriented extends UserFacingIconWithUserData implements Renderable, OrderedRenderable {

    private final Position posWgs84;

    public UserFacingIconOriented(BufferedImage icon, Position pos, Object userDataObject) {
        super(icon, pos, userDataObject);
        posWgs84 = StaticInjector.getInstance(IElevationModel.class).getPositionOverGround(pos);
    }

    public UserFacingIconOriented(Object icon, Position pos, Object userDataObject) {
        super(icon, pos, userDataObject);
        posWgs84 = StaticInjector.getInstance(IElevationModel.class).getPositionOverGround(pos);
    }

    public UserFacingIconOriented(String iconPath, Position pos, Object userDataObject) {
        super(iconPath, pos, userDataObject);
        posWgs84 = StaticInjector.getInstance(IElevationModel.class).getPositionOverGround(pos);
    }

    @Override
    public Position getPosition() {
        return posWgs84;
    }

    double roll = 0; // deg
    double pitch = 0; // deg
    double yaw = 0; // deg

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public void render(DrawContext dc) {
        if (getPosition() == null) {
            return;
        }

        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        try {
            beginDraw(dc);
            draw(dc, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endDraw(dc);
        }
    }

    protected Layer pickLayer;
    Vec4 point;

    protected void draw(DrawContext dc, Model model) {
        if (dc.isOrderedRenderingMode()) {
            this.drawOrderedRenderable(dc);
            return;
        }

        point = dc.getGlobe().computePointFromPosition(getPosition());
        this.eyeDistance = point.distanceTo3(dc.getView().getEyePoint());

        if (dc.getView().getFrustumInModelCoordinates().contains(point)) {
            if (dc.isPickingMode()) {
                this.pickLayer = dc.getCurrentLayer();
            }

            dc.addOrderedRenderable(this); // add the ordered renderable
        }
    }

    void drawOrderedRenderable(DrawContext dc) {
        double d = point.distanceTo3(dc.getView().getEyePoint());
        double localSize = dc.getView().computePixelSizeAtDistance(d);

        if (dc.getView().getFrustumInModelCoordinates().getNear().distanceTo(point) < 0) {
            return;
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!getImageTexture().bind(dc)) {
            return;
        }

        // this.setDepthFunc(dc, uIcon, screenPoint);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        dc.getView().pushReferenceCenter(dc, point);
        if (!(dc.getView().getGlobe() instanceof FlatGlobe)) {
            gl.glRotated(getPosition().getLongitude().degrees, 0, 1, 0);
            gl.glRotated(-getPosition().getLatitude().degrees, 1, 0, 0);
        }

        // orientation of icon
        gl.glRotated(getYaw(), 0, 0, -1);
        gl.glRotated(getPitch(), 1, 0, 0);
        gl.glRotated(getRoll(), 0, 1, 0);

        Dimension size = getSize();
        double width = size != null ? size.getWidth() : getImageTexture().getWidth(dc);
        double height = size != null ? size.getHeight() : getImageTexture().getHeight(dc);
        // System.out.println("with=" + width);
        // System.out.println("height=" + height);
        // localSize *= width;
        gl.glScaled(localSize, localSize, localSize);

        if (isHighlighted()) {
            gl.glScaled(getHighlightScale(), getHighlightScale(), getHighlightScale());
        }

        gl.glScaled(width, height, 1d); // since texture will be painted on unit square
        gl.glTranslated(-.5, -.5, 0); // centering icon

        if (dc.isPickingMode()) {
            // If in picking mode and pick clipping is enabled, check to see if the icon is within the pick volume.
            java.awt.Color color = dc.getUniquePickColor();
            int colorCode = color.getRGB();
            this.pickSupport.addPickableObject(colorCode, this, getPosition(), false);
            gl.glColor3ub((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue());
        }

        TextureCoords texCoords = getImageTexture().getTexCoords();
        dc.drawUnitQuad(texCoords);

        dc.getView().popReferenceCenter(dc);
    }

    // puts opengl in the correct state for this layer
    protected void beginDraw(DrawContext dc) {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        this.oglStackHandler.clear();

        int attributeMask =
            GL2.GL_DEPTH_BUFFER_BIT // for depth test, depth mask and depth func
                | GL2.GL_TRANSFORM_BIT // for modelview and perspective
                | GL2.GL_VIEWPORT_BIT // for depth range
                | GL2.GL_CURRENT_BIT // for current color
                | GL2.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
                | GL2.GL_DEPTH_BUFFER_BIT // for depth func
                | GL2.GL_ENABLE_BIT; // for enable/disable changes
        this.oglStackHandler.pushAttrib(gl, attributeMask);

        // Apply the depth buffer but don't change it.
        if ((!dc.isDeepPickingEnabled())) {
            gl.glEnable(GL.GL_DEPTH_TEST);
        }

        gl.glDepthMask(false);

        // Suppress any fully transparent image pixels
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL2.GL_GREATER, 0.001f);

        // Load a parallel projection with dimensions (viewportWidth, viewportHeight)
        // this.oglStackHandler.pushProjectionIdentity(gl);
        // gl.glOrtho(0d, dc.getView().getViewport().width, 0d, dc.getView().getViewport().height, -1d, 1d);

        this.oglStackHandler.pushModelview(gl);
        this.oglStackHandler.pushTexture(gl);

        if (dc.isPickingMode()) {
            this.pickSupport.beginPicking(dc);

            // Set up to replace the non-transparent texture colors with the single pick color.
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_PREVIOUS);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_REPLACE);
        } else {
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    // resets opengl state
    protected void endDraw(DrawContext dc) {
        if (dc.isPickingMode()) {
            this.pickSupport.endPicking(dc);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (dc.isPickingMode()) {
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, OGLUtil.DEFAULT_TEX_ENV_MODE);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, OGLUtil.DEFAULT_SRC0_RGB);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, OGLUtil.DEFAULT_COMBINE_RGB);
        }

        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

        this.oglStackHandler.pop(gl);
    }

    double eyeDistance;

    @Override
    public double getDistanceFromEye() {
        return eyeDistance;
    }

    protected OGLStackHandler oglStackHandler = new OGLStackHandler();

    protected PickSupport pickSupport = new PickSupport();

    @Override
    public void pick(DrawContext dc, Point pickPoint) {
        this.pickSupport.clearPickList();
        try {
            this.pickSupport.beginPicking(dc);
            this.render(dc);
        } finally {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, this.pickLayer);
            this.pickSupport.clearPickList();
        }
    }

}
