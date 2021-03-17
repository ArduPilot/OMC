/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.map.worldwind.impl.BasicSelectListener;
import com.intel.missioncontrol.map.worldwind.impl.EventDispatcherSelectListener;
import com.intel.missioncontrol.map.worldwind.impl.WWWidgetMouseAdapter;
import com.intel.missioncontrol.mission.Mission;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.RayCastingSupport;
import java.awt.Rectangle;
import java.util.Stack;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;

public class WWMapController implements IMapController {

    private final AsyncObjectProperty<Position> pointerPosition = new SimpleAsyncObjectProperty<>(this);

    private final AsyncObjectProperty<InputMode> mouseMode =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<InputMode>().initialValue(InputMode.DEFAULT).create());

    private final Stack<InputMode> stack = new Stack<>();
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    private IWWMapView mapView;
    private IWWGlobes globes;
    private WorldWindow wwd;

    @Inject
    public WWMapController(
            IApplicationContext applicationContext,
            IMapModel mapModel,
            IWWMapView mapView,
            IWWGlobes globes,
            ISelectionManager selectionManager,
            WorldWindowProvider worldWindowProvider) {
        this.mapView = mapView;
        this.globes = globes;
        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .selectReadOnlyObject(Mission::currentMatchingProperty)
            .addListener((observable, oldValue, newValue) -> setMouseMode(InputMode.DEFAULT));

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .selectReadOnlyObject(Mission::currentFlightPlanProperty)
            .addListener((observable, oldValue, newValue) -> setMouseMode(InputMode.DEFAULT));

        worldWindowProvider.whenAvailable(
            worldWindow -> {
                this.wwd = worldWindow;
                worldWindow.addSelectListener(
                    new BasicSelectListener(mapModel, mapView, worldWindow, selectionManager, this));
                worldWindow.addSelectListener(new EventDispatcherSelectListener(worldWindow));
                worldWindow.addPositionListener(listener -> pointerPosition.set(listener.getPosition()));
                worldWindow
                    .getInputHandler()
                    .addMouseListener(
                        new WWWidgetMouseAdapter(mapModel, mapView, globes, worldWindow, selectionManager, this));
            });
    }

    @Override
    public AsyncObjectProperty<Position> pointerPositionProperty() {
        return pointerPosition;
    }

    @Override
    public boolean popLastMouseMode() {
        if (stack.isEmpty()) {
            return false;
        } else {
            PropertyHelper.setValueSafe(mouseMode, stack.pop());
            return true;
        }
    }

    @Override
    public boolean tryCancelMouseModes(InputMode... modes) {
        InputMode current = getMouseMode();
        for (InputMode oneToCancel : modes) {
            if (oneToCancel == current) {
                popLastMouseMode();
                return true;
            }
        }

        return false;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<InputMode> mouseModeProperty() {
        return mouseMode;
    }

    @Override
    public InputMode getMouseMode() {
        return mouseMode.get();
    }

    @Override
    public void setMouseMode(InputMode mouseMode) {
        if (getMouseMode() == mouseMode) {
            return;
        }

        stack.push(this.mouseMode.get());
        PropertyHelper.setValueSafe(this.mouseMode, mouseMode);
    }

    @Override
    public boolean isSelecting() {
        return mouseMode.get().isSelecting();
    }

    @Override
    public boolean isEditing() {
        return mouseMode.get().equals(InputMode.ADD_POINTS);
    }

    @Override
    public Position getScreenCenter() {
        return mapView.getCenterPosition();
    }

    @Override
    public Position[] get4MapSectorsCenters() {
        Rectangle rect = mapView.getDC().getView().getViewport();

        double xs[] = new double[4];
        double ys[] = new double[4];
        Position pos[] = new Position[4];

        xs[0] = 0.35 * rect.width;
        ys[0] = 0.35 * rect.height;
        xs[1] = 0.35 * rect.width;
        ys[1] = 0.65 * rect.height;
        xs[2] = 0.65 * rect.width;
        ys[2] = 0.65 * rect.height;
        xs[3] = 0.65 * rect.width;
        ys[3] = 0.35 * rect.height;

        var terrain = wwd.getSceneController().getTerrain();

        for (int i = 0; i != 4; ++i) {
            double x = xs[i];
            double y = ys[i];

            Globe globe = globes.getActiveGlobe();
            Line ray = mapView.computeRayFromScreenPoint(x, y);
            Position p = null;
            if (mapView.isFlatEarth()) {
                p = mapView.computePositionFromScreenPoint(x, y);
            }

            if (wwd != null && p == null && mapView.getEyePosition().getElevation() < globe.getMaxElevation() * 10) {
                // Use ray casting below some altitude
                // Try ray intersection with current terrain geometry
                Intersection[] intersections = terrain != null ? terrain.intersect(ray) : null;
                if (intersections != null && intersections.length > 0) {
                    p = globe.computePositionFromPoint(intersections[0].getIntersectionPoint());
                } else {
                    // Fallback on raycasting using elevation data
                    p = RayCastingSupport.intersectRayWithTerrain(globe, ray.getOrigin(), ray.getDirection(), 1000, 2);
                }
            }

            // fallback for groundsticker, or directly for fix altitude dragger
            if (p == null) {
                // Use intersection with sphere at reference altitude.
                Intersection[] inters = globe.intersect(ray, 0);
                if (inters != null) {
                    p = globe.computePositionFromPoint(inters[0].getIntersectionPoint());
                }
            }

            if (p == null) {
                return null; // dont know where in the world we are
            }

            pos[i] = p;
        }

        return pos;
    }
}
