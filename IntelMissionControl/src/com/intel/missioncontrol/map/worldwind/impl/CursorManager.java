/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.impl;

import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.SvgUtils;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.map.worldwind.WorldWindowProvider;
import eu.mavinci.desktop.gui.wwext.IWWRenderableWithUserData;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.InputHandler;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.javafx.WWNode;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;

public class CursorManager implements MouseListener, SelectListener {

    private final Map<InputMode, Cursor> mouseModeCursorsDown = new EnumMap<>(InputMode.class);
    private final Map<InputMode, Cursor> mouseModeCursorsUp = new EnumMap<>(InputMode.class);

    private InputHandler inputHandler;
    private boolean mouseDown;
    private InputMode mouseMode = InputMode.DEFAULT;
    private WorldWindow wwd;

    public CursorManager(IMapModel mapModel, IMapController mapController, WorldWindowProvider worldWindowProvider) {
        mapModel.getDragManager()
            .isDraggingProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    isDragging = newValue;
                    updateCursor();
                });

        mouseModeCursorsDown.put(InputMode.DEFAULT, Cursor.CLOSED_HAND);
        mouseModeCursorsUp.put(InputMode.DEFAULT, Cursor.OPEN_HAND);

        mouseModeCursorsDown.put(InputMode.ADD_POINTS, Cursor.CLOSED_HAND);
        mouseModeCursorsUp.put(InputMode.ADD_POINTS, Cursor.CROSSHAIR);

        mouseModeCursorsDown.put(InputMode.SET_REF_POINT, Cursor.CROSSHAIR);
        mouseModeCursorsUp.put(InputMode.SET_REF_POINT, Cursor.CROSSHAIR);

        mouseModeCursorsDown.put(InputMode.SET_TAKEOFF_POINT, Cursor.CROSSHAIR);
        mouseModeCursorsUp.put(InputMode.SET_TAKEOFF_POINT, Cursor.CROSSHAIR);

        mouseModeCursorsDown.put(InputMode.SET_LANDING_POINT, Cursor.CROSSHAIR);
        mouseModeCursorsUp.put(InputMode.SET_LANDING_POINT, Cursor.CROSSHAIR);

        mouseModeCursorsDown.put(InputMode.SET_MODEL_ORIGIN, Cursor.CROSSHAIR);
        mouseModeCursorsUp.put(InputMode.SET_MODEL_ORIGIN, Cursor.CROSSHAIR);

        mouseModeCursorsDown.put(InputMode.ADD_MEASURMENT_POINTS, Cursor.CROSSHAIR);
        mouseModeCursorsUp.put(InputMode.ADD_MEASURMENT_POINTS, Cursor.CROSSHAIR);

        mouseModeCursorsDown.put(InputMode.SET_SIMULATION_TAKEOFF, Cursor.CROSSHAIR);
        mouseModeCursorsUp.put(InputMode.SET_SIMULATION_TAKEOFF, Cursor.CROSSHAIR);

        worldWindowProvider.whenAvailable(this::setWorldWindow);
        mapController.mouseModeProperty().addListener((observable, oldValue, newValue) -> mouseModeChanges(newValue));
    }

    private boolean isHoveringSelectable;
    private boolean isDragging;

    private Cursor createCustomCursor(String resourcePath, Point hotSpot) throws FileNotFoundException {
        BufferedImage image = SvgUtils.getImageFromFile(resourcePath);
        Ensure.notNull(image, "image");
        image = SvgUtils.getImageFromFile(resourcePath, image.getWidth(), image.getHeight());
        if (image == null) {
            throw new FileNotFoundException(resourcePath);
        }

        return new ImageCursor(SwingFXUtils.toFXImage(image, null), (double)hotSpot.x, (double)hotSpot.y);
    }

    public void setWorldWindow(WorldWindow worldWindow) {
        if (inputHandler != null) {
            inputHandler.removeMouseListener(this);
            worldWindow.removeSelectListener(this);
            inputHandler = null;
        }

        wwd = worldWindow;
        if (wwd != null) {
            inputHandler = wwd.getInputHandler();
            inputHandler.addMouseListener(this);
            worldWindow.addSelectListener(this);
        }
    }

    public void updateCursor() {
        WWNode wwd = (WWNode)this.wwd;
        if (wwd == null) {
            return;
        }

        Cursor cursor;
        if (isDragging) {
            cursor = Cursor.MOVE;
        } else if (!mouseDown && isHoveringSelectable && mouseMode.isSelecting()) {
            cursor = Cursor.DEFAULT;
        } else {
            Map<InputMode, Cursor> mouseModeCursors = mouseDown ? mouseModeCursorsDown : mouseModeCursorsUp;
            cursor = mouseModeCursors.get(mouseMode);
        }

        if (cursor == null) {
            cursor = Cursor.DEFAULT;
        }

        wwd.setCursor(cursor);
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (mouseDown) {
            return;
        }

        mouseDown = true;
        updateCursor();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if (!mouseDown) {
            return;
        }

        mouseDown = false;
        updateCursor();
    }

    public void mouseModeChanges(InputMode newMouseMode) {
        if (mouseMode == newMouseMode) {
            return;
        }

        mouseMode = newMouseMode;
        updateCursor();
    }

    @Override
    public void selected(SelectEvent event) {
        Object pickedObject = PickingHelper.getPickedObject(event, false, false, wwd);

        boolean isHoveringSelectableNew = false;
        if (pickedObject instanceof IWWRenderableWithUserData) {
            IWWRenderableWithUserData obj = (IWWRenderableWithUserData)pickedObject;
            isHoveringSelectableNew = obj.isSelectable();
        }

        if (isHoveringSelectableNew != isHoveringSelectable) {
            isHoveringSelectable = isHoveringSelectableNew;
            updateCursor();
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}

    @Override
    public void mouseExited(MouseEvent mouseEvent) {}

}
