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
import eu.mavinci.core.plane.management.BackendState;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.IRtkStatisticListener;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionState;
import eu.mavinci.desktop.gui.wwext.IconLayerCentered;
import eu.mavinci.desktop.gui.wwext.UserFacingIconWithUserData;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.plane.management.Airport;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.WWIcon;

public class BackendLayer extends IconLayerCentered implements IBackendBroadcastListener, IRtkStatisticListener {

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.wwd.BackendLayer";

    IAirplane plane;
    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    public BackendLayer(IAirplane plane) {
        this.plane = plane;
        setAlwaysUseAbsoluteElevation(false);
        setRenderAlwaysOverGround(true);
        setPickEnabled(false);
        plane.getRtkClient().addListener(this);

        Airport.getInstance().addBackendBroadcastListener(this);
    }

    @Override
    public void backendListChanged() {}

    @Override
    public void recv_backend(Backend host, MVector<Port> ports) {
        removeAllIcons();

        for (BackendState s : Airport.getInstance().getBackendList()) {
            if (!s.getBackend().hasFix) {
                continue;
            }

            LatLon l =
                new LatLon(
                    Angle.fromDegreesLatitude(s.getBackend().lat), Angle.fromDegreesLongitude(s.getBackend().lon));
            WWIcon icon;
            try {
                if (plane.getAirplaneCache().getBackendStateOffline() == s) {
                    icon =
                        UserFacingIconWithUserData.getOnTerrainRelativeHeightInLayer(
                            "eu/mavinci/icons/16x16/MyBackend.png", l, s);
                } else {
                    icon =
                        UserFacingIconWithUserData.getOnTerrainRelativeHeightInLayer(
                            "eu/mavinci/icons/16x16/Backend.png", l, s);
                }
            } catch (AirplaneCacheEmptyException e) {
                icon =
                    UserFacingIconWithUserData.getOnTerrainRelativeHeightInLayer(
                        "eu/mavinci/icons/16x16/Backend.png", l, s);
            }

            icon.setHighlightScale(1.5);
            icon.setToolTipText(
                languageHelper.getString(KEY + ".backend", s.getBackend().name, s.getHost().getHostString()));
            icon.setToolTipTextColor(java.awt.Color.YELLOW);

            addIcon(icon);
        }

        Position p = plane.getRtkClient().getParser().lastStationPos;
        if (plane.getRtkClient().isConnected() && p != null) {
            WWIcon icon =
                UserFacingIconWithUserData.getOnTerrainRelativeHeightInLayer(
                    "com/intel/missioncontrol/gfx/icon_flight-basestation-home.svg", p, plane.getRtkClient());

            icon.setHighlightScale(1.5);
            icon.setToolTipText(languageHelper.getString(KEY + ".externalBase", plane.getRtkClient().toString()));
            icon.setToolTipTextColor(java.awt.Color.YELLOW);

            addIcon(icon);
        }

        // inform WWD about layer change
        firePropertyChange(AVKey.LAYER, null, this);
    }

    @Override
    public void connectionStateChanged(NtripConnectionState conState, int msecUnitlReconnect) {
        recv_backend(null, null);
    }

    @Override
    public void timerTickWhileConnected(long msecConnected, long byteTransferredIn, long byteTransferredOut) {
        recv_backend(null, null);
    }

    @Override
    public void packageReceived(byte[] msg, int type) {}

}
