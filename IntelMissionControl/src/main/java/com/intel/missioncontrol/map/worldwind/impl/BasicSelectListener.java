/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.impl;

import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeKnownImage;
import eu.mavinci.desktop.gui.wwext.IWWRenderableWithUserData;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.render.WWIcon;
import java.io.File;
import java.util.logging.Level;

public class BasicSelectListener implements SelectListener {

    protected final IMapModel mapModel;
    protected final IWWMapView mapView;
    protected final ISelectionManager selectionManager;
    protected final IMapController mapController;

    protected Object lastHighlightedObject;
    protected Highlightable lastHighlightable;
    protected WWIcon lastPickedIcon;
    protected WorldWindow wwd;

    public BasicSelectListener(
            IMapModel mapModel,
            IWWMapView mapView,
            WorldWindow wwd,
            ISelectionManager selectionManager,
            IMapController mapController) {
        this.mapModel = mapModel;
        this.mapView = mapView;
        this.selectionManager = selectionManager;
        this.mapController = mapController;
        this.wwd = wwd;
    }

    public void selected(SelectEvent event) {
        // System.out.println("select event " + event + " - " + event.getMouseEvent());
        // maybe reselt highlighting
        if (event.getEventAction().equals(SelectEvent.HOVER)) {
            return;
        }

        /*
         * if (pickedObject != null && MDialogAnnotation.class.isAssignableFrom(pickedObject.getClass())) { event.consume(); return; }
         */

        // tell the controller the selection change
        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK)) {
            highlight(null);
            handleSelect(event);
            // System.out.println("delegating click");
            // controller.getWorldWindow().getView().getViewInputHandler().mousePressed(event.getMouseEvent());
            // dragging
        } else if (event.getEventAction().equals(SelectEvent.LEFT_DOUBLE_CLICK)) {
            highlight(null);
            handleDoubleClick(event);
        } else if (event.getEventAction().equals(SelectEvent.DRAG)) {
            highlight(null);
            handleDrag(event);

            // Have rollover events highlight the rolled-over object.
        } else if ((event.getEventAction().equals(SelectEvent.ROLLOVER)
                    || event.getEventAction().equals(SelectEvent.RIGHT_PRESS))
                && !this.mapModel.getDragManager().isDragging()) {
            Object pickedObject = PickingHelper.getPickedObject(event, true, false, wwd);

            if (this.lastHighlightedObject != null
                    && (pickedObject == null || pickedObject != this.lastHighlightedObject)) {
                highlight(null);
            }

            highlight(pickedObject);
            event.consume();
            // Have drag events drag the selected object.
        } else if (event.getEventAction().equals(SelectEvent.DRAG_END)) {
            highlight(null);
            handleDragEnd(event);
        } else if (event.getEventAction().equals(SelectEvent.RIGHT_CLICK)) {
            highlight(null);
            handleRightClick(event);
        }
    }

    protected void handleRightClick(SelectEvent event) {
        Object realObj = PickingHelper.getPickedObject(event, false, false, wwd);
        if (realObj instanceof AerialPinholeKnownImage) {
            AerialPinholeKnownImage img = (AerialPinholeKnownImage)realObj;
            File f = (img.getPhotoFile() != null) ? img.getPhotoFile().getFile() : null;
            if (f != null && f.exists()) {
                try {
                    FileHelper.openFile(f);
                } catch (Exception e) {
                    Debug.getLog().log(Level.CONFIG, "cant open image " + img, e);
                }
            }

            event.consume();
        } else if (realObj instanceof IWWRenderableWithUserData
                && ((IWWRenderableWithUserData)realObj).getUserData() instanceof MapLayerMatch) {
            MapLayerMatch match = (MapLayerMatch)(((IWWRenderableWithUserData)realObj).getUserData());
            File f = (match.getCurPhotoFile() != null) ? match.getCurPhotoFile().getFile() : null;
            if (f != null && f.exists()) {
                try {
                    FileHelper.openFile(f);
                } catch (Exception e) {
                    Debug.getLog().log(Level.CONFIG, "cant open image " + match, e);
                }
            }

            event.consume();
        } else if (mapController.isSelecting() && event.hasObjects()) {
            doSelect(realObj);
            mapModel.deleteSelectionAsync();
            event.consume();
        }
    }

    protected Object handleDoubleClick(SelectEvent event) {
        if (mapController.tryCancelMouseModes(InputMode.ADD_POINTS)) {
            event.consume();
        } else if (mapController.tryCancelMouseModes(InputMode.ADD_MEASURMENT_POINTS)) {
            event.consume();
        }

        return handleSelect(event);
    }

    private Object handleSelect(SelectEvent event) {
        if (mapController.isSelecting() && event.hasObjects()) {
            Object selection = doSelect(PickingHelper.getPickedObject(event, false, false, wwd));
            event.consume();
            return selection;
        }

        return null;
    }

    protected Object doSelect(Object pickedObject) {
        if (!mapController.isSelecting()) {
            return null;
        }
        // System.out.println("doSelect:"+pickedObject + " class:" +(pickedObject != null ? pickedObject.getClass() :
        // null));
        if (pickedObject instanceof IWWRenderableWithUserData) {
            IWWRenderableWithUserData wwObj = (IWWRenderableWithUserData)pickedObject;
            // System.out.println("try selecting " + wwObj.getUserData() + " isSelectable" + wwObj.isSelectable());
            if (!wwObj.isSelectable()) {
                return null;
            }

            Object userData = wwObj.getUserData();
            // System.out.println("userDataOfSelect:" + userData);
            if (mapController.getMouseMode() == InputMode.ADD_POINTS && !wwObj.isSelectableWhileAddNewPoints()) {
                // this shortcut prevents from selection pictures or so, while trying to add new waypoints
                return null;
            }

            selectionManager.setSelection(userData);
            return userData;
        }

        return null;
    }

    protected void handleDrag(SelectEvent event) {
        if (mapController.isSelecting()) {
            PickedObject topObject = event.getTopPickedObject();
            Object pickedObject = PickingHelper.getPickedObject(event, false, false, wwd);

            // System.out.println("topObject " + topObject);
            if (!mapModel.getDragManager().isDragging()) {
                doSelect(pickedObject);
                double startAlt = 0;

                // System.out.println("topObjPos "+ topObject.getPosition());
                if (mapModel.getDragManager()
                        .startDragging(pickedObject, event.getPickPoint(), startAlt, topObject.getPosition())) {
                    event.consume();
                } else {
                    mapView.getViewInputHandler().mouseDragged(event.getMouseEvent());
                    event.consume();
                }
            } else if (event.getMouseEvent().getPoint() != null) {
                mapModel.getDragManager().move(event.getMouseEvent().getPoint());
                event.consume();
            }
        } else {
            mapView.getViewInputHandler().mouseDragged(event.getMouseEvent());
            event.consume();
        }
    }

    protected void handleDragEnd(SelectEvent event) {
        mapModel.getDragManager().stopDragging();
        event.consume();
    }

    protected void highlight(Object o) {
        if (o instanceof IWWRenderableWithUserData) {
            IWWRenderableWithUserData rend = (IWWRenderableWithUserData)o;
            if (!rend.isSelectable() && !rend.isHighlightableEvenWithoutSelectability()) {
                o = null;
            }
        }

        if (o != this.lastPickedIcon) {
            // Turn off highlight if on.
            if (this.lastPickedIcon != null) {
                this.lastPickedIcon.setHighlighted(false);
                this.lastPickedIcon = null;
            }

            // Turn on highlight if object selected.
            if (o != null && o instanceof WWIcon) {
                this.lastPickedIcon = (WWIcon)o;
                this.lastPickedIcon.setHighlighted(true);
            }
        }

        // System.out.println("ty highligh : " + o);
        if (o != this.lastHighlightable) {
            // Turn off highlight if on.
            if (this.lastHighlightable != null) {
                this.lastHighlightable.setHighlighted(false);
                this.lastHighlightable = null;
            }

            // Turn on highlight if object selected.
            if (o != null && o instanceof Highlightable) {
                // System.out.println("do Highlighting:"+o);
                this.lastHighlightable = (Highlightable)o;
                this.lastHighlightable.setHighlighted(true);
            }
        }

        lastHighlightedObject = o;
    }

}
