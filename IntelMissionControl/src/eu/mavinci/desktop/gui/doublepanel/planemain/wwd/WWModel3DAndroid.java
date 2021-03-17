/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

/*
 * WWModel3D_new.java
 *
 * Created on February 14, 2008, 9:11 PM
 *
 * toolshed - http://forum.worldwindcentral.com/showthread.php?t=15222&page=6
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import eu.mavinci.core.plane.listeners.IAirplaneListenerAndroidState;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.desktop.gui.wwext.M_WWModel3D;
import eu.mavinci.desktop.gui.wwext.WWFactory;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;

/**
 * Draw small plane at current position: if plane was loaded, move to current position and update angles
 *
 * @author RodgersGB, Shawn Gano, Marco Möller, Peter Schauß
 */
public class WWModel3DAndroid extends M_WWModel3D implements IAirplaneListenerAndroidState {

    /**
     * Creates a new instance of WWModel3D_new
     *
     * @param model
     * @param pos
     */
    public WWModel3DAndroid(IAirplane plane, RenderableLayer wwLayer) {
        super(wwLayer);
        // // load model asynchronosly
        // Thread t = new Thread(new Runnable() {
        // @Override
        // public void run() {
        setModel(WWFactory.getAndroidModel());
        // }
        // }, "Loading 3d Android Model");
        // t.start();

        setSize(0.4);
        plane.addListener(this);
    }

    @Override
    public void recv_androidState(AndroidState state) {
        setYaw(state.yaw);
        setPitch(state.pitch);
        setRoll(state.roll);
        setPosition(new Position(LatLon.fromDegrees(state.lat, state.lon), state.alt));
        fireLayerChanged();
    }

}
