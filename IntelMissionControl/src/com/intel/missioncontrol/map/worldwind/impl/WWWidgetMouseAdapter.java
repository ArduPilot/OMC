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
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AltitudeModes;
import eu.mavinci.desktop.gui.wwext.IWWRenderableWithUserData;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.ReferencePoint;
import eu.mavinci.flightplan.Takeoff;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.util.RayCastingSupport;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

public class WWWidgetMouseAdapter extends MouseAdapter {
    private final IMapModel mapModel;
    private final IWWMapView mapView;
    private final IWWGlobes globes;
    private final ISelectionManager selectionManager;
    private final IMapController mapController;
    private WorldWindow wwd;

    public WWWidgetMouseAdapter(
            IMapModel mapModel,
            IWWMapView mapView,
            IWWGlobes globes,
            WorldWindow wwd,
            ISelectionManager selectionManager,
            IMapController mapController) {
        this.mapModel = mapModel;
        this.mapView = mapView;
        this.globes = globes;
        this.wwd = wwd;
        this.selectionManager = selectionManager;
        this.mapController = mapController;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mapModel.getDragManager().stopDragging();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mapView.getViewInputHandler().mousePressed(e);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        if (((IWWMapView)wwd.getView()).getDC() == null) {
            mouseEvent.consume();
            return;
        }

        if (!SwingUtilities.isLeftMouseButton(mouseEvent) || mouseEvent.getClickCount() > 1) {
            return;
        }

        // System.out.println("\n\nmouseClicked " + mouseEvent + " mouseMode:" + mapController.getMouseMode());
        // Debug.printStackTrace();
        // in case we are trying to select something, we shouldn't add at the same time new points
        // lets check for this and break in case needed
        if (mapController.isSelecting()) {
            PickedObjectList objects = wwd.getObjectsAtCurrentPosition();
            // System.out.println("pickedobjectList"+objects);
            if (objects != null && objects.size() != 0) {
                Object topObject =
                    PickingHelper.getPickedObject(
                        objects, wwd.getCurrentPosition(), objects.getTopPickedObject().getPickPoint(), false, false);

                // System.out.println("topObject" + topObject + topObject.getClass());
                if (topObject != null && topObject instanceof IWWRenderableWithUserData) {
                    IWWRenderableWithUserData wwObj = (IWWRenderableWithUserData)topObject;
                    // System.out.println("userOB" + wwObj.getUserData() + " selectable " + wwObj.isSelectable());

                    if (wwObj.isSelectable()) {
                        // Object userData= wwObj.getUserData();
                        if (mapController.getMouseMode() != InputMode.ADD_POINTS
                                || wwObj.isSelectableWhileAddNewPoints()) {
                            // this shortcut prevents from selection pictures or so, while trying to add new waypoints
                            return;
                        }
                    }
                }

                if ((topObject instanceof Position || !(topObject instanceof IWWRenderableWithUserData))
                        && (mapController.getMouseMode() == InputMode.DEFAULT)) {
                    selectionManager.setSelection(null);
                    mouseEvent.consume();
                    return;
                }
            }
        }

        Line ray = wwd.getView().computeRayFromScreenPoint(mouseEvent.getX(), mouseEvent.getY());
        Position p = null;
        Intersection[] intersections;
        switch (mapController.getMouseMode()) {
        case ADD_POINTS:
            // compute p as point at the same level as NodeToInsertBehind
            // where the mouse points to
            if (selectionManager.getSelection() == null) {
                // maybe inform user aboud reason, why nothing happens?
                Debug.getLog()
                    .log(
                        Level.WARNING,
                        "current selection shouldnt be NULL while trying to add points by clicking",
                        new Exception());
                mouseEvent.consume();
                return;
            }

            double lastAlt = mapModel.getAltitudeForNewNodesWithinM();

            if (lastAlt == Float.NEGATIVE_INFINITY
                    || mapModel.getAltitudeModeForNewNodes() == AltitudeModes.clampToGround
                    || mapView.isFlatEarth()) {
                intersections = wwd.getSceneController().getTerrain().intersect(ray);
                if (intersections != null && intersections.length > 0) {
                    p = globes.getActiveGlobe().computePositionFromPoint(intersections[0].getIntersectionPoint());
                } else {
                    break;
                }

                mapModel.addNodeFromGui(p.getLatitude().degrees, p.getLongitude().degrees);
                mouseEvent.consume();
                return;
            }

            lastAlt = 0;
            // System.out.println("AAASDASDASD");

            double lastNodeAlt = lastAlt;

            Intersection inters[] = globes.getActiveGlobe().intersect(ray, lastNodeAlt);
            if (inters != null) {
                p = globes.getActiveGlobe().computePositionFromPoint(inters[0].getIntersectionPoint());
            }

            // System.out.println("ppppppppp;"+p);
            if (p == null) {
                return;
            }

            mapModel.addNodeFromGui(p.getLatitude().degrees, p.getLongitude().degrees);
            mouseEvent.consume();
            break;
        case SET_REF_POINT:
        case SET_TAKEOFF_POINT:
        case SET_LANDING_POINT:
        case SET_MODEL_ORIGIN:
        case SET_SIMULATION_TAKEOFF:
            // compute p as point at terrain where the mouse points to
            intersections = wwd.getSceneController().getTerrain().intersect(ray);
            if (intersections != null && intersections.length > 0) {
                p = globes.getActiveGlobe().computePositionFromPoint(intersections[0].getIntersectionPoint());
            } else {
                // Fallback on raycasting using elevation data
                p =
                    RayCastingSupport.intersectRayWithTerrain(
                        globes.getActiveGlobe(), ray.getOrigin(), ray.getDirection(), 1000, 2);
            }

            if (p == null) {
                return;
            }

            Object tmp = selectionManager.getSelection();
            if (tmp instanceof Takeoff) {
                Takeoff origin = (Takeoff)tmp;
                origin.setIsAuto(false);
                origin.setLatLon(p.getLatitude().degrees, p.getLongitude().degrees);
                origin.setDefined(true);
            } else if (tmp instanceof ReferencePoint) {
                ReferencePoint origin = (ReferencePoint)tmp;
                origin.setIsAuto(false);
                origin.setLatLon(p.getLatitude().degrees, p.getLongitude().degrees);
                origin.setDefined(true);
            } else if (tmp instanceof IAirplane) {
                IAirplane airplane = (IAirplane)tmp;
                airplane.setStartpos(p.getLatitude().degrees, p.getLongitude().degrees);
            }

            mapController.tryCancelMouseModes(
                InputMode.SET_REF_POINT,
                InputMode.SET_TAKEOFF_POINT,
                InputMode.SET_LANDING_POINT,
                InputMode.SET_MODEL_ORIGIN,
                InputMode.SET_SIMULATION_TAKEOFF);

            mouseEvent.consume();
            break;
        default:
            break;
        }
    }
}
