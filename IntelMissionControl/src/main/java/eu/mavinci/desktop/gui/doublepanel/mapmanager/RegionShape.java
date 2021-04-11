/*
 * This Source is licenced under the NASA OPEN SOURCE AGREEMENT VERSION 1.3
 *
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 *
 * Modifications by MAVinci GmbH, Germany (C) 2009-2016: this class wasn't public visible
 *
 */
package eu.mavinci.desktop.gui.doublepanel.mapmanager;

import com.intel.missioncontrol.PublishSource;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceSector;
import gov.nasa.worldwind.util.Logging;

import java.awt.Color;

@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class RegionShape extends SurfaceSector {
    private boolean resizeable = false;
    private Position startPosition;
    private Position endPosition;
    private SurfaceSector borderShape;

    public RegionShape(Sector sector) {
        super(sector);
        if (sector != null && !sector.equals(Sector.EMPTY_SECTOR)) {
            setStartPosition(new Position(sector.getMinLatitude(), sector.getMinLongitude(), 0));
            setEndPosition(new Position(sector.getMaxLatitude(), sector.getMaxLongitude(), 0));
        }

        // Create the default border shape.
        this.setBorder(new SurfaceSector(sector));

        // The edges of the region shape should be constant lines of latitude
        // and longitude.
        this.setPathType(AVKey.LINEAR);
        this.getBorder().setPathType(AVKey.LINEAR);

        // Setup default interior rendering attributes. Note that the interior
        // rendering attributes are
        // configured so only the SurfaceSector's interior is rendered.
        ShapeAttributes interiorAttrs = new BasicShapeAttributes();
        interiorAttrs.setDrawOutline(false);
        interiorAttrs.setInteriorMaterial(Material.WHITE);
        interiorAttrs.setInteriorOpacity(0.1);
        this.setAttributes(interiorAttrs);

        // Setup default border rendering attributes. Note that the border
        // rendering attributes are configured
        // so that only the SurfaceSector's outline is rendered.
        ShapeAttributes borderAttrs = new BasicShapeAttributes();
        borderAttrs.setDrawInterior(false);
        borderAttrs.setOutlineMaterial(Material.RED);
        borderAttrs.setOutlineOpacity(0.7);
        borderAttrs.setOutlineWidth(3);
        this.getBorder().setAttributes(borderAttrs);
    }

    public Color getInteriorColor() {
        return this.getAttributes().getInteriorMaterial().getDiffuse();
    }

    public void setInteriorColor(Color color) {
        ShapeAttributes attr = this.getAttributes();
        attr.setInteriorMaterial(new Material(color));
        this.setAttributes(attr);
    }

    public Color getBorderColor() {
        return this.getBorder().getAttributes().getOutlineMaterial().getDiffuse();
    }

    public void setBorderColor(Color color) {
        ShapeAttributes attr = this.getBorder().getAttributes();
        attr.setOutlineMaterial(new Material(color));
        this.getBorder().setAttributes(attr);
    }

    public double getInteriorOpacity() {
        return this.getAttributes().getInteriorOpacity();
    }

    public void setInteriorOpacity(double opacity) {
        ShapeAttributes attr = this.getAttributes();
        attr.setInteriorOpacity(opacity);
        this.setAttributes(attr);
    }

    public double getBorderOpacity() {
        return this.getBorder().getAttributes().getOutlineOpacity();
    }

    public void setBorderOpacity(double opacity) {
        ShapeAttributes attr = this.getBorder().getAttributes();
        attr.setOutlineOpacity(opacity);
        this.getBorder().setAttributes(attr);
    }

    public double getBorderWidth() {
        return this.getBorder().getAttributes().getOutlineWidth();
    }

    public void setBorderWidth(double width) {
        ShapeAttributes attr = this.getBorder().getAttributes();
        attr.setOutlineWidth(width);
        this.getBorder().setAttributes(attr);
    }

    public void setSector(Sector sector) {
        super.setSector(sector);
        this.getBorder().setSector(sector);
    }

    protected boolean isResizeable() {
        return resizeable;
    }

    protected void setResizeable(boolean resizeable) {
        this.resizeable = resizeable;
    }

    protected Position getStartPosition() {
        return startPosition;
    }

    protected void setStartPosition(Position startPosition) {
        this.startPosition = startPosition;
    }

    protected Position getEndPosition() {
        return endPosition;
    }

    protected void setEndPosition(Position endPosition) {
        this.endPosition = endPosition;
    }

    protected SurfaceSector getBorder() {
        return borderShape;
    }

    protected void setBorder(SurfaceSector shape) {
        if (shape == null) {
            String message = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.borderShape = shape;
    }

    protected boolean hasSelection() {
        return getStartPosition() != null && getEndPosition() != null;
    }

    protected void clear() {
        this.setStartPosition(null);
        this.setEndPosition(null);
        this.setSector(Sector.EMPTY_SECTOR);
    }

    public void preRender(DrawContext dc) {
        // This is called twice: once during normal rendering, then again during
        // ordered surface rendering. During
        // normal renering we pre-render both the interior and border shapes.
        // During ordered surface rendering, both
        // shapes are already added to the DrawContext and both will be
        // individually processed. Therefore we just
        // call our superclass behavior
        if (dc.isOrderedRenderingMode()) {
            super.preRender(dc);
            return;
        }

        this.doPreRender(dc);
    }

    @Override
    public void render(DrawContext dc) {
        if (dc.isPickingMode() && this.isResizeable()) {
            return;
        }

        // This is called twice: once during normal rendering, then again during
        // ordered surface rendering. During
        // normal renering we render both the interior and border shapes. During
        // ordered surface rendering, both
        // shapes are already added to the DrawContext and both will be
        // individually processed. Therefore we just
        // call our superclass behavior
        if (dc.isOrderedRenderingMode()) {
            super.render(dc);
            return;
        }

        if (!this.isResizeable()) {
            if (this.hasSelection()) {
                this.doRender(dc);
            }

            return;
        }

        PickedObjectList pos = dc.getPickedObjects();
        PickedObject terrainObject = pos != null ? pos.getTerrainObject() : null;

        if (terrainObject == null) {
            return;
        }

        if (this.getStartPosition() != null) {
            Position end = terrainObject.getPosition();
            if (!this.getStartPosition().equals(end)) {
                this.setEndPosition(end);
                this.setSector(Sector.boundingSector(this.getStartPosition(), this.getEndPosition()));
                this.doRender(dc);
            }
        } else {
            this.setStartPosition(pos.getTerrainObject().getPosition());
        }
    }

    protected void doPreRender(DrawContext dc) {
        this.doPreRenderInterior(dc);
        this.doPreRenderBorder(dc);
    }

    protected void doPreRenderInterior(DrawContext dc) {
        super.preRender(dc);
    }

    protected void doPreRenderBorder(DrawContext dc) {
        this.getBorder().preRender(dc);
    }

    protected void doRender(DrawContext dc) {
        this.doRenderInterior(dc);
        this.doRenderBorder(dc);
    }

    protected void doRenderInterior(DrawContext dc) {
        super.render(dc);
    }

    protected void doRenderBorder(DrawContext dc) {
        this.getBorder().render(dc);
    }
}
