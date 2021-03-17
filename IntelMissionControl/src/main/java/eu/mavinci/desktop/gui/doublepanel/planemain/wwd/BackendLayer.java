/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.listeners.IBackendBroadcastListener;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.desktop.gui.wwext.IconLayerCentered;
import eu.mavinci.desktop.gui.wwext.UserFacingIconWithUserData;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.WWIcon;

public class BackendLayer extends IconLayerCentered implements IBackendBroadcastListener {

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.wwd.BackendLayer";

    IAirplane plane;
    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    public BackendLayer(IAirplane plane) {
        this.plane = plane;
        setAlwaysUseAbsoluteElevation(false);
        setRenderAlwaysOverGround(true);
        setPickEnabled(false);
    }

    @Override
    public void backendListChanged() {}

    @Override
    public void recv_backend(Backend host, MVector<Port> ports) {
        removeAllIcons();

        // inform WWD about layer change
        firePropertyChange(AVKey.LAYER, null, this);
    }
}
