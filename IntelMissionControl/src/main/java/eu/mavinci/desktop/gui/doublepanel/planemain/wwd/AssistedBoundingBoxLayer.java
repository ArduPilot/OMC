/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConfig;
import eu.mavinci.core.plane.listeners.IAirplaneListenerStartPos;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.desktop.gui.wwext.SurfaceFilledCircleWithUserData;
import eu.mavinci.geo.ISectorReferenced;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.awt.Color;
import java.util.OptionalDouble;

public class AssistedBoundingBoxLayer extends RenderableLayer
        implements ISectorReferenced,
            IAirplaneListenerConfig,
            IAirplaneListenerStartPos,
            INotificationObject.ChangeListener {

    private IAirplane plane;

    LatLon center = null;

    public static final Color defCol = new Color(0.f, 1.f, 0.f, 0.15f);
    private static final Globe globe = StaticInjector.getInstance(IWWGlobes.class).getDefaultGlobe();

    public AssistedBoundingBoxLayer(IAirplane plane) {
        this.plane = plane;
        setPickEnabled(false);
        setName("AssistedBoundingBoxLayerName");
        setEnabled(true);
        plane.addListener(this);
        plane.getHardwareConfiguration().addListener(new INotificationObject.WeakChangeListener(this));
    }

    protected void rebuild() {
        removeAllRenderables();
        IHardwareConfiguration nativeHardwareConfig = plane.getNativeHardwareConfiguration();
        if (nativeHardwareConfig == null) {
            return;
        }

        IPlatformDescription plat = nativeHardwareConfig.getPlatformDescription();
        if (plat == null) {
            plat = plane.getHardwareConfiguration().getPlatformDescription();
        }

        if (plat == null || plat.isInCopterMode()) {
            return;
        }

        try {
            center = plane.getAirplaneCache().getStartPos();
        } catch (AirplaneCacheEmptyException e) {
            return;
        }

        SurfaceFilledCircleWithUserData circ =
            new SurfaceFilledCircleWithUserData(this, new Position(center, 0), getRadius());
        circ.setDraggable(false);
        circ.setPopupTriggering(false);
        circ.setSelectable(false);
        circ.setHasTooltip(false);

        ShapeAttributes attr = new BasicShapeAttributes();
        circ.setAttributes(attr);

        attr.setDrawInterior(true);
        attr.setDrawOutline(true);
        attr.setOutlineWidth(8);

        Material m = new Material(defCol);
        attr.setInteriorMaterial(m);
        attr.setOutlineMaterial(new Material(Color.red));
        attr.setInteriorOpacity(defCol.getAlpha() / 255.);
        attr.setOutlineOpacity(defCol.getAlpha() / 255.);

        addRenderable(circ);
        firePropertyChange(AVKey.LAYER, null, this);
    }

    @SuppressWarnings("deprecation")
    public double getRadius() {
        try {
            if (plane.getAirplaneCache().getConf().USER_BBOXSIZE > 0) {
                return plane.getAirplaneCache().getConf().USER_BBOXSIZE / 100.;
            } else if (plane.getAirplaneCache().getConf().MISC_BBOXSIZE > 0) {
                return plane.getAirplaneCache().getConf().MISC_BBOXSIZE / 100.;
            } else {
                return PlaneConstants.DEF_MISC_BBOXSIZE / 100.;
            }
        } catch (AirplaneCacheEmptyException e) {
            return PlaneConstants.DEF_MISC_BBOXSIZE / 100.;
        }
    }

    @Override
    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        rebuild();
    }

    @Override
    public void recv_config(Config_variables c) {
        rebuild();
    }

    @Override
    public Sector getSector() {
        if (center == null) {
            return null;
        }

        return Sector.boundingSector(globe, center, getRadius());
    }

    @Override
    public OptionalDouble getMaxElev() {
        return OptionalDouble.empty();
    }

    @Override
    public OptionalDouble getMinElev() {
        return OptionalDouble.empty();
    }

    @Override
    public void propertyChange(INotificationObject.ChangeEvent propertyChangeEvent) {
        // TODO: PERF - This seems to be a very heavy-weight event handler, it probably should work asynchronously.
        rebuild();
    }
}
