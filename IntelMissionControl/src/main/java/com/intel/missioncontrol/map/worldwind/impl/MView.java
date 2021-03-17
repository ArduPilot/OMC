/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.intel.missioncontrol.map.worldwind.impl;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.map.ViewMode;
import com.intel.missioncontrol.mission.Drone;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.listeners.IAirplaneListenerOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPowerOn;
import eu.mavinci.core.plane.listeners.IAirplaneListenerStartPos;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.view.ViewUtil;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.asyncfx.concurrent.SynchronizationRoot;

/**
 * This view can handle the different modes from {@link ViewMode} therefore it prevents different kinds of user
 * interaction in certain modes
 *
 * @author Marco
 */
@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class MView extends MView2d
        implements IAirplaneListenerOrientation,
            IAirplaneListenerPosition,
            IAirplaneListenerStartPos,
            IAirplaneListenerPowerOn {

    private double lastZoom = getZoom();
    private Angle lastHeading = getHeading();
    private Angle lastPitch = getPitch();

    public static final int MAX_COMMON_ZOOM = 15000;

    ReadOnlyObjectProperty<Drone> uav;
    private WeakReference<IAirplane> plane;

    Angle fieldOfViewDef;

    private static final int MIN_ZOOM_LIMIT = 0;
    private static final int MAX_ZOOM_LIMIT = 20000000; // 2000 km

    public MView() {
        super(null);

        fieldOfViewDef = getFieldOfView();

        setPlane(null);
        getOrbitViewLimits().setZoomLimits(MIN_ZOOM_LIMIT, MAX_ZOOM_LIMIT);
    }

    public void setViewPort(int width, int height) {
        this.viewport = new java.awt.Rectangle(0, 0, width, height);
    }

    public MView(ReadOnlyObjectProperty<Drone> uav, SynchronizationRoot syncRoot) {
        super(null);
        this.uav = uav; // if this is just a property path selection, it would get garbage collected.... so lets store a
        // reference
        fieldOfViewDef = getFieldOfView();
        uav.addListener(
            (observable, oldValue, newValue) -> syncRoot.runAsync(() -> setPlane(newValue.getLegacyPlane())));
        Drone uavIns = uav.get();
        setPlane(uavIns != null ? uavIns.getLegacyPlane() : null);
        getOrbitViewLimits().setZoomLimits(MIN_ZOOM_LIMIT, MAX_ZOOM_LIMIT);
    }

    public void setPlane(IAirplane plane) {
        IAirplane p = this.plane == null ? null : this.plane.get();
        if (p != null) {
            p.removeListener(this);
            setHardwareConfiguration(null);
        }

        this.plane = new WeakReference<IAirplane>(plane);
        if (plane != null) {
            plane.addListener(this);
            setHardwareConfiguration(plane.getHardwareConfiguration());
        }
    }

    private OrientationData lastOrientation;

    @Override
    public void recv_orientation(OrientationData o) {
        lastOrientation = o;
        if (viewMode.isPlaneCentered() && !isFlatEarth) {
            IAirplane airplane = plane.get();
            if (airplane != null && airplane.getHardwareConfiguration().getPlatformDescription().isInCopterMode()) {
                setRollInt(Angle.fromDegrees(o.cameraRoll));
                setPitchInt(
                    Angle.fromDegrees(
                        o.cameraPitch
                            - 90)); // in falcon mode we will look into the camera direction, since view has another 90
                // deg orientation offset make minus
                setHeadingInt(Angle.fromDegrees(o.cameraYaw));
            } else {
                // for fixwing we like to look to the flying direction... so even the view has an
                setRollInt(Angle.fromDegrees(o.roll));
                setPitchInt(Angle.fromDegrees(o.pitch)); // in fixwing mode look into nose direction
                setHeadingInt(Angle.fromDegrees(o.yaw));
            }
            //			System.out.println("orient:"+o);
        }
    }

    public void setFlatEarth(boolean enabled) {
        super.setFlatEarth(enabled);
        if (!enabled && viewMode.isPlaneCentered()) {
            recv_orientation(lastOrientation);
        }
    }

    @Override
    public void recv_position(PositionData p) {
        double elevationStartPoint;
        Position pos;
        try {
            elevationStartPoint = plane.get().getAirplaneCache().getStartElevOverWGS84();
            pos = plane.get().getAirplaneCache().getCurPos();
        } catch (AirplaneCacheEmptyException e) {
            return;
        }
        //		Position pos = new Position(Angle.fromDegreesLatitude(p.lat), Angle
        //				.fromDegreesLongitude(p.lon), p.altitude / 100
        //				+ elevationStartPoint);

        if (viewMode == ViewMode.DEFAULT && shouldAutoCenter) {
            recenterNow();
        } else if (viewMode == ViewMode.FOLLOW) {
            setCenterPositionInt(pos);

            // maybe adjust zoom
            if (shouldAutoCenter && getZoom() > MAX_COMMON_ZOOM) {
                setZoom(MAX_COMMON_ZOOM);
                //				System.out.println("auto zoom" + shouldAutoCenter);
            }

            shouldAutoCenter = false;
        } else if (viewMode.isPlaneCentered()) {
            // set eye to this position
            double d;
            try {
                d =
                    Math.max(
                        plane.get().getAirplaneCache().getCurAlt(), plane.get().getAirplaneCache().getCurGroundElev());
            } catch (AirplaneCacheEmptyException e) {
                d = p.altitude / 100. + elevationStartPoint;
                Debug.getLog().log(Level.FINER, "cant calculate airplane altitude");
            }

            Angle fieldOfView = fieldOfViewDef;

            IHardwareConfiguration hardwareConfiguration = getHardwareConfiguration();
            Ensure.notNull(hardwareConfiguration);

            if (viewMode == ViewMode.PAYLOAD) {
                IAirplane planeR = plane.get();
                if (planeR != null) {
                    Vec4[] corners = CameraHelper.getCornerDirections(hardwareConfiguration);
                    MinMaxPair minMax = new MinMaxPair();
                    for (Vec4 v : corners) {
                        minMax.update(v.x);
                        minMax.update(v.y);
                    }

                    double halfWidth = minMax.absMax();

                    halfWidth *= 1.5; // add some margin

                    fieldOfView =
                        Angle.fromRadians(
                            2 * Math.atan(halfWidth / CameraHelper.getFocalLength35mm(hardwareConfiguration)));
                }
            }

            setFieldOfView(fieldOfView);

            Position pos2 = new Position(pos, d);
            setEyePositionInt(pos2);
            shouldAutoCenter = false;
        }
    }

    @Override
    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        if (pressureZero != 0) return;
        try {
            if (plane.get().getAirplaneCache().wasLastRecvStartPosMajorChange()) shouldAutoCenter = true;
            doCentering(
                plane.get().getAirplaneCache().getStartPosBaro(),
                plane.get().getAirplaneCache().getStartElevOverWGS84());
        } catch (AirplaneCacheEmptyException e) {
            Debug.getLog().log(Level.SEVERE, "cache shouldn't be empty!", e);
            return;
        }
    }

    public void doCentering(LatLon pos, double elev) {
        if (!shouldAutoCenter) return;

        setCenterPositionInt(new Position(pos, elev));

        // maybe adjust zoom
        if (getZoom() > MAX_COMMON_ZOOM) {
            setZoom(MAX_COMMON_ZOOM);
            //			System.out.println("auto zoom" + shouldAutoCenter);
        }

        shouldAutoCenter = false;
    }

    public void setViewMode(ViewMode newViewMode) {
        if (this.viewMode.equals(newViewMode)) return;

        ViewMode lastMode = this.viewMode;
        this.viewMode = newViewMode;

        // always a good idea ;-) if you change your view
        stopAnimations();
        stopMovement();
        stopMovementOnCenter();
        if (viewMode != ViewMode.PAYLOAD) {
            setFieldOfView(fieldOfViewDef);
        }

        if (viewMode.isPlaneCentered() && !lastMode.isPlaneCentered()) {
            // so last view wasn't coockpit

            // save this for being able to resore it later on switch back to non
            // cockpit viewmode
            lastZoom = getZoom();
            lastHeading = getHeading();
            lastPitch = getPitch();
            lastPosition = getCenterPosition();

            // zoom = 0, because eye position is already in cockpit
            setZoomInt(0);
        } else {
            switch (lastMode) {
            case COCKPIT:
            case PAYLOAD:
                if (canFocusOnTerrainCenter()) focusOnTerrainCenter();

                if (!isFlatEarth) {
                    setRollInt(Angle.ZERO); // normal view modes cant roll
                    setHeadingInt(lastHeading);
                    setPitchInt(lastPitch);
                }

                // restore last zoom and camera orientation
                setZoomInt(lastZoom);
                setCenterPositionInt(lastPosition);

                break;
            case FOLLOW:
                if (!isFlatEarth && canFocusOnTerrainCenter()) focusOnTerrainCenter();
                break;

            case DEFAULT:
                // problem with zooming on stay -> follow
                // zoomlevel jumps because of the altitude jump
                shouldInitFollowZoom = true;
                break;

            default:
                break;
            }
        }

        try {
            recv_orientation(plane.get().getAirplaneCache().getOrientation());
        } catch (AirplaneCacheEmptyException e) {
        }

        try {
            recv_position(plane.get().getAirplaneCache().getPosition());
        } catch (AirplaneCacheEmptyException e) {
        }

        firePropertyChange(AVKey.VIEW, null, this);
    }

    protected boolean shouldAutoCenter = true;

    private void recenterNow() {
        shouldAutoCenter = true;
        try {
            doCentering(plane.get().getAirplaneCache().getCurLatLon(), plane.get().getAirplaneCache().getCurAlt());
        } catch (AirplaneCacheEmptyException e) {
            try {
                doCentering(
                    plane.get().getAirplaneCache().getStartPosBaro(),
                    plane.get().getAirplaneCache().getStartElevOverWGS84());
            } catch (AirplaneCacheEmptyException e1) {
            }
        }
    }

    public static final String KEY = "airplaneView";

    @Override
    public void recv_powerOn() {
        shouldAutoCenter = true;
    }

    @Override
    protected double computeNearDistance(Position eyePosition) {
        // dont clip objects even when they are on altitude after coming close to them.
        // most likely ;-) we have no images higher than 1000m, so this should be safe!
        // making it always small will lead to z-fighting artefacts on large zoom number in the rendering
        double distanceToSurface = ViewUtil.computeElevationAboveSurface(this.dc, eyePosition);
        if (distanceToSurface < 1000) return 0.5;
        return super.computeNearDistance(eyePosition);
    }

}
