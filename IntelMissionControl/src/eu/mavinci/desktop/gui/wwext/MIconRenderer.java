/*
/*
* This Source is licenced under the NASA OPEN SOURCE AGREEMENT VERSION 1.3
*
* Copyright (C) 2011 United States Government as represented by the Administrator of the
* National Aeronautics and Space Administration.
* All Rights Reserved.
*
* Modifications by MAVinci GmbH, Germany (C) 2009-2016: Using parts of NASA code to build a icon renderer which centers icons on coordinates
*
*/
package eu.mavinci.desktop.gui.wwext;

import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.IconRenderer;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.util.Logging;

import com.jogamp.opengl.GL2;
import java.awt.Dimension;
import java.awt.Rectangle;

public class MIconRenderer extends IconRenderer {

    public static int ORIENTATION_C = 0;
    public static int ORIENTATION_T = 1;
    public static int ORIENTATION_B = -1;
    public static int ORIENTATION_R = 2;
    public static int ORIENTATION_L = -2;
    int iconOrientation = ORIENTATION_C;

    public void setOrientation(int iconOrientation) {
        this.iconOrientation = iconOrientation;
    }

    @Override
    protected Vec4 drawIcon(DrawContext dc, OrderedIcon uIcon) {
        if (uIcon.getPoint() == null) {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);

            // Record feedback data for this WWIcon if feedback is enabled.
            if (uIcon.getIcon() != null) {
                this.recordFeedback(dc, uIcon.getIcon(), null, null);
            }

            return null;
        }

        WWIcon icon = uIcon.getIcon();
        if (dc.getView().getFrustumInModelCoordinates().getNear().distanceTo(uIcon.getPoint()) < 0) {
            // Record feedback data for this WWIcon if feedback is enabled.
            this.recordFeedback(dc, icon, uIcon.getPoint(), null);

            return null;
        }

        final Vec4 screenPoint = dc.getView().project(uIcon.getPoint());
        if (screenPoint == null) {
            // Record feedback data for this WWIcon if feedback is enabled.
            this.recordFeedback(dc, icon, uIcon.getPoint(), null);

            return null;
        }

        double pedestalScale;
        double pedestalSpacing;
        if (this.pedestal != null) {
            pedestalScale = this.pedestal.getScale();
            pedestalSpacing = pedestal.getSpacingPixels();
        } else {
            pedestalScale = 0d;
            pedestalSpacing = 0d;
        }

        GL2 gl = dc.getGL().getGL2();

        this.setDepthFunc(dc, uIcon, screenPoint);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        Dimension size = icon.getSize();
        double width = size != null ? size.getWidth() : icon.getImageTexture().getWidth(dc);
        double height = size != null ? size.getHeight() : icon.getImageTexture().getHeight(dc);

        double xRef = 0.5;
        double yRef = 0.5;
        if (icon instanceof IIconOffsetReferenced) {
            IIconOffsetReferenced iicon = (IIconOffsetReferenced)icon;
            xRef = iicon.getXRefpoint();
            yRef = iicon.getYRefpoint();
        }

        double offsetWidth = -width * xRef;
        double offsetHeight = -height * yRef;
        double factorHeight = height / 2;
        double factorWidth = width / 2;
        double heightDelta = this.pedestal != null ? 0 : factorHeight; // expand only above the pedestal

        gl.glTranslated(
            screenPoint.x + offsetWidth, screenPoint.y + offsetHeight + (pedestalScale * height) + pedestalSpacing, 0d);

        if (icon.isHighlighted()) {
            gl.glTranslated(factorWidth, heightDelta, 0);
            gl.glScaled(icon.getHighlightScale(), icon.getHighlightScale(), icon.getHighlightScale());
            gl.glTranslated(offsetWidth, -heightDelta, 0);
        }

        Rectangle rect =
            new Rectangle(
                (int)(screenPoint.x + offsetWidth),
                (int)(screenPoint.y),
                (int)width,
                (int)(height + (pedestalScale * height) + pedestalSpacing));

        if (dc.isPickingMode()) {
            // If in picking mode and pick clipping is enabled, check to see if the icon is within the pick volume.
            if (this.isPickFrustumClippingEnabled() && !dc.getPickFrustums().intersectsAny(rect)) {
                // Record feedback data for this WWIcon if feedback is enabled.
                this.recordFeedback(dc, icon, uIcon.getPoint(), rect);

                return screenPoint;
            } else {
                java.awt.Color color = dc.getUniquePickColor();
                int colorCode = color.getRGB();
                this.pickSupport.addPickableObject(colorCode, icon, uIcon.getPosition(), false);
                gl.glColor3ub((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue());
            }
        }

        if (icon.getBackgroundTexture() != null) {
            this.applyBackground(dc, icon, screenPoint, width, height, pedestalSpacing, pedestalScale);
        }

        if (icon.getImageTexture().bind(dc)) {
            TextureCoords texCoords = icon.getImageTexture().getTexCoords();
            gl.glScaled(width, height, 1d);
            dc.drawUnitQuad(texCoords);
        }

        if (this.pedestal != null && this.pedestal.getImageTexture() != null) {
            gl.glLoadIdentity();
            gl.glTranslated(screenPoint.x - (pedestalScale * (factorWidth)), screenPoint.y, 0d);
            gl.glScaled(width * pedestalScale, height * pedestalScale, 1d);

            if (this.pedestal.getImageTexture().bind(dc)) {
                TextureCoords texCoords = this.pedestal.getImageTexture().getTexCoords();
                dc.drawUnitQuad(texCoords);
            }
        }

        // Record feedback data for this WWIcon if feedback is enabled.
        this.recordFeedback(dc, icon, uIcon.getPoint(), rect);

        return screenPoint;
    }

}
