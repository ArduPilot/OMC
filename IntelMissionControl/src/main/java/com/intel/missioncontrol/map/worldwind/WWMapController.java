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
import gov.nasa.worldwind.geom.Position;
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

    @Inject
    public WWMapController(
            IApplicationContext applicationContext,
            IMapModel mapModel,
            IWWMapView mapView,
            IWWGlobes globes,
            ISelectionManager selectionManager,
            WorldWindowProvider worldWindowProvider) {
        propertyPathStore
            .from(applicationContext.currentLegacyMissionProperty())
            .selectReadOnlyObject(Mission::currentMatchingProperty)
            .addListener((observable, oldValue, newValue) -> setMouseMode(InputMode.DEFAULT));

        propertyPathStore
            .from(applicationContext.currentLegacyMissionProperty())
            .selectReadOnlyObject(Mission::currentFlightPlanProperty)
            .addListener((observable, oldValue, newValue) -> setMouseMode(InputMode.DEFAULT));

        worldWindowProvider.whenAvailable(
            worldWindow -> {
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

}
