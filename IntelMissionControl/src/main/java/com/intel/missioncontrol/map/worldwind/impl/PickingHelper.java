/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.impl;

import eu.mavinci.desktop.gui.wwext.IUserObjectComposite;
import eu.mavinci.desktop.gui.wwext.IWWPickableAdvancedTooltip;
import eu.mavinci.desktop.gui.wwext.IWWRenderableWithUserData;
import eu.mavinci.desktop.gui.wwext.MAnalyticSurface;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import java.awt.Point;

public class PickingHelper {

    public static Object getPickedObject(
            PickedObjectList pickedObjects,
            Position position,
            Point point,
            boolean alsoNonSelectable,
            boolean onlyTooltip) {
        if (pickedObjects == null) {
            return null;
        }

        for (int i = pickedObjects.size() - 1; i >= 0; i--) {
            PickedObject p = pickedObjects.get(i);

            Object pickedObject = p.getObject();
            if (pickedObject instanceof MAnalyticSurface.AnalyticSurfaceObject) {
                if (!alsoNonSelectable || !onlyTooltip) {
                    continue;
                }

                MAnalyticSurface.AnalyticSurfaceObject so = (MAnalyticSurface.AnalyticSurfaceObject)pickedObject;
                pickedObject = so.getAnalyticSurface();
            }

            if (pickedObject instanceof IWWRenderableWithUserData) {
                IWWRenderableWithUserData userDataProvider = (IWWRenderableWithUserData)pickedObject;
                if (!alsoNonSelectable && !userDataProvider.isSelectable()) {
                    continue;
                }

                if (onlyTooltip && !userDataProvider.hasTooltip()) {
                    continue;
                }

                Object o = userDataProvider.getUserData();
                if (o instanceof IUserObjectComposite) {
                    IUserObjectComposite uoc = (IUserObjectComposite)o;
                    Object o2 = uoc.getRealUserObject(position, point);

                    if (o2 instanceof IWWRenderableWithUserData) {
                        IWWRenderableWithUserData userDataProvider2 = (IWWRenderableWithUserData)pickedObject;
                        if (!alsoNonSelectable && !userDataProvider2.isSelectable()) {
                            continue;
                        }

                        if (onlyTooltip && !userDataProvider2.hasTooltip()) {
                            continue;
                        }
                    }

                    pickedObject = o2;
                }
            } else if (onlyTooltip && !(pickedObject instanceof IWWPickableAdvancedTooltip)) {
                continue;
            }

            return pickedObject;
        }

        return null;
    }

    public static Object getPickedObject(
            SelectEvent event, boolean alsoNonSelectable, boolean onlyTooltip, WorldWindow wwd) {
        Position p = wwd == null ? null : wwd.getCurrentPosition();

        if (p == null) {
            p = event.getTopPickedObject() == null ? null : event.getTopPickedObject().getPosition();
        }

        return getPickedObject(event.getObjects(), p, event.getPickPoint(), alsoNonSelectable, onlyTooltip);
    }

}
