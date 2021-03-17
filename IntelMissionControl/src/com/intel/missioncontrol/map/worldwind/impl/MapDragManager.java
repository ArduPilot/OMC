/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.intel.missioncontrol.map.worldwind.impl;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.map.IMapDragManager;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.elevation.ElevationModelRequestException;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.map.worldwind.WorldWindowProvider;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPicAreaCorners;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.IFlightplanLatLonReferenced;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IMuteable;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPicArea;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AltitudeModes;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.StartingPosLayer;
import eu.mavinci.desktop.gui.wwext.IWWRenderableWithUserData;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.PhantomCorner;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.ReferencePoint;
import eu.mavinci.flightplan.StartProcedure;
import eu.mavinci.flightplan.Takeoff;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.RayCastingSupport;
import java.awt.Point;
import java.util.OptionalDouble;
import javafx.application.Platform;

public class MapDragManager implements IMapDragManager {

    private WorldWindow wwd;
    private final AsyncBooleanProperty dragging = new SimpleAsyncBooleanProperty(this);

    private double dragRefAltitude;
    private Movable dragObject;

    private final AsyncObjectProperty userData = new SimpleAsyncObjectProperty(this);
    private AltitudeModes altMode = AltitudeModes.absolute;
    private boolean sticksToGround = false;

    private final IElevationModel elevationModel;
    private final IWWGlobes globes;
    private final IWWMapView mapView;
    private final ISelectionManager selectionManager;

    private Globe globe;
    private Vec4 refPoint;
    private Position refPos;
    private Matrix m;

    private Vec4 lastShift;

    private final AsyncObjectProperty<Position> lastPos = new SimpleAsyncObjectProperty<>(this);

    private MuteVisitor muteVis = new MuteVisitor();
    private double planeRefElevation;

    public MapDragManager(
            IWWGlobes globes,
            IWWMapView mapView,
            IElevationModel elevationModel,
            ISelectionManager selectionManager,
            WorldWindowProvider worldWindowProvider) {
        this.globes = globes;
        this.mapView = mapView;
        this.elevationModel = elevationModel;
        this.selectionManager = selectionManager;
        worldWindowProvider.whenAvailable(wwd -> MapDragManager.this.wwd = wwd);
    }

    public ReadOnlyAsyncBooleanProperty isDraggingProperty() {
        return dragging;
    }

    @Override
    public ReadOnlyAsyncObjectProperty dragObjectUserDataProperty() {
        return userData;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Position> lastDraggingPositionProperty() {
        return lastPos;
    }

    synchronized boolean tryStartDragging() {
        if (!isDragging()) {
            return false;
        }

        setDragging(true);
        return true;
    }

    synchronized boolean tryStopDragging() {
        if (!isDragging()) {
            return false;
        }

        setDragging(false);
        return true;
    }

    /**
     * returns true if dragging was started
     *
     * @param dragedRenderable
     * @param cursorPointStart
     * @param planeRefElevation
     * @param pickedPosition
     * @return
     */
    public boolean startDragging(
            Object dragedRenderable, Point cursorPointStart, double planeRefElevation, Position pickedPosition) {
        synchronized (this) {
            if (isDragging()) {
                return false;
            }

            if (dragedRenderable == null) {
                return false;
            }

            if (!(dragedRenderable instanceof IWWRenderableWithUserData)) {
                return false;
            }

            if (cursorPointStart == null) {
                return false;
            }

            sticksToGround = false;
            altMode = AltitudeModes.absolute;
            dragObject = null;
            this.planeRefElevation = planeRefElevation;

            globe = globes.getActiveGlobe();

            IWWRenderableWithUserData userDatContainer = (IWWRenderableWithUserData)dragedRenderable;
            if (!userDatContainer.isDraggable()) {
                return false;
            }

            Object userData = userDatContainer.getUserData();
            this.userData.set(userData);
            if (userData == null) {
                return false;
            }

            if (userData instanceof PhantomCorner) {
                PhantomCorner corner = (PhantomCorner)userData;
                try {
                    userData = corner.makeReal();
                    selectionManager.setSelection(userData);
                } catch (FlightplanContainerFullException e) {
                    Debug.getLog().log(Debug.WARNING, "cant add point by drag&drop", e);
                }
            }

            this.userData.set(userData);

            if (userData instanceof PicArea) {
                PicArea picArea = (PicArea)userData;
                dragObject = (Movable)dragedRenderable;
                sticksToGround = true;
                altMode = AltitudeModes.clampToGround;
                refPos = new Position(picArea.getCenterShifted(), 0);
            } else if (userData instanceof MapLayerPicArea) {
                MapLayerPicArea picArea = (MapLayerPicArea)userData;
                dragObject = (Movable)dragedRenderable;
                sticksToGround = true;
                altMode = AltitudeModes.clampToGround;
                refPos = new Position(picArea.getCenter(), 0);
                System.out.println("start dragging picArea");
            } else if (userData instanceof StartingPosLayer) {
                StartingPosLayer startPos = (StartingPosLayer)userData;
                if (!startPos.isChangeable()) {
                    return false;
                }

                sticksToGround = true;
                altMode = AltitudeModes.relativeToGround;
                dragObject = (Movable)dragedRenderable;
                refPos = startPos.getPosition();
            } else if (userData instanceof IFlightplanLatLonReferenced) {
                IFlightplanLatLonReferenced posRef = (IFlightplanLatLonReferenced)userData;
                sticksToGround = posRef.isStickingToGround();
                double elev = planeRefElevation;
                if (sticksToGround) {
                    altMode = AltitudeModes.relativeToGround;
                    elev =
                        elevationModel.getElevationAsGoodAsPossible(
                            Angle.fromDegreesLatitude(posRef.getLat()), Angle.fromDegreesLongitude(posRef.getLon()));
                } else {
                    altMode = AltitudeModes.relativeToStart;
                    if (userData instanceof IFlightplanPositionReferenced) {
                        IFlightplanPositionReferenced wp = (IFlightplanPositionReferenced)userData;
                        elev += wp.getAltInMAboveFPRefPoint();
                    }
                }

                refPos =
                    new Position(
                        Angle.fromDegreesLatitude(posRef.getLat()), Angle.fromDegreesLongitude(posRef.getLon()), elev);
            } else if (userData instanceof ISectorReferenced) {
                ISectorReferenced sectorRef = (ISectorReferenced)userData;
                OptionalDouble minElev = sectorRef.getMinElev();
                OptionalDouble maxElev = sectorRef.getMaxElev();
                if (minElev.isPresent() && maxElev.isPresent()) {
                    double offset = (minElev.getAsDouble() + maxElev.getAsDouble()) * 0.5;
                    planeRefElevation += offset;
                }

                sticksToGround = false;
                altMode = AltitudeModes.absolute;
                refPos = pickedPosition;
                if (refPos == null) {
                    Intersection[] intersection =
                        globe.intersect(
                            mapView.computeRayFromScreenPoint(cursorPointStart.x, cursorPointStart.y),
                            planeRefElevation);
                    if (intersection == null) {
                        return false;
                    }

                    refPos = globe.computePositionFromPoint(intersection[0].getIntersectionPoint());
                }

                if (refPos == null) {
                    return false;
                }
            } else if (dragedRenderable instanceof Movable) {
                dragObject = (Movable)dragedRenderable;
                refPos = dragObject.getReferencePosition();
            } else {
                return false;
            }

            if (refPos == null) {
                return false;
            }

            setDragging(true);
        }

        if (sticksToGround) {
            refPos = elevationModel.getPositionOnGround(refPos);
        }

        // reference is above geoid
        refPoint = globe.computePointFromPosition(refPos);
        m = globe.computeModelCoordinateOriginTransform(refPos).getInverse();

        // Save initial reference points for object and cursor in screen coordinates
        // Note: y is inverted for the object point.
        // Save cursor position
        if (mapView.isFlatEarth()) {
            sticksToGround = true;
        }

        switch (altMode) {
        case clampToGround:
            this.dragRefAltitude = globe.computePositionFromPoint(refPoint).getElevation();
            break;
        default:
            this.dragRefAltitude = refPos.getElevation();
        }

        lastShift = Vec4.ZERO;
        return true;
    }

    synchronized void setDragging(boolean isDragging) {
        this.dragging.set(isDragging);
        Object userData = MapDragManager.this.userData.get();
        if (userData instanceof IFlightplanRelatedObject) {
            IFlightplanRelatedObject fpObj = (IFlightplanRelatedObject)userData;
            CFlightplan fp = fpObj.getFlightplan();
            if (fp != null && isDragging) {
                fp.setMuteAutoRecalc(true);
            }
        }
    }

    public void stopDragging() {
        if (!tryStopDragging()) return;
        Platform.runLater(
            () -> {
                Object userData = MapDragManager.this.userData.get();
                Position lastPos = MapDragManager.this.lastPos.get();

                if (userData instanceof StartingPosLayer) {
                    // making the starting position of the simulator
                    // moveable
                    if (lastPos != null) {
                        StartingPosLayer startPoint = (StartingPosLayer)userData;
                        startPoint.setStartingPoint(lastPos);
                    }
                }

                if (userData instanceof IFlightplanRelatedObject) {
                    IFlightplanRelatedObject fpObj = (IFlightplanRelatedObject)userData;
                    CFlightplan fp = fpObj.getFlightplan();
                    if (fp != null) {
                        if (!fp.setMuteAutoRecalc(false)) {
                            // only trigger rerendering in case of computation isnt happening
                            fp.flightplanStatementChanged(
                                fpObj); // to trigger rendering update since dragging is now over
                        }
                    }
                }
            });
    }

    public void move(Point curserPoint) {
        if (curserPoint == null) {
            return;
        }

        if (!isDragging()) {
            return;
        }

        Line ray = mapView.computeRayFromScreenPoint(curserPoint.x, curserPoint.y);
        Position pickPos = null;
        WorldWindow wwd = this.wwd;
        if (sticksToGround && wwd != null) {
            // i didn't understand the following if (maybe we can get rid of it?)
            // globe.getMaxElevation() == 1 in 2D mode
            if (mapView.getEyePosition().getElevation() < globe.getMaxElevation() * 10
                    || globe.getMaxElevation() == 1) {
                // Use ray casting below some altitude
                // Try ray intersection with current terrain geometry
                SceneController sceneController = wwd.getSceneController();
                SectorGeometryList terrain = sceneController != null ? sceneController.getTerrain() : null;
                Intersection[] intersections = terrain != null ? terrain.intersect(ray) : null;
                if (intersections != null && intersections.length > 0) {
                    pickPos = globe.computePositionFromPoint(intersections[0].getIntersectionPoint());
                } else {
                    // Fallback on raycasting using elevation data
                    pickPos =
                        RayCastingSupport.intersectRayWithTerrain(globe, ray.getOrigin(), ray.getDirection(), 1000, 2);
                }
            }
        }
        // fallback for groundsticker, or directly for fix altitude dragger
        if (pickPos == null) {
            // Use intersection with sphere at reference altitude.
            Intersection inters[] = globe.intersect(ray, this.dragRefAltitude);
            if (inters != null) {
                pickPos = globe.computePositionFromPoint(inters[0].getIntersectionPoint());
            }
        }

        if (pickPos != null) {
            // Intersection with globe. Move reference point to the intersection point,
            // but maintain current altitude.
            Position p;
            double ground = 0;
            try {
                ground = elevationModel.getElevation(pickPos);
            } catch (ElevationModelRequestException e) {
                if (e.isWorst) {
                    ground = this.dragRefAltitude;
                } else {
                    ground = e.achievedAltitude;
                }
            }

            if (sticksToGround) {
                p = new Position(pickPos, ground);
            } else {
                p = new Position(pickPos, this.dragRefAltitude);
            }

            switch (altMode) {
            case absolute:
                break;
            case relativeToGround:
                p = new Position(p, p.elevation - ground);
                break;
            case clampToGround:
                p = new Position(p, 0);
                break;
            case relativeToStart:
                p = new Position(p, p.elevation - planeRefElevation);
                break;
            }

            moveObject(p);
        }
    }

    protected void moveObject(final Position p) {
        this.lastPos.set(p);
        final Object userData = this.userData.get();
        final Movable dragObject = this.dragObject;

        Platform.runLater(
            () -> {
                if (userData instanceof Takeoff) {
                    Takeoff origin = (Takeoff)userData;
                    origin.setIsAuto(false);
                    origin.setDefined(true);
                } else if (userData instanceof ReferencePoint) {
                    ReferencePoint origin = (ReferencePoint)userData;
                    origin.setIsAuto(false);
                    origin.setDefined(true);
                }

                if (userData instanceof IFlightplanLatLonReferenced) {
                    IFlightplanLatLonReferenced latLonRef = (IFlightplanLatLonReferenced)userData;
                    latLonRef.setLatLon(p.latitude.degrees, p.longitude.degrees);
                } else if (userData instanceof IFlightplanRelatedObject) {
                    IFlightplanRelatedObject fpObj = (IFlightplanRelatedObject)userData;
                    Vec4 v = globe.computePointFromPosition(p);

                    v = v.transformBy4(m);

                    Vec4 delta = v.subtract3(lastShift);
                    muteVis.setMute(true);
                    lastShift = v;
                    AllObjectShifterVisitor vis = new AllObjectShifterVisitor(delta);
                    muteVis.startVisit(fpObj);
                    vis.startVisit(fpObj);
                    muteVis.setMute(false);
                    muteVis.startVisit(fpObj);
                    if (!(fpObj instanceof CFlightplan)) {
                        CFlightplan fp = fpObj.getFlightplan();
                        if (fp != null) {
                            fp.flightplanStatementChanged(fpObj);
                        }
                    }
                } else if (userData instanceof StartingPosLayer) {
                    dragObject.moveTo(p);
                } else if (dragObject != null) {
                    dragObject.moveTo(p);
                }
            });
    }

    private class AllObjectShifterVisitor extends AFlightplanVisitor {

        Vec4 shift;

        public AllObjectShifterVisitor(Vec4 shift) {
            this.shift = shift;
        }

        @Override
        public boolean visit(IFlightplanRelatedObject fpObj) {
            if (fpObj instanceof IFlightplanLatLonReferenced) {
                if (fpObj instanceof StartProcedure) {
                    return false;
                }

                IFlightplanLatLonReferenced latLonRef = (IFlightplanLatLonReferenced)fpObj;
                Angle lat = Angle.fromDegrees(latLonRef.getLat());
                Angle lon = Angle.fromDegrees(latLonRef.getLon());

                Matrix m = globe.computeModelCoordinateOriginTransform(lat, lon, planeRefElevation);
                Vec4 v = shift.transformBy4(m);
                Position pNew = globe.computePositionFromPoint(v);

                latLonRef.setLatLon(pNew.getLatitude().degrees, pNew.getLongitude().degrees);
            }

            return false;
        }

    }

    private static class MuteVisitor extends AFlightplanVisitor implements IMuteable {

        boolean mute;

        public MuteVisitor() {}

        @Override
        public boolean visit(IFlightplanRelatedObject fpObj) {
            IMuteable muteable = null;
            if (fpObj instanceof IMuteable) {
                muteable = (IMuteable)fpObj;
                if (mute) {
                    muteable.setMute(mute);
                } else {
                    if (fpObj instanceof CPicAreaCorners
                            || fpObj instanceof MapLayerPicArea.MapLayerPicAreaFpContainer) {
                        // this way others are informed about the change deep inside
                        muteable.setMute(false);
                    } else {
                        muteable.setSilentUnmute();
                    }
                }
            }

            return false;
        }

        @Override
        public void setMute(boolean mute) {
            this.mute = mute;
        }

        @Override
        public boolean isMute() {
            return mute;
        }

        @Override
        public void setSilentUnmute() {
            this.mute = false;
        }
    }
}
