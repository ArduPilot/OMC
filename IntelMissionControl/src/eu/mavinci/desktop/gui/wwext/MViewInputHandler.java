/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.settings.MapRotationStyle;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.awt.ViewInputAttributes;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.view.orbit.OrbitViewInputHandler;
import java.awt.Component;

public class MViewInputHandler extends OrbitViewInputHandler {

    private MapRotationStyle mapRotationStyle = MapRotationStyle.DEFAULT;

    public void setMapRotationStyle(MapRotationStyle mapRotationStyle) {
        this.mapRotationStyle = mapRotationStyle;
    }

    public MapRotationStyle getMapRotationStyle() {
        return mapRotationStyle;
    }

    @Override
    protected Position computeSelectedPosition() {
        // System.out.println("compute");
        // Position pos = super.computeSelectedPosition();
        // if (pos == null){
        PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
        if (pickedObjects != null) {
            PickedObject top = pickedObjects.getTopPickedObject();
            // System.out.println("top Picked Object" + top);
            if (top != null && top.isTerrain()) {
                // System.out.println("use it");
                return top.getPosition();
            }

            for (PickedObject p : pickedObjects) {
                // System.out.println("alternative " + p);
                if (p.isTerrain()) {
                    return p.getPosition();
                }
            }
        }
        // System.out.println("return " + null);
        return null;
    }

    protected void onRotateView(
            double headingInput,
            double pitchInput,
            double totalHeadingInput,
            double totalPitchInput,
            ViewInputAttributes.DeviceAttributes deviceAttributes,
            ViewInputAttributes.ActionAttributes actionAttributes) {
        if (mapRotationStyle != null && mapRotationStyle.isFlipViewPitchEnabled()) {
            super.onRotateView(
                headingInput, -pitchInput, totalHeadingInput, -totalPitchInput, deviceAttributes, actionAttributes);
        } else {
            WorldWindow worldWindow = getWorldWindow();
            int sign = 1;
            // Switch the direction of heading change depending on whether the cursor is above or below
            // the center of the screen.
            if (worldWindow instanceof javafx.scene.Node) {
                double scaleY = 1.0;

                javafx.scene.Node node = (javafx.scene.Node)worldWindow;
                javafx.scene.Scene scene = node.getScene();
                if (scene != null) {
                    javafx.stage.Window window = scene.getWindow();
                    if (window != null) {
                        scaleY = window.getOutputScaleY();
                    }
                }

                double height = (int)(node.getBoundsInLocal().getHeight() * scaleY);
                if (getMouseDownPoint() != null && getMouseDownPoint().y < height / 2) {
                    sign = -1;
                }
            } else if (worldWindow instanceof Component) {
                if (getMouseDownPoint() != null && getMouseDownPoint().y < ((Component)worldWindow).getHeight() / 2) {
                    sign = -1;
                }
            }

            super.onRotateView(
                headingInput,
                -sign * pitchInput,
                totalHeadingInput,
                -sign * totalPitchInput,
                deviceAttributes,
                actionAttributes);
        }
    }

    @Override
    protected boolean isFlipViewRotationEnabled() {
        if (mapRotationStyle != null) {
            return mapRotationStyle.isFlipViewRotationEnabled();
        } else {
            return false;
        }
    }

}
