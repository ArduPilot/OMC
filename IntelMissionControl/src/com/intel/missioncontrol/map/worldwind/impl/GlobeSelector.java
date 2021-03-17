/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.impl;

import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Triangle;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.globes.GlobeStateKey;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.terrain.Tessellator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GlobeSelector implements Globe {

    private Globe globe;

    public GlobeSelector(SynchronizationRoot syncRoot, IWWMapView mapView, Globe defaultGlobe, Globe flatGlobe) {
        this.globe = defaultGlobe;

        mapView.flatEarthProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    this.globe = newValue ? flatGlobe : defaultGlobe;
                    // trigger redraw:
                    mapView.firePropertyChange(AVKey.VIEW, null, this);
                },
                syncRoot);
    }

    public void setGlobe(Globe globe) {
        this.globe = globe;
    }

    public Globe getGlobe() {
        return globe;
    }

    @Override
    public Extent getExtent() {
        return globe.getExtent();
    }

    @Override
    public double getEquatorialRadius() {
        return globe.getEquatorialRadius();
    }

    @Override
    public double getPolarRadius() {
        return globe.getPolarRadius();
    }

    @Override
    public double getMaximumRadius() {
        return globe.getMaximumRadius();
    }

    @Override
    public double getRadiusAt(Angle latitude, Angle longitude) {
        return globe.getRadiusAt(latitude, longitude);
    }

    @Override
    public double getElevation(Angle latitude, Angle longitude) {
        return globe.getElevation(latitude, longitude);
    }

    @Override
    public double getElevations(
            Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] elevations) {
        return globe.getElevations(sector, latlons, targetResolution, elevations);
    }

    @Override
    public double[] getElevations(
            Sector sector, List<? extends LatLon> latlons, double[] targetResolution, double[] elevations) {
        return globe.getElevations(sector, latlons, targetResolution, elevations);
    }

    @Override
    public double getMaxElevation() {
        return globe.getMaxElevation();
    }

    @Override
    public double getMinElevation() {
        return globe.getMinElevation();
    }

    @Override
    public Position getIntersectionPosition(Line line) {
        return globe.getIntersectionPosition(line);
    }

    @Override
    public double getEccentricitySquared() {
        return globe.getEccentricitySquared();
    }

    @Override
    public Vec4 computePointFromPosition(Angle latitude, Angle longitude, double metersElevation) {
        return globe.computePointFromPosition(latitude, longitude, metersElevation);
    }

    @Override
    public Vec4 computePointFromPosition(LatLon latLon, double metersElevation) {
        return globe.computePointFromPosition(latLon, metersElevation);
    }

    @Override
    public Vec4 computePointFromPosition(Position position) {
        return globe.computePointFromPosition(position);
    }

    @Override
    public Vec4 computePointFromLocation(LatLon location) {
        return globe.computePointFromLocation(location);
    }

    @Override
    public Position computePositionFromPoint(Vec4 point) {
        return globe.computePositionFromPoint(point);
    }

    @Override
    public void computePointsFromPositions(
            Sector sector, int numLat, int numLon, double[] metersElevation, Vec4[] out) {
        globe.computePointsFromPositions(sector, numLat, numLon, metersElevation, out);
    }

    @Override
    public Vec4 computeSurfaceNormalAtLocation(Angle latitude, Angle longitude) {
        return globe.computeSurfaceNormalAtLocation(latitude, longitude);
    }

    @Override
    public Vec4 computeSurfaceNormalAtPoint(Vec4 point) {
        return globe.computeSurfaceNormalAtPoint(point);
    }

    @Override
    public Vec4 computeNorthPointingTangentAtLocation(Angle latitude, Angle longitude) {
        return globe.computeNorthPointingTangentAtLocation(latitude, longitude);
    }

    @Override
    public Matrix computeModelCoordinateOriginTransform(Angle latitude, Angle longitude, double metersElevation) {
        return globe.computeModelCoordinateOriginTransform(latitude, longitude, metersElevation);
    }

    @Override
    public Matrix computeModelCoordinateOriginTransform(Position position) {
        return globe.computeModelCoordinateOriginTransform(position);
    }

    @Override
    public Matrix computeSurfaceOrientationAtPosition(Angle latitude, Angle longitude, double metersElevation) {
        return globe.computeSurfaceOrientationAtPosition(latitude, longitude, metersElevation);
    }

    @Override
    public Matrix computeSurfaceOrientationAtPosition(Position position) {
        return globe.computeSurfaceOrientationAtPosition(position);
    }

    @Override
    public Vec4 computeEllipsoidalPointFromPosition(Angle latitude, Angle longitude, double metersElevation) {
        return globe.computeEllipsoidalPointFromPosition(latitude, longitude, metersElevation);
    }

    @Override
    public Vec4 computeEllipsoidalPointFromLocation(LatLon location) {
        return globe.computeEllipsoidalPointFromLocation(location);
    }

    @Override
    public Vec4 computeEllipsoidalPointFromPosition(Position position) {
        return globe.computeEllipsoidalPointFromPosition(position);
    }

    @Override
    public Position computePositionFromEllipsoidalPoint(Vec4 ellipsoidalPoint) {
        return globe.computePositionFromEllipsoidalPoint(ellipsoidalPoint);
    }

    @Override
    public Vec4 computeEllipsoidalNormalAtLocation(Angle latitude, Angle longitude) {
        return globe.computeEllipsoidalNormalAtLocation(latitude, longitude);
    }

    @Override
    public Matrix computeEllipsoidalOrientationAtPosition(Angle latitude, Angle longitude, double metersElevation) {
        return globe.computeEllipsoidalOrientationAtPosition(latitude, longitude, metersElevation);
    }

    @Override
    public double getRadiusAt(LatLon location) {
        return globe.getRadiusAt(location);
    }

    @Override
    public double[] getMinAndMaxElevations(Angle latitude, Angle longitude) {
        return globe.getMinAndMaxElevations(latitude, longitude);
    }

    @Override
    public double[] getMinAndMaxElevations(Sector sector) {
        return globe.getMinAndMaxElevations(sector);
    }

    @Override
    public Intersection[] intersect(Line line, double altitude) {
        return globe.intersect(line, altitude);
    }

    @Override
    public Intersection[] intersect(Triangle triangle, double altitude) {
        return globe.intersect(triangle, altitude);
    }

    @Override
    public Tessellator getTessellator() {
        return globe.getTessellator();
    }

    @Override
    public void setTessellator(Tessellator tessellator) {
        globe.setTessellator(tessellator);
    }

    @Override
    public SectorGeometryList tessellate(DrawContext dc) {
        return globe.tessellate(dc);
    }

    @Override
    public Object getStateKey(DrawContext dc) {
        return globe.getStateKey(dc);
    }

    @Override
    public GlobeStateKey getGlobeStateKey(DrawContext dc) {
        return globe.getGlobeStateKey(dc);
    }

    @Override
    public GlobeStateKey getGlobeStateKey() {
        return globe.getGlobeStateKey();
    }

    @Override
    public ElevationModel getElevationModel() {
        return globe.getElevationModel();
    }

    @Override
    public void setElevationModel(ElevationModel elevationModel) {
        globe.setElevationModel(elevationModel);
    }

    @Override
    public boolean isPointAboveElevation(Vec4 point, double elevation) {
        return globe.isPointAboveElevation(point, elevation);
    }

    @Override
    public Object setValue(String key, Object value) {
        return globe.setValue(key, value);
    }

    @Override
    public AVList setValues(AVList avList) {
        return globe.setValues(avList);
    }

    @Override
    public Object getValue(String key) {
        return globe.getValue(key);
    }

    @Override
    public Collection<Object> getValues() {
        return globe.getValues();
    }

    @Override
    public String getStringValue(String key) {
        return globe.getStringValue(key);
    }

    @Override
    public Set<Map.Entry<String, Object>> getEntries() {
        return globe.getEntries();
    }

    @Override
    public boolean hasKey(String key) {
        return globe.hasKey(key);
    }

    @Override
    public Object removeKey(String key) {
        return globe.removeKey(key);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        globe.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        globe.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        globe.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        globe.removePropertyChangeListener(listener);
    }

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        globe.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        globe.firePropertyChange(propertyChangeEvent);
    }

    @Override
    public AVList copy() {
        return globe.copy();
    }

    @Override
    public AVList clearList() {
        return globe.clearList();
    }

    @Override
    public void onMessage(Message msg) {
        globe.onMessage(msg);
    }

    @Override
    public Vec4 getCenter() {
        return globe.getCenter();
    }

    @Override
    public double getDiameter() {
        return globe.getDiameter();
    }

    @Override
    public double getRadius() {
        return globe.getRadius();
    }

    @Override
    public boolean intersects(Frustum frustum) {
        return globe.intersects(frustum);
    }

    @Override
    public Intersection[] intersect(Line line) {
        return globe.intersect(line);
    }

    @Override
    public boolean intersects(Line line) {
        return globe.intersects(line);
    }

    @Override
    public boolean intersects(Plane plane) {
        return globe.intersects(plane);
    }

    @Override
    public double getEffectiveRadius(Plane plane) {
        return globe.getEffectiveRadius(plane);
    }

    @Override
    public double getProjectedArea(View view) {
        return globe.getProjectedArea(view);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        globe.propertyChange(evt);
    }

}
