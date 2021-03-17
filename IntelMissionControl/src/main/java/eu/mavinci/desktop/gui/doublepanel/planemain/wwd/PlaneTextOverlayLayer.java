/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.helper.FontHelper;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.gui.wwext.UserFacingTextLayer;
import eu.mavinci.plane.AirplaneCache;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.UserFacingText;
import java.awt.Color;
import java.awt.Font;

public class PlaneTextOverlayLayer extends UserFacingTextLayer implements IAirplaneListenerPosition {

    UserFacingText text = new UserFacingText("-", Position.ZERO);

    AirplaneCache cache;

    public PlaneTextOverlayLayer(IAirplane plane) {
        setName("PlaneTextOverlayLayerName");
        cache = plane.getAirplaneCache();
        add(text);
        text.setPriority(1e6);
        text.setColor(Color.yellow);
        text.setBackgroundColor(Color.BLACK);
        plane.addListener(this);
        getTextRenderer().setAlwaysOnTop(true);
        getTextRenderer().setOnTopEyeDistance(9);
        getTextRenderer().setEffect(AVKey.TEXT_EFFECT_SHADOW);

        text.setFont(FontHelper.getBaseFont(1.3, Font.BOLD));
    }

    @Override
    public void recv_position(PositionData p) {
        try {
            // strange: the position seems to be above ground... stange!
            text.setPosition(new Position(cache.getCurPos(), cache.getCurPlaneElevOverGround()));
            text.setText(getText());
        } catch (AirplaneCacheEmptyException e) {
        }

        fireLayerChanged();
    }

    public String getText() {
        //TODO
        return "";
    }
}
