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

import com.intel.missioncontrol.PublishSource;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.IconRenderer;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.Logging;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;

@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class IconRendererCentered extends IconRenderer {

    public enum ReferencePoint {
        CENTER_CENTER,
        BOTTOM_CENTER;
    }

    boolean renderAlwaysOverGround = true;
    ReferencePoint referencePoint = ReferencePoint.CENTER_CENTER;

    public void setRenderAlwaysOverGround(boolean renderAlwaysOverGround) {
        this.renderAlwaysOverGround = renderAlwaysOverGround;
    }

    public void setReferencePoint(ReferencePoint referencePoint) {
        this.referencePoint = referencePoint;
    }

    protected void drawMany(DrawContext dc, Iterable<? extends WWIcon> icons, Layer layer) {
        if (dc == null) {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getVisibleSector() == null) return;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        //noinspection RedundantIfStatement
        if (geos == null) return;

        if (icons == null) {
            String msg = Logging.getMessage("nullValue.IconIterator");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Iterator<? extends WWIcon> iterator = icons.iterator();

        if (!iterator.hasNext()) return;

        double horizon = dc.getView().getHorizonDistance();

        while (iterator.hasNext()) {
            WWIcon icon = iterator.next();
            if (!isIconValid(icon, true)) {
                // Record feedback data for this WWIcon if feedback is enabled.
                if (icon != null) this.recordFeedback(dc, icon, null, null);

                continue;
            }

            if (!icon.isVisible()) {
                // Record feedback data for this WWIcon if feedback is enabled.
                this.recordFeedback(dc, icon, null, null);

                continue;
            }

            // Determine Cartesian position from the surface geometry if the icon is near the surface,
            // otherwise draw it from the globe.
            Position pos = icon.getPosition();
            Vec4 iconPoint = null;
            if (dc.is2DGlobe()) {
                iconPoint = dc.getGlobe().computePointFromLocation(pos);
            } else if (pos.getElevation() < dc.getGlobe().getMaxElevation() && !this.isAlwaysUseAbsoluteElevation()) {
                iconPoint = dc.getSurfaceGeometry().getSurfacePoint(icon.getPosition());
            } else if (this.renderAlwaysOverGround) {
                Angle lat = pos.getLatitude();
                Angle lon = pos.getLongitude();
                double elevationTerrain = dc.getGlobe().getElevation(lat, lon);
                iconPoint =
                    dc.getGlobe().computePointFromPosition(lat, lon, Math.max(elevationTerrain + 0.01, pos.elevation));
            }

            if (iconPoint == null) {
                Angle lat = pos.getLatitude();
                Angle lon = pos.getLongitude();
                double elevation = pos.getElevation();
                if (!this.isAlwaysUseAbsoluteElevation()) elevation += dc.getGlobe().getElevation(lat, lon);
                iconPoint = dc.getGlobe().computePointFromPosition(lat, lon, elevation);
            }

            double eyeDistance = icon.isAlwaysOnTop() ? 0 : dc.getView().getEyePoint().distanceTo3(iconPoint);

            if (this.isHorizonClippingEnabled() && !dc.is2DGlobe() && eyeDistance > horizon) {
                // Record feedback data for this WWIcon if feedback is enabled.
                this.recordFeedback(dc, icon, iconPoint, null);

                continue; // don't render horizon-clipped icons
            }

            // If enabled, eliminate icons outside the view volume. Primarily used to control icon visibility beyond
            // the view volume's far clipping plane.
            if (this.isViewClippingEnabled() && !dc.getView().getFrustumInModelCoordinates().contains(iconPoint)) {
                // Record feedback data for this WWIcon if feedback is enabled.
                this.recordFeedback(dc, icon, iconPoint, null);

                continue; // don't render frustum-clipped icons
            }

            // The icons aren't drawn here, but added to the ordered queue to be drawn back-to-front.
            dc.addOrderedRenderable(new OrderedIcon(icon, iconPoint, layer, eyeDistance, horizon));

            if (icon.isShowToolTip()) this.addToolTip(dc, icon, iconPoint);
        }
    }

    @Override
    protected Vec4 drawIcon(DrawContext dc, OrderedIcon uIcon) {
        if (referencePoint == ReferencePoint.BOTTOM_CENTER) {
            return super.drawIcon(dc, uIcon);
        }

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
        gl.glTranslated(
            screenPoint.x - width / 2, screenPoint.y - height / 2 + (pedestalScale * height) + pedestalSpacing, 0d);

        if (icon.isHighlighted()) {
            double heightDelta = this.pedestal != null ? 0 : height / 2; // expand only above the pedestal
            gl.glTranslated(width / 2, heightDelta, 0);
            gl.glScaled(icon.getHighlightScale(), icon.getHighlightScale(), icon.getHighlightScale());
            gl.glTranslated(-width / 2, -heightDelta, 0);
        }

        Rectangle rect =
            new Rectangle(
                (int)(screenPoint.x - width / 2),
                (int)(screenPoint.y - height / 2),
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
            gl.glTranslated(screenPoint.x - (pedestalScale * (width / 2)), screenPoint.y, 0d);
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
