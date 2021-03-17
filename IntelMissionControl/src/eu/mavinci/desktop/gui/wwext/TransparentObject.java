/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.flightplan.computation.LocalTransformationProvider;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.Renderable;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.flightplan.computation.LocalTransformationProvider;
import eu.mavinci.plane.IAirplane;

import java.awt.Color;

public class TransparentObject implements IAirplaneListenerPosition, Renderable {

    private static final double SECONDS_VISIBLE =
        20; // all gates that are in radius of 20 seconds flight should be visible,
    // with degrading opacity, everything that is futher - not

    private static final double SECONDS_FULL_VISIBLE = 5;

    /*
     * Continuous function return opacity to draw some point in "distance" meters from the plane maps distance
     * [0.2*(dist_reachable_in_20_sec) : (dist_reachable_in_20_sec)] - [1 : 0] if distance > (dist_reachable_in_20_sec) returns 0 if
     * distance < 0.2*(dist_reachable_in_20_sec) returns 1 distance - distance to point in meters opacity of the point [0 : 1]
     */
    public static float getOpacity(double distance, double planeSpeed) {
        double visibleLength = SECONDS_VISIBLE * planeSpeed;
        double fullVisibleLength = SECONDS_FULL_VISIBLE * planeSpeed;

        if (distance >= visibleLength) {
            return 0;
        }

        float opacity = (float)(fullVisibleLength / distance);
        return opacity > 1 ? 1 : opacity;
    }

    private volatile float opacity;

    protected final Renderable wrapped;

    private final Position position;

    protected Color color;

    private LocalTransformationProvider transform;

    private IAirplane plane;

    private boolean visible = true;

    private double planeSpeed = 0;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Position getPosition() {
        return position;
    }

    public TransparentObject(Renderable wrapped, Position pos, IAirplane plane, Color color) {
        this.wrapped = wrapped;
        this.position = pos;
        this.color = color;
        this.plane = plane;
        this.planeSpeed =
            plane.getHardwareConfiguration()
                .getPlatformDescription()
                .getMaxPlaneSpeed()
                .convertTo(Unit.METER_PER_SECOND)
                .getValue()
                .doubleValue();
        plane.addListener(this);
    }

    @Override
    public void render(DrawContext dc) {
        // depending on level
        // draw wrapped with fading
        Color newColor = new Color(color.getColorSpace(), color.getColorComponents(null), opacity);
        if (wrapped != null) {
            if (wrapped instanceof Polyline) {
                ((Polyline)wrapped).setColor(newColor);
            } else if (wrapped instanceof PointPlacemark) {
                PointPlacemarkAttributes pointAttributes = ((PointPlacemark)wrapped).getAttributes();
                pointAttributes.setImageColor(newColor);
                ((PointPlacemark)wrapped).setAttributes(pointAttributes);
            }
            // ...else etc

            if (visible) {
                wrapped.render(dc);
            }
        }
    }

    @Override
    public void recv_position(PositionData p) {
        Position pos = new Position(Angle.fromDegrees(p.lat), Angle.fromDegrees(p.lon), p.gpsAltitude / 100.0);
        if (transform == null) {
            transform = new LocalTransformationProvider(pos, Angle.ZERO, 0, 0, true);
        }

        Vec4 p1 = transform.transformToLocalInclAlt(pos);
        Vec4 p2 = transform.transformToLocalInclAlt(position);
        double dist = p1.distanceTo3(p2);

        opacity = getOpacity(dist, planeSpeed);
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
