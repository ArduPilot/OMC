/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.map.elevation.IElevationModel;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.gui.wwext.sun.SunPositionProviderSingleton;
import eu.mavinci.wwjextensions.Movable3DModel;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import net.java.joglutils.model.examples.DisplayListRenderer;

public class M_WWModel3D extends Movable3DModel {
    protected RenderableLayer wwLayer;
    protected boolean setOverGround = true;
    protected boolean setOnGround = false;
    protected double altOffset = IElevationModel.MIN_LEVEL_OVER_GROUND;

    private static final IElevationModel elevationModel =
        DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);

    private static final DisplayListRenderer renderer;

    static {
        renderer = new DisplayListRenderer();
        renderer.debug(false);
    }

    public M_WWModel3D(RenderableLayer wwLayer) {
        super(renderer);
        setYawModel(90);
        setPitchModel(0);
        setRollModel(-90);
        setKeepConstantSize(false);
        this.wwLayer = wwLayer;
        wwLayer.addRenderable(this);
    }

    double sollElevation;

    @Override
    public void setPosition(Position position) {
        if (setOnGround) {
            position = elevationModel.getPositionOnGround(position);
            position = new Position(position, position.elevation + altOffset);
        } else if (setOverGround) {
            position = elevationModel.getPositionOverGround(position, altOffset);
        } else if (altOffset != 0) {
            position = new Position(position, position.elevation + altOffset);
        }

        if (getSunPosition() == null) {
            setSunPosition(SunPositionProviderSingleton.getInstance().getPosition());
        }

        super.setPosition(position);
    }

    public void fireLayerChanged() {
        wwLayer.firePropertyChange(AVKey.LAYER, null, wwLayer);
    }

    @Override
    public void setYaw(double val) {
        super.setYaw(-val);
    }

    @Override
    public double getYaw() {
        return -super.getYaw();
    }

    @Override
    public void setRoll(double val) {
        super.setRoll(-val);
    }

    @Override
    public double getRoll() {
        return -super.getRoll();
    }

}
