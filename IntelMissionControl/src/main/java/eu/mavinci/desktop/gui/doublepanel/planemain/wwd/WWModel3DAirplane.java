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

import com.intel.missioncontrol.INotificationObject;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.listeners.IAirplaneListenerOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.desktop.gui.wwext.M_WWModel3D;
import eu.mavinci.desktop.gui.wwext.WWFactory;
import eu.mavinci.plane.AirplaneCache;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.layers.RenderableLayer;
import net.java.joglutils.model.geometry.Model;

/**
 * Draw small plane at current position: if plane was loaded, move to current position and update angles
 *
 * @author RodgersGB, Shawn Gano, Marco Möller
 */
public class WWModel3DAirplane extends M_WWModel3D
        implements IAirplaneListenerPosition, IAirplaneListenerOrientation, INotificationObject.ChangeListener {

    private AirplaneCache cache;
    // private IPlatformDescription camera;

    private IAirplane airplane;

    // protected Calendar cal;

    Model model_manual;
    Model model_assisted;
    Model model_else;
    Model model_copter;
    Model model_custom;
    Model model_grayHawk;
    Model model_falcon8;

    public WWModel3DAirplane(IAirplane plane, RenderableLayer wwLayer) {
        super(wwLayer);

        this.airplane = plane;
        // FIXME: Find sensible values here
        this.polygonOffsetFactor = -300;
        this.polygonOffsetUnits = -300;

        // load model asynchronosly
        // Thread t = new Thread(new Runnable() {
        // @Override
        // public void run() {
        model_manual = WWFactory.getAirplaneModelRed();
        model_assisted = WWFactory.getAirplaneModelOrange();
        model_else = WWFactory.getAirplaneModelGreen();
        //        model_copter = WWFactory.getAirplaneModelFalcon8();
        model_copter = WWFactory.getAirplaneModeGenericDrone();
        model_falcon8 = WWFactory.getAirplaneModelFalcon8();
        model_grayHawk = WWFactory.getAirplaneModelGrayHawk();
        setOverGround = true;
        setOnGround = false;
        altOffset = 0;
        setSize(1.5);

        setModel(model_manual);
        cache = plane.getAirplaneCache();

        plane.addListener(this);
        plane.getHardwareConfiguration().addListener(new INotificationObject.WeakChangeListener(this));
    }

    private AirplaneFlightmode oldMode = AirplaneFlightmode.ManualControl;
    private boolean wasCopter;

    // final int switchInterval = 1;//00;
    @Override
    public void recv_position(PositionData p) {
        // model_copter.setSize(100);

        AirplaneFlightmode flightmode = AirplaneFlightmode.ManualControl;
        try {
            flightmode = cache.getFlightMode();
        } catch (AirplaneCacheEmptyException e) {
        }

        // AirplaneFlightmode flightmode = oldMode;// = AirplaneFlightmode.ManualControl;
        // switchCnt++;
        // if (switchCnt%switchInterval==0){
        // System.out.println("switchCnt"+switchCnt);
        // flightmode = AirplaneFlightmode.values()[(switchCnt/switchInterval)%AirplaneFlightmode.values().length];
        // System.out.println("newMode "+ flightmode);
        // }
        if (model_custom == null) {
            boolean isCopter = airplane.getHardwareConfiguration().getPlatformDescription().isInCopterMode();
            if ((!wasCopter && oldMode != flightmode) || isCopter != wasCopter) {
                oldMode = flightmode;
                wasCopter = isCopter;
                if (isCopter) {
                    if (airplane.getHardwareConfiguration().getPlatformDescription().getAirplaneType().isGrayHawk()) {
                        setModel(model_grayHawk);
                        setYawModel(-90);
                        setPitchModel(0);
                        setRollModel(90);
                    } else if (airplane.getHardwareConfiguration()
                            .getPlatformDescription()
                            .getAirplaneType()
                            .isFalcon()) {
                        setModel(model_falcon8);
                        setYawModel(90);
                        setPitchModel(0);
                        setRollModel(0);
                    } else {
                        setModel(model_copter);
                        setYawModel(-90);
                        setPitchModel(0);
                        setRollModel(90);
                    }

                } else {
                    if (flightmode == AirplaneFlightmode.ManualControl) {
                        setModel(model_manual);
                    } else if (flightmode == AirplaneFlightmode.AssistedFlying) {
                        setModel(model_assisted);
                    } else {
                        setModel(model_else);
                    }

                    setYawModel(90);
                    setPitchModel(0);
                    setRollModel(-90);
                }
            }
        } else {
            setModel(model_custom);
        }

        try {
            setPosition(cache.getCurPos());
            fireLayerChanged();
        } catch (AirplaneCacheEmptyException e) {
        }
    }

    @Override
    public void recv_orientation(OrientationData o) {
        setYaw(o.yaw);
        setPitch(o.pitch);
        setRoll(o.roll);
        fireLayerChanged();
    }

    @Override
    public void propertyChange(INotificationObject.ChangeEvent propertyChangeEvent) {
        recv_position(null);
    }

    // @Override
    // protected void draw(DrawContext dc) {
    // System.out.println("draw");
    // super.draw(dc);
    // }
    //
    // @Override
    // protected void beginDraw(DrawContext dc) {
    // System.out.println("beginDraw");
    // super.beginDraw(dc);
    // }
    //
    // @Override
    // public void render(DrawContext dc) {
    // super.render(dc);
    // }
    //
    // @Override
    // protected void draw(DrawContext dc, Model model) {
    // getModel().setRenderPicker(false);
    // getModel().setRenderModel(true);
    // getModel().setRenderAsWireframe(false);
    // getModel().setRenderModelBounds(false);
    // getModel().setRenderObjectBounds(false);
    // System.out.println("draw(..,..)");
    // super.draw(dc, model);
    // }
}
