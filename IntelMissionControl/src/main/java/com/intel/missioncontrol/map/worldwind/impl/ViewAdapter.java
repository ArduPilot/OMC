/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.impl;

import eu.mavinci.desktop.gui.wwext.MViewInputHandler;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.BasicView;
import gov.nasa.worldwind.view.ViewPropertyLimits;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ViewAdapter<T extends BasicView> implements View {

    private final T view;

    public ViewAdapter(T view) {
        this.view = view;

        view.setViewInputHandler(
            new MViewInputHandler() {
                @Override
                protected View getView() {
                    return view;
                }
            });
    }

    protected T getAdaptedView() {
        return view;
    }

    @Override
    public void stopMovement() {
        view.stopMovement();
    }

    @Override
    public Position getEyePosition() {
        return view.getEyePosition();
    }

    @Override
    public void setEyePosition(Position eyePosition) {
        view.setEyePosition(eyePosition);
    }

    @Override
    public Position getCurrentEyePosition() {
        return view.getCurrentEyePosition();
    }

    @Override
    public void setOrientation(Position eyePosition, Position centerPosition) {
        view.setOrientation(eyePosition, centerPosition);
    }

    @Override
    public void setHeading(Angle heading) {
        view.setHeading(heading);
    }

    @Override
    public void setPitch(Angle pitch) {
        view.setPitch(pitch);
    }

    @Override
    public Angle getHeading() {
        return view.getHeading();
    }

    @Override
    public Angle getPitch() {
        return view.getPitch();
    }

    @Override
    public Angle getRoll() {
        return view.getRoll();
    }

    @Override
    public void setRoll(Angle roll) {
        view.setRoll(roll);
    }

    @Override
    public Vec4 getEyePoint() {
        return view.getEyePoint();
    }

    @Override
    public Vec4 getCurrentEyePoint() {
        return view.getCurrentEyePoint();
    }

    @Override
    public Vec4 getUpVector() {
        return view.getUpVector();
    }

    @Override
    public Vec4 getForwardVector() {
        return view.getForwardVector();
    }

    @Override
    public Matrix getModelviewMatrix() {
        return view.getModelviewMatrix();
    }

    @Override
    public long getViewStateID() {
        return view.getViewStateID();
    }

    @Override
    public Angle getFieldOfView() {
        return view.getFieldOfView();
    }

    @Override
    public void setFieldOfView(Angle fieldOfView) {
        view.setFieldOfView(fieldOfView);
    }

    @Override
    public Rectangle getViewport() {
        return view.getViewport();
    }

    @Override
    public double getNearClipDistance() {
        return view.getNearClipDistance();
    }

    @Override
    public double getFarClipDistance() {
        return view.getFarClipDistance();
    }

    @Override
    public Frustum getFrustum() {
        return view.getFrustum();
    }

    @Override
    public Frustum getFrustumInModelCoordinates() {
        return view.getFrustumInModelCoordinates();
    }

    @Override
    public Matrix getProjectionMatrix() {
        return view.getProjectionMatrix();
    }

    @Override
    public void apply(DrawContext dc) {
        view.apply(dc);
    }

    @Override
    public Vec4 project(Vec4 modelPoint) {
        return view.project(modelPoint);
    }

    @Override
    public Vec4 unProject(Vec4 windowPoint) {
        return view.unProject(windowPoint);
    }

    @Override
    public Matrix pushReferenceCenter(DrawContext dc, Vec4 referenceCenter) {
        return view.pushReferenceCenter(dc, referenceCenter);
    }

    @Override
    public void popReferenceCenter(DrawContext dc) {
        view.popReferenceCenter(dc);
    }

    @Override
    public Matrix setReferenceCenter(DrawContext dc, Vec4 referenceCenter) {
        return view.setReferenceCenter(dc, referenceCenter);
    }

    @Override
    public Line computeRayFromScreenPoint(double x, double y) {
        return view.computeRayFromScreenPoint(x, y);
    }

    @Override
    public Position computePositionFromScreenPoint(double x, double y) {
        return view.computePositionFromScreenPoint(x, y);
    }

    @Override
    public double computePixelSizeAtDistance(double distance) {
        return view.computePixelSizeAtDistance(distance);
    }

    @Override
    public Vec4 getCenterPoint() {
        return view.getCenterPoint();
    }

    @Override
    public Globe getGlobe() {
        return view.getGlobe();
    }

    @Override
    public ViewInputHandler getViewInputHandler() {
        return view.getViewInputHandler();
    }

    @Override
    public void stopAnimations() {
        view.stopAnimations();
    }

    @Override
    public void goTo(Position position, double elevation) {
        view.goTo(position, elevation);
    }

    @Override
    public boolean isAnimating() {
        return view.isAnimating();
    }

    @Override
    public ViewPropertyLimits getViewPropertyLimits() {
        return view.getViewPropertyLimits();
    }

    @Override
    public void copyViewState(View view) {
        this.view.copyViewState(view);
    }

    @Override
    public void addAnimator(Animator animator) {
        view.addAnimator(animator);
    }

    @Override
    public double getHorizonDistance() {
        return view.getHorizonDistance();
    }

    @Override
    public Object setValue(String key, Object value) {
        return view.setValue(key, value);
    }

    @Override
    public AVList setValues(AVList avList) {
        return view.setValues(avList);
    }

    @Override
    public Object getValue(String key) {
        return view.getValue(key);
    }

    @Override
    public Collection<Object> getValues() {
        return view.getValues();
    }

    @Override
    public String getStringValue(String key) {
        return view.getStringValue(key);
    }

    @Override
    public Set<Map.Entry<String, Object>> getEntries() {
        return view.getEntries();
    }

    @Override
    public boolean hasKey(String key) {
        return view.hasKey(key);
    }

    @Override
    public Object removeKey(String key) {
        return view.removeKey(key);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        view.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        view.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        view.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        view.removePropertyChangeListener(listener);
    }

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        view.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        view.firePropertyChange(propertyChangeEvent);
    }

    @Override
    public AVList copy() {
        return view.copy();
    }

    @Override
    public AVList clearList() {
        return view.clearList();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        view.propertyChange(evt);
    }

    @Override
    public void onMessage(Message msg) {
        view.onMessage(msg);
    }

    @Override
    public String getRestorableState() {
        return view.getRestorableState();
    }

    @Override
    public void restoreState(String stateInXml) {
        view.restoreState(stateInXml);
    }

}
